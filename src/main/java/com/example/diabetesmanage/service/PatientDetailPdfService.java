package com.example.diabetesmanage.service;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDetailPdfService {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(Patient patient, HealthRecord healthRecord,
                           List<MedicalEncounter> history) throws Exception {
        return generate(patient, healthRecord, history, null, null);
    }

    public byte[] generate(Patient patient, HealthRecord healthRecord,
                           List<MedicalEncounter> history,
                           LocalDate fromDate, LocalDate toDate) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleFont = font(16, Font.BOLD);
        Font headerFont = font(12, Font.BOLD);
        Font normalFont = font(10, Font.NORMAL);
        Font smallFont = font(9, Font.NORMAL);
        Font footerFont = font(8, Font.ITALIC);

        Paragraph title = new Paragraph("CHI TIET BENH NHAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        if (patient != null) {
            addPatientSection(document, patient, headerFont, normalFont);
        }
        if (healthRecord != null) {
            addHealthRecordSection(document, healthRecord, headerFont, normalFont);
        } else {
            document.add(sectionTitle("Ho so suc khoe", headerFont));
            document.add(new Paragraph("Chua co du lieu", normalFont));
            document.add(Chunk.NEWLINE);
        }
        boolean filtered = fromDate != null && toDate != null;
        if (history != null && (!history.isEmpty() || filtered)) {
            addHistorySection(document, history, headerFont, normalFont, fromDate, toDate);
        }

        document.add(new Paragraph("Thoi gian xuat: " + LocalDateTime.now().format(DATE_TIME), smallFont));
        document.add(new Paragraph(
                "Disclaimer: Bao cao chi mang tinh tham khao y khoa.", footerFont));

        document.close();
        return output.toByteArray();
    }

    private void addPatientSection(Document document, Patient patient,
                                   Font headerFont, Font normalFont) throws DocumentException {
        document.add(sectionTitle("Thong tin benh nhan", headerFont));
        PdfPTable table = twoColTable();
        addRow(table, "Ma benh nhan:", safe(patient.getPatientCode()), normalFont);
        if (patient.getUser() != null) {
            addRow(table, "Ho va ten:", safe(patient.getUser().getHoTen()), normalFont);
            addRow(table, "So dien thoai:", safe(patient.getUser().getSoDienThoai()), normalFont);
            addRow(table, "Email:", safe(patient.getUser().getEmail()), normalFont);
        }
        addRow(table, "Ngay sinh:", safe(patient.getNgaySinh()), normalFont);
        addRow(table, "Tuoi:", safe(patient.getTuoi()), normalFont);
        addRow(table, "Gioi tinh:", safe(patient.getGioiTinh()), normalFont);
        addRow(table, "Dia chi:", safe(patient.getDiaChi()), normalFont);
        addRow(table, "Loai tieu duong:", safe(patient.getLoaiTieuDuong()), normalFont);
        addRow(table, "Tien su benh:", safe(patient.getTienSuBenh()), normalFont);
        addRow(table, "Nhom mau:", safe(patient.getNhomMau()), normalFont);
        addRow(table, "Bao hiem y te:", safe(patient.getBaoHiemYTe()), normalFont);
        addRow(table, "Di ung:", safe(patient.getDiUng()), normalFont);
        addRow(table, "Ngay chan doan tieu duong:", safe(patient.getNgayChanDoanTieuDuong()), normalFont);
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addHealthRecordSection(Document document, HealthRecord hr,
                                        Font headerFont, Font normalFont) throws DocumentException {
        document.add(sectionTitle("Ho so suc khoe", headerFont));

        document.add(subTitle("Kham noi tiet & dieu tri", normalFont));
        addField(document, "Trieu chung", hr.getTrieuChung(), normalFont);
        addField(document, "Tien su benh", hr.getTienSuBenh(), normalFont);
        addField(document, "Kham lam sang", hr.getKhamLamSang(), normalFont);
        addPair(document, normalFont,
                "Chan doan chinh", hr.getChanDoanChinh(),
                "Chan doan phu", hr.getChanDoanPhu());
        addPair(document, normalFont,
                "Phan loai tieu duong", hr.getPhanLoaiTieuDuong(),
                "Huong xu tri", hr.getHuongXuTri());
        addField(document, "Khuyen nghi dieu tri",
                firstNonBlank(hr.getKhuyenNghiDieuTri(), hr.getKhuyenNghi()), normalFont);
        addPair(document, normalFont, "Che do an", hr.getCheDoAn(), "Luyen tap", hr.getLuyenTap());

        document.add(subTitle("Chi so sinh ton", normalFont));
        addPair(document, normalFont,
                "Chieu cao (cm)", fmt(hr.getChieuCaoCm()),
                "Can nang (kg)", fmt(hr.getCanNangKg()));
        addPair(document, normalFont,
                "BMI", fmt(hr.getBmi()),
                "Huyet ap", formatBloodPressure(hr));
        addPair(document, normalFont,
                "Nhip tim", fmt(hr.getNhipTim()),
                "Nhiet do", fmt(hr.getNhietDoC()));
        addField(document, "Nhip tho", fmt(hr.getNhipTho()), normalFont);

        document.add(subTitle("Xet nghiem & chi so mau", normalFont));
        addPair(document, normalFont,
                "Duong huyet (mg/dL)", fmt(hr.getDuongHuyetMgdl()),
                "HbA1c (%)", fmt(hr.getHba1cPercent()));
        addPair(document, normalFont,
                "Cholesterol", fmt(hr.getCholesterolMmol()),
                "Triglyceride", fmt(hr.getTriglycerideMmol()));
        addPair(document, normalFont, "HDL", fmt(hr.getHdlMmol()), "LDL", fmt(hr.getLdlMmol()));
        addPair(document, normalFont, "WBC", fmt(hr.getWbc()), "RBC", fmt(hr.getRbc()));
        addPair(document, normalFont, "HGB", fmt(hr.getHgb()), "HCT", fmt(hr.getHct()));
        addPair(document, normalFont, "PLT", fmt(hr.getPlt()), "AST", fmt(hr.getAst()));
        addPair(document, normalFont, "ALT", fmt(hr.getAlt()), "Ure", fmt(hr.getUre()));
        addField(document, "Creatinine", fmt(hr.getCreatinine()), normalFont);

        document.add(subTitle("Che do sinh hoat", normalFont));
        addPair(document, normalFont,
                "So buoc chan", fmt(hr.getSoBuocChan()),
                "Carbohydrate (g)", fmt(hr.getCarbsG()));
        addField(document, "So gio ngu", fmt(hr.getSoGioNgu()), normalFont);

        document.add(subTitle("Dieu tri", normalFont));
        addPair(document, normalFont,
                "Lieu insulin (UI)", fmt(hr.getLieuLuongInsulinUi()),
                "Loai insulin", safe(hr.getLoaiInsulinTiem()));

        document.add(subTitle("Trieu chung", normalFont));
        addPair(document, normalFont,
                "Dau nguc", boolLabel(hr.getChestPainBoolean()),
                "Chong mat", boolLabel(hr.getDizzinessBoolean()));
        addField(document, "Met moi", boolLabel(hr.getFatigueBoolean()), normalFont);
        document.add(Chunk.NEWLINE);
    }

    private void addHistorySection(Document document, List<MedicalEncounter> history,
                                   Font headerFont, Font normalFont,
                                   LocalDate fromDate, LocalDate toDate) throws DocumentException {
        if (fromDate != null && toDate != null) {
            document.add(sectionTitle(
                    "Lich su kham benh tu " + fromDate.format(DATE_ONLY)
                            + " den " + toDate.format(DATE_ONLY),
                    headerFont));
        } else {
            document.add(sectionTitle("Lich su kham benh toan bo", headerFont));
        }
        int count = history == null ? 0 : history.size();
        document.add(new Paragraph("Tong so lan kham: " + count, normalFont));
        if (history == null || history.isEmpty()) {
            document.add(new Paragraph("Chua co du lieu", normalFont));
            document.add(Chunk.NEWLINE);
            return;
        }
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2.5f, 2f, 2.5f, 1.5f, 1.5f});
        addHeaderCell(table, "Ngay kham", headerFont);
        addHeaderCell(table, "Bac si", headerFont);
        addHeaderCell(table, "Loai ho so", headerFont);
        addHeaderCell(table, "Chan doan", headerFont);
        addHeaderCell(table, "Duong huyet", headerFont);
        addHeaderCell(table, "HbA1c", headerFont);
        for (MedicalEncounter enc : history) {
            table.addCell(cell(formatDate(enc.getNgayKham()), normalFont));
            table.addCell(cell(safe(enc.getDoctorName()), normalFont));
            table.addCell(cell(safe(enc.getShortEncounterTypeLabel()), normalFont));
            table.addCell(cell(safe(enc.getHistoryDiagnosisDisplay()), normalFont));
            table.addCell(cell(enc.getDuongHuyetMgdl() != null ? fmt(enc.getDuongHuyetMgdl()) : "-", normalFont));
            table.addCell(cell(enc.getHba1cPercent() != null ? fmt(enc.getHba1cPercent()) : "-", normalFont));
        }
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addField(Document document, String label, String value, Font font)
            throws DocumentException {
        document.add(new Paragraph(label + ": " + display(value), font));
    }

    private void addPair(Document document, Font font,
                         String label1, Object value1, String label2, Object value2)
            throws DocumentException {
        document.add(new Paragraph(
                label1 + ": " + display(value1) + "    |    " + label2 + ": " + display(value2),
                font));
    }

    private PdfPTable twoColTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    private void addRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new java.awt.Color(240, 244, 255));
        table.addCell(cell);
    }

    private PdfPCell cell(String text, Font font) {
        return new PdfPCell(new Phrase(text, font));
    }

    private Paragraph sectionTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(8);
        return p;
    }

    private Paragraph subTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private Font font(int size, int style) throws Exception {
        return new Font(loadBaseFont(), size, style);
    }

    private BaseFont loadBaseFont() throws Exception {
        String[] paths = {
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/times.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }

    private String formatBloodPressure(HealthRecord hr) {
        if (hr.getHuyetApTamThu() == null && hr.getHuyetApTamTruong() == null) {
            return display(null);
        }
        return safe(hr.getHuyetApTamThu()) + " / " + safe(hr.getHuyetApTamTruong());
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_ONLY);
    }

    private String boolLabel(Boolean value) {
        if (value == null) {
            return display(null);
        }
        return Boolean.TRUE.equals(value) ? "Co" : "Khong";
    }

    private String fmt(Object value) {
        return value == null ? display(null) : String.valueOf(value);
    }

    private String display(Object value) {
        if (value == null) {
            return "Chua co du lieu";
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "Chua co du lieu" : text;
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
