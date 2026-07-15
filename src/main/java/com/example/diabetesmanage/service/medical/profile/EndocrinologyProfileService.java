package com.example.diabetesmanage.service.medical.profile;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aggregates patient endocrinology data.
 * health_records = baseline; medical_encounters + labs + prescriptions = enrichment only.
 */
public class EndocrinologyProfileService {

    private static final Logger LOG = Logger.getLogger(EndocrinologyProfileService.class.getName());

    private static final String SQL_PATIENT =
            "SELECT p.id AS patient_id, " +
                    "p.patient_code AS patient_code, " +
                    "p.ngay_sinh AS patient_birth_date, " +
                    "p.gioi_tinh AS patient_gender, " +
                    "p.chieu_cao_cm AS patient_height_cm, " +
                    "p.dia_chi AS patient_address, " +
                    "p.bao_hiem_y_te AS patient_insurance, " +
                    "p.tien_su_benh AS patient_medical_history, " +
                    "p.di_ung AS patient_allergies, " +
                    "p.nhom_mau AS patient_blood_type, " +
                    "p.loai_tieu_duong AS patient_diabetes_type, " +
                    "p.ngay_chan_doan_tieu_duong AS patient_diabetes_diagnosis_date, " +
                    "p.bac_si_id AS assigned_doctor_id, " +
                    "TIMESTAMPDIFF(YEAR, p.ngay_sinh, CURDATE()) AS patient_age, " +
                    "u.id AS user_id, " +
                    "u.ho_ten AS user_full_name, " +
                    "u.email AS user_email, " +
                    "u.so_dien_thoai AS user_phone " +
                    "FROM patients p " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE p.id = ?";

    private static final String SQL_BASELINE_HEALTH_RECORD =
            "SELECT hr.id AS health_record_id, " +
                    "hr.patient_id AS health_patient_id, " +
                    "hr.thoi_gian_do AS health_measured_at, " +
                    "hr.ngay_tao AS health_updated_at, " +
                    "hr.duong_huyet_mgdl AS health_glucose_mgdl, " +
                    "hr.thoi_diem_do_duong AS health_glucose_timing, " +
                    "hr.huyet_ap_tam_thu AS health_systolic_bp, " +
                    "hr.huyet_ap_tam_truong AS health_diastolic_bp, " +
                    "hr.nhip_tim AS health_heart_rate, " +
                    "hr.nhiet_do_c AS health_temperature_c, " +
                    "hr.nhip_tho AS health_respiratory_rate, " +
                    "hr.can_nang_kg AS health_weight_kg, " +
                    "hr.bmi AS health_bmi, " +
                    "hr.hba1c_percent AS health_hba1c, " +
                    "hr.cholesterol_mmol AS health_cholesterol, " +
                    "hr.triglyceride_mmol AS health_triglyceride, " +
                    "hr.carbs_g AS health_carbs_g, " +
                    "hr.lieu_luong_insulin_ui AS health_insulin_units, " +
                    "hr.loai_insulin_tiem AS health_insulin_type, " +
                    "hr.ghi_chu AS health_note " +
                    "FROM health_records hr " +
                    "WHERE hr.patient_id = ? " +
                    "ORDER BY hr.thoi_gian_do DESC " +
                    "LIMIT 1";

    private static final String SQL_ENCOUNTERS =
            "SELECT me.id AS encounter_id, " +
                    "me.encounter_code AS encounter_code, " +
                    "me.patient_id AS encounter_patient_id, " +
                    "me.bac_si_id AS encounter_doctor_id, " +
                    "me.ngay_kham AS encounter_visit_date, " +
                    "me.ngay_tao AS encounter_created_at, " +
                    "me.ly_do_kham AS encounter_chief_complaint, " +
                    "me.qua_trinh_benh_ly AS encounter_illness_history, " +
                    "me.kham_lam_sang AS encounter_clinical_exam, " +
                    "me.chan_doan_chinh AS encounter_primary_diagnosis, " +
                    "me.chan_doan_phu AS encounter_secondary_diagnosis, " +
                    "me.huong_xu_tri AS encounter_treatment_direction, " +
                    "bs.ho_ten AS encounter_doctor_name " +
                    "FROM medical_encounters me " +
                    "LEFT JOIN users bs ON me.bac_si_id = bs.id " +
                    "WHERE me.patient_id = ? " +
                    "ORDER BY me.ngay_kham DESC";

