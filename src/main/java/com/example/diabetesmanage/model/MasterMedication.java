package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class MasterMedication {

    private String id;
    private String tenThuoc;
    private String hoatChat;
    private String donViTinh;
    private String loaiThuoc;
    private String huongDanGoc;
    private boolean trangThai;
    private Timestamp ngayTao;

    // Constructor rỗng
    public MasterMedication() {
    }

    // Constructor không có id
    public MasterMedication(String tenThuoc,
                            String hoatChat,
                            String donViTinh,
                            String loaiThuoc,
                            String huongDanGoc,
                            boolean trangThai) {

        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViTinh = donViTinh;
        this.loaiThuoc = loaiThuoc;
        this.huongDanGoc = huongDanGoc;
        this.trangThai = trangThai;
    }

    // Constructor đầy đủ

    public MasterMedication(String id,
                            String tenThuoc,
                            String hoatChat,
                            String donViTinh,
                            String loaiThuoc,
                            String huongDanGoc,
                            boolean trangThai,
                            Timestamp ngayTao) {

        this.id = id;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViTinh = donViTinh;
        this.loaiThuoc = loaiThuoc;
        this.huongDanGoc = huongDanGoc;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    // =====================
    // Getter & Setter
    // =====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getHoatChat() {
        return hoatChat;
    }

    public void setHoatChat(String hoatChat) {
        this.hoatChat = hoatChat;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getLoaiThuoc() {
        return loaiThuoc;
    }

    public void setLoaiThuoc(String loaiThuoc) {
        this.loaiThuoc = loaiThuoc;
    }

    public String getHuongDanGoc() {
        return huongDanGoc;
    }

    public void setHuongDanGoc(String huongDanGoc) {
        this.huongDanGoc = huongDanGoc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Override
    public String toString() {
        return "MasterMedication{" +
                "id='" + id + '\'' +
                ", tenThuoc='" + tenThuoc + '\'' +
                ", hoatChat='" + hoatChat + '\'' +
                ", donViTinh='" + donViTinh + '\'' +
                ", loaiThuoc='" + loaiThuoc + '\'' +
                ", huongDanGoc='" + huongDanGoc + '\'' +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                '}';
    }
}