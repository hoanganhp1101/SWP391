package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class MasterFood {

    private String id;
    private String tenThucPham;
    private String loaiMon;
    private String donViKhauPhan;
    private double carbsG;
    private Double caloKcal;
    private Double chiSoGI;
    private boolean trangThai;
    private Timestamp ngayTao;

    // Constructor rỗng
    public MasterFood() {
    }

    // Constructor không có id

    public MasterFood(String tenThucPham,
                      String loaiMon,
                      String donViKhauPhan,
                      double carbsG,
                      Double caloKcal,
                      Double chiSoGI,
                      boolean trangThai) {

        this.tenThucPham = tenThucPham;
        this.loaiMon = loaiMon;
        this.donViKhauPhan = donViKhauPhan;
        this.carbsG = carbsG;
        this.caloKcal = caloKcal;
        this.chiSoGI = chiSoGI;
        this.trangThai = trangThai;
    }

    // Constructor đầy đủ

    public MasterFood(String id,
                      String tenThucPham,
                      String loaiMon,
                      String donViKhauPhan,
                      double carbsG,
                      Double caloKcal,
                      Double chiSoGI,
                      boolean trangThai,
                      Timestamp ngayTao) {

        this.id = id;
        this.tenThucPham = tenThucPham;
        this.loaiMon = loaiMon;
        this.donViKhauPhan = donViKhauPhan;
        this.carbsG = carbsG;
        this.caloKcal = caloKcal;
        this.chiSoGI = chiSoGI;
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

    public String getTenThucPham() {
        return tenThucPham;
    }

    public void setTenThucPham(String tenThucPham) {
        this.tenThucPham = tenThucPham;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public String getDonViKhauPhan() {
        return donViKhauPhan;
    }

    public void setDonViKhauPhan(String donViKhauPhan) {
        this.donViKhauPhan = donViKhauPhan;
    }

    public double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getCaloKcal() {
        return caloKcal;
    }

    public void setCaloKcal(Double caloKcal) {
        this.caloKcal = caloKcal;
    }

    public Double getChiSoGI() {
        return chiSoGI;
    }

    public void setChiSoGI(Double chiSoGI) {
        this.chiSoGI = chiSoGI;
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
        return "MasterFood{" +
                "id='" + id + '\'' +
                ", tenThucPham='" + tenThucPham + '\'' +
                ", loaiMon='" + loaiMon + '\'' +
                ", donViKhauPhan='" + donViKhauPhan + '\'' +
                ", carbsG=" + carbsG +
                ", caloKcal=" + caloKcal +
                ", chiSoGI=" + chiSoGI +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                '}';
    }
}