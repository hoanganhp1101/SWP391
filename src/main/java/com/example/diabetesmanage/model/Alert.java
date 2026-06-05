package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class Alert {
    private String id;
    private String patientId;
    private String loaiCanhBao;
    private String mucDo;
    private String tieuDe;
    private String noiDung;
    private Timestamp thoiGianTao;

    public Alert() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getLoaiCanhBao() { return loaiCanhBao; }
    public void setLoaiCanhBao(String loaiCanhBao) { this.loaiCanhBao = loaiCanhBao; }

    public String getMucDo() { return mucDo; }
    public void setMucDo(String mucDo) { this.mucDo = mucDo; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
