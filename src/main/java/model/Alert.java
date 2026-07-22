package model;

import java.sql.Timestamp; // ĐÃ SỬA LỖI TẠI ĐÂY (Phải dùng java.sql.Timestamp)
import java.util.UUID;

public class Alert {
    private UUID id;
    private UUID patientId;
    private UUID aiAnalysisId;
    private String loaiCanhBao;
    private String mucDo;
    private String tieuDe;
    private String noiDung;
    private boolean daDocBn;
    private boolean daDocBs;
    private UUID xuLyBoi;
    private String ghiChuXuLy;
    private Timestamp thoiGianTao;
    private Timestamp thoiGianXuLy;

    // --- CÁC TRƯỜNG MỞ RỘNG (Dùng để hiển thị ở JSP thông qua lệnh JOIN) ---
    private String hoTenBenhNhan;
    private String soDienThoaiBenhNhan;
    private Double duongHuyet;

    public Alert() {
    }

    // Constructor nguyên bản (Giữ nguyên)
    public Alert(UUID id, UUID patientId, UUID aiAnalysisId, String loaiCanhBao, String mucDo, String tieuDe, String noiDung, boolean daDocBn, boolean daDocBs, UUID xuLyBoi, String ghiChuXuLy, Timestamp thoiGianTao, Timestamp thoiGianXuLy) {
        this.id = id;
        this.patientId = patientId;
        this.aiAnalysisId = aiAnalysisId;
        this.loaiCanhBao = loaiCanhBao;
        this.mucDo = mucDo;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.daDocBn = daDocBn;
        this.daDocBs = daDocBs;
        this.xuLyBoi = xuLyBoi;
        this.ghiChuXuLy = ghiChuXuLy;
        this.thoiGianTao = thoiGianTao;
        this.thoiGianXuLy = thoiGianXuLy;
    }

    // --- CÁC GETTER/SETTER GỐC ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getAiAnalysisId() { return aiAnalysisId; }
    public void setAiAnalysisId(UUID aiAnalysisId) { this.aiAnalysisId = aiAnalysisId; }
    public String getLoaiCanhBao() { return loaiCanhBao; }
    public void setLoaiCanhBao(String loaiCanhBao) { this.loaiCanhBao = loaiCanhBao; }
    public String getMucDo() { return mucDo; }
    public void setMucDo(String mucDo) { this.mucDo = mucDo; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public boolean isDaDocBn() { return daDocBn; }
    public void setDaDocBn(boolean daDocBn) { this.daDocBn = daDocBn; }
    public boolean isDaDocBs() { return daDocBs; }
    public void setDaDocBs(boolean daDocBs) { this.daDocBs = daDocBs; }
    public UUID getXuLyBoi() { return xuLyBoi; }
    public void setXuLyBoi(UUID xuLyBoi) { this.xuLyBoi = xuLyBoi; }
    public String getGhiChuXuLy() { return ghiChuXuLy; }
    public void setGhiChuXuLy(String ghiChuXuLy) { this.ghiChuXuLy = ghiChuXuLy; }
    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }
    public Timestamp getThoiGianXuLy() { return thoiGianXuLy; }
    public void setThoiGianXuLy(Timestamp thoiGianXuLy) { this.thoiGianXuLy = thoiGianXuLy; }
    public String getMucDoCss() {
        if (mucDo == null) {
            return "severity-medium";
        }

        String normalized = mucDo.toLowerCase();
        if (normalized.contains("nguy") || normalized.contains("danger")) {
            return "severity-danger";
        }
        if (normalized.contains("cao") || normalized.contains("high")) {
            return "severity-high";
        }
        return "severity-medium";
    }
    public String getTrangThaiXuLy() {
        if (!daDocBs) {
            return "Chưa xem";
        }
        if (thoiGianXuLy != null) {
            return "Đã giải quyết";
        }
        return "Đang xử lý";
    }
    public String getTrangThaiCss() {
        if (!daDocBs) {
            return "status-unread";
        }
        if (thoiGianXuLy != null) {
            return "status-resolved";
        }
        return "status-processing";
    }

    // --- CÁC GETTER/SETTER MỞ RỘNG (THÊM MỚI) ---
    public String getHoTenBenhNhan() { return hoTenBenhNhan; }
    public void setHoTenBenhNhan(String hoTenBenhNhan) { this.hoTenBenhNhan = hoTenBenhNhan; }
    public String getSoDienThoaiBenhNhan() { return soDienThoaiBenhNhan; }
    public void setSoDienThoaiBenhNhan(String soDienThoaiBenhNhan) { this.soDienThoaiBenhNhan = soDienThoaiBenhNhan; }
    public Double getDuongHuyet() { return duongHuyet; }
    public void setDuongHuyet(Double duongHuyet) { this.duongHuyet = duongHuyet; }
}
