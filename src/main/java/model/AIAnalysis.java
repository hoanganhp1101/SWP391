package model;

import java.sql.Timestamp;
import java.util.UUID;

public class AIAnalysis {
    private UUID id;
    private UUID patientId;
    private UUID healthRecordId;

    private Double diemNguyCo;
    private String mucCanhBao;
    private Double doTinCay;

    private String phanTichChiTiet;
    private String yeuToNguyCo;
    private String khuyenNghi;
    private String duLieuDauVao;

    private String modelVersion;
    private Timestamp thoiGianPhanTich;
    private Integer tokensSuDung;

    /** chua_xem | da_xem | da_ap_dung | bo_qua */
    private String trangThai;
    private String ghiChuBs;
    private UUID xuLyBoi;

    // JOIN hiển thị
    private String hoTenBenhNhan;

    public AIAnalysis() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(UUID healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public Double getDiemNguyCo() {
        return diemNguyCo;
    }

    public void setDiemNguyCo(Double diemNguyCo) {
        this.diemNguyCo = diemNguyCo;
    }

    public String getMucCanhBao() {
        return mucCanhBao;
    }

    public void setMucCanhBao(String mucCanhBao) {
        this.mucCanhBao = mucCanhBao;
    }

    public Double getDoTinCay() {
        return doTinCay;
    }

    public void setDoTinCay(Double doTinCay) {
        this.doTinCay = doTinCay;
    }

    public String getPhanTichChiTiet() {
        return phanTichChiTiet;
    }

    public void setPhanTichChiTiet(String phanTichChiTiet) {
        this.phanTichChiTiet = phanTichChiTiet;
    }

    public String getYeuToNguyCo() {
        return yeuToNguyCo;
    }

    public void setYeuToNguyCo(String yeuToNguyCo) {
        this.yeuToNguyCo = yeuToNguyCo;
    }

    public String getKhuyenNghi() {
        return khuyenNghi;
    }

    public void setKhuyenNghi(String khuyenNghi) {
        this.khuyenNghi = khuyenNghi;
    }

    public String getDuLieuDauVao() {
        return duLieuDauVao;
    }

    public void setDuLieuDauVao(String duLieuDauVao) {
        this.duLieuDauVao = duLieuDauVao;
    }

    /** Ẩn mã [SYNC:…] — chỉ hiện phần metrics tóm tắt cho bác sĩ. */
    public String getDuLieuDauVaoDisplay() {
        if (duLieuDauVao == null || duLieuDauVao.isBlank()) {
            return "—";
        }
        String cleaned = duLieuDauVao.replaceAll("(?i)\\[SYNC:[^\\]]+\\]", "").trim();
        cleaned = cleaned.replaceAll("^;\\s*", "").replaceAll("\\s*;\\s*", " · ").trim();
        return cleaned.isEmpty() ? "—" : cleaned;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Timestamp getThoiGianPhanTich() {
        return thoiGianPhanTich;
    }

    /** Hiển thị đến giây, bỏ phần thập phân (vd .7633333). */
    public String getThoiGianPhanTichDisplay() {
        if (thoiGianPhanTich == null) {
            return "";
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(thoiGianPhanTich);
    }

    public void setThoiGianPhanTich(Timestamp thoiGianPhanTich) {
        this.thoiGianPhanTich = thoiGianPhanTich;
    }

    public Integer getTokensSuDung() {
        return tokensSuDung;
    }

    public void setTokensSuDung(Integer tokensSuDung) {
        this.tokensSuDung = tokensSuDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChuBs() {
        return ghiChuBs;
    }

    public void setGhiChuBs(String ghiChuBs) {
        this.ghiChuBs = ghiChuBs;
    }

    public UUID getXuLyBoi() {
        return xuLyBoi;
    }

    public void setXuLyBoi(UUID xuLyBoi) {
        this.xuLyBoi = xuLyBoi;
    }

    public String getHoTenBenhNhan() {
        return hoTenBenhNhan;
    }

    public void setHoTenBenhNhan(String hoTenBenhNhan) {
        this.hoTenBenhNhan = hoTenBenhNhan;
    }

    public String getTrangThaiLabel() {
        if (trangThai == null) {
            return "Chưa xem";
        }
        return switch (trangThai) {
            case "da_xem" -> "Đã xem";
            case "da_ap_dung" -> "Đã áp dụng";
            case "bo_qua" -> "Bỏ qua";
            default -> "Chưa xem";
        };
    }

    public String getMucCanhBaoLabel() {
        if (mucCanhBao == null || mucCanhBao.isBlank()) {
            return "Trung bình";
        }
        String m = mucCanhBao.trim().toLowerCase();
        if (m.contains("nguy") || "nguy_hiem".equals(m)) {
            return "Nguy hiểm";
        }
        if ("cao".equals(m) || m.contains("high")) {
            return "Cao";
        }
        if (m.contains("thap") || m.contains("thấp") || "low".equals(m)) {
            return "Thấp";
        }
        if ("trung_binh".equals(m) || m.contains("trung") || m.contains("medium")) {
            return "Trung bình";
        }
        return mucCanhBao;
    }

    public String getMucCanhBaoCss() {
        if (mucCanhBao == null) {
            return "level-medium";
        }
        String m = mucCanhBao.toLowerCase();
        if (m.contains("cao") || m.contains("nguy") || m.contains("high")) {
            return "level-high";
        }
        if (m.contains("thap") || m.contains("thấp") || m.contains("low")) {
            return "level-low";
        }
        return "level-medium";
    }

    public String getKhuyenNghiShort() {
        if (khuyenNghi == null || khuyenNghi.isBlank()) {
            return "";
        }
        String text = khuyenNghi.trim().replace('\n', ' ');
        return text.length() <= 120 ? text : text.substring(0, 117) + "...";
    }
}
