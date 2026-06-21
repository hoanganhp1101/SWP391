/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.security.Timestamp;
import java.util.UUID;

/**
 *
 * @author iac26
 */
public class HealthRecord {
    private UUID id;
    private UUID patientId;
    private UUID nhapBoi;

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

    private Double carbsG;

    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private String ghiChu;

    private Timestamp thoiGianDo;
    private Timestamp ngayTao;

    public HealthRecord() {
    }

    public HealthRecord(UUID id, UUID patientId, UUID nhapBoi, Double duongHuyetMgdl, String thoiDiemDoDuong, Integer huyetApTamThu, Integer huyetApTamTruong, Integer nhipTim, Double canNangKg, Double bmi, Double hba1cPercent, Double cholesterolMmol, Double triglycerideMmol, Double carbsG, Double soGioNgu, Integer lieuLuongInsulinUi, String loaiInsulinTiem, String ghiChu, Timestamp thoiGianDo, Timestamp ngayTao) {
        this.id = id;
        this.patientId = patientId;
        this.nhapBoi = nhapBoi;
        this.duongHuyetMgdl = duongHuyetMgdl;
        this.thoiDiemDoDuong = thoiDiemDoDuong;
        this.huyetApTamThu = huyetApTamThu;
        this.huyetApTamTruong = huyetApTamTruong;
        this.nhipTim = nhipTim;
        this.canNangKg = canNangKg;
        this.bmi = bmi;
        this.hba1cPercent = hba1cPercent;
        this.cholesterolMmol = cholesterolMmol;
        this.triglycerideMmol = triglycerideMmol;
        this.carbsG = carbsG;
        this.soGioNgu = soGioNgu;
        this.lieuLuongInsulinUi = lieuLuongInsulinUi;
        this.loaiInsulinTiem = loaiInsulinTiem;
        this.ghiChu = ghiChu;
        this.thoiGianDo = thoiGianDo;
        this.ngayTao = ngayTao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getNhapBoi() {
        return nhapBoi;
    }

    public void setNhapBoi(UUID nhapBoi) {
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

    public Timestamp getThoiGianDo() {
        return thoiGianDo;
    }

    public void setThoiGianDo(Timestamp thoiGianDo) {
        this.thoiGianDo = thoiGianDo;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }
    
}
