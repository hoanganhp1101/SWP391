package com.example.diabetesmanage.service.medical.profile;

import java.time.LocalDateTime;

public class LabResultProfileDto {

    private String labResultId;
    private String patientId;
    private String encounterId;
    private LocalDateTime testDate;
    private Double glucoseMmol;
    private Double hba1c;
    private Double cholesterol;
    private Double triglyceride;
    private Double hdl;
    private Double ldl;
    private Double ast;
    private Double alt;
    private Double ure;
    private Double creatinine;
    private Double wbc;
    private Double rbc;
    private Double hgb;
    private Double hct;
    private Double plt;
    private String note;

    public String getLabResultId() {
        return labResultId;
    }

    public void setLabResultId(String labResultId) {
        this.labResultId = labResultId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }

    public Double getGlucoseMmol() {
        return glucoseMmol;
    }

    public void setGlucoseMmol(Double glucoseMmol) {
        this.glucoseMmol = glucoseMmol;
    }

    public Double getHba1c() {
        return hba1c;
    }

    public void setHba1c(Double hba1c) {
        this.hba1c = hba1c;
    }

    public Double getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(Double cholesterol) {
        this.cholesterol = cholesterol;
    }

    public Double getTriglyceride() {
        return triglyceride;
    }

    public void setTriglyceride(Double triglyceride) {
        this.triglyceride = triglyceride;
    }

    public Double getHdl() {
        return hdl;
    }

    public void setHdl(Double hdl) {
        this.hdl = hdl;
    }

    public Double getLdl() {
        return ldl;
    }

    public void setLdl(Double ldl) {
        this.ldl = ldl;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
