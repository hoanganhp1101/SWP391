package com.example.diabetesmanage.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HighRiskPatient {
    private String patientId;
    private String patientName;
    private String email;
    private String phone;
    private String diabetesType;
    private String doctorName;
    private Double latestGlucose;
    private Double latestHba1c;
    private Integer systolicBloodPressure;
    private Integer diastolicBloodPressure;
    private Double bmi;
    private Timestamp lastMeasurementTime;
    private int recentAlertCount;
    private int unreadDoctorAlertCount;
    private int riskScore;
    private String riskLevel;
    private final List<String> riskReasons = new ArrayList<>();

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDiabetesType() { return diabetesType; }
    public void setDiabetesType(String diabetesType) { this.diabetesType = diabetesType; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Double getLatestGlucose() { return latestGlucose; }
    public void setLatestGlucose(Double latestGlucose) { this.latestGlucose = latestGlucose; }
    public Double getLatestHba1c() { return latestHba1c; }
    public void setLatestHba1c(Double latestHba1c) { this.latestHba1c = latestHba1c; }
    public Integer getSystolicBloodPressure() { return systolicBloodPressure; }
    public void setSystolicBloodPressure(Integer systolicBloodPressure) { this.systolicBloodPressure = systolicBloodPressure; }
    public Integer getDiastolicBloodPressure() { return diastolicBloodPressure; }
    public void setDiastolicBloodPressure(Integer diastolicBloodPressure) { this.diastolicBloodPressure = diastolicBloodPressure; }
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    public Timestamp getLastMeasurementTime() { return lastMeasurementTime; }
    public void setLastMeasurementTime(Timestamp lastMeasurementTime) { this.lastMeasurementTime = lastMeasurementTime; }
    public int getRecentAlertCount() { return recentAlertCount; }
    public void setRecentAlertCount(int recentAlertCount) { this.recentAlertCount = recentAlertCount; }
    public int getUnreadDoctorAlertCount() { return unreadDoctorAlertCount; }
    public void setUnreadDoctorAlertCount(int unreadDoctorAlertCount) { this.unreadDoctorAlertCount = unreadDoctorAlertCount; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public List<String> getRiskReasons() { return riskReasons; }

    public String getRiskLabel() {
        if ("critical".equals(riskLevel)) {
            return "Nguy kịch";
        }
        if ("high".equals(riskLevel)) {
            return "Nguy cơ cao";
        }
        if ("medium".equals(riskLevel)) {
            return "Cần theo dõi";
        }
        return "Ổn định";
    }

    public String getRiskBadgeClass() {
        if ("critical".equals(riskLevel)) {
            return "bg-danger";
        }
        if ("high".equals(riskLevel)) {
            return "bg-warning text-dark";
        }
        if ("medium".equals(riskLevel)) {
            return "bg-info text-dark";
        }
        return "bg-success";
    }
}
