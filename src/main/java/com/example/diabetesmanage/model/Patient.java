package com.example.diabetesmanage.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Patient {
    private String id;
    private String userId;
    private String bacSiId;
    private String patientCode;

    private User user;
    private User doctor;
    private Integer tuoi;

    private Date ngaySinh;
    private String gioiTinh;
    private Double chieuCaoCm;
    private String diaChi;
    private String baoHiemYTe;
    private String loaiTieuDuong;
    private String tienSuBenh;
    private String tienSuGiaDinh;
    private String diUng;
    private String nhomMau;
    private Date ngayChanDoanTieuDuong;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;

    private String hoTen;
    private String tenBenhNhan;
    private String email;
    private String soDienThoai;
    private String tenBacSi;
    private String anhDaiDien;

    private Double duongHuyetGanNhat;
    private Double bmiGanNhat;
    private Double hba1cGanNhat;

    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;

    private String mucNguyCo;
    private Double diemNguyCo;
    private Timestamp lanDoCuoi;
    private Integer canhBaoChuaDoc;

    public Patient() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBacSiId() {
        return bacSiId;
    }

    public void setBacSiId(String bacSiId) {
        this.bacSiId = bacSiId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
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

    public Integer getTuoi() {
        return tuoi;
    }

    public void setTuoi(Integer tuoi) {
        this.tuoi = tuoi;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public LocalDate getNgaySinhLocalDate() {
        return ngaySinh != null ? ngaySinh.toLocalDate() : null;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh != null ? Date.valueOf(ngaySinh) : null;
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

    public String getLoaiTieuDuong() {
        return loaiTieuDuong;
    }

    public void setLoaiTieuDuong(String loaiTieuDuong) {
        this.loaiTieuDuong = loaiTieuDuong;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Timestamp getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Timestamp ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getHoTen() {
        if (hoTen != null && !hoTen.isBlank()) {
            return hoTen;
        }
        if (user != null && user.getHoTen() != null && !user.getHoTen().isBlank()) {
            return user.getHoTen();
        }
        return tenBenhNhan;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTienSuBenh() {
        return tienSuBenh;
    }

    public void setTienSuBenh(String tienSuBenh) {
        this.tienSuBenh = tienSuBenh;
    }

    public String getTienSuGiaDinh() {
        return tienSuGiaDinh;
    }

    public void setTienSuGiaDinh(String tienSuGiaDinh) {
        this.tienSuGiaDinh = tienSuGiaDinh;
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

    public Date getNgayChanDoanTieuDuong() {
        return ngayChanDoanTieuDuong;
    }

    public void setNgayChanDoanTieuDuong(Date ngayChanDoanTieuDuong) {
        this.ngayChanDoanTieuDuong = ngayChanDoanTieuDuong;
    }

    public LocalDate getNgayChanDoanTieuDuongLocalDate() {
        return ngayChanDoanTieuDuong != null ? ngayChanDoanTieuDuong.toLocalDate() : null;
    }

    public void setNgayChanDoanTieuDuong(LocalDate ngayChanDoanTieuDuong) {
        this.ngayChanDoanTieuDuong = ngayChanDoanTieuDuong != null ? Date.valueOf(ngayChanDoanTieuDuong) : null;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public void setTenBenhNhan(String tenBenhNhan) {
        this.tenBenhNhan = tenBenhNhan;
    }

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return user != null ? user.getEmail() : null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            return soDienThoai;
        }
        return user != null ? user.getSoDienThoai() : null;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenBacSi() {
        return tenBacSi;
    }

    public void setTenBacSi(String tenBacSi) {
        this.tenBacSi = tenBacSi;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

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
