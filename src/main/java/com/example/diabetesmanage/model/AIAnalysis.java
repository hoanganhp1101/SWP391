package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class AIAnalysis {
    private String id;
    private String patientId;
    private String healthRecordId;
    private double diemNguyCo;
    private String mucCanhBao; // 'an_toan','trung_binh','cao','nguy_hiem'
    private Double doTinCay;
    private String phanTichChiTiet;
    private String yeuToNguyCo;    // JSON string
    private String khuyenNghi;     // JSON string
    private String duLieuDauVao;   // JSON string
    private String modelVersion;
    private Timestamp thoiGianPhanTich;
    private Integer tokensSuDung;
    /** Workflow bác sĩ: chua_xem | da_xem | da_ap_dung | bo_qua */
    private String trangThai;
    private String ghiChuBs;
    private String xuLyBoi;
    private String hoTenBenhNhan;

    public AIAnalysis() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getHealthRecordId() { return healthRecordId; }
    public void setHealthRecordId(String healthRecordId) { this.healthRecordId = healthRecordId; }

    public double getDiemNguyCo() { return diemNguyCo; }
    public void setDiemNguyCo(double diemNguyCo) { this.diemNguyCo = diemNguyCo; }

    public String getMucCanhBao() { return mucCanhBao; }
    public void setMucCanhBao(String mucCanhBao) { this.mucCanhBao = mucCanhBao; }

    public Double getDoTinCay() { return doTinCay; }
    public void setDoTinCay(Double doTinCay) { this.doTinCay = doTinCay; }

    public String getPhanTichChiTiet() { return phanTichChiTiet; }
    public void setPhanTichChiTiet(String phanTichChiTiet) { this.phanTichChiTiet = phanTichChiTiet; }

    public String getYeuToNguyCo() { return yeuToNguyCo; }
    public void setYeuToNguyCo(String yeuToNguyCo) { this.yeuToNguyCo = yeuToNguyCo; }

    public String getKhuyenNghi() { return khuyenNghi; }
    public void setKhuyenNghi(String khuyenNghi) { this.khuyenNghi = khuyenNghi; }

    public String getDuLieuDauVao() { return duLieuDauVao; }
    public void setDuLieuDauVao(String duLieuDauVao) { this.duLieuDauVao = duLieuDauVao; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Timestamp getThoiGianPhanTich() { return thoiGianPhanTich; }
    public void setThoiGianPhanTich(Timestamp thoiGianPhanTich) { this.thoiGianPhanTich = thoiGianPhanTich; }

    public Integer getTokensSuDung() { return tokensSuDung; }
    public void setTokensSuDung(Integer tokensSuDung) { this.tokensSuDung = tokensSuDung; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChuBs() { return ghiChuBs; }
    public void setGhiChuBs(String ghiChuBs) { this.ghiChuBs = ghiChuBs; }

    public String getXuLyBoi() { return xuLyBoi; }
    public void setXuLyBoi(String xuLyBoi) { this.xuLyBoi = xuLyBoi; }

    public String getHoTenBenhNhan() { return hoTenBenhNhan; }
    public void setHoTenBenhNhan(String hoTenBenhNhan) { this.hoTenBenhNhan = hoTenBenhNhan; }
}
