package com.example.diabetesmanage.service;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.dto.MedicalEncounterDTO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Medical Record: validate + create encounter, và load chi tiết hồ sơ để render Detail/PDF.
 */
public class MedicalRecordService {

    private static final double UMOL_TO_MGDL = 1.0 / 88.4;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final UserDAO userDAO = UserDAO.getInstance();

    public static class CreateResult {
        private final String encounterId;

        public CreateResult(String encounterId) {
            this.encounterId = encounterId;
        }

        public String getEncounterId() {
            return encounterId;
        }
    }

    /**
     * Validation Bước 1 (trước khi gọi AI / tạo encounter).
     * KHÔNG bắt buộc chẩn đoán / hướng xử trí — những trường này bác sĩ nhập ở Bước 2
     * (Treatment Plan) và UPDATE lại encounter sau đó.
     * Chỉ kiểm tra: bệnh nhân, ngày khám, loại hồ sơ, và dữ liệu tối thiểu theo loại hồ sơ
     * (glucose cho nội tiết, chỉ số xét nghiệm cho hồ sơ máu) để đủ dữ kiện phân tích AI.
     */
    public List<String> validateStep1(EncounterCreateDTO form) {
        List<String> errors = new ArrayList<>();
        String rawType = form.getEncounterType();
        String type = form.resolveEncounterType();
        boolean validType = "tai_kham_noi_tiet".equalsIgnoreCase(rawType)
                || "mau_tong_quat".equalsIgnoreCase(rawType)
                || "sinh_hoa_mau".equalsIgnoreCase(rawType);

        if (form.getPatientId() == null || form.getPatientId().isBlank()) {
            errors.add("Vui lòng chọn bệnh nhân.");
        } else if (!isValidUuid(form.getPatientId())
                || form.getPatientId().trim().matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            errors.add("Bệnh nhân đã chọn không hợp lệ. Vui lòng chọn lại bệnh nhân.");
        }
        validateVisitDate(errors, form.getNgayKham());
        if (rawType == null || rawType.isBlank()) {
            errors.add("Vui lòng chọn Loại hồ sơ.");
        } else if (!validType) {
            errors.add("Loại hồ sơ không hợp lệ. Vui lòng chọn lại.");
        }
        if (!validType) {
            return errors;
        }

        if ("tai_kham_noi_tiet".equalsIgnoreCase(type)) {
            if (isBlank(form.getLyDoKham())) {
                errors.add("Vui lòng nhập Lý do khám.");
            }
            if (form.getDuongHuyetMgdl() == null) {
                errors.add("Vui lòng nhập Đường huyết.");
            }
            validateRange(errors, form.getDuongHuyetMgdl(), 20, 800,
                    "Đường huyết phải nằm trong khoảng 20–800 mg/dL.");
            validateRange(errors, form.getHba1cPercent(), 3, 20,
                    "HbA1c chỉ được nhập từ 3% đến 20%.");
            validateHeight(errors, form.getChieuCaoCm());
            validateRange(errors, form.getCanNangKg(), 2, 500,
                    "Cân nặng phải nằm trong khoảng 2–500 kg.");
            validateNonNegative(errors, form.getBmi(), "BMI");
            validateBloodPressure(errors, form.getHuyetApTamThu(), form.getHuyetApTamTruong());
            validateHeartRate(errors, form.getNhipTim());
            validateRespiratoryRate(errors, form.getNhipTho());
            validateTemperature(errors, form.getNhietDoC());
        } else if ("mau_tong_quat".equalsIgnoreCase(type)) {
            validateBloodCount(errors, form);
            if (!form.hasBloodCountData()) {
                errors.add("Vui lòng nhập ít nhất một chỉ số xét nghiệm máu tổng quát.");
            }
        } else if ("sinh_hoa_mau".equalsIgnoreCase(type)) {
            validateLabNumbers(errors, form);
            if (!form.hasBiochemistryData()) {
                errors.add("Vui lòng nhập ít nhất một chỉ số sinh hóa máu.");
            }
        }

        return errors;
    }

