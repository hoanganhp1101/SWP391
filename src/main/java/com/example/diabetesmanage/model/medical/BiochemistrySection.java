package com.example.diabetesmanage.model.medical;

import java.util.ArrayList;
import java.util.List;

public class BiochemistrySection {

    private MedicalFieldItem glucose;
    private MedicalFieldItem hba1c;
    private List<MedicalFieldItem> lipidProfile = new ArrayList<>();
    private List<MedicalFieldItem> liverEnzymes = new ArrayList<>();
    private List<MedicalFieldItem> kidneyFunction = new ArrayList<>();
    private List<String> alerts = new ArrayList<>();

    public MedicalFieldItem getGlucose() {
        return glucose;
    }

    public void setGlucose(MedicalFieldItem glucose) {
        this.glucose = glucose;
    }

    public MedicalFieldItem getHba1c() {
        return hba1c;
    }

    public void setHba1c(MedicalFieldItem hba1c) {
        this.hba1c = hba1c;
    }

    public List<MedicalFieldItem> getLipidProfile() {
        return lipidProfile;
    }

    public void setLipidProfile(List<MedicalFieldItem> lipidProfile) {
        this.lipidProfile = lipidProfile;
    }

    public List<MedicalFieldItem> getLiverEnzymes() {
        return liverEnzymes;
    }

    public void setLiverEnzymes(List<MedicalFieldItem> liverEnzymes) {
        this.liverEnzymes = liverEnzymes;
    }

    public List<MedicalFieldItem> getKidneyFunction() {
        return kidneyFunction;
    }

    public void setKidneyFunction(List<MedicalFieldItem> kidneyFunction) {
        this.kidneyFunction = kidneyFunction;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }
}
