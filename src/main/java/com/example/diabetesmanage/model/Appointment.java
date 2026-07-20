package com.example.diabetesmanage.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Appointment {

    public static final String STATUS_CHO_KHAM = "cho_kham";
    public static final String STATUS_DA_KHAM = "da_kham";
    /** Khớp ENUM trong DB: da_huy */
    public static final String STATUS_HUY = "da_huy";

    private static final DateTimeFormatter THOI_GIAN_HEN_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.US);

    private String id;
    private String patientId;
    private String patientCode;
    private String patientName;
    private String bacSiId;
    private String doctorName;
    private String noiDungKham;
    private LocalDateTime thoiGianHen;
    private String diaDiem;
    private String trangThai;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBacSiId() {
        return bacSiId;
    }

    public void setBacSiId(String bacSiId) {
        this.bacSiId = bacSiId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getNoiDungKham() {
        return noiDungKham;
    }

    public void setNoiDungKham(String noiDungKham) {
        this.noiDungKham = noiDungKham;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThaiLabel() {
        if (STATUS_DA_KHAM.equals(trangThai)) {
            return "Đã khám";
        }
        if (STATUS_HUY.equals(trangThai) || "huy".equals(trangThai)) {
            return "Hủy";
        }
        return "Chờ khám";
    }

    public String getThoiGianHenDisplay() {
        return thoiGianHen == null ? "—" : thoiGianHen.format(THOI_GIAN_HEN_FMT);
    }

    public String getStatusCssClass() {
        return trangThai == null ? "status-cho_kham" : "status-" + trangThai;
    }

    public boolean isChoKham() {
        return STATUS_CHO_KHAM.equals(trangThai);
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