    /**
     * Tạo encounter ở cuối Bước 1 (sau validate + AI). Với hồ sơ tái khám Nội tiết,
     * chẩn đoán chính chỉ được bác sĩ nhập ở Bước 2 (Treatment Plan), nhưng cột
     * {@code chan_doan_chinh} là NOT NULL nên đặt placeholder "Đang cập nhật" trước khi
     * INSERT; Bước 2 sẽ UPDATE lại bằng chẩn đoán thật.
     */
    public CreateResult create(EncounterCreateDTO form, String doctorId) throws SQLException {
        String type = form.resolveEncounterType();
        form.prepareForSave();

        String patientUuid = requirePatientUuid(form.getPatientId());
        String doctorUuid = requireDoctorUuid(doctorId);
        form.setPatientId(patientUuid);

        if ("tai_kham_noi_tiet".equalsIgnoreCase(type)) {
            validateEndocrineInsertFields(form, doctorUuid);
            // Chẩn đoán chính nhập ở Bước 2 → giữ chỗ để thỏa ràng buộc NOT NULL.
            if (isBlank(form.getChanDoanChinh())) {
                form.setChanDoanChinh("Đang cập nhật");
            }
        }
        encounterDAO.validateInsertFields(form, doctorUuid);

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        String encounterId;

        try {
            encounterId = encounterDAO.insert(con, form, doctorUuid);

            if ("tai_kham_noi_tiet".equalsIgnoreCase(type)) {
                patientDAO.updateLoaiTieuDuong(con, patientUuid, form.getPhanLoaiTieuDuong());
                if (form.hasPrescriptionData()) {
                    String prescriptionId = prescriptionDAO.insert(
                            con, form, patientUuid, doctorUuid, encounterId);
                    if (form.hasMedications()) {
                        medicationDAO.insertAll(con, prescriptionId, form.getMedications());
                    }
                }
            } else if ("mau_tong_quat".equalsIgnoreCase(type)) {
                labResultDAO.insertBloodCount(con, form, patientUuid, encounterId);
            } else if ("sinh_hoa_mau".equalsIgnoreCase(type)) {
                labResultDAO.insertBiochemistry(con, form, patientUuid, encounterId);
            }

            if (!encounterDAO.existsById(con, encounterId)) {
                throw new SQLException("Không tìm thấy dữ liệu lần khám trước khi lưu, mã=" + encounterId);
            }
            MedicalEncounter encounter = encounterDAO.getEncounterById(con, encounterId);
            if ("tai_kham_noi_tiet".equalsIgnoreCase(type)
                    && encounter != null
                    && encounter.getNgayKham() != null) {
                healthRecordDAO.insert(
                        con,
                        form,
                        encounterId,
                        patientUuid,
                        doctorUuid,
                        encounter.getNgayKham()
                );
            }
            con.commit();
        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
            }
            throw ex;
        } finally {
            con.setAutoCommit(previousAutoCommit);
            con.close();
        }

        if (!encounterDAO.existsById(encounterId)) {
            throw new SQLException("Lần khám chưa được lưu sau khi hoàn tất giao dịch, mã=" + encounterId);
        }

