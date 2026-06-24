package com.example.diabetesmanage.model;

import java.time.LocalDateTime;

public class HealthRecord {

    private String id;
    private String healthRecordId;
    private Patient patient;
    private User nhapBoi;

    private Double duongHuyetMgdl;
    private String thoiDiemDoDuong;

    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Integer nhipTim;

    private Double canNangKg;
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
    private Double creatinine;

    private String trieuChung;
    private String chanDoanChinh;
    private String khuyenNghi;

    private Integer soBuocChan;
    private Double carbsG;
    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private String ghiChu;

    private LocalDateTime thoiGianDo;
    private LocalDateTime ngayTao;
    private int daysSinceLastVisit;

    public HealthRecord() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(String healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public User getNhapBoi() {
        return nhapBoi;
    }

    public void setNhapBoi(User nhapBoi) {
        this.nhapBoi = nhapBoi;
    }

    public Double getDuongHuyetMgdl() {
        return duongHuyetMgdl;
    }

    public void setDuongHuyetMgdl(Double duongHuyetMgdl) {
        this.duongHuyetMgdl = duongHuyetMgdl;
    }

    public String getThoiDiemDoDuong() {
        return thoiDiemDoDuong;
    }

    public void setThoiDiemDoDuong(String thoiDiemDoDuong) {
        this.thoiDiemDoDuong = thoiDiemDoDuong;
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

    public Integer getNhipTim() {
        return nhipTim;
    }

    public void setNhipTim(Integer nhipTim) {
        this.nhipTim = nhipTim;
    }

    public Double getCanNangKg() {
        return canNangKg;
    }

    public void setCanNangKg(Double canNangKg) {
        this.canNangKg = canNangKg;
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

    public Double getCreatinine() {
        return creatinine;
    }

    public void setCreatinine(Double creatinine) {
        this.creatinine = creatinine;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getChanDoanChinh() {
        return chanDoanChinh;
    }

    public void setChanDoanChinh(String chanDoanChinh) {
        this.chanDoanChinh = chanDoanChinh;
    }

    public String getKhuyenNghi() {
        return khuyenNghi;
    }

    public void setKhuyenNghi(String khuyenNghi) {
        this.khuyenNghi = khuyenNghi;
    }

    public Integer getSoBuocChan() {
        return soBuocChan;
    }

    public void setSoBuocChan(Integer soBuocChan) {
        this.soBuocChan = soBuocChan;
    }

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getSoGioNgu() {
        return soGioNgu;
    }

    public void setSoGioNgu(Double soGioNgu) {
        this.soGioNgu = soGioNgu;
    }

    public Integer getLieuLuongInsulinUi() {
        return lieuLuongInsulinUi;
    }

    public void setLieuLuongInsulinUi(Integer lieuLuongInsulinUi) {
        this.lieuLuongInsulinUi = lieuLuongInsulinUi;
    }

    public String getLoaiInsulinTiem() {
        return loaiInsulinTiem;
    }

    public void setLoaiInsulinTiem(String loaiInsulinTiem) {
        this.loaiInsulinTiem = loaiInsulinTiem;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGianDo() {
        return thoiGianDo;
    }

    public void setThoiGianDo(LocalDateTime thoiGianDo) {
        this.thoiGianDo = thoiGianDo;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public int getDaysSinceLastVisit() {
        return daysSinceLastVisit;
    }

    public void setDaysSinceLastVisit(int daysSinceLastVisit) {
        this.daysSinceLastVisit = daysSinceLastVisit;
    }

    // Getter Setter
}
