package com.example.diabetesmanage.model;

import java.time.LocalDate;

public class Patient {

    private String id;

    private User user;
    private User doctor;

    private LocalDate ngaySinh;
    private String gioiTinh;
    private Double chieuCaoCm;

    private String diaChi;
    private String baoHiemYTe;
    private String tienSuBenh;
    private String diUng;
    private String nhomMau;

    private LocalDate ngayChanDoanTieuDuong;
    private String loaiTieuDuong;

    public Patient() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public Double getChieuCaoCm() {
        return chieuCaoCm;
    }

    public void setChieuCaoCm(Double chieuCaoCm) {
        this.chieuCaoCm = chieuCaoCm;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getBaoHiemYTe() {
        return baoHiemYTe;
    }

    public void setBaoHiemYTe(String baoHiemYTe) {
        this.baoHiemYTe = baoHiemYTe;
    }

    public String getTienSuBenh() {
        return tienSuBenh;
    }

    public void setTienSuBenh(String tienSuBenh) {
        this.tienSuBenh = tienSuBenh;
    }

    public String getDiUng() {
        return diUng;
    }

    public void setDiUng(String diUng) {
        this.diUng = diUng;
    }

    public String getNhomMau() {
        return nhomMau;
    }

    public void setNhomMau(String nhomMau) {
        this.nhomMau = nhomMau;
    }

    public LocalDate getNgayChanDoanTieuDuong() {
        return ngayChanDoanTieuDuong;
    }

    public void setNgayChanDoanTieuDuong(LocalDate ngayChanDoanTieuDuong) {
        this.ngayChanDoanTieuDuong = ngayChanDoanTieuDuong;
    }

    public String getLoaiTieuDuong() {
        return loaiTieuDuong;
    }

    public void setLoaiTieuDuong(String loaiTieuDuong) {
        this.loaiTieuDuong = loaiTieuDuong;
    }
// Getter Setter
}
