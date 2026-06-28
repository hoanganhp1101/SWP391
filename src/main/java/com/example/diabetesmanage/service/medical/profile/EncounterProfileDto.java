package com.example.diabetesmanage.service.medical.profile;

import java.time.LocalDateTime;

public class EncounterProfileDto {

    private String encounterId;
    private String encounterCode;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private LocalDateTime visitDate;
    private LocalDateTime createdAt;
    private String encounterType;
    private String status;
    private String chiefComplaint;
    private String illnessHistory;
    private String clinicalExam;
    private String primaryDiagnosis;
    private String secondaryDiagnosis;
    private String treatmentDirection;
    private LabResultProfileDto labResult;
    private PrescriptionProfileDto prescription;

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getEncounterCode() {
        return encounterCode;
    }

    public void setEncounterCode(String encounterCode) {
        this.encounterCode = encounterCode;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getIllnessHistory() {
        return illnessHistory;
    }

    public void setIllnessHistory(String illnessHistory) {
        this.illnessHistory = illnessHistory;
    }

    public String getClinicalExam() {
        return clinicalExam;
    }

    public void setClinicalExam(String clinicalExam) {
        this.clinicalExam = clinicalExam;
    }

    public String getPrimaryDiagnosis() {
        return primaryDiagnosis;
    }

    public void setPrimaryDiagnosis(String primaryDiagnosis) {
        this.primaryDiagnosis = primaryDiagnosis;
    }

    public String getSecondaryDiagnosis() {
        return secondaryDiagnosis;
    }

    public void setSecondaryDiagnosis(String secondaryDiagnosis) {
        this.secondaryDiagnosis = secondaryDiagnosis;
    }

    public String getTreatmentDirection() {
        return treatmentDirection;
    }

    public void setTreatmentDirection(String treatmentDirection) {
        this.treatmentDirection = treatmentDirection;
    }

    public LabResultProfileDto getLabResult() {
        return labResult;
    }

    public void setLabResult(LabResultProfileDto labResult) {
        this.labResult = labResult;
    }

    public PrescriptionProfileDto getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionProfileDto prescription) {
        this.prescription = prescription;
    }
}
