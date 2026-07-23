package com.example.diabetesmanage.model;

import java.util.ArrayList;
import java.util.List;

public class AdminReportStats {
    private int periodDays;
    private int totalPatients;
    private int patientsWithRecentMeasurements;
    private double averageGlucose;
    private int controlledGlucoseCount;
    private int highGlucoseCount;
    private int criticalGlucoseCount;
    private double averageHba1c;
    private int controlledHba1cCount;
    private int highHba1cCount;
    private int controlledBloodPressureCount;
    private int highBloodPressureCount;
    private int totalAlerts;
    private int highAlerts;
    private int unreadDoctorAlerts;
    private int totalAppointments;
    private int pendingAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private int upcomingAppointments;
    private int telehealthAppointments;
    private final List<ReportBucket> glucoseBuckets = new ArrayList<>();
    private final List<ReportBucket> appointmentBuckets = new ArrayList<>();

    public int getPeriodDays() { return periodDays; }
    public void setPeriodDays(int periodDays) { this.periodDays = periodDays; }
    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
    public int getPatientsWithRecentMeasurements() { return patientsWithRecentMeasurements; }
    public void setPatientsWithRecentMeasurements(int patientsWithRecentMeasurements) { this.patientsWithRecentMeasurements = patientsWithRecentMeasurements; }
    public double getAverageGlucose() { return averageGlucose; }
    public void setAverageGlucose(double averageGlucose) { this.averageGlucose = averageGlucose; }
    public int getControlledGlucoseCount() { return controlledGlucoseCount; }
    public void setControlledGlucoseCount(int controlledGlucoseCount) { this.controlledGlucoseCount = controlledGlucoseCount; }
    public int getHighGlucoseCount() { return highGlucoseCount; }
    public void setHighGlucoseCount(int highGlucoseCount) { this.highGlucoseCount = highGlucoseCount; }
    public int getCriticalGlucoseCount() { return criticalGlucoseCount; }
    public void setCriticalGlucoseCount(int criticalGlucoseCount) { this.criticalGlucoseCount = criticalGlucoseCount; }
    public double getAverageHba1c() { return averageHba1c; }
    public void setAverageHba1c(double averageHba1c) { this.averageHba1c = averageHba1c; }
    public int getControlledHba1cCount() { return controlledHba1cCount; }
    public void setControlledHba1cCount(int controlledHba1cCount) { this.controlledHba1cCount = controlledHba1cCount; }
    public int getHighHba1cCount() { return highHba1cCount; }
    public void setHighHba1cCount(int highHba1cCount) { this.highHba1cCount = highHba1cCount; }
    public int getControlledBloodPressureCount() { return controlledBloodPressureCount; }
    public void setControlledBloodPressureCount(int controlledBloodPressureCount) { this.controlledBloodPressureCount = controlledBloodPressureCount; }
    public int getHighBloodPressureCount() { return highBloodPressureCount; }
    public void setHighBloodPressureCount(int highBloodPressureCount) { this.highBloodPressureCount = highBloodPressureCount; }
    public int getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }
    public int getHighAlerts() { return highAlerts; }
    public void setHighAlerts(int highAlerts) { this.highAlerts = highAlerts; }
    public int getUnreadDoctorAlerts() { return unreadDoctorAlerts; }
    public void setUnreadDoctorAlerts(int unreadDoctorAlerts) { this.unreadDoctorAlerts = unreadDoctorAlerts; }
    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }
    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }
    public int getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
    public int getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(int upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }
    public int getTelehealthAppointments() { return telehealthAppointments; }
    public void setTelehealthAppointments(int telehealthAppointments) { this.telehealthAppointments = telehealthAppointments; }
    public List<ReportBucket> getGlucoseBuckets() { return glucoseBuckets; }
    public List<ReportBucket> getAppointmentBuckets() { return appointmentBuckets; }

    public int getGlucoseControlRate() {
        int total = controlledGlucoseCount + highGlucoseCount + criticalGlucoseCount;
        return total == 0 ? 0 : (int) Math.round(controlledGlucoseCount * 100.0 / total);
    }

    public int getAppointmentCompletionRate() {
        return totalAppointments == 0 ? 0 : (int) Math.round(completedAppointments * 100.0 / totalAppointments);
    }
}
