package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class User {
    private String id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String vaiTro;
    private String matKhauHash;
    private Timestamp ngayTao;

    public User() {}

    public User(String id, String hoTen, String email, String soDienThoai, String vaiTro, String matKhauHash, Timestamp ngayTao) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro;
        this.matKhauHash = matKhauHash;
        this.ngayTao = ngayTao;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getMatKhauHash() { return matKhauHash; }
    public void setMatKhauHash(String matKhauHash) { this.matKhauHash = matKhauHash; }
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
}
