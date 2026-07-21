package com.example.diabetesmanage.model;

import java.sql.Timestamp;

public class Appointment {

    public static final String STATUS_CHO_KHAM = "cho_kham";
    public static final String STATUS_DA_KHAM = "da_kham";
    /** Khớp ENUM trong DB: da_huy */
    public static final String STATUS_HUY = "da_huy";

    private String id;
    private String patientId;
    private String patientCode;
    private String patientName;
    private String bacSiId;
    private String doctorName;
    private String noiDungKham;
    private String tieuDe;
    private Timestamp thoiGianHen;
    private String diaDiem;
    private String trangThai;
    private Timestamp ngayTao;
    private String bacSiName;

    public Appointment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBacSiId() { return bacSiId; }
    public void setBacSiId(String bacSiId) { this.bacSiId = bacSiId; }

    public String getDoctorName() {
        if (doctorName != null && !doctorName.isBlank()) {
            return doctorName;
        }
        return bacSiName;
    }

    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getNoiDungKham() {
        if (noiDungKham != null && !noiDungKham.isBlank()) {
            return noiDungKham;
        }
        return tieuDe;
    }

    public void setNoiDungKham(String noiDungKham) { this.noiDungKham = noiDungKham; }

    public String getTieuDe() {
        if (tieuDe != null && !tieuDe.isBlank()) {
            return tieuDe;
        }
        return noiDungKham;
    }

    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public Timestamp getThoiGianHen() { return thoiGianHen; }
    public void setThoiGianHen(Timestamp thoiGianHen) { this.thoiGianHen = thoiGianHen; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    public String getBacSiName() { return bacSiName; }
    public void setBacSiName(String bacSiName) { this.bacSiName = bacSiName; }

    public String getTrangThaiLabel() {
        if (STATUS_DA_KHAM.equals(trangThai)) {
            return "Đã khám";
        }
        if (STATUS_HUY.equals(trangThai) || "huy".equals(trangThai)) {
            return "Hủy";
        }
        return "Chờ khám";
    }

    /** Chuẩn hóa tham số filter từ URL (huy -> da_huy). */
    public static String normalizeStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if ("huy".equalsIgnoreCase(status) || STATUS_HUY.equalsIgnoreCase(status)) {
            return STATUS_HUY;
        }
        if (STATUS_CHO_KHAM.equalsIgnoreCase(status)) {
            return STATUS_CHO_KHAM;
        }
        if (STATUS_DA_KHAM.equalsIgnoreCase(status)) {
            return STATUS_DA_KHAM;
        }
        return status;
    }
}
