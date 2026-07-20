package com.example.diabetesmanage.model;

import java.time.LocalDateTime;

public class LabResult {

    private String id;
    private String displayCode;
    private String patientId;
    private String encounterId;
    private LocalDateTime ngayXetNghiem;

    private Double glucoseMau;
    private Double hba1c;
    private Double cholesterolTp;
    private Double triglyceride;
    private Double hdlC;
    private Double ldlC;
    private Double ast;
    private Double alt;
    private Double ure;
    private Double creatinine;

    private Double wbc;
    private Double rbc;
    private Double hgb;
    private Double hct;
    private Double plt;

    private String ghiChu;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayCode() {
        return displayCode;
    }

    public void setDisplayCode(String displayCode) {
        this.displayCode = displayCode;
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

    public LocalDateTime getNgayXetNghiem() {
        return ngayXetNghiem;
    }

    public void setNgayXetNghiem(LocalDateTime ngayXetNghiem) {
        this.ngayXetNghiem = ngayXetNghiem;
    }

    public Double getGlucoseMau() {
        return glucoseMau;
    }

    public void setGlucoseMau(Double glucoseMau) {
        this.glucoseMau = glucoseMau;
    }

    public Double getHba1c() {
        return hba1c;
    }

    public void setHba1c(Double hba1c) {
        this.hba1c = hba1c;
    }

    public Double getCholesterolTp() {
        return cholesterolTp;
    }

    public void setCholesterolTp(Double cholesterolTp) {
        this.cholesterolTp = cholesterolTp;
    }

    public Double getTriglyceride() {
        return triglyceride;
    }

    public void setTriglyceride(Double triglyceride) {
        this.triglyceride = triglyceride;
    }

    public Double getHdlC() {
        return hdlC;
    }

    public void setHdlC(Double hdlC) {
        this.hdlC = hdlC;
    }

    public Double getLdlC() {
        return ldlC;
    }

    public void setLdlC(Double ldlC) {
        this.ldlC = ldlC;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Double getGlucoseMgdl() {
        if (glucoseMau == null) {
            return null;
        }
        return glucoseMau * 18.0182;
    }
}
