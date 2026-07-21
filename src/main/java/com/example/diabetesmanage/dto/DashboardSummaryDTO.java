package com.example.diabetesmanage.dto;

public class DashboardSummaryDTO {

    private int totalPatients;
    private int activeAlerts;
    private int todayHealthRecords;
    private int riskLow;
    private int riskMedium;
    private int riskHigh;
    private int riskCritical;
    private int priorityLevel1Count;

    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public int getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(int activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public void setTodayHealthRecords(int todayHealthRecords) {
        this.todayHealthRecords = todayHealthRecords;
    }

    public int getTodayHealthRecords() {
        return todayHealthRecords;
    }

    public void setRiskLow(int riskLow) {
        this.riskLow = riskLow;
    }

    public int getRiskLow() {
        return riskLow;
    }

    public void setRiskMedium(int riskMedium) {
        this.riskMedium = riskMedium;
    }

    public int getRiskMedium() {
        return riskMedium;
    }

    public int getRiskHigh() {
        return riskHigh;
    }

    public void setRiskHigh(int riskHigh) {
        this.riskHigh = riskHigh;
    }

    public int getRiskCritical() {
        return riskCritical;
    }

    public void setRiskCritical(int riskCritical) {
        this.riskCritical = riskCritical;
    }

    public void setPriorityLevel1Count(int priorityLevel1Count) {
        this.priorityLevel1Count = priorityLevel1Count;
    }

    public int getPriorityLevel1Count() {
        return priorityLevel1Count;
    }
}
