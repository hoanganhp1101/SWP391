package com.example.diabetesmanage.service;

/**
 * Dữ liệu bệnh nhân tối thiểu từ SQL gửi cho AI — không chứa PII (tên, email, SĐT, địa chỉ).
 * Đây không phải "train model", mà là ngữ cảnh (context) để cá nhân hóa câu trả lời.
 */
public class PatientHealthContext {

    private String loaiTieuDuong;
    private String tienSuBenhTomTat;
    private String diUngTomTat;
    private Double chieuCaoCm;
    private Double canNangKg;
    private Double bmi;
    private Double duongHuyetMgdl;
    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Double hba1cPercent;
    private Double soGioNgu;
    private Integer soBuocChan;
    private Double carbsGGanNhat;

    public String getLoaiTieuDuong() { return loaiTieuDuong; }
    public void setLoaiTieuDuong(String loaiTieuDuong) { this.loaiTieuDuong = loaiTieuDuong; }

    public String getTienSuBenhTomTat() { return tienSuBenhTomTat; }
    public void setTienSuBenhTomTat(String tienSuBenhTomTat) { this.tienSuBenhTomTat = tienSuBenhTomTat; }

    public String getDiUngTomTat() { return diUngTomTat; }
    public void setDiUngTomTat(String diUngTomTat) { this.diUngTomTat = diUngTomTat; }

    public Double getChieuCaoCm() { return chieuCaoCm; }
    public void setChieuCaoCm(Double chieuCaoCm) { this.chieuCaoCm = chieuCaoCm; }

    public Double getCanNangKg() { return canNangKg; }
    public void setCanNangKg(Double canNangKg) { this.canNangKg = canNangKg; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    public Double getDuongHuyetMgdl() { return duongHuyetMgdl; }
    public void setDuongHuyetMgdl(Double duongHuyetMgdl) { this.duongHuyetMgdl = duongHuyetMgdl; }

    public Integer getHuyetApTamThu() { return huyetApTamThu; }
    public void setHuyetApTamThu(Integer huyetApTamThu) { this.huyetApTamThu = huyetApTamThu; }

    public Integer getHuyetApTamTruong() { return huyetApTamTruong; }
    public void setHuyetApTamTruong(Integer huyetApTamTruong) { this.huyetApTamTruong = huyetApTamTruong; }

    public Double getHba1cPercent() { return hba1cPercent; }
    public void setHba1cPercent(Double hba1cPercent) { this.hba1cPercent = hba1cPercent; }

    public Double getSoGioNgu() { return soGioNgu; }
    public void setSoGioNgu(Double soGioNgu) { this.soGioNgu = soGioNgu; }

    public Integer getSoBuocChan() { return soBuocChan; }
    public void setSoBuocChan(Integer soBuocChan) { this.soBuocChan = soBuocChan; }

    public Double getCarbsGGanNhat() { return carbsGGanNhat; }
    public void setCarbsGGanNhat(Double carbsGGanNhat) { this.carbsGGanNhat = carbsGGanNhat; }

    /** Các thông tin còn thiếu — AI nên hỏi thêm (đời sống / bệnh). */
    public String missingLifestyleHints() {
        StringBuilder sb = new StringBuilder();
        if (duongHuyetMgdl == null) {
            sb.append("- Chưa có đường huyết gần nhất\n");
        }
        if (canNangKg == null && bmi == null) {
            sb.append("- Chưa có cân nặng/BMI\n");
        }
        if (soGioNgu == null) {
            sb.append("- Chưa có thông tin giấc ngủ\n");
        }
        if (soBuocChan == null) {
            sb.append("- Chưa có mức vận động (bước chân)\n");
        }
        if (carbsGGanNhat == null) {
            sb.append("- Chưa có lượng carbs gần nhất\n");
        }
        if (tienSuBenhTomTat == null || tienSuBenhTomTat.isBlank()) {
            sb.append("- Chưa có tiền sử bệnh tóm tắt\n");
        }
        return sb.toString().trim();
    }

    public String toPromptBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("Loại tiểu đường: ")
                .append(loaiTieuDuong != null && !loaiTieuDuong.isBlank() ? loaiTieuDuong : "Type 2")
                .append("\n");
        if (tienSuBenhTomTat != null && !tienSuBenhTomTat.isBlank()) {
            sb.append("Tiền sử bệnh (tóm tắt): ").append(truncate(tienSuBenhTomTat, 200)).append("\n");
        }
        if (diUngTomTat != null && !diUngTomTat.isBlank()) {
            sb.append("Dị ứng (tóm tắt): ").append(truncate(diUngTomTat, 120)).append("\n");
        }
        if (chieuCaoCm != null) {
            sb.append("Chiều cao: ").append(chieuCaoCm).append(" cm\n");
        }
        if (canNangKg != null) {
            sb.append("Cân nặng gần nhất: ").append(canNangKg).append(" kg\n");
        }
        if (bmi != null) {
            sb.append("BMI gần nhất: ").append(bmi).append("\n");
        }
        if (duongHuyetMgdl != null) {
            sb.append("Đường huyết gần nhất: ").append(duongHuyetMgdl).append(" mg/dL\n");
        }
        if (huyetApTamThu != null && huyetApTamTruong != null) {
            sb.append("Huyết áp gần nhất: ").append(huyetApTamThu).append("/").append(huyetApTamTruong).append(" mmHg\n");
        }
        if (hba1cPercent != null) {
            sb.append("HbA1c gần nhất: ").append(hba1cPercent).append("%\n");
        }
        if (soGioNgu != null) {
            sb.append("Giấc ngủ gần nhất: ").append(soGioNgu).append(" giờ\n");
        }
        if (soBuocChan != null) {
            sb.append("Số bước gần nhất: ").append(soBuocChan).append("\n");
        }
        if (carbsGGanNhat != null) {
            sb.append("Carbs gần nhất: ").append(carbsGGanNhat).append(" g\n");
        }

        String missing = missingLifestyleHints();
        if (!missing.isBlank()) {
            sb.append("\n[Thông tin còn thiếu — có thể hỏi nhẹ 1 câu về đời sống/bệnh]\n");
            sb.append(missing).append("\n");
        }
        return sb.toString().trim();
    }

    private static String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }
}
