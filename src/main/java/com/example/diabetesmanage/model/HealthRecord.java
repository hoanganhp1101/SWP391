package com.example.diabetesmanage.model;

import java.time.LocalDateTime;

/**
 * Snapshot sức khỏe — entity map trực tiếp bảng {@code health_records}.
 * UI chi tiết sức khỏe chỉ bind object này; không đọc MedicalEncounter.
 */
public class HealthRecord {

    private String id;
    private String encounterId;
    private String lastEncounterId;
    private String healthRecordId;
    private Patient patient;
    private User nhapBoi;

    private Double chieuCaoCm;

    private Double duongHuyetMgdl;
    private String thoiDiemDoDuong;

    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Integer nhipTim;
    private Double nhietDoC;
    private Integer nhipTho;

    private Double canNangKg;
    private Double bmi;

    private Double hba1cPercent;
    private Double cholesterolMmol;
    private Double triglycerideMmol;
    private Double hdlMmol;
    private Double ldlMmol;

    private Double wbc;
    private Double rbc;
    private Double hgb;
    private Double hct;
    private Double plt;

    private Double ast;
    private Double alt;
    private Double ure;
    private Double creatinine;

    private String trieuChung;
    private String tienSuBenh;
    private String khamLamSang;
    private String chanDoanChinh;
    private String chanDoanPhu;
    private String phanLoaiTieuDuong;
    private String huongXuTri;
    private String khuyenNghi;
    private String khuyenNghiDieuTri;
    private String cheDoAn;
    private String luyenTap;

    private Integer soBuocChan;
    private Double carbsG;
    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private String ghiChu;

    private Boolean chestPain;
    private Boolean dizziness;
    private Boolean fatigue;

    private LocalDateTime thoiGianDo;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private int daysSinceLastVisit;

    public HealthRecord() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getLastEncounterId() {
        return lastEncounterId;
    }

    public void setLastEncounterId(String lastEncounterId) {
        this.lastEncounterId = lastEncounterId;
    }

    public Double getChieuCaoCm() {
        return chieuCaoCm;
    }

    public void setChieuCaoCm(Double chieuCaoCm) {
        this.chieuCaoCm = chieuCaoCm;
    }

