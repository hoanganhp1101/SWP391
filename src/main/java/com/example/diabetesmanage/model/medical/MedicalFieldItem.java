package com.example.diabetesmanage.model.medical;

public class MedicalFieldItem {

    public enum HighlightLevel {
        NORMAL, WARNING, CRITICAL, CORE
    }

    private String label;
    private String value;
    private String unit;
    private String referenceRange;
    private boolean abnormal;
    private HighlightLevel highlightLevel = HighlightLevel.NORMAL;

    public MedicalFieldItem() {
    }

    public MedicalFieldItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public MedicalFieldItem(String label, String value, String unit) {
        this.label = label;
        this.value = value;
        this.unit = unit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getReferenceRange() {
        return referenceRange;
    }

    public void setReferenceRange(String referenceRange) {
        this.referenceRange = referenceRange;
    }

    public boolean isAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
    }

    public HighlightLevel getHighlightLevel() {
        return highlightLevel;
    }

    public void setHighlightLevel(HighlightLevel highlightLevel) {
        this.highlightLevel = highlightLevel;
    }

    public String getDisplayValue() {
        if (value == null || value.isBlank()) {
            return "—";
        }
        if (unit == null || unit.isBlank()) {
            return value;
        }
        return value + " " + unit;
    }
}
