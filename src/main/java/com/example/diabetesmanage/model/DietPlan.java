package com.example.diabetesmanage.model;

import java.sql.Timestamp;
import java.util.List;

public class DietPlan {
    private String id;
    private String patientId;
    private String doctorId;
    private Timestamp ngayTao;
    private String ghiChu;

    private List<DietPlanDetail> chiTietThucPham;

    public DietPlan() {}

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public List<DietPlanDetail> getChiTietThucPham() { return chiTietThucPham; }
    public void setChiTietThucPham(List<DietPlanDetail> chiTietThucPham) { this.chiTietThucPham = chiTietThucPham; }
}