    public String getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(String healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public User getNhapBoi() {
        return nhapBoi;
    }

    public void setNhapBoi(User nhapBoi) {
        this.nhapBoi = nhapBoi;
    }

    public Double getDuongHuyetMgdl() {
        return duongHuyetMgdl;
    }

    public void setDuongHuyetMgdl(Double duongHuyetMgdl) {
        this.duongHuyetMgdl = duongHuyetMgdl;
    }

    public String getThoiDiemDoDuong() {
        return thoiDiemDoDuong;
    }

    public void setThoiDiemDoDuong(String thoiDiemDoDuong) {
        this.thoiDiemDoDuong = thoiDiemDoDuong;
    }

    public Integer getHuyetApTamThu() {
        return huyetApTamThu;
    }

    public void setHuyetApTamThu(Integer huyetApTamThu) {
        this.huyetApTamThu = huyetApTamThu;
    }

    public Integer getHuyetApTamTruong() {
        return huyetApTamTruong;
    }

    public void setHuyetApTamTruong(Integer huyetApTamTruong) {
        this.huyetApTamTruong = huyetApTamTruong;
    }

    public Integer getNhipTim() {
        return nhipTim;
    }

    public void setNhipTim(Integer nhipTim) {
        this.nhipTim = nhipTim;
    }

    public Double getNhietDoC() {
        return nhietDoC;
    }

    public void setNhietDoC(Double nhietDoC) {
        this.nhietDoC = nhietDoC;
    }

    public Integer getNhipTho() {
        return nhipTho;
    }

    public void setNhipTho(Integer nhipTho) {
        this.nhipTho = nhipTho;
    }

    public Double getCanNangKg() {
        return canNangKg;
    }

    public void setCanNangKg(Double canNangKg) {
        this.canNangKg = canNangKg;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Double getHba1cPercent() {
        return hba1cPercent;
    }

    public void setHba1cPercent(Double hba1cPercent) {
        this.hba1cPercent = hba1cPercent;
    }

    public Double getCholesterolMmol() {
        return cholesterolMmol;
    }

    public void setCholesterolMmol(Double cholesterolMmol) {
        this.cholesterolMmol = cholesterolMmol;
    }

    public Double getTriglycerideMmol() {
        return triglycerideMmol;
    }

    public void setTriglycerideMmol(Double triglycerideMmol) {
        this.triglycerideMmol = triglycerideMmol;
    }

    public Double getHdlMmol() {
        return hdlMmol;
    }

    public void setHdlMmol(Double hdlMmol) {
        this.hdlMmol = hdlMmol;
    }

    public Double getLdlMmol() {
        return ldlMmol;
    }

    public void setLdlMmol(Double ldlMmol) {
        this.ldlMmol = ldlMmol;
    }

    public Double getWbc() {
        return wbc;
    }

    public void setWbc(Double wbc) {
        this.wbc = wbc;
    }

    public Double getRbc() {
        return rbc;
    }

    public void setRbc(Double rbc) {
        this.rbc = rbc;
    }

    public Double getHgb() {
        return hgb;
    }

    public void setHgb(Double hgb) {
        this.hgb = hgb;
    }

    public Double getHct() {
        return hct;
    }

    public void setHct(Double hct) {
        this.hct = hct;
    }

    public Double getPlt() {
        return plt;
    }

    public void setPlt(Double plt) {
        this.plt = plt;
    }

    public Double getAst() {
        return ast;
    }

    public void setAst(Double ast) {
        this.ast = ast;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public Double getUre() {
        return ure;
    }

    public void setUre(Double ure) {
        this.ure = ure;
    }

    public Double getCreatinine() {
        return creatinine;
    }

    public void setCreatinine(Double creatinine) {
        this.creatinine = creatinine;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getTienSuBenh() {
        return tienSuBenh;
    }

    public void setTienSuBenh(String tienSuBenh) {
        this.tienSuBenh = tienSuBenh;
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

    public String getPhanLoaiTieuDuong() {
        return phanLoaiTieuDuong;
    }

    public void setPhanLoaiTieuDuong(String phanLoaiTieuDuong) {
        this.phanLoaiTieuDuong = phanLoaiTieuDuong;
    }

    public String getHuongXuTri() {
        return huongXuTri;
    }

    public void setHuongXuTri(String huongXuTri) {
        this.huongXuTri = huongXuTri;
    }

    public String getKhuyenNghi() {
        return khuyenNghi;
    }

    public void setKhuyenNghi(String khuyenNghi) {
        this.khuyenNghi = khuyenNghi;
    }

    public String getKhuyenNghiDieuTri() {
        return khuyenNghiDieuTri;
    }

    public void setKhuyenNghiDieuTri(String khuyenNghiDieuTri) {
        this.khuyenNghiDieuTri = khuyenNghiDieuTri;
    }

    public String getCheDoAn() {
        return cheDoAn;
    }

    public void setCheDoAn(String cheDoAn) {
        this.cheDoAn = cheDoAn;
    }

    public String getLuyenTap() {
        return luyenTap;
    }

    public void setLuyenTap(String luyenTap) {
        this.luyenTap = luyenTap;
    }

    public Integer getSoBuocChan() {
        return soBuocChan;
    }

    public void setSoBuocChan(Integer soBuocChan) {
        this.soBuocChan = soBuocChan;
    }

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getSoGioNgu() {
        return soGioNgu;
    }

    public void setSoGioNgu(Double soGioNgu) {
        this.soGioNgu = soGioNgu;
    }

    public Integer getLieuLuongInsulinUi() {
        return lieuLuongInsulinUi;
    }

    public void setLieuLuongInsulinUi(Integer lieuLuongInsulinUi) {
        this.lieuLuongInsulinUi = lieuLuongInsulinUi;
    }

    public String getLoaiInsulinTiem() {
        return loaiInsulinTiem;
    }

    public void setLoaiInsulinTiem(String loaiInsulinTiem) {
        this.loaiInsulinTiem = loaiInsulinTiem;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Boolean getChestPain() {
        return chestPain;
    }

    public void setChestPain(Boolean chestPain) {
        this.chestPain = chestPain;
    }

    public Boolean getDizziness() {
        return dizziness;
    }

    public void setDizziness(Boolean dizziness) {
        this.dizziness = dizziness;
    }

    public Boolean getFatigue() {
        return fatigue;
    }

    public void setFatigue(Boolean fatigue) {
        this.fatigue = fatigue;
    }

    public LocalDateTime getThoiGianDo() {
        return thoiGianDo;
    }

    public void setThoiGianDo(LocalDateTime thoiGianDo) {
        this.thoiGianDo = thoiGianDo;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public int getDaysSinceLastVisit() {
        return daysSinceLastVisit;
    }

    public void setDaysSinceLastVisit(int daysSinceLastVisit) {
        this.daysSinceLastVisit = daysSinceLastVisit;
    }

    // Getter Setter
}
