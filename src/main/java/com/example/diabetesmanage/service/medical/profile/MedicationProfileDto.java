package com.example.diabetesmanage.service.medical.profile;

public class MedicationProfileDto {

    private String medicationId;
    private String prescriptionId;
    private String drugName;
    private String activeIngredient;
    private String dosage;
    private String unit;
    private String frequency;
    private String intakeTime;
    private Integer durationDays;
    private String note;

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getIntakeTime() {
        return intakeTime;
    }

    public void setIntakeTime(String intakeTime) {
        this.intakeTime = intakeTime;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
