package com.example.diabetesmanage.service.medical.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionProfileDto {

    private String prescriptionId;
    private String patientId;
    private String encounterId;
    private String doctorId;
    private LocalDateTime prescribedAt;
    private String diagnosis;
    private String treatmentPlan;
    private String dietAdvice;
    private String exerciseAdvice;
    private String note;
    private List<MedicationProfileDto> medications = new ArrayList<>();

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getPrescribedAt() {
        return prescribedAt;
    }

    public void setPrescribedAt(LocalDateTime prescribedAt) {
        this.prescribedAt = prescribedAt;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getDietAdvice() {
        return dietAdvice;
    }

    public void setDietAdvice(String dietAdvice) {
        this.dietAdvice = dietAdvice;
    }

    public String getExerciseAdvice() {
        return exerciseAdvice;
    }

    public void setExerciseAdvice(String exerciseAdvice) {
        this.exerciseAdvice = exerciseAdvice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<MedicationProfileDto> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationProfileDto> medications) {
        this.medications = medications != null ? medications : new ArrayList<>();
    }

    public void addMedication(MedicationProfileDto medication) {
        if (medication != null) {
            medications.add(medication);
        }
    }
}
