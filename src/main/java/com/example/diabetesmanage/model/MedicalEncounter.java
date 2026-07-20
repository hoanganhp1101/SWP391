package com.example.diabetesmanage.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicalEncounter {

    private static final DateTimeFormatter HISTORY_DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    private Double duongHuyetMgdl;
    private Double hba1cPercent;

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

    public String getNgayKhamDisplay() {
        return ngayKham == null ? "—" : ngayKham.format(HISTORY_DATETIME_FMT);
    }

    public String getNgayTaoDisplay() {
        if (ngayTao != null) {
            return ngayTao.format(HISTORY_DATETIME_FMT);
        }
        return getNgayKhamDisplay();
    }

    public String getPatientDisplayName() {
        return patientName != null ? patientName : patientCode;
    }

    public String getDoctorDisplayName() {
        return doctorName != null ? doctorName : "—";
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

    public Double getDuongHuyetMgdl() {
        return duongHuyetMgdl;
    }

    public void setDuongHuyetMgdl(Double duongHuyetMgdl) {
        this.duongHuyetMgdl = duongHuyetMgdl;
    }

    public Double getHba1cPercent() {
        return hba1cPercent;
    }

    public void setHba1cPercent(Double hba1cPercent) {
        this.hba1cPercent = hba1cPercent;
    }

    /** Nhãn ngắn cho bảng lịch sử khám. */
    public String getShortEncounterTypeLabel() {
        if (loaiEncounter == null || loaiEncounter.isBlank()) {
            return "Không xác định";
        }
        switch (loaiEncounter.trim().toLowerCase()) {
            case "tai_kham_noi_tiet":
            case "internal_examination":
            case "noi_tiet":
            case "kham_noi_tiet":
                return "Khám Nội tiết";
            case "mau_tong_quat":
            case "general_blood_test":
            case "blood_test":
            case "cbc":
                return "Xét nghiệm CBC";
            case "sinh_hoa_mau":
            case "biochemistry_test":
            case "biochemistry":
            case "sinh_hoa":
                return "Sinh hóa máu";
            default:
                return getEncounterTypeLabel();
        }
    }

    /** Chẩn đoán hiển thị trên lịch sử — bỏ nhãn loại hồ sơ placeholder. */
    public String getHistoryDiagnosisDisplay() {
        if (chanDoanChinh != null && !chanDoanChinh.isBlank()
                && !isEncounterTypePlaceholder(chanDoanChinh)) {
            return chanDoanChinh.trim();
        }
        return null;
    }

    private boolean isEncounterTypePlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return "Bệnh án tái khám Nội tiết".equalsIgnoreCase(trimmed)
                || "Kết quả xét nghiệm máu tổng quát".equalsIgnoreCase(trimmed)
                || "Kết quả sinh hóa máu".equalsIgnoreCase(trimmed);
    }

    public String getLoaiEncounter() {
        return loaiEncounter;
    }

    public void setLoaiEncounter(String loaiEncounter) {
        this.loaiEncounter = loaiEncounter;
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
    public String getEncounterTypeLabel() {

        if (loaiEncounter == null || loaiEncounter.isBlank()) {
            return "Không xác định";
        }

        switch (loaiEncounter.trim().toLowerCase()) {

            case "tai_kham_noi_tiet":
            case "internal_examination":
            case "noi_tiet":
            case "kham_noi_tiet":
                return "Bệnh án tái khám Nội tiết";

            case "mau_tong_quat":
            case "general_blood_test":
            case "blood_test":
            case "cbc":
                return "Kết quả xét nghiệm máu tổng quát";

            case "sinh_hoa_mau":
            case "biochemistry_test":
            case "biochemistry":
            case "sinh_hoa":
                return "Kết quả sinh hóa máu";

            default:
                return loaiEncounter;
        }
    }
}
