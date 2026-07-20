package com.example.diabetesmanage.dto;

import com.example.diabetesmanage.model.HealthRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HighRiskPatientDTO {

    private String patientId;
    private String patientCode;
    private String patientName;
    private String initials;
    private String loaiTieuDuong;
    private String riskLevel;
    private int riskScore;
    private boolean critical;
    private boolean needsUrgentReview;

    private Double duongHuyetGanNhat;
    private Double hba1cGanNhat;
    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Double bmiGanNhat;
    private Integer insulinGanNhat;
    private String timeAgo;

    private List<String> riskReasons = new ArrayList<>();
    private List<Map<String, Object>> metricTags = new ArrayList<>();
    private List<HealthRecord> recentRecords = new ArrayList<>();

    private String aiSummary;
    private String aiDetailAnalysis;
    private List<String> aiRecommendations = new ArrayList<>();
    private boolean geminiUsed;
    private String geminiError;

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

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getLoaiTieuDuong() {
        return loaiTieuDuong;
    }

    public void setLoaiTieuDuong(String loaiTieuDuong) {
        this.loaiTieuDuong = loaiTieuDuong;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
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
        return riskReasons != null && !riskReasons.isEmpty();
    }

    public boolean isNeedsUrgentReview() {
        return needsUrgentReview;
    }

    public void setNeedsUrgentReview(boolean needsUrgentReview) {
        this.needsUrgentReview = needsUrgentReview;
    }

    public Double getDuongHuyetGanNhat() {
        return duongHuyetGanNhat;
    }

    public void setDuongHuyetGanNhat(Double duongHuyetGanNhat) {
        this.duongHuyetGanNhat = duongHuyetGanNhat;
    }

    public Double getHba1cGanNhat() {
        return hba1cGanNhat;
    }

    public void setHba1cGanNhat(Double hba1cGanNhat) {
        this.hba1cGanNhat = hba1cGanNhat;
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

    public Double getBmiGanNhat() {
        return bmiGanNhat;
    }

    public void setBmiGanNhat(Double bmiGanNhat) {
        this.bmiGanNhat = bmiGanNhat;
    }

    public Integer getInsulinGanNhat() {
        return insulinGanNhat;
    }

    public void setInsulinGanNhat(Integer insulinGanNhat) {
        this.insulinGanNhat = insulinGanNhat;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public List<String> getRiskReasons() {
        return riskReasons;
    }

    public void setRiskReasons(List<String> riskReasons) {
        this.riskReasons = riskReasons;
    }

    public List<Map<String, Object>> getMetricTags() {
        return metricTags;
    }

    public void setMetricTags(List<Map<String, Object>> metricTags) {
        this.metricTags = metricTags;
    }

    public List<HealthRecord> getRecentRecords() {
        return recentRecords;
    }

    public void setRecentRecords(List<HealthRecord> recentRecords) {
        this.recentRecords = recentRecords;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getAiDetailAnalysis() {
        return aiDetailAnalysis;
    }

    public void setAiDetailAnalysis(String aiDetailAnalysis) {
        this.aiDetailAnalysis = aiDetailAnalysis;
    }

    public List<String> getAiRecommendations() {
        return aiRecommendations;
    }

    public void setAiRecommendations(List<String> aiRecommendations) {
        this.aiRecommendations = aiRecommendations;
    }

    public boolean isGeminiUsed() {
        return geminiUsed;
    }

    public void setGeminiUsed(boolean geminiUsed) {
        this.geminiUsed = geminiUsed;
    }

    public String getGeminiError() {
        return geminiError;
    }

    public void setGeminiError(String geminiError) {
        this.geminiError = geminiError;
    }
}
