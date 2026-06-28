package com.example.diabetesmanage.service.medical.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified endocrinology medical record profile for one patient.
 */
public class EndocrinologyProfile {

    private String patientId;
    private PatientProfileDto patient;
    private HealthRecordBaselineDto baselineHealthRecord;
    private List<EncounterProfileDto> encounters = new ArrayList<>();
    private LabResultProfileDto latestLabResult;
    private PrescriptionProfileDto latestPrescription;

    public EndocrinologyProfile() {
    }

    public EndocrinologyProfile(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public PatientProfileDto getPatient() {
        return patient;
    }

    public void setPatient(PatientProfileDto patient) {
        this.patient = patient;
    }

    public HealthRecordBaselineDto getBaselineHealthRecord() {
        return baselineHealthRecord;
    }

    public void setBaselineHealthRecord(HealthRecordBaselineDto baselineHealthRecord) {
        this.baselineHealthRecord = baselineHealthRecord;
    }

    public List<EncounterProfileDto> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<EncounterProfileDto> encounters) {
        this.encounters = encounters != null ? encounters : new ArrayList<>();
    }

    public LabResultProfileDto getLatestLabResult() {
        return latestLabResult;
    }

    public void setLatestLabResult(LabResultProfileDto latestLabResult) {
        this.latestLabResult = latestLabResult;
    }

    public PrescriptionProfileDto getLatestPrescription() {
        return latestPrescription;
    }

    public void setLatestPrescription(PrescriptionProfileDto latestPrescription) {
        this.latestPrescription = latestPrescription;
    }
}
