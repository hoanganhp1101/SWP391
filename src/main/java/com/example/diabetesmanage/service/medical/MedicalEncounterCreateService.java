package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest.MedicationLineItem;
import com.example.diabetesmanage.util.SqlDiagnostics;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lưu Medical Encounter theo đúng loại hồ sơ trong một transaction.
 */
public class MedicalEncounterCreateService {

    private static final Logger LOG = Logger.getLogger(MedicalEncounterCreateService.class.getName());

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final HealthRecordSnapshotService snapshotService = new HealthRecordSnapshotService();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public static class CreateResult {
        private final String encounterId;

        public CreateResult(String encounterId) {
            this.encounterId = encounterId;
        }

        public String getEncounterId() {
            return encounterId;
        }
    }

    public List<String> validate(EncounterCreateRequest form) {
        List<String> errors = new ArrayList<>();
        EncounterType type = form.resolveEncounterType();

        if (form.getPatientId() == null || form.getPatientId().isBlank()) {
            errors.add("Vui lòng chọn bệnh nhân.");
        } else if (!isValidUuid(form.getPatientId())) {
            errors.add("Mã bệnh nhân phải là UUID (không dùng mã PAT-000001).");
        } else if (form.getPatientId().trim().matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            errors.add("Mã bệnh nhân phải là UUID, không phải mã hiển thị.");
        }
        if (form.getNgayKham() == null || form.getNgayKham().isBlank()) {
            errors.add("Ngày khám là bắt buộc.");
        }

        switch (type) {
            case TAI_KHAM_NOI_TIET:
                normalizeEndocrinePayload(null, form);
                validateTaiKham(errors, form);
                break;
            case MAU_TONG_QUAT:
                validateBloodCount(errors, form);
                if (!form.hasBloodCountData()) {
                    errors.add("Vui lòng nhập ít nhất một chỉ số xét nghiệm máu tổng quát.");
                }
                break;
            case SINH_HOA_MAU:
                validateLabNumbers(errors, form);
                if (!form.hasBiochemistryData()) {
                    errors.add("Vui lòng nhập ít nhất một chỉ số sinh hóa máu.");
                }
                break;
            default:
                errors.add("Loại hồ sơ không hợp lệ.");
        }

        return errors;
    }

    public CreateResult create(EncounterCreateRequest form, String doctorId) throws SQLException {
        EncounterType type = form.resolveEncounterType();
        form.calculateBmiIfNeeded();
        if (type.isSinhHoaMau()) {
            form.syncLabToHealthMetrics();
        }

        String patientUuid = requirePatientUuid(form.getPatientId());
        String doctorUuid = requireDoctorUuid(doctorId);
        form.setPatientId(patientUuid);

        LOG.log(Level.INFO, "create medical_encounter patient_id={0} bac_si_id={1} type={2}",
                new Object[]{patientUuid, doctorUuid, type.getCode()});

        encounterDAO.validateInsertFields(form, doctorUuid);
        if (type.isTaiKhamNoiTiet()) {
            normalizeEndocrinePayload(null, form);
            validateEndocrineInsertFields(form, doctorUuid);
            logEndocrinePayload("before-sql-insert", form);
        }

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối database");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        String encounterId;

        try {
            encounterId = encounterDAO.insert(con, form, doctorUuid);

            if (type.isTaiKhamNoiTiet()) {
                patientDAO.updateLoaiTieuDuong(con, patientUuid, form.getPhanLoaiTieuDuong());
                if (form.hasPrescriptionData()) {
                    String prescriptionId = prescriptionDAO.insert(
                            con, form, patientUuid, doctorUuid, encounterId);
                    if (form.hasMedications()) {
                        medicationDAO.insertAll(con, prescriptionId, form.getMedications());
                    }
                }
            } else if (type.isMauTongQuat()) {
                labResultDAO.insertBloodCount(con, form, patientUuid, encounterId);
            } else if (type.isSinhHoaMau()) {
                labResultDAO.insertBiochemistry(con, form, patientUuid, encounterId);
            }

            snapshotService.applyEncounterToSnapshot(
                    con, form, patientUuid, doctorUuid, encounterId);

            if (!encounterDAO.existsById(con, encounterId)) {
                throw new SQLException("Encounter row missing before commit id=" + encounterId);
            }

            con.commit();
            LOG.log(Level.INFO,
                    "Committed medical_encounters transaction id={0} patient_id={1} bac_si_id={2}",
                    new Object[]{encounterId, patientUuid, doctorUuid});
        } catch (SQLException ex) {
            SqlDiagnostics.log(LOG, Level.SEVERE,
                    "create-medical-encounter",
                    "TRANSACTION medical_encounters + related tables",
                    new Object[]{patientUuid, doctorUuid, type.getCode()},
                    ex);
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
                SqlDiagnostics.log(LOG, Level.SEVERE, "rollback", null, null, rollbackEx);
            }
            LOG.log(Level.SEVERE,
                    "Rollback medical_encounters transaction patient_id=" + patientUuid
                            + " bac_si_id=" + doctorUuid, ex);
            throw ex;
        } finally {
            con.setAutoCommit(previousAutoCommit);
            con.close();
        }

