package com.example.diabetesmanage.model.medical;

import java.util.ArrayList;
import java.util.List;

public class InternalMedicineSection {

    private List<MedicalFieldItem> clinicalInfo = new ArrayList<>();
    private List<MedicalFieldItem> diagnosisInfo = new ArrayList<>();
    private List<MedicationChip> medications = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    public List<MedicalFieldItem> getClinicalInfo() {
        return clinicalInfo;
    }

    public void setClinicalInfo(List<MedicalFieldItem> clinicalInfo) {
        this.clinicalInfo = clinicalInfo;
    }

    public List<MedicalFieldItem> getDiagnosisInfo() {
        return diagnosisInfo;
    }

    public void setDiagnosisInfo(List<MedicalFieldItem> diagnosisInfo) {
        this.diagnosisInfo = diagnosisInfo;
    }

    public List<MedicationChip> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationChip> medications) {
        this.medications = medications;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
