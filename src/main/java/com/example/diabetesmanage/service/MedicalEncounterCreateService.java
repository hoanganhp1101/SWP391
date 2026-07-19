package com.example.diabetesmanage.service;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.model.MedicalEncounter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lưu Medical Encounter theo đúng loại hồ sơ trong một transaction.
 */
public class MedicalEncounterCreateService {
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
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

}
