package com.example.diabetesmanage.model;

public class Medication {
    private String id;
    private String prescriptionId;
    private String tenThuoc;
    private String hoatChat;
    private String lieuLuong;
    private String donVi;
    private String tanSuat;
    private String thoiDiemUong;
    private Integer thoiGianDungNgay;
    private String ghiChu;

    public Medication() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }
    public String getHoatChat() { return hoatChat; }
    public void setHoatChat(String hoatChat) { this.hoatChat = hoatChat; }
    public String getLieuLuong() { return lieuLuong; }
    public void setLieuLuong(String lieuLuong) { this.lieuLuong = lieuLuong; }
    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }
    public String getTanSuat() { return tanSuat; }
    public void setTanSuat(String tanSuat) { this.tanSuat = tanSuat; }
    public String getThoiDiemUong() { return thoiDiemUong; }
    public void setThoiDiemUong(String thoiDiemUong) { this.thoiDiemUong = thoiDiemUong; }
    public Integer getThoiGianDungNgay() { return thoiGianDungNgay; }
    public void setThoiGianDungNgay(Integer thoiGianDungNgay) { this.thoiGianDungNgay = thoiGianDungNgay; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