        if (!encounterDAO.existsById(encounterId)) {
            throw new SQLException("Encounter not persisted after commit id=" + encounterId);
        }

        return new CreateResult(encounterId);
    }

    /** Đọc lại encounter từ DB sau commit — UI không dùng object trong memory. */
    public MedicalEncounter loadPersistedEncounter(String encounterId, String scopeDoctorId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        return encounterDAO.getEncounterById(encounterId, scopeDoctorId);
    }

    public boolean isEncounterPersisted(String encounterId) throws SQLException {
        return encounterDAO.existsById(encounterId);
    }

    private String requirePatientUuid(String patientId) throws SQLException {
        if (patientId == null || patientId.isBlank()) {
            throw new SQLException("patient_id is required");
        }
        String trimmed = patientId.trim();
        if (trimmed.matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            throw new SQLException("patient_id must be UUID, not display code: " + trimmed);
        }
        if (!isValidUuid(trimmed)) {
            throw new SQLException("patient_id must be a valid UUID: " + trimmed);
        }
        return trimmed;
    }

    private String requireDoctorUuid(String doctorId) throws SQLException {
        if (doctorId == null || doctorId.isBlank()) {
            throw new SQLException("bac_si_id is required");
        }
        String trimmed = doctorId.trim();
        if (trimmed.matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            throw new SQLException("bac_si_id must be UUID, not display code: " + trimmed);
        }
        if (!isValidUuid(trimmed)) {
            throw new SQLException("bac_si_id must be a valid UUID: " + trimmed);
        }
        return trimmed;
    }

    private boolean isValidUuid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Maps UI "Triệu chứng" to {@code medical_encounters.ly_do_kham} and syncs related endocrine fields.
     */
    public void normalizeEndocrinePayload(HttpServletRequest request, EncounterCreateRequest form) {
        if (!form.isTaiKhamNoiTiet()) {
            return;
        }

        String trieuChung = firstNonBlank(
                trimToNull(form.getTrieuChung()),
                trimToNull(form.getLyDoKham()),
                requestParam(request, "trieuChung"),
                requestParam(request, "trieu_chung"),
                requestParam(request, "lyDoKham"),
                requestParam(request, "ly_do_kham")
        );
        if (trieuChung != null) {
            form.setTrieuChung(trieuChung);
            form.setLyDoKham(trieuChung);
        }

        String chanDoanChinh = firstNonBlank(
                trimToNull(form.getChanDoanChinh()),
                requestParam(request, "chanDoanChinh"),
                requestParam(request, "chan_doan_chinh")
        );
        if (chanDoanChinh != null) {
            form.setChanDoanChinh(chanDoanChinh);
        }

        String tienSuBenh = firstNonBlank(
                trimToNull(form.getTienSuBenh()),
                trimToNull(form.getQuaTrinhBenhLy()),
                requestParam(request, "tienSuBenh"),
                requestParam(request, "tien_su_benh")
        );
        if (tienSuBenh != null) {
            form.setTienSuBenh(tienSuBenh);
            if (isBlank(form.getQuaTrinhBenhLy())) {
                form.setQuaTrinhBenhLy(tienSuBenh);
            }
        }
    }

    public void logEndocrinePayload(String stage, EncounterCreateRequest form) {
        if (!form.isTaiKhamNoiTiet()) {
            return;
        }
        LOG.log(Level.INFO,
                "Endocrine encounter payload [{0}]: patientId={1}, encounterType={2}, ngayKham={3}, "
                        + "lyDoKham={4}, trieuChung={5}, chanDoanChinh={6}, chanDoanPhu={7}, "
                        + "huongXuTri={8}, phanLoaiTieuDuong={9}, khamLamSang={10}, tienSuBenh={11}",
                new Object[] {
                        stage,
                        form.getPatientId(),
                        form.getEncounterType(),
                        form.getNgayKham(),
                        form.getLyDoKham(),
                        form.getTrieuChung(),
                        form.getChanDoanChinh(),
                        form.getChanDoanPhu(),
                        form.getHuongXuTri(),
                        form.getPhanLoaiTieuDuong(),
                        form.getKhamLamSang(),
                        form.getTienSuBenh()
                });
    }

    private void validateEndocrineInsertFields(EncounterCreateRequest form, String doctorId) throws SQLException {
        if (!isValidUuid(form.getPatientId())) {
            throw new SQLException("patient_id must be a valid UUID");
        }
        if (!isValidUuid(doctorId)) {
            throw new SQLException("bac_si_id must be a valid UUID");
        }
        if (isBlank(form.getNgayKham())) {
            throw new SQLException("ngay_kham is required");
        }
        if (isBlank(form.getLyDoKham())) {
            throw new SQLException("ly_do_kham is required (Triệu chứng)");
        }
        if (isBlank(form.getChanDoanChinh())) {
            throw new SQLException("chan_doan_chinh is required (Chẩn đoán chính)");
        }
        if (isBlank(form.getEncounterType())) {
            form.setEncounterType(EncounterType.TAI_KHAM_NOI_TIET.getCode());
        }
    }

    private void validateTaiKham(List<String> errors, EncounterCreateRequest form) {
        if (isBlank(form.getLyDoKham())) {
            errors.add("Triệu chứng là bắt buộc.");
        }
        if (isBlank(form.getChanDoanChinh())) {
            errors.add("Chẩn đoán chính là bắt buộc.");
        }
        validateNonNegative(errors, form.getCanNangKg(), "Cân nặng");
        validateNonNegative(errors, form.getChieuCaoCm(), "Chiều cao");
        validateNonNegative(errors, form.getBmi(), "BMI");
        validateNonNegative(errors, form.getNhietDoC(), "Nhiệt độ");
        validateBloodPressure(errors, form.getHuyetApTamThu(), form.getHuyetApTamTruong());
        validateHeartRate(errors, form.getNhipTim());
        validateRespiratoryRate(errors, form.getNhipTho());
        validateTemperature(errors, form.getNhietDoC());
        validateMedications(errors, form.getMedications());
    }

    private void validateBloodPressure(List<String> errors, Integer systolic, Integer diastolic) {
        if (systolic != null && (systolic < 50 || systolic > 250)) {
            errors.add("Huyết áp tâm thu phải từ 50 đến 250 mmHg.");
        }
        if (diastolic != null && (diastolic < 30 || diastolic > 150)) {
            errors.add("Huyết áp tâm trương phải từ 30 đến 150 mmHg.");
        }
        if (systolic != null && diastolic != null && diastolic > systolic) {
            errors.add("Huyết áp tâm trương không được lớn hơn tâm thu.");
        }
    }

    private void validateHeartRate(List<String> errors, Integer heartRate) {
        if (heartRate != null && (heartRate < 30 || heartRate > 220)) {
            errors.add("Nhịp tim phải từ 30 đến 220 bpm.");
        }
    }

    private void validateRespiratoryRate(List<String> errors, Integer rate) {
        if (rate != null && (rate < 8 || rate > 60)) {
            errors.add("Nhịp thở phải từ 8 đến 60 lần/phút.");
        }
    }

    private void validateTemperature(List<String> errors, Double temp) {
        if (temp != null && (temp < 34.0 || temp > 42.0)) {
            errors.add("Nhiệt độ phải từ 34.0 đến 42.0 °C.");
        }
    }

    private void validateLabNumbers(List<String> errors, EncounterCreateRequest form) {
        validateNonNegative(errors, form.getLabGlucoseMau(), "Glucose (sinh hóa)");
        validateNonNegative(errors, form.getLabHba1c(), "HbA1c (sinh hóa)");
        validateNonNegative(errors, form.getLabCholesterol(), "Cholesterol (sinh hóa)");
        validateNonNegative(errors, form.getLabTriglyceride(), "Triglyceride (sinh hóa)");
        validateNonNegative(errors, form.getLabHdl(), "HDL");
        validateNonNegative(errors, form.getLabLdl(), "LDL");
        validateNonNegative(errors, form.getLabAst(), "AST");
        validateNonNegative(errors, form.getLabAlt(), "ALT");
        validateNonNegative(errors, form.getLabUre(), "Ure");
        validateNonNegative(errors, form.getLabCreatinine(), "Creatinine");
    }

    private void validateBloodCount(List<String> errors, EncounterCreateRequest form) {
        validateNonNegative(errors, form.getLabWbc(), "WBC");
        validateNonNegative(errors, form.getLabRbc(), "RBC");
        validateNonNegative(errors, form.getLabHgb(), "HGB");
        validateNonNegative(errors, form.getLabHct(), "HCT");
        validateNonNegative(errors, form.getLabPlt(), "PLT");
    }

    private void validateMedications(List<String> errors, List<MedicationLineItem> medications) {
        for (int i = 0; i < medications.size(); i++) {
            MedicationLineItem med = medications.get(i);
            if (!med.hasContent()) {
                continue;
            }
            int row = i + 1;
            if (med.getLieuLuong() == null || med.getLieuLuong().isBlank()) {
                errors.add("Thuốc dòng " + row + ": liều lượng là bắt buộc.");
            }
            if (med.getTanSuat() == null || med.getTanSuat().isBlank()) {
                errors.add("Thuốc dòng " + row + ": tần suất là bắt buộc.");
            }
            if (med.getThoiGianDungNgay() != null && med.getThoiGianDungNgay() < 0) {
                errors.add("Thuốc dòng " + row + ": số ngày dùng không được âm.");
            }
        }
    }

    private void validateNonNegative(List<String> errors, Double value, String label) {
        if (value != null && value < 0) {
            errors.add(label + " không được âm.");
        }
    }

    private static String requestParam(HttpServletRequest request, String name) {
        if (request == null) {
            return null;
        }
        String value = request.getParameter(name);
        return trimToNull(value);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
