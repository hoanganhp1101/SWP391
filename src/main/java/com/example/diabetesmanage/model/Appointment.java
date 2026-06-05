package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class Appointment {
    private String id;
    private String patientId;
    private String bacSiId;
    private String tieuDe;
    private Timestamp thoiGianHen;
    private String diaDiem;
    private String trangThai;
    private Timestamp ngayTao;

    public Appointment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getBacSiId() { return bacSiId; }
    public void setBacSiId(String bacSiId) { this.bacSiId = bacSiId; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public Timestamp getThoiGianHen() { return thoiGianHen; }
    public void setThoiGianHen(Timestamp thoiGianHen) { this.thoiGianHen = thoiGianHen; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
}
