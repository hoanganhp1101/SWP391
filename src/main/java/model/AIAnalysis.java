/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.security.Timestamp;
import java.util.UUID;

/**
 *
 * @author iac26
 */
public class AIAnalysis {
    private UUID id;
    private UUID patientId;
    private UUID healthRecordId;

    private Double diemNguyCo;
    private String mucCanhBao;
    private Double doTinCay;

    private String phanTichChiTiet;

    private String yeuToNguyCo;
    private String khuyenNghi;
    private String duLieuDauVao;

    private String modelVersion;

    private Timestamp thoiGianPhanTich;
    private Integer tokensSuDung;

    public AIAnalysis() {
    }

    public AIAnalysis(UUID id, UUID patientId, UUID healthRecordId, Double diemNguyCo, String mucCanhBao, Double doTinCay, String phanTichChiTiet, String yeuToNguyCo, String khuyenNghi, String duLieuDauVao, String modelVersion, Timestamp thoiGianPhanTich, Integer tokensSuDung) {
        this.id = id;
        this.patientId = patientId;
        this.healthRecordId = healthRecordId;
        this.diemNguyCo = diemNguyCo;
        this.mucCanhBao = mucCanhBao;
        this.doTinCay = doTinCay;
        this.phanTichChiTiet = phanTichChiTiet;
        this.yeuToNguyCo = yeuToNguyCo;
        this.khuyenNghi = khuyenNghi;
        this.duLieuDauVao = duLieuDauVao;
        this.modelVersion = modelVersion;
        this.thoiGianPhanTich = thoiGianPhanTich;
        this.tokensSuDung = tokensSuDung;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(UUID healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public Double getDiemNguyCo() {
        return diemNguyCo;
    }

    public void setDiemNguyCo(Double diemNguyCo) {
        this.diemNguyCo = diemNguyCo;
    }

    public String getMucCanhBao() {
        return mucCanhBao;
    }

    public void setMucCanhBao(String mucCanhBao) {
        this.mucCanhBao = mucCanhBao;
    }

    public Double getDoTinCay() {
        return doTinCay;
    }

    public void setDoTinCay(Double doTinCay) {
        this.doTinCay = doTinCay;
    }

    public String getPhanTichChiTiet() {
        return phanTichChiTiet;
    }

    public void setPhanTichChiTiet(String phanTichChiTiet) {
        this.phanTichChiTiet = phanTichChiTiet;
    }

    public String getYeuToNguyCo() {
        return yeuToNguyCo;
    }

    public void setYeuToNguyCo(String yeuToNguyCo) {
        this.yeuToNguyCo = yeuToNguyCo;
    }

    public String getKhuyenNghi() {
        return khuyenNghi;
    }

    public void setKhuyenNghi(String khuyenNghi) {
        this.khuyenNghi = khuyenNghi;
    }

    public String getDuLieuDauVao() {
        return duLieuDauVao;
    }

    public void setDuLieuDauVao(String duLieuDauVao) {
        this.duLieuDauVao = duLieuDauVao;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Timestamp getThoiGianPhanTich() {
        return thoiGianPhanTich;
    }

    public void setThoiGianPhanTich(Timestamp thoiGianPhanTich) {
        this.thoiGianPhanTich = thoiGianPhanTich;
    }

    public Integer getTokensSuDung() {
        return tokensSuDung;
    }

    public void setTokensSuDung(Integer tokensSuDung) {
        this.tokensSuDung = tokensSuDung;
    }
    
}
