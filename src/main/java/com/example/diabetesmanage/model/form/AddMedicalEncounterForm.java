package com.example.diabetesmanage.model.form;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AddMedicalEncounterForm {

    private String patientId;

    private String ngayKham;
    private String khoaKham;

    private String trieuChung;
    private String tienSuBenh;
    private String lyDoKham;
    private String quaTrinhBenhLy;
    private String khamLamSang;
    private String chanDoanChinh;
    private String chanDoanPhu;
    private String phanLoaiTieuDuong;
    private String huongXuTri;
    private String khuyenNghiDieuTri;
    private String cheDoAn;
    private String luyenTap;

    private Double duongHuyetMgdl;
    private String thoiDiemDoDuong;
    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Integer nhipTim;
    private Double nhietDoC;
    private Integer nhipTho;
    private Double chieuCaoCm;
    private Double canNangKg;
    private Double bmi;
    private Double hba1cPercent;
    private Double cholesterolMmol;
    private Double triglycerideMmol;
    private Double carbsG;
    private String loaiInsulinTiem;
    private Integer lieuLuongInsulinUi;
    private String ghiChuSucKhoe;

    private Double labGlucoseMau;
    private Double labHba1c;
    private Double labCholesterol;
    private Double labTriglyceride;
    private Double labHdl;
    private Double labLdl;
    private Double labAst;
    private Double labAlt;
    private Double labUre;
    private Double labCreatinine;
    private String labHbsag;
    private String labAntiHcv;
    private String labNuocTieu;
    private String labGhiChu;

    private Double labWbc;
    private Double labRbc;
    private Double labHgb;
    private Double labHct;
    private Double labPlt;

    private List<MedicationFormItem> medications = new ArrayList<>();

    public static AddMedicalEncounterForm fromRequest(HttpServletRequest request) {
        AddMedicalEncounterForm form = new AddMedicalEncounterForm();
        form.setPatientId(trim(request.getParameter("patientId")));
        form.setNgayKham(trim(request.getParameter("ngayKham")));
        form.setKhoaKham(trim(request.getParameter("khoaKham")));

        form.setTrieuChung(trim(request.getParameter("trieuChung")));
        form.setTienSuBenh(trim(request.getParameter("tienSuBenh")));
        form.setLyDoKham(firstNonBlank(form.getTrieuChung(), trim(request.getParameter("lyDoKham"))));
        form.setQuaTrinhBenhLy(firstNonBlank(form.getTienSuBenh(), trim(request.getParameter("quaTrinhBenhLy"))));
        form.setKhamLamSang(trim(request.getParameter("khamLamSang")));
        form.setChanDoanChinh(trim(request.getParameter("chanDoanChinh")));
        form.setChanDoanPhu(trim(request.getParameter("chanDoanPhu")));
        form.setPhanLoaiTieuDuong(trim(request.getParameter("phanLoaiTieuDuong")));
        form.setHuongXuTri(trim(request.getParameter("huongXuTri")));
        form.setKhuyenNghiDieuTri(trim(request.getParameter("khuyenNghiDieuTri")));
        form.setCheDoAn(trim(request.getParameter("cheDoAn")));
        form.setLuyenTap(trim(request.getParameter("luyenTap")));

        form.setDuongHuyetMgdl(parseDouble(request.getParameter("duongHuyetMgdl")));
        form.setThoiDiemDoDuong(trim(request.getParameter("thoiDiemDoDuong")));
        form.setHuyetApTamThu(parseInteger(request.getParameter("huyetApTamThu")));
        form.setHuyetApTamTruong(parseInteger(request.getParameter("huyetApTamTruong")));
        form.setNhipTim(parseInteger(request.getParameter("nhipTim")));
        form.setNhietDoC(parseDouble(request.getParameter("nhietDoC")));
        form.setNhipTho(parseInteger(request.getParameter("nhipTho")));
        form.setChieuCaoCm(parseDouble(request.getParameter("chieuCaoCm")));
        form.setCanNangKg(parseDouble(request.getParameter("canNangKg")));
        form.setBmi(parseDouble(request.getParameter("bmi")));
        form.setHba1cPercent(parseDouble(request.getParameter("hba1cPercent")));
        form.setCholesterolMmol(parseDouble(request.getParameter("cholesterolMmol")));
        form.setTriglycerideMmol(parseDouble(request.getParameter("triglycerideMmol")));
        form.setCarbsG(parseDouble(request.getParameter("carbsG")));
        form.setLoaiInsulinTiem(trim(request.getParameter("loaiInsulinTiem")));
        form.setLieuLuongInsulinUi(parseInteger(request.getParameter("lieuLuongInsulinUi")));
        form.setGhiChuSucKhoe(trim(request.getParameter("ghiChuSucKhoe")));

        form.setLabGlucoseMau(parseDouble(request.getParameter("labGlucoseMau")));
        form.setLabHba1c(parseDouble(request.getParameter("labHba1c")));
        form.setLabCholesterol(parseDouble(request.getParameter("labCholesterol")));
        form.setLabTriglyceride(parseDouble(request.getParameter("labTriglyceride")));
        form.setLabHdl(parseDouble(request.getParameter("labHdl")));
        form.setLabLdl(parseDouble(request.getParameter("labLdl")));
        form.setLabAst(parseDouble(request.getParameter("labAst")));
        form.setLabAlt(parseDouble(request.getParameter("labAlt")));
        form.setLabUre(parseDouble(request.getParameter("labUre")));
        form.setLabCreatinine(parseDouble(request.getParameter("labCreatinine")));
        form.setLabHbsag(trim(request.getParameter("labHbsag")));
        form.setLabAntiHcv(trim(request.getParameter("labAntiHcv")));
        form.setLabNuocTieu(trim(request.getParameter("labNuocTieu")));
        form.setLabGhiChu(trim(request.getParameter("labGhiChu")));

        form.setLabWbc(parseDouble(request.getParameter("labWbc")));
        form.setLabRbc(parseDouble(request.getParameter("labRbc")));
        form.setLabHgb(parseDouble(request.getParameter("labHgb")));
        form.setLabHct(parseDouble(request.getParameter("labHct")));
        form.setLabPlt(parseDouble(request.getParameter("labPlt")));

        form.setMedications(parseMedications(request));
        form.calculateBmiIfNeeded();
        form.syncLabToHealthMetrics();
        return form;
    }

    private static List<MedicationFormItem> parseMedications(HttpServletRequest request) {
        List<MedicationFormItem> list = new ArrayList<>();
        String[] tenThuoc = request.getParameterValues("medTenThuoc");
        if (tenThuoc == null) {
            return list;
        }

        String[] hoatChat = defaultArray(request.getParameterValues("medHoatChat"), tenThuoc.length);
        String[] lieuLuong = defaultArray(request.getParameterValues("medLieuLuong"), tenThuoc.length);
        String[] donVi = defaultArray(request.getParameterValues("medDonVi"), tenThuoc.length);
        String[] tanSuat = defaultArray(request.getParameterValues("medTanSuat"), tenThuoc.length);
        String[] duongDung = defaultArray(request.getParameterValues("medDuongDung"), tenThuoc.length);
        String[] thoiDiem = defaultArray(request.getParameterValues("medThoiDiemUong"), tenThuoc.length);
        String[] soNgay = defaultArray(request.getParameterValues("medThoiGianDungNgay"), tenThuoc.length);
        String[] ghiChu = defaultArray(request.getParameterValues("medGhiChu"), tenThuoc.length);

        for (int i = 0; i < tenThuoc.length; i++) {
            MedicationFormItem item = new MedicationFormItem();
            item.setTenThuoc(trim(tenThuoc[i]));
            item.setHoatChat(trim(hoatChat[i]));
            item.setLieuLuong(trim(lieuLuong[i]));
            item.setDonVi(trim(donVi[i]));
            item.setTanSuat(trim(tanSuat[i]));
            item.setDuongDung(trim(duongDung[i]));
            item.setThoiDiemUong(firstNonBlank(trim(duongDung[i]), trim(thoiDiem[i])));
            item.setThoiGianDungNgay(parseInteger(soNgay[i]));
            item.setGhiChu(trim(ghiChu[i]));
            if (item.hasContent()) {
                list.add(item);
            }
        }
        return list;
    }

    private static String[] defaultArray(String[] values, int length) {
        if (values == null) {
            return new String[length];
        }
        return values;
    }

    public void calculateBmiIfNeeded() {
        if (bmi != null || chieuCaoCm == null || canNangKg == null || chieuCaoCm <= 0) {
            return;
        }
        double heightM = chieuCaoCm / 100.0;
        bmi = Math.round((canNangKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    public void syncLabToHealthMetrics() {
        if (duongHuyetMgdl == null && labGlucoseMau != null) {
            duongHuyetMgdl = Math.round(labGlucoseMau * 18.0182 * 10.0) / 10.0;
        }
        if (hba1cPercent == null && labHba1c != null) {
            hba1cPercent = labHba1c;
        }
        if (cholesterolMmol == null && labCholesterol != null) {
            cholesterolMmol = labCholesterol;
        }
        if (triglycerideMmol == null && labTriglyceride != null) {
            triglycerideMmol = labTriglyceride;
        }
    }

    public LocalDateTime resolveNgayKham() {
        if (ngayKham != null && !ngayKham.isBlank()) {
            try {
                return LocalDate.parse(ngayKham, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.now());
            } catch (Exception ignored) {
                // fall through
            }
        }
        return LocalDateTime.now();
    }

    public String resolveKhoaKham() {
        if (khoaKham == null || khoaKham.isBlank()) {
            return "Khoa Nội tiết";
        }
        return khoaKham;
    }

    public boolean hasBloodCountData() {
        return labWbc != null || labRbc != null || labHgb != null || labHct != null || labPlt != null;
    }

    public boolean hasPrescriptionData() {
        return hasMedications()
                || (khuyenNghiDieuTri != null && !khuyenNghiDieuTri.isBlank())
                || (cheDoAn != null && !cheDoAn.isBlank())
                || (luyenTap != null && !luyenTap.isBlank());
    }

    public boolean hasLabData() {
        return labGlucoseMau != null || labHba1c != null || labCholesterol != null
                || labTriglyceride != null || labHdl != null || labLdl != null
                || labAst != null || labAlt != null || labUre != null || labCreatinine != null
                || hasBloodCountData()
                || (labHbsag != null && !labHbsag.isBlank())
                || (labAntiHcv != null && !labAntiHcv.isBlank())
                || (labNuocTieu != null && !labNuocTieu.isBlank())
                || (labGhiChu != null && !labGhiChu.isBlank());
    }

    public boolean hasMedications() {
        return medications.stream().anyMatch(MedicationFormItem::hasContent);
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

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    public String getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(String ngayKham) {
        this.ngayKham = ngayKham;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getKhoaKham() {
        return khoaKham;
    }

    public void setKhoaKham(String khoaKham) {
        this.khoaKham = khoaKham;
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

    public String getPhanLoaiTieuDuong() {
        return phanLoaiTieuDuong;
    }

    public void setPhanLoaiTieuDuong(String phanLoaiTieuDuong) {
        this.phanLoaiTieuDuong = phanLoaiTieuDuong;
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

    public Double getLabWbc() {
        return labWbc;
    }

    public void setLabWbc(Double labWbc) {
        this.labWbc = labWbc;
    }

    public Double getLabRbc() {
        return labRbc;
    }

    public void setLabRbc(Double labRbc) {
        this.labRbc = labRbc;
    }

    public Double getLabHgb() {
        return labHgb;
    }

    public void setLabHgb(Double labHgb) {
        this.labHgb = labHgb;
    }

    public Double getLabHct() {
        return labHct;
    }

    public void setLabHct(Double labHct) {
        this.labHct = labHct;
    }

    public Double getLabPlt() {
        return labPlt;
    }

    public void setLabPlt(Double labPlt) {
        this.labPlt = labPlt;
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

    public Double getChieuCaoCm() {
        return chieuCaoCm;
    }

    public void setChieuCaoCm(Double chieuCaoCm) {
        this.chieuCaoCm = chieuCaoCm;
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

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public String getLoaiInsulinTiem() {
        return loaiInsulinTiem;
    }

    public void setLoaiInsulinTiem(String loaiInsulinTiem) {
        this.loaiInsulinTiem = loaiInsulinTiem;
    }

    public Integer getLieuLuongInsulinUi() {
        return lieuLuongInsulinUi;
    }

    public void setLieuLuongInsulinUi(Integer lieuLuongInsulinUi) {
        this.lieuLuongInsulinUi = lieuLuongInsulinUi;
    }

    public String getGhiChuSucKhoe() {
        return ghiChuSucKhoe;
    }

    public void setGhiChuSucKhoe(String ghiChuSucKhoe) {
        this.ghiChuSucKhoe = ghiChuSucKhoe;
    }

    public Double getLabGlucoseMau() {
        return labGlucoseMau;
    }

    public void setLabGlucoseMau(Double labGlucoseMau) {
        this.labGlucoseMau = labGlucoseMau;
    }

    public Double getLabHba1c() {
        return labHba1c;
    }

    public void setLabHba1c(Double labHba1c) {
        this.labHba1c = labHba1c;
    }

    public Double getLabCholesterol() {
        return labCholesterol;
    }

    public void setLabCholesterol(Double labCholesterol) {
        this.labCholesterol = labCholesterol;
    }

    public Double getLabTriglyceride() {
        return labTriglyceride;
    }

    public void setLabTriglyceride(Double labTriglyceride) {
        this.labTriglyceride = labTriglyceride;
    }

    public Double getLabHdl() {
        return labHdl;
    }

    public void setLabHdl(Double labHdl) {
        this.labHdl = labHdl;
    }

    public Double getLabLdl() {
        return labLdl;
    }

    public void setLabLdl(Double labLdl) {
        this.labLdl = labLdl;
    }

    public Double getLabAst() {
        return labAst;
    }

    public void setLabAst(Double labAst) {
        this.labAst = labAst;
    }

    public Double getLabAlt() {
        return labAlt;
    }

    public void setLabAlt(Double labAlt) {
        this.labAlt = labAlt;
    }

    public Double getLabUre() {
        return labUre;
    }

    public void setLabUre(Double labUre) {
        this.labUre = labUre;
    }

    public Double getLabCreatinine() {
        return labCreatinine;
    }

    public void setLabCreatinine(Double labCreatinine) {
        this.labCreatinine = labCreatinine;
    }

    public String getLabHbsag() {
        return labHbsag;
    }

    public void setLabHbsag(String labHbsag) {
        this.labHbsag = labHbsag;
    }

    public String getLabAntiHcv() {
        return labAntiHcv;
    }

    public void setLabAntiHcv(String labAntiHcv) {
        this.labAntiHcv = labAntiHcv;
    }

    public String getLabNuocTieu() {
        return labNuocTieu;
    }

    public void setLabNuocTieu(String labNuocTieu) {
        this.labNuocTieu = labNuocTieu;
    }

    public String getLabGhiChu() {
        return labGhiChu;
    }

    public void setLabGhiChu(String labGhiChu) {
        this.labGhiChu = labGhiChu;
    }

    public List<MedicationFormItem> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationFormItem> medications) {
        this.medications = medications;
    }

    public static class MedicationFormItem {

        private String tenThuoc;
        private String hoatChat;
        private String lieuLuong;
        private String donVi;
        private String tanSuat;
        private String duongDung;
        private String thoiDiemUong;
        private Integer thoiGianDungNgay;
        private String ghiChu;

        public boolean hasContent() {
            return tenThuoc != null && !tenThuoc.isBlank();
        }

        public boolean isValid() {
            return hasContent()
                    && lieuLuong != null && !lieuLuong.isBlank()
                    && tanSuat != null && !tanSuat.isBlank();
        }

        public String getTenThuoc() {
            return tenThuoc;
        }

        public void setTenThuoc(String tenThuoc) {
            this.tenThuoc = tenThuoc;
        }

        public String getHoatChat() {
            return hoatChat;
        }

        public void setHoatChat(String hoatChat) {
            this.hoatChat = hoatChat;
        }

        public String getLieuLuong() {
            return lieuLuong;
        }

        public void setLieuLuong(String lieuLuong) {
            this.lieuLuong = lieuLuong;
        }

        public String getDonVi() {
            return donVi;
        }

        public void setDonVi(String donVi) {
            this.donVi = donVi;
        }

        public String getTanSuat() {
            return tanSuat;
        }

        public void setTanSuat(String tanSuat) {
            this.tanSuat = tanSuat;
        }

        public String getDuongDung() {
            return duongDung;
        }

        public void setDuongDung(String duongDung) {
            this.duongDung = duongDung;
        }

        public String getThoiDiemUong() {
            return thoiDiemUong;
        }

        public void setThoiDiemUong(String thoiDiemUong) {
            this.thoiDiemUong = thoiDiemUong;
        }

        public Integer getThoiGianDungNgay() {
            return thoiGianDungNgay;
        }

        public void setThoiGianDungNgay(Integer thoiGianDungNgay) {
            this.thoiGianDungNgay = thoiGianDungNgay;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }
    }
}
