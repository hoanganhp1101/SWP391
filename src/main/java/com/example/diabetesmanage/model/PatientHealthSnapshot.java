package com.example.diabetesmanage.model;

import java.util.ArrayList;
import java.util.List;

public class PatientHealthSnapshot {

    private String patientId;
    private String patientCode;
    private String patientName;
    private String loaiTieuDuong;
    private List<HealthRecord> recentRecords = new ArrayList<>();
    private List<String> riskReasons = new ArrayList<>();
    private int riskScore;
    private boolean critical;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getLoaiTieuDuong() {
        return loaiTieuDuong;
    }

    public void setLoaiTieuDuong(String loaiTieuDuong) {
        this.loaiTieuDuong = loaiTieuDuong;
    }

    public List<HealthRecord> getRecentRecords() {
        return recentRecords;
    }

    public void setRecentRecords(List<HealthRecord> recentRecords) {
        this.recentRecords = recentRecords;
    }

    public List<String> getRiskReasons() {
        return riskReasons;
    }

    public void setRiskReasons(List<String> riskReasons) {
        this.riskReasons = riskReasons;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isDangerous() {
        return !riskReasons.isEmpty();
    }
}