    private static final String SQL_LAB_RESULTS =
            "SELECT lr.id AS lab_result_id, " +
                    "lr.patient_id AS lab_patient_id, " +
                    "lr.encounter_id AS lab_encounter_id, " +
                    "lr.ngay_xet_nghiem AS lab_test_date, " +
                    "lr.glucose_mau AS lab_glucose_mmol, " +
                    "lr.hba1c AS lab_hba1c, " +
                    "lr.cholesterol_tp AS lab_cholesterol, " +
                    "lr.triglyceride AS lab_triglyceride, " +
                    "lr.hdl_c AS lab_hdl, " +
                    "lr.ldl_c AS lab_ldl, " +
                    "lr.ast AS lab_ast, " +
                    "lr.alt AS lab_alt, " +
                    "lr.ure AS lab_ure, " +
                    "lr.creatinine AS lab_creatinine, " +
                    "lr.wbc AS lab_wbc, " +
                    "lr.rbc AS lab_rbc, " +
                    "lr.hgb AS lab_hgb, " +
                    "lr.hct AS lab_hct, " +
                    "lr.plt AS lab_plt, " +
                    "lr.ghi_chu AS lab_note " +
                    "FROM lab_results lr " +
                    "WHERE lr.patient_id = ? " +
                    "ORDER BY lr.ngay_xet_nghiem DESC";

    private static final String SQL_LAB_RESULTS_LATEST = SQL_LAB_RESULTS + " LIMIT 1";

    private static final String SQL_PRESCRIPTIONS =
            "SELECT rx.id AS prescription_id, " +
                    "rx.patient_id AS rx_patient_id, " +
                    "rx.encounter_id AS rx_encounter_id, " +
                    "rx.bac_si_id AS rx_doctor_id, " +
                    "rx.ngay_ke_don AS rx_prescribed_at, " +
                    "rx.chan_doan AS rx_diagnosis, " +
                    "rx.huong_dieu_tri AS rx_treatment_plan, " +
                    "rx.che_do_an AS rx_diet_advice, " +
                    "rx.luyen_tap AS rx_exercise_advice, " +
                    "rx.ghi_chu AS rx_note " +
                    "FROM prescriptions rx " +
                    "WHERE rx.patient_id = ? " +
                    "ORDER BY rx.ngay_ke_don DESC";

    private static final String SQL_PRESCRIPTIONS_LATEST = SQL_PRESCRIPTIONS + " LIMIT 1";

    private static final String SQL_MEDICATIONS =
            "SELECT m.id AS medication_id, " +
                    "m.prescription_id AS medication_prescription_id, " +
                    "m.ten_thuoc AS medication_drug_name, " +
                    "m.hoat_chat AS medication_active_ingredient, " +
                    "m.lieu_luong AS medication_dosage, " +
                    "m.don_vi AS medication_unit, " +
                    "m.tan_suat AS medication_frequency, " +
                    "m.thoi_diem_uong AS medication_intake_time, " +
                    "m.thoi_gian_dung_ngay AS medication_duration_days, " +
                    "m.ghi_chu AS medication_note " +
                    "FROM medications m " +
                    "JOIN prescriptions rx ON m.prescription_id = rx.id " +
                    "WHERE rx.patient_id = ? " +
                    "ORDER BY m.ten_thuoc ASC";

