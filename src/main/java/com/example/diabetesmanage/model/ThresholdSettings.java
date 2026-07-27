package com.example.diabetesmanage.model;

import java.sql.Timestamp;

/** Ngưỡng giám sát riêng của từng bác sĩ (bảng threshold_settings). */
public class ThresholdSettings {

    private String id;
    private String bacSiId;
    private int glucoseLow;
    private int glucoseHigh;
    private int glucoseDanger;
    private double hba1cTarget;
    private double hba1cPoor;
    private int daysNoMeasure;
    private Timestamp ngayCapNhat;

    public static ThresholdSettings defaults(String bacSiId) {
        ThresholdSettings s = new ThresholdSettings();
        s.bacSiId = bacSiId;
        s.glucoseLow = 70;
        s.glucoseHigh = 180;
        s.glucoseDanger = 250;
        s.hba1cTarget = 7.0;
        s.hba1cPoor = 9.0; // khớp ClinicalRiskService.HBA1C_CRITICAL
        s.daysNoMeasure = 7;
        return s;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBacSiId() { return bacSiId; }
    public void setBacSiId(String bacSiId) { this.bacSiId = bacSiId; }

    public int getGlucoseLow() { return glucoseLow; }
    public void setGlucoseLow(int glucoseLow) { this.glucoseLow = glucoseLow; }

    public int getGlucoseHigh() { return glucoseHigh; }
    public void setGlucoseHigh(int glucoseHigh) { this.glucoseHigh = glucoseHigh; }

    public int getGlucoseDanger() { return glucoseDanger; }
    public void setGlucoseDanger(int glucoseDanger) { this.glucoseDanger = glucoseDanger; }

    public double getHba1cTarget() { return hba1cTarget; }
    public void setHba1cTarget(double hba1cTarget) { this.hba1cTarget = hba1cTarget; }

    public double getHba1cPoor() { return hba1cPoor; }
    public void setHba1cPoor(double hba1cPoor) { this.hba1cPoor = hba1cPoor; }

    public int getDaysNoMeasure() { return daysNoMeasure; }
    public void setDaysNoMeasure(int daysNoMeasure) { this.daysNoMeasure = daysNoMeasure; }

    public Timestamp getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Timestamp ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
