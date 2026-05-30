/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.util.UUID;

/**
 *
 * @author iac26
 */
public class User {
    private UUID id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String vaiTro;
    private String matKhauHash;
    private String anhDaiDien;
    private boolean kichHoat;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;
    private Timestamp lanDangNhapCuoi;
    public User(){
        
    }
    public User(UUID id, String hoTen, String email, String soDienThoai,
                String vaiTro, String matKhauHash, String anhDaiDien,
                boolean kichHoat, Timestamp ngayTao,
                Timestamp ngayCapNhat, Timestamp lanDangNhapCuoi) {

        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro;
        this.matKhauHash = matKhauHash;
        this.anhDaiDien = anhDaiDien;
        this.kichHoat = kichHoat;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public boolean isKichHoat() {
        return kichHoat;
    }

    public void setKichHoat(boolean kichHoat) {
        this.kichHoat = kichHoat;
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

    public Timestamp getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(Timestamp lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }
    
}
