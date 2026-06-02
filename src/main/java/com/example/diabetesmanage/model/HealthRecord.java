package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class HealthRecord {
    private String id;
    private String patientId;
    private String nhapBoi;
    private Double duongHuyetMgdl;
    private String thoiDiemDoDuong;
    private Double hba1cPercent;
    private Double carbsG;
    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;
    private Integer nhipTim;
    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private String ghiChu;
    private Timestamp thoiGianDo;
    private Timestamp ngayTao;

    public HealthRecord() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getNhapBoi() { return nhapBoi; }
    public void setNhapBoi(String nhapBoi) { this.nhapBoi = nhapBoi; }
    public Double getDuongHuyetMgdl() { return duongHuyetMgdl; }
    public void setDuongHuyetMgdl(Double duongHuyetMgdl) { this.duongHuyetMgdl = duongHuyetMgdl; }
    public String getThoiDiemDoDuong() { return thoiDiemDoDuong; }
    public void setThoiDiemDoDuong(String thoiDiemDoDuong) { this.thoiDiemDoDuong = thoiDiemDoDuong; }
    public Double getHba1cPercent() { return hba1cPercent; }
    public void setHba1cPercent(Double hba1cPercent) { this.hba1cPercent = hba1cPercent; }
    public Double getCarbsG() { return carbsG; }
    public void setCarbsG(Double carbsG) { this.carbsG = carbsG; }
    public Integer getLieuLuongInsulinUi() { return lieuLuongInsulinUi; }
    public void setLieuLuongInsulinUi(Integer lieuLuongInsulinUi) { this.lieuLuongInsulinUi = lieuLuongInsulinUi; }
    public String getLoaiInsulinTiem() { return loaiInsulinTiem; }
    public void setLoaiInsulinTiem(String loaiInsulinTiem) { this.loaiInsulinTiem = loaiInsulinTiem; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public Timestamp getThoiGianDo() { return thoiGianDo; }
    public void setThoiGianDo(Timestamp thoiGianDo) { this.thoiGianDo = thoiGianDo; }
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    public Integer getNhipTim() { return nhipTim; }
    public void setNhipTim(Integer nhipTim) { this.nhipTim = nhipTim; }
    public Integer getHuyetApTamThu() { return huyetApTamThu; }
    public void setHuyetApTamThu(Integer huyetApTamThu) { this.huyetApTamThu = huyetApTamThu; }
    public Integer getHuyetApTamTruong() { return huyetApTamTruong; }
    public void setHuyetApTamTruong(Integer huyetApTamTruong) { this.huyetApTamTruong = huyetApTamTruong; }
}
