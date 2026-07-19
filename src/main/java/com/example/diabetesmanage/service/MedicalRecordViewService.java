package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.dto.MedicalEncounterDTO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordViewService {

    private static final double UMOL_TO_MGDL = 1.0 / 88.4;

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final UserDAO userDAO = UserDAO.getInstance();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MedicalEncounterDTO loadDetailViewByEncounterId(String encounterId, String scopeDoctorId) {
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            encounter = encounterDAO.getEncounterById(encounterId, null);
        }
        if (encounter == null) {
            return null;
        }
        return buildDetailViewFromEncounter(encounter, scopeDoctorId);
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
