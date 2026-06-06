package com.example.diabetesmanage.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Patient {

    private String id;

    private User user;
    private User doctor;
    private Integer tuoi;

    private LocalDate ngaySinh;
    private String gioiTinh;
    private Double chieuCaoCm;

    private String diaChi;
    private String baoHiemYTe;
    private String tienSuBenh;
    private String diUng;
    private String nhomMau;

    private Timestamp ngayCapNhat;
    private LocalDate ngayChanDoanTieuDuong;
    private String loaiTieuDuong;

    private Double duongHuyetGanNhat;
    private Double bmiGanNhat;
    private Double hba1cGanNhat;

    private String mucNguyCo;
    private Double diemNguyCo;

    private Timestamp lanDoCuoi;
    private Integer canhBaoChuaDoc;

    public Integer getTuoi() {
        return tuoi;
    }

    public void setTuoi(Integer tuoi) {
        this.tuoi = tuoi;
    }

    public Patient() {
    }

    public Timestamp getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Timestamp ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
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

    public Double getDuongHuyetGanNhat() {
        return duongHuyetGanNhat;
    }

    public void setDuongHuyetGanNhat(Double duongHuyetGanNhat) {
        this.duongHuyetGanNhat = duongHuyetGanNhat;
    }

    public Double getBmiGanNhat() {
        return bmiGanNhat;
    }

    public void setBmiGanNhat(Double bmiGanNhat) {
        this.bmiGanNhat = bmiGanNhat;
    }

    public Double getHba1cGanNhat() {
        return hba1cGanNhat;
    }

    public void setHba1cGanNhat(Double hba1cGanNhat) {
        this.hba1cGanNhat = hba1cGanNhat;
    }

    public String getMucNguyCo() {
        return mucNguyCo;
    }

    public void setMucNguyCo(String mucNguyCo) {
        this.mucNguyCo = mucNguyCo;
    }

    public Double getDiemNguyCo() {
        return diemNguyCo;
    }

    public void setDiemNguyCo(Double diemNguyCo) {
        this.diemNguyCo = diemNguyCo;
    }

    public Timestamp getLanDoCuoi() {
        return lanDoCuoi;
    }

    public void setLanDoCuoi(Timestamp lanDoCuoi) {
        this.lanDoCuoi = lanDoCuoi;
    }

    public Integer getCanhBaoChuaDoc() {
        return canhBaoChuaDoc;
    }

    public void setCanhBaoChuaDoc(Integer canhBaoChuaDoc) {
        this.canhBaoChuaDoc = canhBaoChuaDoc;
    }
}
