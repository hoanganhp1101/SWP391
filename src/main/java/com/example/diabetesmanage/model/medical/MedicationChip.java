package com.example.diabetesmanage.model.medical;

public class MedicationChip {

    private String name;
    private String dose;
    private String note;

    public MedicationChip() {
    }

    public MedicationChip(String name, String dose) {
        this.name = name;
        this.dose = dose;
    }

    public MedicationChip(String name, String dose, String note) {
        this.name = name;
        this.dose = dose;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
