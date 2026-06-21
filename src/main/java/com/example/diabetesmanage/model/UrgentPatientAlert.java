package com.example.diabetesmanage.model;

public class UrgentPatientAlert {

    private String patientId;
    private String patientCode;
    private String patientName;
    private String loaiTieuDuong;
    private Double duongHuyetGanNhat;
    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private String vitalDisplay;
    private String detectedAgo;
    private boolean critical;
    private java.util.List<String> riskReasons = new java.util.ArrayList<>();
    private String aiSummary;
    private int riskScore;

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

    public Double getDuongHuyetGanNhat() {
        return duongHuyetGanNhat;
    }

    public void setDuongHuyetGanNhat(Double duongHuyetGanNhat) {
        this.duongHuyetGanNhat = duongHuyetGanNhat;
    }

    public Integer getHuyetApTamThu() {
        return huyetApTamThu;
    }

    public void setHuyetApTamThu(Integer huyetApTamThu) {
        this.huyetApTamThu = huyetApTamThu;
    }

    public Integer getHuyetApTamTruong() {
        return huyetApTamTruong;
    }

    public void setHuyetApTamTruong(Integer huyetApTamTruong) {
        this.huyetApTamTruong = huyetApTamTruong;
    }

    public String getVitalDisplay() {
        return vitalDisplay;
    }

    public void setVitalDisplay(String vitalDisplay) {
        this.vitalDisplay = vitalDisplay;
    }

    public String getDetectedAgo() {
        return detectedAgo;
    }

    public void setDetectedAgo(String detectedAgo) {
        this.detectedAgo = detectedAgo;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public java.util.List<String> getRiskReasons() {
        return riskReasons;
    }

    public void setRiskReasons(java.util.List<String> riskReasons) {
        this.riskReasons = riskReasons;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }
}
