package com.example.diabetesmanage.service.medical.profile;

import java.time.LocalDateTime;

/**
 * Latest health_records snapshot (baseline vitals) for endocrinology profile.
 */
public class HealthRecordBaselineDto {

    private String healthRecordId;
    private String patientId;
    private LocalDateTime measuredAt;
    private LocalDateTime updatedAt;

    private Double glucoseMgdl;
    private String glucoseTiming;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private Double temperatureC;
    private Integer respiratoryRate;
    private Double heightCm;
    private Double weightKg;
    private Double bmi;
    private Double hba1cPercent;
    private Double cholesterolMmol;
    private Double triglycerideMmol;
    private Double hdlMmol;
    private Double ldlMmol;
    private Double wbc;
    private Double rbc;
    private Double hgb;
    private Double hct;
    private Double plt;
    private Double ast;
    private Double alt;
    private Double ure;
    private Double creatinine;
    private String symptoms;
    private String primaryDiagnosis;
    private Integer insulinUnits;
    private String insulinType;

    public String getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(String healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getGlucoseMgdl() {
        return glucoseMgdl;
    }

    public void setGlucoseMgdl(Double glucoseMgdl) {
        this.glucoseMgdl = glucoseMgdl;
    }

    public String getGlucoseTiming() {
        return glucoseTiming;
    }

    public void setGlucoseTiming(String glucoseTiming) {
        this.glucoseTiming = glucoseTiming;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Double getHba1cPercent() {
        return hba1cPercent;
    }

    public void setHba1cPercent(Double hba1cPercent) {
        this.hba1cPercent = hba1cPercent;
    }

    public Double getCholesterolMmol() {
        return cholesterolMmol;
    }

    public void setCholesterolMmol(Double cholesterolMmol) {
        this.cholesterolMmol = cholesterolMmol;
    }

    public Double getTriglycerideMmol() {
        return triglycerideMmol;
    }

    public void setTriglycerideMmol(Double triglycerideMmol) {
        this.triglycerideMmol = triglycerideMmol;
    }

    public Double getHdlMmol() {
        return hdlMmol;
    }

    public void setHdlMmol(Double hdlMmol) {
        this.hdlMmol = hdlMmol;
    }

    public Double getLdlMmol() {
        return ldlMmol;
    }

    public void setLdlMmol(Double ldlMmol) {
        this.ldlMmol = ldlMmol;
    }

    public Double getWbc() {
        return wbc;
    }

    public void setWbc(Double wbc) {
        this.wbc = wbc;
    }

    public Double getRbc() {
        return rbc;
    }

    public void setRbc(Double rbc) {
        this.rbc = rbc;
    }

    public Double getHgb() {
        return hgb;
    }

    public void setHgb(Double hgb) {
        this.hgb = hgb;
    }

    public Double getHct() {
        return hct;
    }

    public void setHct(Double hct) {
        this.hct = hct;
    }

    public Double getPlt() {
        return plt;
    }

    public void setPlt(Double plt) {
        this.plt = plt;
    }

    public Double getAst() {
        return ast;
    }

    public void setAst(Double ast) {
        this.ast = ast;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public Double getUre() {
        return ure;
    }

    public void setUre(Double ure) {
        this.ure = ure;
    }

    public Double getCreatinine() {
        return creatinine;
    }

    public void setCreatinine(Double creatinine) {
        this.creatinine = creatinine;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getPrimaryDiagnosis() {
        return primaryDiagnosis;
    }

    public void setPrimaryDiagnosis(String primaryDiagnosis) {
        this.primaryDiagnosis = primaryDiagnosis;
    }

    public Integer getInsulinUnits() {
        return insulinUnits;
    }

    public void setInsulinUnits(Integer insulinUnits) {
        this.insulinUnits = insulinUnits;
    }

    public String getInsulinType() {
        return insulinType;
    }

    public void setInsulinType(String insulinType) {
        this.insulinType = insulinType;
    }
}
