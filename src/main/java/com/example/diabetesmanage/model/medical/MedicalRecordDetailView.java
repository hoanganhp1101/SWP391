package com.example.diabetesmanage.model.medical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicalRecordDetailView {

    private String recordId;
    private String recordCode;
    private String patientName;
    private String patientCode;
    private String examDate;
    private String department = "Khoa Nội tiết";
    private String doctorName = "Bác sĩ phụ trách";

    private InternalMedicineSection internalMedicine = new InternalMedicineSection();
    private BloodCountSection bloodCount = new BloodCountSection();
    private BiochemistrySection biochemistry = new BiochemistrySection();

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

    public InternalMedicineSection getInternalMedicine() {
        return internalMedicine;
    }

    public void setInternalMedicine(InternalMedicineSection internalMedicine) {
        this.internalMedicine = internalMedicine;
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

    public static class InternalMedicineSection {

        private List<Map<String, Object>> clinicalInfo = new ArrayList<>();
        private List<Map<String, Object>> diagnosisInfo = new ArrayList<>();
        private List<Map<String, String>> medications = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();

        public List<Map<String, Object>> getClinicalInfo() {
            return clinicalInfo;
        }

        public void setClinicalInfo(List<Map<String, Object>> clinicalInfo) {
            this.clinicalInfo = clinicalInfo;
        }

        public List<Map<String, Object>> getDiagnosisInfo() {
            return diagnosisInfo;
        }

        public void setDiagnosisInfo(List<Map<String, Object>> diagnosisInfo) {
            this.diagnosisInfo = diagnosisInfo;
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
            return items.stream().anyMatch(item -> {
                Object val = item.get("value");
                return val != null && !val.toString().isBlank() && !"—".equals(val.toString());
            });
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
            this.alerts = alerts;
        }
    }
}
