package com.example.diabetesmanage.model;

import java.time.LocalDateTime;

public class HealthRecord {

    private String id;

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

    private Integer soBuocChan;
    private Double carbsG;
    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private String ghiChu;

    private LocalDateTime thoiGianDo;
    private LocalDateTime ngayTao;

    public HealthRecord() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
// Getter Setter
}
