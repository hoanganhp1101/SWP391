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
}
