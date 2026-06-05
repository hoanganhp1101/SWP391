package com.example.diabetesmanage.model;

import java.sql.Date;
import java.sql.Timestamp;

public class MedicalDocument {
    private String id;
    private String patientId;
    private String bacSiId;
    private String loaiTaiLieu;
    private String trangThai;
    private String fileUrl;
    private Date ngayThucHien;
    private Timestamp ngayTao;

    public MedicalDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getBacSiId() { return bacSiId; }
    public void setBacSiId(String bacSiId) { this.bacSiId = bacSiId; }

    public String getLoaiTaiLieu() { return loaiTaiLieu; }
    public void setLoaiTaiLieu(String loaiTaiLieu) { this.loaiTaiLieu = loaiTaiLieu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Date getNgayThucHien() { return ngayThucHien; }
    public void setNgayThucHien(Date ngayThucHien) { this.ngayThucHien = ngayThucHien; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
}