    public EndocrinologyProfile getProfile(String patientId) {
        EndocrinologyProfile profile = new EndocrinologyProfile(patientId);
        if (patientId == null || patientId.isBlank()) {
            return profile;
        }

        try {
            PatientProfileDto patient = loadPatient(patientId);
            if (patient != null) {
                profile.setPatient(patient);
            }

            HealthRecordBaselineDto baseline = loadBaselineHealthRecord(patientId);
            if (baseline != null) {
                profile.setBaselineHealthRecord(baseline);
            }

            List<EncounterProfileDto> encounters = loadEncounters(patientId);
            Map<String, LabResultProfileDto> labsByEncounter = loadLabsGroupedByEncounter(patientId);
            Map<String, PrescriptionProfileDto> prescriptionsByEncounter =
                    loadPrescriptionsGroupedByEncounter(patientId);

            for (EncounterProfileDto encounter : encounters) {
                enrichEncounter(encounter, labsByEncounter, prescriptionsByEncounter);
            }
            profile.setEncounters(encounters);

            LabResultProfileDto latestLab = resolveLatestLab(patientId, labsByEncounter);
            if (latestLab != null) {
                profile.setLatestLabResult(latestLab);
            }

            PrescriptionProfileDto latestRx = resolveLatestPrescription(patientId, prescriptionsByEncounter);
            if (latestRx != null) {
                profile.setLatestPrescription(latestRx);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to build endocrinology profile for patientId=" + patientId, e);
        }

        return profile;
    }

    private PatientProfileDto loadPatient(String patientId) throws SQLException {
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_PATIENT)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPatientInfo(rs);
            }
        }
        return null;
    }

    private HealthRecordBaselineDto loadBaselineHealthRecord(String patientId) throws SQLException {
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_BASELINE_HEALTH_RECORD)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapHealthRecord(rs);
            }
        }
        return null;
    }

    private List<EncounterProfileDto> loadEncounters(String patientId) throws SQLException {
        List<EncounterProfileDto> list = new ArrayList<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_ENCOUNTERS)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEncounter(rs));
            }
        }
        return list;
    }

    private Map<String, LabResultProfileDto> loadLabsGroupedByEncounter(String patientId) throws SQLException {
        Map<String, LabResultProfileDto> grouped = new LinkedHashMap<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_LAB_RESULTS)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LabResultProfileDto lab = mapLabResult(rs);
                String encounterId = lab.getEncounterId();
                if (encounterId != null && !encounterId.isBlank()) {
                    grouped.putIfAbsent(encounterId, lab);
                }
            }
        }
        return grouped;
    }

    private Map<String, PrescriptionProfileDto> loadPrescriptionsGroupedByEncounter(String patientId)
            throws SQLException {
        Map<String, PrescriptionProfileDto> grouped = new LinkedHashMap<>();
        Map<String, List<MedicationProfileDto>> medsByPrescription = loadMedicationsGrouped(patientId);

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_PRESCRIPTIONS)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PrescriptionProfileDto rx = mapPrescription(rs);
                attachMedicationsIfPresent(rx, medsByPrescription.get(rx.getPrescriptionId()));
                String encounterId = rx.getEncounterId();
                if (encounterId != null && !encounterId.isBlank()) {
                    grouped.putIfAbsent(encounterId, rx);
                }
            }
        }
        return grouped;
    }

    private Map<String, List<MedicationProfileDto>> loadMedicationsGrouped(String patientId) throws SQLException {
        Map<String, List<MedicationProfileDto>> grouped = new HashMap<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_MEDICATIONS)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MedicationProfileDto med = mapMedication(rs);
                grouped.computeIfAbsent(med.getPrescriptionId(), k -> new ArrayList<>()).add(med);
            }
        }
        return grouped;
    }

    private void enrichEncounter(
            EncounterProfileDto encounter,
            Map<String, LabResultProfileDto> labsByEncounter,
            Map<String, PrescriptionProfileDto> prescriptionsByEncounter
    ) {
        if (encounter == null || encounter.getEncounterId() == null) {
            return;
        }
        String encounterId = encounter.getEncounterId();

        LabResultProfileDto lab = labsByEncounter.get(encounterId);
        if (lab != null && encounter.getLabResult() == null) {
            encounter.setLabResult(lab);
        }

        PrescriptionProfileDto rx = prescriptionsByEncounter.get(encounterId);
        if (rx != null && encounter.getPrescription() == null) {
            encounter.setPrescription(rx);
        }
    }

    private LabResultProfileDto resolveLatestLab(
            String patientId,
            Map<String, LabResultProfileDto> labsByEncounter
    ) throws SQLException {
        if (!labsByEncounter.isEmpty()) {
            return labsByEncounter.values().iterator().next();
        }
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_LAB_RESULTS_LATEST)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapLabResult(rs);
            }
        }
        return null;
    }

    private PrescriptionProfileDto resolveLatestPrescription(
            String patientId,
            Map<String, PrescriptionProfileDto> prescriptionsByEncounter
    ) throws SQLException {
        if (!prescriptionsByEncounter.isEmpty()) {
            return prescriptionsByEncounter.values().iterator().next();
        }
        Map<String, List<MedicationProfileDto>> medsByPrescription = loadMedicationsGrouped(patientId);
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_PRESCRIPTIONS_LATEST)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PrescriptionProfileDto rx = mapPrescription(rs);
                attachMedicationsIfPresent(rx, medsByPrescription.get(rx.getPrescriptionId()));
                return rx;
            }
        }
        return null;
    }

    private void attachMedicationsIfPresent(
            PrescriptionProfileDto rx,
            List<MedicationProfileDto> medications
    ) {
        if (rx == null || medications == null || medications.isEmpty()) {
            return;
        }
        if (rx.getMedications() == null || rx.getMedications().isEmpty()) {
            rx.setMedications(new ArrayList<>(medications));
        }
    }

    private PatientProfileDto mapPatientInfo(ResultSet rs) throws SQLException {
        PatientProfileDto dto = new PatientProfileDto();
        applyString(rs, "patient_id", dto::setPatientId);
        String code = PatientDAO.resolveCode(rs, "patient_code");
        if (code != null && !code.isBlank()) {
            dto.setPatientCode(code);
        }
        applyString(rs, "user_id", dto::setUserId);
        applyString(rs, "user_full_name", dto::setFullName);
        applyString(rs, "user_email", dto::setEmail);
        applyString(rs, "user_phone", dto::setPhone);
        applyInteger(rs, "patient_age", dto::setAge);
        Date birthDate = rs.getDate("patient_birth_date");
        if (birthDate != null) {
            dto.setDateOfBirth(birthDate.toLocalDate());
        }
        applyString(rs, "patient_gender", dto::setGender);
        applyDouble(rs, "patient_height_cm", dto::setHeightCm);
        applyString(rs, "patient_address", dto::setAddress);
        applyString(rs, "patient_insurance", dto::setHealthInsurance);
        applyString(rs, "patient_medical_history", dto::setMedicalHistory);
        applyString(rs, "patient_allergies", dto::setAllergies);
        applyString(rs, "patient_blood_type", dto::setBloodType);
        applyString(rs, "patient_diabetes_type", dto::setDiabetesType);
        Date diagnosisDate = rs.getDate("patient_diabetes_diagnosis_date");
        if (diagnosisDate != null) {
            dto.setDiabetesDiagnosisDate(diagnosisDate.toLocalDate());
        }
        applyString(rs, "assigned_doctor_id", dto::setAssignedDoctorId);
        return dto;
    }

    private HealthRecordBaselineDto mapHealthRecord(ResultSet rs) throws SQLException {
        HealthRecordBaselineDto dto = new HealthRecordBaselineDto();
        applyString(rs, "health_record_id", dto::setHealthRecordId);
        applyString(rs, "health_patient_id", dto::setPatientId);
        applyTimestamp(rs, "health_measured_at", dto::setMeasuredAt);
        applyTimestamp(rs, "health_updated_at", dto::setUpdatedAt);
        applyDouble(rs, "health_glucose_mgdl", dto::setGlucoseMgdl);
        applyString(rs, "health_glucose_timing", dto::setGlucoseTiming);
        applyInteger(rs, "health_systolic_bp", dto::setSystolicBp);
        applyInteger(rs, "health_diastolic_bp", dto::setDiastolicBp);
        applyInteger(rs, "health_heart_rate", dto::setHeartRate);
        applyDouble(rs, "health_temperature_c", dto::setTemperatureC);
        applyInteger(rs, "health_respiratory_rate", dto::setRespiratoryRate);
        applyDouble(rs, "health_weight_kg", dto::setWeightKg);
        applyDouble(rs, "health_bmi", dto::setBmi);
        applyDouble(rs, "health_hba1c", dto::setHba1cPercent);
        applyDouble(rs, "health_cholesterol", dto::setCholesterolMmol);
        applyDouble(rs, "health_triglyceride", dto::setTriglycerideMmol);
        applyInteger(rs, "health_insulin_units", dto::setInsulinUnits);
        applyString(rs, "health_insulin_type", dto::setInsulinType);
        return dto;
    }

    private EncounterProfileDto mapEncounter(ResultSet rs) throws SQLException {
        EncounterProfileDto dto = new EncounterProfileDto();
        applyString(rs, "encounter_id", dto::setEncounterId);
        applyString(rs, "encounter_code", dto::setEncounterCode);
        applyString(rs, "encounter_patient_id", dto::setPatientId);
        applyString(rs, "encounter_doctor_id", dto::setDoctorId);
        applyString(rs, "encounter_doctor_name", dto::setDoctorName);
        applyTimestamp(rs, "encounter_visit_date", dto::setVisitDate);
        applyTimestamp(rs, "encounter_created_at", dto::setCreatedAt);
        String clinicalJson = readString(rs, "encounter_clinical_exam");
        String encounterType = EncounterClinicalJson.parseString(clinicalJson, "loai_encounter");
        String resolved = EncounterType.resolveTypeCode(
                null, encounterType,
                readString(rs, "encounter_primary_diagnosis"),
                readString(rs, "encounter_chief_complaint"));
        dto.setEncounterType(resolved);
        dto.setStatus("da_kham");
        applyString(rs, "encounter_chief_complaint", dto::setChiefComplaint);
        applyString(rs, "encounter_illness_history", dto::setIllnessHistory);
        applyString(rs, "encounter_clinical_exam", dto::setClinicalExam);
        applyString(rs, "encounter_primary_diagnosis", dto::setPrimaryDiagnosis);
        applyString(rs, "encounter_secondary_diagnosis", dto::setSecondaryDiagnosis);
        applyString(rs, "encounter_treatment_direction", dto::setTreatmentDirection);
        return dto;
    }

    private LabResultProfileDto mapLabResult(ResultSet rs) throws SQLException {
        LabResultProfileDto dto = new LabResultProfileDto();
        applyString(rs, "lab_result_id", dto::setLabResultId);
        applyString(rs, "lab_patient_id", dto::setPatientId);
        applyString(rs, "lab_encounter_id", dto::setEncounterId);
        applyTimestamp(rs, "lab_test_date", dto::setTestDate);
        applyDouble(rs, "lab_glucose_mmol", dto::setGlucoseMmol);
        applyDouble(rs, "lab_hba1c", dto::setHba1c);
        applyDouble(rs, "lab_cholesterol", dto::setCholesterol);
        applyDouble(rs, "lab_triglyceride", dto::setTriglyceride);
        applyDouble(rs, "lab_hdl", dto::setHdl);
        applyDouble(rs, "lab_ldl", dto::setLdl);
        applyDouble(rs, "lab_ast", dto::setAst);
        applyDouble(rs, "lab_alt", dto::setAlt);
        applyDouble(rs, "lab_ure", dto::setUre);
        applyDouble(rs, "lab_creatinine", dto::setCreatinine);
        applyDouble(rs, "lab_wbc", dto::setWbc);
        applyDouble(rs, "lab_rbc", dto::setRbc);
        applyDouble(rs, "lab_hgb", dto::setHgb);
        applyDouble(rs, "lab_hct", dto::setHct);
        applyDouble(rs, "lab_plt", dto::setPlt);
        applyString(rs, "lab_note", dto::setNote);
        return dto;
    }

    private PrescriptionProfileDto mapPrescription(ResultSet rs) throws SQLException {
        PrescriptionProfileDto dto = new PrescriptionProfileDto();
        applyString(rs, "prescription_id", dto::setPrescriptionId);
        applyString(rs, "rx_patient_id", dto::setPatientId);
        applyString(rs, "rx_encounter_id", dto::setEncounterId);
        applyString(rs, "rx_doctor_id", dto::setDoctorId);
        applyTimestamp(rs, "rx_prescribed_at", dto::setPrescribedAt);
        applyString(rs, "rx_diagnosis", dto::setDiagnosis);
        applyString(rs, "rx_treatment_plan", dto::setTreatmentPlan);
        applyString(rs, "rx_diet_advice", dto::setDietAdvice);
        applyString(rs, "rx_exercise_advice", dto::setExerciseAdvice);
        applyString(rs, "rx_note", dto::setNote);
        return dto;
    }

    private MedicationProfileDto mapMedication(ResultSet rs) throws SQLException {
        MedicationProfileDto dto = new MedicationProfileDto();
        applyString(rs, "medication_id", dto::setMedicationId);
        applyString(rs, "medication_prescription_id", dto::setPrescriptionId);
        applyString(rs, "medication_drug_name", dto::setDrugName);
        applyString(rs, "medication_active_ingredient", dto::setActiveIngredient);
        applyString(rs, "medication_dosage", dto::setDosage);
        applyString(rs, "medication_unit", dto::setUnit);
        applyString(rs, "medication_frequency", dto::setFrequency);
        applyString(rs, "medication_intake_time", dto::setIntakeTime);
        applyInteger(rs, "medication_duration_days", dto::setDurationDays);
        applyString(rs, "medication_note", dto::setNote);
        return dto;
    }

    private static void applyString(ResultSet rs, String alias, Consumer<String> setter) throws SQLException {
        String value = readString(rs, alias);
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private static void applyDouble(ResultSet rs, String alias, Consumer<Double> setter) throws SQLException {
        Double value = readDouble(rs, alias);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void applyInteger(ResultSet rs, String alias, Consumer<Integer> setter) throws SQLException {
        Integer value = readInteger(rs, alias);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void applyTimestamp(
            ResultSet rs,
            String alias,
            Consumer<LocalDateTime> setter
    ) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(alias);
        if (timestamp != null) {
            setter.accept(timestamp.toLocalDateTime());
        }
    }

    private static String readString(ResultSet rs, String alias) throws SQLException {
        String value = rs.getString(alias);
        return rs.wasNull() ? null : value;
    }

    private static Double readDouble(ResultSet rs, String alias) throws SQLException {
        double value = rs.getDouble(alias);
        return rs.wasNull() ? null : value;
    }

    private static Integer readInteger(ResultSet rs, String alias) throws SQLException {
        int value = rs.getInt(alias);
        return rs.wasNull() ? null : value;
    }
}
