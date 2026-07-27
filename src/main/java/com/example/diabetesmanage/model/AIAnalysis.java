package com.example.diabetesmanage.model;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AIAnalysis {
    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String id;
    private String patientId;
    private String healthRecordId;
    private double diemNguyCo;
    private String mucCanhBao; // 'an_toan','trung_binh','cao','nguy_hiem'
    private Double doTinCay;
    private String phanTichChiTiet;
    private String yeuToNguyCo;    // JSON string
    private String khuyenNghi;     // JSON string
    private String duLieuDauVao;   // JSON string
    private String modelVersion;
    private Timestamp thoiGianPhanTich;
    private Integer tokensSuDung;
    /** Workflow bác sĩ: chua_xem | da_xem | da_ap_dung | bo_qua */
    private String trangThai;
    private String ghiChuBs;
    private String xuLyBoi;
    private String hoTenBenhNhan;
    private String patientCode;

    public AIAnalysis() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getHealthRecordId() { return healthRecordId; }
    public void setHealthRecordId(String healthRecordId) { this.healthRecordId = healthRecordId; }

    public double getDiemNguyCo() { return diemNguyCo; }
    public void setDiemNguyCo(double diemNguyCo) { this.diemNguyCo = diemNguyCo; }

    public String getMucCanhBao() { return mucCanhBao; }
    public void setMucCanhBao(String mucCanhBao) { this.mucCanhBao = mucCanhBao; }

    public Double getDoTinCay() { return doTinCay; }
    public void setDoTinCay(Double doTinCay) { this.doTinCay = doTinCay; }

    public String getPhanTichChiTiet() { return phanTichChiTiet; }
    public void setPhanTichChiTiet(String phanTichChiTiet) { this.phanTichChiTiet = phanTichChiTiet; }

    public String getYeuToNguyCo() { return yeuToNguyCo; }
    public void setYeuToNguyCo(String yeuToNguyCo) { this.yeuToNguyCo = yeuToNguyCo; }

    public String getKhuyenNghi() { return khuyenNghi; }
    public void setKhuyenNghi(String khuyenNghi) { this.khuyenNghi = khuyenNghi; }

    public String getDuLieuDauVao() { return duLieuDauVao; }
    public void setDuLieuDauVao(String duLieuDauVao) { this.duLieuDauVao = duLieuDauVao; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Timestamp getThoiGianPhanTich() { return thoiGianPhanTich; }
    public void setThoiGianPhanTich(Timestamp thoiGianPhanTich) { this.thoiGianPhanTich = thoiGianPhanTich; }

    public Integer getTokensSuDung() { return tokensSuDung; }
    public void setTokensSuDung(Integer tokensSuDung) { this.tokensSuDung = tokensSuDung; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChuBs() { return ghiChuBs; }
    public void setGhiChuBs(String ghiChuBs) { this.ghiChuBs = ghiChuBs; }

    public String getXuLyBoi() { return xuLyBoi; }
    public void setXuLyBoi(String xuLyBoi) { this.xuLyBoi = xuLyBoi; }

    public String getHoTenBenhNhan() { return hoTenBenhNhan; }
    public void setHoTenBenhNhan(String hoTenBenhNhan) { this.hoTenBenhNhan = hoTenBenhNhan; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getMaBenhNhanDisplay() {
        if (patientCode != null && !patientCode.isBlank()) {
            return patientCode.trim();
        }
        return "—";
    }

    /** Nhãn mức cảnh báo tiếng Việt cho JSP. */
    public String getMucCanhBaoLabel() {
        String level = mucCanhBao == null ? "" : mucCanhBao.trim().toLowerCase();
        return switch (level) {
            case "nguy_hiem" -> "Nguy hiểm";
            case "cao" -> "Cao";
            case "trung_binh" -> "Trung bình";
            case "an_toan", "thap", "low" -> "An toàn";
            default -> level.isBlank() ? "Chưa rõ" : mucCanhBao;
        };
    }

    /** CSS class badge: level-high | level-medium | level-low */
    public String getMucCanhBaoCss() {
        String level = mucCanhBao == null ? "" : mucCanhBao.trim().toLowerCase();
        return switch (level) {
            case "nguy_hiem", "cao" -> "level-high";
            case "trung_binh" -> "level-medium";
            default -> "level-low";
        };
    }

    public String getTrangThaiLabel() {
        String state = trangThai == null || trangThai.isBlank() ? "chua_xem" : trangThai.trim();
        return switch (state) {
            case "da_xem" -> "Đã xem";
            case "da_ap_dung" -> "Đã áp dụng";
            case "bo_qua" -> "Bỏ qua";
            case "chua_xem" -> "Chưa xem";
            default -> state;
        };
    }

    public String getThoiGianPhanTichDisplay() {
        if (thoiGianPhanTich == null) {
            return "—";
        }
        return DISPLAY_TIME.format(
                thoiGianPhanTich.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    /** Tóm tắt dữ liệu đầu vào, tránh dump JSON dài gây lỗi/UI vỡ. */
    public String getDuLieuDauVaoDisplay() {
        if (duLieuDauVao == null || duLieuDauVao.isBlank()) {
            return "Không có dữ liệu đầu vào.";
        }
        String text = duLieuDauVao.trim();
        if (text.length() > 800) {
            return text.substring(0, 800) + "…";
        }
        return text;
    }
}
