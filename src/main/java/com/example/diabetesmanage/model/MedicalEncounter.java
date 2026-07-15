package com.example.diabetesmanage.model;

import java.time.LocalDateTime;

public class MedicalEncounter {

    private String id;
    private String displayCode;
    private String patientId;
    private String bacSiId;
    private LocalDateTime ngayKham;
    private String lyDoKham;
    private String quaTrinhBenhLy;
    private String khamLamSang;
    private String chanDoanChinh;
    private String chanDoanPhu;
    private String huongXuTri;
    private String loaiEncounter;
    private String patientCode;
    private String patientName;
    private String doctorName;
    private LocalDateTime ngayTao;
    private String trangThai;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayCode() {
        return displayCode;
    }

    public void setDisplayCode(String displayCode) {
        this.displayCode = displayCode;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getBacSiId() {
        return bacSiId;
    }

    public void setBacSiId(String bacSiId) {
        this.bacSiId = bacSiId;
    }

    public LocalDateTime getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDateTime ngayKham) {
        this.ngayKham = ngayKham;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        this.lyDoKham = lyDoKham;
    }

    public String getQuaTrinhBenhLy() {
        return quaTrinhBenhLy;
    }

    public void setQuaTrinhBenhLy(String quaTrinhBenhLy) {
        this.quaTrinhBenhLy = quaTrinhBenhLy;
    }

    public String getKhamLamSang() {
        return khamLamSang;
    }

    public void setKhamLamSang(String khamLamSang) {
        this.khamLamSang = khamLamSang;
    }

    public String getChanDoanChinh() {
        return chanDoanChinh;
    }

    public void setChanDoanChinh(String chanDoanChinh) {
        this.chanDoanChinh = chanDoanChinh;
    }

    public String getChanDoanPhu() {
        return chanDoanPhu;
    }

    public void setChanDoanPhu(String chanDoanPhu) {
        this.chanDoanPhu = chanDoanPhu;
    }

    public String getHuongXuTri() {
        return huongXuTri;
    }

    public void setHuongXuTri(String huongXuTri) {
        this.huongXuTri = huongXuTri;
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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getLoaiEncounter() {
        return loaiEncounter;
    }

    public void setLoaiEncounter(String loaiEncounter) {
        this.loaiEncounter = loaiEncounter;
    }

    public EncounterType getEncounterType() {
        return EncounterType.fromCode(loaiEncounter);
    }

    public String getEncounterTypeLabel() {
        return getEncounterType().getLabel();
    }

    public String getStatusLabel() {
        if (trangThai == null || trangThai.isBlank()) {
            return "Đã khám";
        }
        switch (trangThai) {
            case "cho_kham":
                return "Chờ khám";
            case "da_kham":
            case "hoan_thanh":
                return "Đã khám";
            case "da_huy":
            case "huy":
                return "Đã hủy";
            default:
                return trangThai;
        }
    }
}
