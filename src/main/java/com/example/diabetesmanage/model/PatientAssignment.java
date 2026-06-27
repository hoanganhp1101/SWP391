package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class PatientAssignment {
    private String id;
    private String patientId;
    private String doctorId;
    private Timestamp ngayPhanCong;
    private boolean trangThai;

    public PatientAssignment() {}

    public PatientAssignment(String patientId, String doctorId, boolean trangThai) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.trangThai = trangThai;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public Timestamp getNgayPhanCong() { return ngayPhanCong; }
    public void setNgayPhanCong(Timestamp ngayPhanCong) { this.ngayPhanCong = ngayPhanCong; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}