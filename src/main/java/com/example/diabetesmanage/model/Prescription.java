package com.example.diabetesmanage.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class Prescription {
    private String id;
    private String patientId;
    private String bacSiId;
    private Date ngayKeDon;
    private String chanDoan;
    private String huongDieuTri;
    private String cheDoAn;
    private String luyenTap;
    private String ghiChu;
    private Timestamp ngayTaiKham;
    
    // For joining with doctor User table
    private String bacSiName;
    
    // List of medications
    private List<Medication> medications;

    public Prescription() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getBacSiId() { return bacSiId; }
    public void setBacSiId(String bacSiId) { this.bacSiId = bacSiId; }
    public Date getNgayKeDon() { return ngayKeDon; }
    public void setNgayKeDon(Date ngayKeDon) { this.ngayKeDon = ngayKeDon; }
    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
    public Timestamp getNgayTaiKham() { return ngayTaiKham; }
    public void setNgayTaiKham(Timestamp ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }

    public String getBacSiName() { return bacSiName; }
    public void setBacSiName(String bacSiName) { this.bacSiName = bacSiName; }
    public String getHuongDieuTri() { return huongDieuTri; }
    public void setHuongDieuTri(String huongDieuTri) { this.huongDieuTri = huongDieuTri; }
    public String getCheDoAn() { return cheDoAn; }
    public void setCheDoAn(String cheDoAn) { this.cheDoAn = cheDoAn; }
    public String getLuyenTap() { return luyenTap; }
    public void setLuyenTap(String luyenTap) { this.luyenTap = luyenTap; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }
}
