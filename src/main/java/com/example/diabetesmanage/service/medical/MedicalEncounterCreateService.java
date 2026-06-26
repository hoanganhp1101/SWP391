package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.form.AddMedicalEncounterForm;
import com.example.diabetesmanage.model.form.AddMedicalEncounterForm.MedicationFormItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu hồ sơ bệnh án mới trong một transaction duy nhất.
 */
public class MedicalEncounterCreateService {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public static class CreateResult {
        private final String healthRecordId;
        private final String encounterId;

        public CreateResult(String healthRecordId, String encounterId) {
            this.healthRecordId = healthRecordId;
            this.encounterId = encounterId;
        }

        public String getHealthRecordId() {
            return healthRecordId;
        }

        public String getEncounterId() {
            return encounterId;
        }
    }

    public List<String> validate(AddMedicalEncounterForm form) {
        List<String> errors = new ArrayList<>();

        if (form.getTrieuChung() == null || form.getTrieuChung().isBlank()) {
            errors.add("Triệu chứng là bắt buộc.");
        }
        if (form.getChanDoanChinh() == null || form.getChanDoanChinh().isBlank()) {
            errors.add("Chẩn đoán chính là bắt buộc.");
        }

        validateNonNegative(errors, form.getDuongHuyetMgdl(), "Đường huyết");
        validateNonNegative(errors, form.getCanNangKg(), "Cân nặng");
        validateNonNegative(errors, form.getChieuCaoCm(), "Chiều cao");
        validateNonNegative(errors, form.getBmi(), "BMI");
        validateNonNegative(errors, form.getHba1cPercent(), "HbA1c");
        validateNonNegative(errors, form.getCholesterolMmol(), "Cholesterol");
        validateNonNegative(errors, form.getTriglycerideMmol(), "Triglyceride");
        validateNonNegative(errors, form.getCarbsG(), "Carbs");
        validateNonNegative(errors, form.getNhietDoC(), "Nhiệt độ");

        validateBloodPressure(errors, form.getHuyetApTamThu(), form.getHuyetApTamTruong());
        validateHeartRate(errors, form.getNhipTim());
        validateRespiratoryRate(errors, form.getNhipTho());
        validateTemperature(errors, form.getNhietDoC());

        validateLabNumbers(errors, form);
        validateBloodCount(errors, form);
        validateMedications(errors, form.getMedications());

        return errors;
    }

    public CreateResult create(AddMedicalEncounterForm form, String doctorId) throws SQLException {
        form.syncLabToHealthMetrics();
        form.calculateBmiIfNeeded();

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối database");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            String encounterId = encounterDAO.insert(con, form, doctorId);
            String healthRecordId = healthRecordDAO.insert(con, form, form.getPatientId(), doctorId);

            patientDAO.updateLoaiTieuDuong(con, form.getPatientId(), form.getPhanLoaiTieuDuong());

            if (form.hasLabData()) {
                labResultDAO.insert(con, form, form.getPatientId(), encounterId);
            }

            if (form.hasPrescriptionData()) {
                String prescriptionId = prescriptionDAO.insert(
                        con, form, form.getPatientId(), doctorId, encounterId);
                if (form.hasMedications()) {
                    medicationDAO.insertAll(con, prescriptionId, form.getMedications());
                }
            }

            con.commit();
            return new CreateResult(healthRecordId, encounterId);
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(previousAutoCommit);
            con.close();
        }
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

    private void validateLabNumbers(List<String> errors, AddMedicalEncounterForm form) {
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

    private void validateBloodCount(List<String> errors, AddMedicalEncounterForm form) {
        validateNonNegative(errors, form.getLabWbc(), "WBC");
        validateNonNegative(errors, form.getLabRbc(), "RBC");
        validateNonNegative(errors, form.getLabHgb(), "HGB");
        validateNonNegative(errors, form.getLabHct(), "HCT");
        validateNonNegative(errors, form.getLabPlt(), "PLT");
    }

    private void validateMedications(List<String> errors, List<MedicationFormItem> medications) {
        for (int i = 0; i < medications.size(); i++) {
            MedicationFormItem med = medications.get(i);
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
}
