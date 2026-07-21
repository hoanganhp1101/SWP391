package com.example.diabetesmanage.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO tổng hợp chỉ dùng để render Medical Record Detail/PDF.
 * Không map vào bảng và không được ghi xuống database.
 */
public class MedicalEncounterDTO {

    private String recordId;
    private String recordCode;
    private String patientName;
    private String patientCode;
    private String examDate;
    private String department = "Khoa Nội tiết";
    private String doctorName = "Bác sĩ phụ trách";
    private String encounterType;
    private String encounterTypeLabel;

    private InternalMedicineSection internalMedicine = new InternalMedicineSection();
    private PrescriptionDetailSection prescriptionDetail = new PrescriptionDetailSection();
    private BloodCountSection bloodCount = new BloodCountSection();
    private BiochemistrySection biochemistry = new BiochemistrySection();
    private UltrasoundSection ultrasound = new UltrasoundSection();

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getEncounterTypeLabel() {
        return encounterTypeLabel;
    }

    public void setEncounterTypeLabel(String encounterTypeLabel) {
        this.encounterTypeLabel = encounterTypeLabel;
    }

    /** Trả về mã loại hồ sơ chuẩn hóa: tai_kham_noi_tiet | mau_tong_quat | sinh_hoa_mau. */
    public String resolveEncounterType() {
        if ("mau_tong_quat".equalsIgnoreCase(encounterType)) {
            return "mau_tong_quat";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(encounterType)) {
            return "sinh_hoa_mau";
        }
        return "tai_kham_noi_tiet";
    }

    public InternalMedicineSection getInternalMedicine() {
        return internalMedicine;
    }

    public void setInternalMedicine(InternalMedicineSection internalMedicine) {
        this.internalMedicine = internalMedicine;
    }

    public PrescriptionDetailSection getPrescriptionDetail() {
        return prescriptionDetail;
    }

    public void setPrescriptionDetail(PrescriptionDetailSection prescriptionDetail) {
        this.prescriptionDetail = prescriptionDetail;
    }

    public BloodCountSection getBloodCount() {
        return bloodCount;
    }

    public void setBloodCount(BloodCountSection bloodCount) {
        this.bloodCount = bloodCount;
    }

    public BiochemistrySection getBiochemistry() {
        return biochemistry;
    }

    public void setBiochemistry(BiochemistrySection biochemistry) {
        this.biochemistry = biochemistry;
    }

    public UltrasoundSection getUltrasound() {
        return ultrasound;
    }

    public void setUltrasound(UltrasoundSection ultrasound) {
        this.ultrasound = ultrasound;
    }

    public static class InternalMedicineSection {

        private List<Map<String, Object>> clinicalInfo = new ArrayList<>();
        private List<Map<String, Object>> healthMetrics = new ArrayList<>();
        private List<Map<String, Object>> diagnosisInfo = new ArrayList<>();
        private List<Map<String, Object>> recommendationFields = new ArrayList<>();
        private List<Map<String, String>> medications = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();

        public List<Map<String, Object>> getClinicalInfo() {
            return clinicalInfo;
        }

        public void setClinicalInfo(List<Map<String, Object>> clinicalInfo) {
            this.clinicalInfo = clinicalInfo != null ? clinicalInfo : new ArrayList<>();
        }

        public List<Map<String, Object>> getHealthMetrics() {
            return healthMetrics;
        }

        public void setHealthMetrics(List<Map<String, Object>> healthMetrics) {
            this.healthMetrics = healthMetrics != null ? healthMetrics : new ArrayList<>();
        }

        public List<Map<String, Object>> getDiagnosisInfo() {
            return diagnosisInfo;
        }

        public void setDiagnosisInfo(List<Map<String, Object>> diagnosisInfo) {
            this.diagnosisInfo = diagnosisInfo != null ? diagnosisInfo : new ArrayList<>();
        }

        public List<Map<String, Object>> getRecommendationFields() {
            return recommendationFields;
        }

        public void setRecommendationFields(List<Map<String, Object>> recommendationFields) {
            this.recommendationFields = recommendationFields != null ? recommendationFields : new ArrayList<>();
        }