        return new CreateResult(encounterId);
    }

    public MedicalEncounterDTO loadMedicalRecordDetail(String encounterId, String scopeDoctorId) {
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            encounter = encounterDAO.getEncounterById(encounterId, null);
        }
        if (encounter == null) {
            return null;
        }
        return buildDetailViewFromEncounter(encounter, scopeDoctorId);
    }

    private String requirePatientUuid(String patientId) throws SQLException {
        if (patientId == null || patientId.isBlank()) {
            throw new SQLException("Thiếu mã bệnh nhân");
        }
        String trimmed = patientId.trim();
        if (trimmed.matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            throw new SQLException("Mã bệnh nhân phải là UUID, không phải mã hiển thị: " + trimmed);
        }
        if (!isValidUuid(trimmed)) {
            throw new SQLException("Mã bệnh nhân không đúng định dạng UUID: " + trimmed);
        }
        return trimmed;
    }

    private String requireDoctorUuid(String doctorId) throws SQLException {
        if (doctorId == null || doctorId.isBlank()) {
            throw new SQLException("Thiếu mã bác sĩ");
        }
        String trimmed = doctorId.trim();
        if (trimmed.matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            throw new SQLException("Mã bác sĩ phải là UUID, không phải mã hiển thị: " + trimmed);
        }
        if (!isValidUuid(trimmed)) {
            throw new SQLException("Mã bác sĩ không đúng định dạng UUID: " + trimmed);
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
     * Guard tối thiểu trước INSERT hồ sơ tái khám Nội tiết (Bước 1).
     * KHÔNG kiểm tra chẩn đoán chính — trường này bác sĩ nhập ở Bước 2 (Treatment Plan)
     * và được UPDATE lại sau khi hoàn thành Bước 2.
     */
    private void validateEndocrineInsertFields(EncounterCreateDTO form, String doctorId) throws SQLException {
        if (!isValidUuid(form.getPatientId())) {
            throw new SQLException("Mã bệnh nhân không đúng định dạng UUID");
        }
        if (!isValidUuid(doctorId)) {
            throw new SQLException("Mã bác sĩ không đúng định dạng UUID");
        }
        if (isBlank(form.getNgayKham())) {
            throw new SQLException("Vui lòng chọn Ngày khám");
        }
        if (isBlank(form.getLyDoKham())) {
            throw new SQLException("Vui lòng nhập Lý do khám");
        }
        if (isBlank(form.getEncounterType())) {
            form.setEncounterType("tai_kham_noi_tiet");
        }
    }

    private void validateBloodPressure(List<String> errors, Integer systolic, Integer diastolic) {
        if (systolic != null && (systolic < 50 || systolic > 300)) {
            errors.add("Huyết áp tâm thu phải từ 50–300 mmHg.");
        }
        if (diastolic != null && (diastolic < 30 || diastolic > 200)) {
            errors.add("Huyết áp tâm trương phải từ 30–200 mmHg.");
        }
        if (systolic != null && diastolic != null && diastolic > systolic) {
            errors.add("Huyết áp tâm trương không được lớn hơn tâm thu.");
        }
    }

    private void validateHeartRate(List<String> errors, Integer heartRate) {
        if (heartRate != null && (heartRate < 20 || heartRate > 250)) {
            errors.add("Nhịp tim phải từ 20–250 bpm.");
        }
    }

    private void validateRespiratoryRate(List<String> errors, Integer rate) {
        if (rate != null && (rate < 5 || rate > 80)) {
            errors.add("Nhịp thở phải từ 5–80 lần/phút.");
        }
    }

    private void validateTemperature(List<String> errors, Double temp) {
        if (temp != null && (temp < 30.0 || temp > 45.0)) {
            errors.add("Nhiệt độ cơ thể phải từ 30°C đến 45°C.");
        }
    }

    private void validateLabNumbers(List<String> errors, EncounterCreateDTO form) {
        validateNonNegative(errors, form.getLabGlucoseMau(), "Glucose (sinh hóa)");
        validateRange(errors, form.getLabHba1c(), 3, 20,
                "HbA1c chỉ được nhập từ 3% đến 20%.");
        validateNonNegative(errors, form.getLabCholesterol(), "Cholesterol (sinh hóa)");
        validateNonNegative(errors, form.getLabTriglyceride(), "Triglyceride (sinh hóa)");
        validateNonNegative(errors, form.getLabHdl(), "HDL");
        validateNonNegative(errors, form.getLabLdl(), "LDL");
        validateNonNegative(errors, form.getLabAst(), "AST");
        validateNonNegative(errors, form.getLabAlt(), "ALT");
        validateNonNegative(errors, form.getLabUre(), "Ure");
        validateNonNegative(errors, form.getLabCreatinine(), "Creatinine");
    }

    private void validateBloodCount(List<String> errors, EncounterCreateDTO form) {
        validateNonNegative(errors, form.getLabWbc(), "WBC");
        validateNonNegative(errors, form.getLabRbc(), "RBC");
        validateNonNegative(errors, form.getLabHgb(), "HGB");
        validateNonNegative(errors, form.getLabHct(), "HCT");
        validateNonNegative(errors, form.getLabPlt(), "PLT");
    }

    private void validateNonNegative(List<String> errors, Double value, String label) {
        if (value != null && !Double.isFinite(value)) {
            errors.add(label + " phải là số hợp lệ.");
        } else if (value != null && value < 0) {
            errors.add(label + " không được âm.");
        }
    }

    private void validateRange(List<String> errors, Double value, double min, double max,
                               String message) {
        if (value != null && !Double.isFinite(value)) {
            errors.add(message.startsWith("HbA1c") ? "HbA1c phải là số."
                    : message.startsWith("Đường huyết") ? "Đường huyết phải là số."
                    : message.substring(0, message.indexOf(" phải")) + " phải là số hợp lệ.");
        } else if (value != null && (value < min || value > max)) {
            errors.add(message);
        }
    }

    private void validateHeight(List<String> errors, Double height) {
        if (height != null && !Double.isFinite(height)) {
            errors.add("Chiều cao phải là số hợp lệ.");
        } else if (height != null && height <= 50) {
            errors.add("Chiều cao phải lớn hơn 50 cm.");
        } else if (height != null && height > 250) {
            errors.add("Chiều cao không được vượt quá 250 cm.");
        }
    }

    private void validateVisitDate(List<String> errors, String visitDate) {
        if (visitDate == null || visitDate.isBlank()) {
            errors.add("Vui lòng chọn Ngày khám.");
            return;
        }
        try {
            LocalDate parsed = LocalDate.parse(visitDate);
            if (parsed.isAfter(LocalDate.now())) {
                errors.add("Ngày khám không được lớn hơn ngày hiện tại.");
            }
        } catch (DateTimeParseException ex) {
            errors.add("Ngày khám không hợp lệ. Vui lòng chọn lại.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private MedicalEncounterDTO buildDetailViewFromEncounter(
            MedicalEncounter encounter, String scopeDoctorId) {
        Patient patient = patientDAO.getPatientById(encounter.getPatientId(), scopeDoctorId);
        HealthRecord healthRecord =
                healthRecordDAO.getByEncounterId(encounter.getId());
        LabResult lab = labResultDAO.getByEncounterId(encounter.getId());
        Map<String, String> prescriptionAdvice =
                prescriptionDAO.getAdviceForEncounterOrLatestPatient(
                        encounter.getId(), encounter.getPatientId());
        String prescriptionId = prescriptionDAO.getIdByEncounterId(encounter.getId());
        List<Map<String, String>> prescriptionItems =
                medicationDAO.getDetailsByPrescriptionId(prescriptionId);

        MedicalEncounterDTO view = new MedicalEncounterDTO();
        view.setRecordId(encounter.getId());
        view.setRecordCode(encounter.getDisplayCode());

        if (patient != null) {
            view.setPatientCode(patient.getPatientCode());
            if (patient.getUser() != null) {
                view.setPatientName(patient.getUser().getHoTen());
            }
        } else {
            view.setPatientCode(encounter.getPatientCode());
            view.setPatientName(encounter.getPatientName());
        }

        if (encounter.getNgayKham() != null) {
            view.setExamDate(encounter.getNgayKham().format(DATE_FMT));
        }

        String encounterType = resolveEncounterType(encounter);
        view.setEncounterType(encounterType);
        view.setEncounterTypeLabel(encounterTypeLabel(encounterType));

        if (encounter.getDoctorName() != null && !encounter.getDoctorName().isBlank()) {
            view.setDoctorName(encounter.getDoctorName());
        } else if (encounter.getBacSiId() != null) {
            String doctorName = userDAO.getNameById(encounter.getBacSiId());
            if (doctorName != null && !doctorName.isBlank()) {
                view.setDoctorName(doctorName);
            }
        }

        String khoaKham = EncounterClinicalJson.parseString(encounter.getKhamLamSang(), "khoa_kham");
        if (khoaKham != null && !khoaKham.isBlank()) {
            view.setDepartment(khoaKham);
        }

        view.setInternalMedicine(buildInternalMedicineFromEncounter(
                encounter, patient, healthRecord, prescriptionAdvice));
        if ("tai_kham_noi_tiet".equalsIgnoreCase(encounterType)) {
            view.setPrescriptionDetail(buildPrescriptionDetail(prescriptionItems));
        } else {
            view.setPrescriptionDetail(new MedicalEncounterDTO.PrescriptionDetailSection());
        }
        if ("mau_tong_quat".equalsIgnoreCase(encounterType)) {
            view.setBloodCount(buildBloodCount(lab));
        } else {
            view.setBloodCount(new MedicalEncounterDTO.BloodCountSection());
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(encounterType)) {
            view.setBiochemistry(buildBiochemistryFromLab(lab));
        } else {
            view.setBiochemistry(new MedicalEncounterDTO.BiochemistrySection());
        }
        view.setUltrasound(new MedicalEncounterDTO.UltrasoundSection());
        return view;
    }

    private String resolveEncounterType(MedicalEncounter encounter) {
        if (encounter.getLoaiEncounter() != null && !encounter.getLoaiEncounter().isBlank()) {
            return canonicalTypeCode(encounter.getLoaiEncounter());
        }
        String json = encounter.getKhamLamSang();
        String fromJson = EncounterClinicalJson.parseString(json, "loai_encounter");
        if (fromJson != null && !fromJson.isBlank()) {
            return canonicalTypeCode(fromJson);
        }
        return "tai_kham_noi_tiet";
    }

    /** Chuẩn hóa mã loại hồ sơ (kể cả alias cũ); mặc định tai_kham_noi_tiet. */
    private static String canonicalTypeCode(String code) {
        String normalized = code == null ? "" : code.trim().replace('-', '_');
        if ("mau_tong_quat".equalsIgnoreCase(normalized)
                || "general_blood_test".equalsIgnoreCase(normalized)
                || "blood_test".equalsIgnoreCase(normalized)
                || "cbc".equalsIgnoreCase(normalized)) {
            return "mau_tong_quat";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(normalized)
                || "biochemistry_test".equalsIgnoreCase(normalized)
                || "biochemistry".equalsIgnoreCase(normalized)
                || "sinh_hoa".equalsIgnoreCase(normalized)) {
            return "sinh_hoa_mau";
        }
        return "tai_kham_noi_tiet";
    }

    private static String encounterTypeLabel(String typeCode) {
        if ("mau_tong_quat".equalsIgnoreCase(typeCode)) {
            return "Kết quả xét nghiệm máu tổng quát";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(typeCode)) {
            return "Kết quả sinh hóa máu";
        }
        return "Bệnh án tái khám Nội tiết";
    }

    private MedicalEncounterDTO.InternalMedicineSection buildInternalMedicineFromEncounter(
            MedicalEncounter encounter,
            Patient patient,
            HealthRecord healthRecord,
            Map<String, String> prescriptionAdvice
    ) {
        MedicalEncounterDTO.InternalMedicineSection section =
                new MedicalEncounterDTO.InternalMedicineSection();
        String json = encounter.getKhamLamSang();

        section.getClinicalInfo().add(textField(
                "Tiền sử bệnh", patient != null ? patient.getTienSuBenh() : null));
        section.getClinicalInfo().add(textField(
                "Khám lâm sàng",
                resolveClinicalExamination(json)));

        Double height = patient != null ? patient.getChieuCaoCm() : null;
        section.getHealthMetrics().add(field(
                "Chiều cao", height != null ? format(height) : "—", "cm", null));

        Double weight = healthRecord != null ? healthRecord.getCanNangKg() : null;
        section.getHealthMetrics().add(field(
                "Cân nặng", weight != null ? format(weight) : "—", "kg", null));

        Double bmi = healthRecord != null ? healthRecord.getBmi() : null;
        if (bmi != null) {
            section.getHealthMetrics().add(bmi(bmi));
        } else {
            section.getHealthMetrics().add(field("BMI", "—", "kg/m²", "18.5-24.9"));
        }

        Double bloodGlucose = healthRecord != null ? healthRecord.getDuongHuyetMgdl() : null;
        section.getHealthMetrics().add(glucose(
                bloodGlucose,
                healthRecord != null ? healthRecord.getThoiDiemDoDuong() : null));

        Integer systolic = healthRecord != null ? healthRecord.getHuyetApTamThu() : null;
        Integer diastolic = healthRecord != null ? healthRecord.getHuyetApTamTruong() : null;
        if (systolic != null || diastolic != null) {
            section.getHealthMetrics().add(bloodPressure(systolic, diastolic));
        } else {
            section.getHealthMetrics().add(field("Huyết áp", "—", "mmHg", "< 120/80"));
        }

        Integer heartRate = healthRecord != null ? healthRecord.getNhipTim() : null;
        section.getHealthMetrics().add(field(
                "Nhịp tim",
                heartRate != null ? String.valueOf(heartRate) : "—",
                "bpm", "60-100"));

        Double temperature = healthRecord != null ? healthRecord.getNhietDoC() : null;
        section.getHealthMetrics().add(field(
                "Nhiệt độ",
                temperature != null ? format(temperature) : "—",
                "°C", "36.0-37.5"));

        Integer respiratoryRate = healthRecord != null ? healthRecord.getNhipTho() : null;
        section.getHealthMetrics().add(field(
                "Nhịp thở",
                respiratoryRate != null ? String.valueOf(respiratoryRate) : "—",
                "lần/phút", "12-20"));
        section.getHealthMetrics().add(field(
                "Liều insulin",
                healthRecord != null && healthRecord.getLieuLuongInsulinUi() != null
                        ? String.valueOf(healthRecord.getLieuLuongInsulinUi()) : "—",
                "UI", null));
        section.getHealthMetrics().add(textField(
                "Ghi chú sức khỏe",
                healthRecord != null ? healthRecord.getGhiChu() : null));

        String diagnosis = encounter.getChanDoanChinh();
        section.getDiagnosisInfo().add(textField(
                "Chẩn đoán chính",
                diagnosis
        ));
        section.getDiagnosisInfo().add(textField(
                "Chẩn đoán phụ",
                encounter.getChanDoanPhu()
        ));
        section.getDiagnosisInfo().add(textField(
                "Phân loại tiểu đường",
                patient != null ? patient.getLoaiTieuDuong() : null
        ));

        section.getRecommendationFields().add(textField(
                "Hướng xử trí",
                encounter.getHuongXuTri()
        ));
        section.getRecommendationFields().add(textField(
                "Chế độ ăn",
                prescriptionAdvice != null ? prescriptionAdvice.get("che_do_an") : null
        ));
        section.getRecommendationFields().add(textField(
                "Luyện tập",
                prescriptionAdvice != null ? prescriptionAdvice.get("luyen_tap") : null
        ));

        List<String> recommendations = new ArrayList<>();
        appendRecommendation(recommendations, encounter.getHuongXuTri());
        if (prescriptionAdvice != null) {
            appendRecommendation(recommendations, prescriptionAdvice.get("che_do_an"));
            appendRecommendation(recommendations, prescriptionAdvice.get("luyen_tap"));
        }
        section.setMedications(List.of());
        section.setRecommendations(recommendations);
        return section;
    }

    private String resolveClinicalExamination(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        String jsonValue = EncounterClinicalJson.parseString(storedValue, "noi_dung");
        if (jsonValue != null && !jsonValue.isBlank()) {
            return jsonValue;
        }
        String trimmed = storedValue.trim();
        return trimmed.startsWith("{") ? null : trimmed;
    }

    private void appendRecommendation(List<String> target, String item) {
        if (item == null || item.isBlank()) {
            return;
        }
        String trimmed = item.trim();
        if (!target.contains(trimmed)) {
            target.add(trimmed);
        }
    }

    private MedicalEncounterDTO.PrescriptionDetailSection buildPrescriptionDetail(
            List<Map<String, String>> items) {
        MedicalEncounterDTO.PrescriptionDetailSection section =
                new MedicalEncounterDTO.PrescriptionDetailSection();
        section.setItems(items != null ? items : List.of());
        return section;
    }

    private MedicalEncounterDTO.BiochemistrySection buildBiochemistryFromLab(LabResult lab) {
        MedicalEncounterDTO.BiochemistrySection section =
                new MedicalEncounterDTO.BiochemistrySection();

        Double glucoseMgdl = lab != null ? lab.getGlucoseMgdl() : null;
        Double hba1c = lab != null ? lab.getHba1c() : null;

        section.setGlucose(glucose(glucoseMgdl, null));
        section.setHba1c(hba1c(hba1c));

        section.getLipidProfile().add(lab(
                "Cholesterol", lab != null ? lab.getCholesterolTp() : null, "mmol/L", "< 5.2", 0, 5.2));
        section.getLipidProfile().add(lab(
                "Triglyceride", lab != null ? lab.getTriglyceride() : null, "mmol/L", "< 1.7", 0, 1.7));
        section.getLipidProfile().add(lab(
                "HDL", lab != null ? lab.getHdlC() : null, "mmol/L", "> 1.0", 1.0, 99));
        section.getLipidProfile().add(lab(
                "LDL", lab != null ? lab.getLdlC() : null, "mmol/L", "< 3.4", 0, 3.4));

        section.getLiverEnzymes().add(lab("AST", lab != null ? lab.getAst() : null, "U/L", "< 40", 0, 40));
        section.getLiverEnzymes().add(lab("ALT", lab != null ? lab.getAlt() : null, "U/L", "< 41", 0, 41));

        Double creatinineMgdl = null;
        if (lab != null && lab.getCreatinine() != null) {
            creatinineMgdl = lab.getCreatinine() * UMOL_TO_MGDL;
        }
        section.getKidneyFunction().add(lab(
                "Creatinine", creatinineMgdl, "mg/dL", "0.6-1.2", 0.6, 1.2));
        if (lab != null && lab.getUre() != null) {
            section.getKidneyFunction().add(lab(
                    "Ure", lab.getUre(), "mmol/L", "2.5-7.5", 2.5, 7.5));
        }

        String alert = diabetesAlert(glucoseMgdl, hba1c);
        if (alert != null) {
            section.getAlerts().add(alert);
        }
        if (isAbnormal(section.getGlucose())) {
            section.getAlerts().add("Đường huyết bất thường — cần can thiệp điều trị");
        }
        return section;
    }

    private MedicalEncounterDTO.BloodCountSection buildBloodCount(LabResult lab) {
        MedicalEncounterDTO.BloodCountSection section =
                new MedicalEncounterDTO.BloodCountSection();
        section.getItems().add(lab("WBC", lab != null ? lab.getWbc() : null, "G/L", "4.0-10.0", 4.0, 10.0));
        section.getItems().add(lab("RBC", lab != null ? lab.getRbc() : null, "T/L", "4.0-5.5", 4.0, 5.5));
        section.getItems().add(lab("HGB", lab != null ? lab.getHgb() : null, "g/dL", "12-16", 12, 16));
        section.getItems().add(lab("HCT", lab != null ? lab.getHct() : null, "%", "36-46", 36, 46));
        section.getItems().add(lab("PLT", lab != null ? lab.getPlt() : null, "G/L", "150-400", 150, 400));
        return section;
    }

    private Map<String, Object> glucose(Double value, String timing) {
        Map<String, Object> item = field("Glucose", format(value), "mg/dL", "70-99 (lúc đói)");
        if (value == null) {
            return item;
        }
        item.put("highlightLevel", "CORE");
        if (value < 70) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 126) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 100) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        if (timing != null && !timing.isBlank()) {
            item.put("label", "Glucose (" + timing + ")");
        }
        return item;
    }

    private Map<String, Object> hba1c(Double value) {
        Map<String, Object> item = field("HbA1c", format(value), "%", "< 5.7");
        item.put("highlightLevel", "CORE");
        if (value == null) {
            return item;
        }
        if (value >= 6.5) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 5.7) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> bmi(Double value) {
        Map<String, Object> item = field("BMI", format(value), "kg/m²", "18.5-24.9");
        if (value != null && value >= 30) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> bloodPressure(Integer systolic, Integer diastolic) {
        String value = "—";
        if (systolic != null && diastolic != null) {
            value = systolic + "/" + diastolic;
        }
        Map<String, Object> item = field("Huyết áp", value, "mmHg", "< 120/80");
        if (systolic != null && systolic >= 140) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        if (diastolic != null && diastolic >= 90) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> lab(String label, Double value, String unit, String range,
                                   double low, double high) {
        Map<String, Object> item = field(label, format(value), unit, range);
        if (value != null && (value < low || value > high)) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> textField(String label, String value) {
        return field(label, value == null || value.isBlank() ? "—" : value, null, null);
    }

    private boolean isAbnormal(Map<String, Object> field) {
        return field != null && Boolean.TRUE.equals(field.get("abnormal"));
    }

    private String diabetesAlert(Double glucoseMgdl, Double hba1c) {
        if (hba1c != null && hba1c >= 6.5) {
            return "HbA1c ≥ 6.5% — tiêu chí chẩn đoán Đái tháo đường";
        }
        if (glucoseMgdl != null && glucoseMgdl >= 126) {
            return "Glucose ≥ 126 mg/dL — đường huyết bất thường";
        }
        if (hba1c != null && hba1c >= 5.7) {
            return "HbA1c 5.7-6.4% — tiền đái tháo đường";
        }
        return null;
    }

    private Map<String, Object> field(String label, String value, String unit, String range) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("unit", unit);
        item.put("referenceRange", range);
        item.put("abnormal", false);
        item.put("highlightLevel", "NORMAL");
        item.put("displayValue", buildDisplayValue(value, unit));
        return item;
    }

    private String buildDisplayValue(String value, String unit) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        if (unit == null || unit.isBlank()) {
            return value;
        }
        return value + " " + unit;
    }

    private String format(Double value) {
        if (value == null) {
            return "—";
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value.doubleValue());
        }
        return String.format("%.1f", value);
    }
}
