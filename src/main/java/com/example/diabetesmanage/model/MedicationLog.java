package com.example.diabetesmanage.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class MedicationLog {
    private String id;
    private String patientId;
    private String medicationId;
    private Date ngayUong;
    private Time thoiDiemDuKien;
    private Timestamp thoiGianThucTe;
    private String trangThai; // 'da_uong', 'bo_qua', 'chua_uong'
    private String ghiChu;

    // Additional fields from Medication table for display
    private String tenThuoc;
    private String lieuLuong;
    private String donVi;
    private String tanSuat;
    private String thoiDiemUong;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public Date getNgayUong() {
        return ngayUong;
    }

    public void setNgayUong(Date ngayUong) {
        this.ngayUong = ngayUong;
    }

    public Time getThoiDiemDuKien() {
        return thoiDiemDuKien;
    }

    public void setThoiDiemDuKien(Time thoiDiemDuKien) {
        this.thoiDiemDuKien = thoiDiemDuKien;
    }

    public Timestamp getThoiGianThucTe() {
        return thoiGianThucTe;
    }

    public void setThoiGianThucTe(Timestamp thoiGianThucTe) {
        this.thoiGianThucTe = thoiGianThucTe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getLieuLuong() {
        return lieuLuong;
    }

    public void setLieuLuong(String lieuLuong) {
        this.lieuLuong = lieuLuong;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public String getTanSuat() {
        return tanSuat;
    }

    public void setTanSuat(String tanSuat) {
        this.tanSuat = tanSuat;
    }

    public String getThoiDiemUong() {
        return thoiDiemUong;
    }

    public void setThoiDiemUong(String thoiDiemUong) {
        this.thoiDiemUong = thoiDiemUong;
    }
}
