package com.example.diabetesmanage.model.form;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HealthRecordUpdateForm {

    private static final DateTimeFormatter DATETIME_LOCAL =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private String recordId;
    private String patientId;
    private String thoiGianDoLocal;

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

    private Integer soBuocChan;
    private Double carbsG;
    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private Boolean chestPain;
    private Boolean dizziness;
    private Boolean fatigue;

    private String ghiChu;

    public static HealthRecordUpdateForm fromRequest(HttpServletRequest request) {
        HealthRecordUpdateForm form = new HealthRecordUpdateForm();
        form.setRecordId(trim(request.getParameter("recordId")));
        form.setPatientId(trim(request.getParameter("patientId")));
        form.setThoiGianDoLocal(trim(request.getParameter("thoiGianDo")));

        form.setDuongHuyetMgdl(parseDouble(request.getParameter("duongHuyetMgdl")));
        form.setThoiDiemDoDuong(trim(request.getParameter("thoiDiemDoDuong")));
        form.setHuyetApTamThu(parseInteger(request.getParameter("huyetApTamThu")));
        form.setHuyetApTamTruong(parseInteger(request.getParameter("huyetApTamTruong")));
        form.setNhipTim(parseInteger(request.getParameter("nhipTim")));
        form.setNhietDoC(parseDouble(request.getParameter("nhietDoC")));
        form.setNhipTho(parseInteger(request.getParameter("nhipTho")));
        form.setCanNangKg(parseDouble(request.getParameter("canNangKg")));
        form.setBmi(parseDouble(request.getParameter("bmi")));
        form.setHba1cPercent(parseDouble(request.getParameter("hba1cPercent")));
        form.setCholesterolMmol(parseDouble(request.getParameter("cholesterolMmol")));
        form.setTriglycerideMmol(parseDouble(request.getParameter("triglycerideMmol")));

        form.setSoBuocChan(parseInteger(request.getParameter("soBuocChan")));
        form.setCarbsG(parseDouble(request.getParameter("carbsG")));
        form.setSoGioNgu(parseDouble(request.getParameter("soGioNgu")));

        form.setLieuLuongInsulinUi(parseInteger(request.getParameter("lieuLuongInsulinUi")));
        form.setLoaiInsulinTiem(trim(request.getParameter("loaiInsulinTiem")));

        form.setChestPain(parseTriState(request.getParameter("chestPain")));
        form.setDizziness(parseTriState(request.getParameter("dizziness")));
        form.setFatigue(parseTriState(request.getParameter("fatigue")));

        form.setGhiChu(trim(request.getParameter("ghiChu")));
        return form;
    }

    public static HealthRecordUpdateForm fromHealthRecord(
            com.example.diabetesmanage.model.HealthRecord hr, String patientId) {
        HealthRecordUpdateForm form = new HealthRecordUpdateForm();
        form.setRecordId(hr.getId());
        form.setPatientId(patientId);
        if (hr.getThoiGianDo() != null) {
            form.setThoiGianDoLocal(hr.getThoiGianDo().format(DATETIME_LOCAL));
        }
        form.setDuongHuyetMgdl(hr.getDuongHuyetMgdl());
        form.setThoiDiemDoDuong(hr.getThoiDiemDoDuong());
        form.setHuyetApTamThu(hr.getHuyetApTamThu());
        form.setHuyetApTamTruong(hr.getHuyetApTamTruong());
        form.setNhipTim(hr.getNhipTim());
        form.setNhietDoC(hr.getNhietDoC());
        form.setNhipTho(hr.getNhipTho());
        form.setCanNangKg(hr.getCanNangKg());
        form.setBmi(hr.getBmi());
        form.setHba1cPercent(hr.getHba1cPercent());
        form.setCholesterolMmol(hr.getCholesterolMmol());
        form.setTriglycerideMmol(hr.getTriglycerideMmol());
        form.setSoBuocChan(hr.getSoBuocChan());
        form.setCarbsG(hr.getCarbsG());
        form.setSoGioNgu(hr.getSoGioNgu());
        form.setLieuLuongInsulinUi(hr.getLieuLuongInsulinUi());
        form.setLoaiInsulinTiem(hr.getLoaiInsulinTiem());
        form.setChestPain(hr.getChestPain());
        form.setDizziness(hr.getDizziness());
        form.setFatigue(hr.getFatigue());
        form.setGhiChu(hr.getGhiChu());
        return form;
    }

    public LocalDateTime resolveThoiGianDo() {
        if (thoiGianDoLocal == null || thoiGianDoLocal.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(thoiGianDoLocal, DATETIME_LOCAL);
    }

    private static Boolean parseTriState(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value.replace(",", "."));
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getThoiGianDoLocal() {
        return thoiGianDoLocal;
    }

    public void setThoiGianDoLocal(String thoiGianDoLocal) {
        this.thoiGianDoLocal = thoiGianDoLocal;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
