package com.example.diabetesmanage.model;

public class PatientMetricTag {

    private String label;
    private String value;
    private String type;
    private boolean trending;

    public PatientMetricTag() {
    }

    public PatientMetricTag(String label, String value, String type, boolean trending) {
        this.label = label;
        this.value = value;
        this.type = type;
        this.trending = trending;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTrending() {
        return trending;
    }

    public void setTrending(boolean trending) {
        this.trending = trending;
    }
}