        public List<Map<String, String>> getMedications() {
            return medications;
        }

        public void setMedications(List<Map<String, String>> medications) {
            this.medications = medications;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        public boolean hasData() {
            return !clinicalInfo.isEmpty()
                    || !healthMetrics.isEmpty()
                    || !diagnosisInfo.isEmpty()
                    || !recommendationFields.isEmpty()
                    || (recommendations != null && !recommendations.isEmpty());
        }
    }

    public static class PrescriptionDetailSection {

        private List<Map<String, String>> items = new ArrayList<>();

        public List<Map<String, String>> getItems() {
            return items;
        }

        public void setItems(List<Map<String, String>> items) {
            this.items = items;
        }

        public boolean hasData() {
            return items != null && !items.isEmpty();
        }
    }

    public static class BloodCountSection {

        private List<Map<String, Object>> items = new ArrayList<>();

        public List<Map<String, Object>> getItems() {
            return items;
        }

        public void setItems(List<Map<String, Object>> items) {
            this.items = items;
        }

        public boolean hasData() {
            if (items == null || items.isEmpty()) {
                return false;
            }
            return items.stream().anyMatch(MedicalEncounterDTO::hasLabFieldValue);
        }
    }

    public static class BiochemistrySection {

        private Map<String, Object> glucose;
        private Map<String, Object> hba1c;
        private List<Map<String, Object>> lipidProfile = new ArrayList<>();
        private List<Map<String, Object>> liverEnzymes = new ArrayList<>();
        private List<Map<String, Object>> kidneyFunction = new ArrayList<>();
        private List<String> alerts = new ArrayList<>();

        public Map<String, Object> getGlucose() {
            return glucose;
        }

        public void setGlucose(Map<String, Object> glucose) {
            this.glucose = glucose;
        }

        public Map<String, Object> getHba1c() {
            return hba1c;
        }

        public void setHba1c(Map<String, Object> hba1c) {
            this.hba1c = hba1c;
        }

        public List<Map<String, Object>> getLipidProfile() {
            return lipidProfile;
        }

        public void setLipidProfile(List<Map<String, Object>> lipidProfile) {
            this.lipidProfile = lipidProfile;
        }

        public List<Map<String, Object>> getLiverEnzymes() {
            return liverEnzymes;
        }

        public void setLiverEnzymes(List<Map<String, Object>> liverEnzymes) {
            this.liverEnzymes = liverEnzymes;
        }

        public List<Map<String, Object>> getKidneyFunction() {
            return kidneyFunction;
        }

        public void setKidneyFunction(List<Map<String, Object>> kidneyFunction) {
            this.kidneyFunction = kidneyFunction;
        }

        public List<String> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<String> alerts) {
            this.alerts = alerts != null ? alerts : new ArrayList<>();
        }

        public boolean hasData() {
            return hasLabFieldValue(glucose)
                    || hasLabFieldValue(hba1c)
                    || hasFieldListData(lipidProfile)
                    || hasFieldListData(liverEnzymes)
                    || hasFieldListData(kidneyFunction);
        }
    }

    public static class UltrasoundSection {

        private List<Map<String, String>> fields = new ArrayList<>();

        public List<Map<String, String>> getFields() {
            return fields;
        }

        public void setFields(List<Map<String, String>> fields) {
            this.fields = fields;
        }

        public boolean hasData() {
            return fields != null && !fields.isEmpty();
        }
    }

    private static boolean hasFieldListData(List<Map<String, Object>> fields) {
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        return fields.stream().anyMatch(MedicalEncounterDTO::hasLabFieldValue);
    }

    private static boolean hasLabFieldValue(Map<String, Object> field) {
        if (field == null || field.isEmpty()) {
            return false;
        }
        Object display = field.get("displayValue");
        if (display != null && !display.toString().isBlank() && !"—".equals(display.toString())) {
            return true;
        }
        Object value = field.get("value");
        return value != null && !value.toString().isBlank() && !"—".equals(value.toString());
    }
}
