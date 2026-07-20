package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dto.MedicalEncounterDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MedicalRecordPdfService {

    public enum PdfExportType {
        FULL("full", "Toan bo ho so"),
        INTERNAL_MEDICINE("internal", "Benh an noi tiet"),
        BLOOD_COUNT("blood", "Xet nghiem mau tong quat"),
        BIOCHEMISTRY("biochemistry", "Sinh hoa mau"),
        ULTRASOUND("ultrasound", "Sieu am bung"),
        PRESCRIPTION("prescription", "Don thuoc");

        private final String param;
        private final String label;

        PdfExportType(String param, String label) {
            this.param = param;
            this.label = label;
        }

        public String getParam() {
            return param;
        }

        public String getLabel() {
            return label;
        }

        public static PdfExportType fromParam(String value) {
            if (value == null || value.isBlank()) {
                return FULL;
            }
            for (PdfExportType type : values()) {
                if (type.param.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return FULL;
        }
    }

    private static final DateTimeFormatter EXPORT_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateMedicalRecordPdf(MedicalEncounterDTO view, PdfExportType type)
            throws Exception {

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleFont = font(16, Font.BOLD);
        Font headerFont = font(12, Font.BOLD);
        Font normalFont = font(10, Font.NORMAL);
        Font smallFont = font(9, Font.NORMAL);
        Font footerFont = font(8, Font.ITALIC);

        addHeader(document, view, titleFont, normalFont);

        switch (type) {
            case INTERNAL_MEDICINE:
                addInternalMedicine(document, view, headerFont, normalFont);
                break;
            case BLOOD_COUNT:
                addBloodCount(document, view, headerFont, normalFont);
                break;
            case BIOCHEMISTRY:
                addBiochemistry(document, view, headerFont, normalFont);
                break;
            case ULTRASOUND:
                addUltrasound(document, view, headerFont, normalFont);
                break;
            case PRESCRIPTION:
                addPrescriptionDetail(document, view, headerFont, normalFont);
                break;
            default:
                addSectionsForEncounterType(document, view, headerFont, normalFont);
        }

        addFooter(document, view, footerFont, smallFont);

        document.close();
        return output.toByteArray();
    }

    private void addSectionsForEncounterType(
            Document document,
            MedicalEncounterDTO view,
            Font headerFont,
            Font normalFont
    ) throws DocumentException {
        String encounterType = view.resolveEncounterType();
        if ("tai_kham_noi_tiet".equalsIgnoreCase(encounterType)) {
            addInternalMedicine(document, view, headerFont, normalFont);
            addPrescriptionDetail(document, view, headerFont, normalFont);
        } else if ("mau_tong_quat".equalsIgnoreCase(encounterType)) {
            addBloodCount(document, view, headerFont, normalFont);
        } else if ("sinh_hoa_mau".equalsIgnoreCase(encounterType)) {
            addBiochemistry(document, view, headerFont, normalFont);
        }
    }

    private void addHeader(Document document, MedicalEncounterDTO view,
                           Font titleFont, Font normalFont) throws DocumentException {

        Paragraph title = new Paragraph("HO SO KHAM BENH - NOI TIET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(16);

        addRow(table, "Benh nhan:", safe(view.getPatientName()), normalFont);
        addRow(table, "Ma encounter:", safe(view.getRecordCode()), normalFont);
        addRow(table, "Ma benh nhan:", safe(view.getPatientCode()), normalFont);
        addRow(table, "Ngay kham:", safe(view.getExamDate()), normalFont);
        addRow(table, "Khoa:", safe(view.getDepartment()), normalFont);

        document.add(table);
        document.add(new Paragraph(" ", normalFont));
    }

    private void addInternalMedicine(Document document, MedicalEncounterDTO view,
                                     Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("I. Benh an tai kham noi tiet", headerFont));

        MedicalEncounterDTO.InternalMedicineSection section = view.getInternalMedicine();

        document.add(subTitle("Thong tin lam sang", normalFont));
        addFieldTable(document, section.getClinicalInfo(), normalFont);

        document.add(subTitle("Chan doan", normalFont));
        addFieldTable(document, section.getDiagnosisInfo(), normalFont);

        document.add(subTitle("Khuyen nghi", normalFont));
        addFieldTable(document, section.getRecommendationFields(), normalFont);

        document.add(subTitle("Chi so suc khoe", normalFont));
        addFieldTable(document, section.getHealthMetrics(), normalFont);

        document.add(Chunk.NEWLINE);
    }

    private void addPrescriptionDetail(Document document, MedicalEncounterDTO view,
                                       Font headerFont, Font normalFont) throws DocumentException {
        document.add(sectionTitle("II. Don thuoc", headerFont));
        for (Map<String, String> med : view.getPrescriptionDetail().getItems()) {
            document.add(new Paragraph(
                    "- " + safe(med.get("name")) + ": " + safe(med.get("dose"))
                            + " | " + safe(med.get("frequency"))
                            + " | " + safe(med.get("usage")), normalFont));
            if (med.get("note") != null && !med.get("note").isBlank()) {
                document.add(new Paragraph("  Ghi chu: " + safe(med.get("note")), normalFont));
            }
        }
        document.add(Chunk.NEWLINE);
    }

    private void addBloodCount(Document document, MedicalEncounterDTO view,
                               Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("III. Xet nghiem mau tong quat", headerFont));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);

        addHeaderCell(table, "Chi so", headerFont);
        addHeaderCell(table, "Gia tri", headerFont);
        addHeaderCell(table, "Tham chieu", headerFont);

        for (Map<String, Object> item : view.getBloodCount().getItems()) {
            addLabRow(table, item, normalFont);
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addBiochemistry(Document document, MedicalEncounterDTO view,
                                 Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("IV. Sinh hoa mau", headerFont));

        MedicalEncounterDTO.BiochemistrySection section = view.getBiochemistry();

        document.add(subTitle("Duong huyet (Core Metrics)", normalFont));
        addFieldTable(document, List.of(section.getGlucose(), section.getHba1c()), normalFont);

        document.add(subTitle("Mo mau", normalFont));
        addFieldTable(document, section.getLipidProfile(), normalFont);

        document.add(subTitle("Gan", normalFont));
        addFieldTable(document, section.getLiverEnzymes(), normalFont);

        document.add(subTitle("Than", normalFont));
        addFieldTable(document, section.getKidneyFunction(), normalFont);

        if (!section.getAlerts().isEmpty()) {
            document.add(subTitle("Canh bao", normalFont));
            for (String alert : section.getAlerts()) {
                document.add(new Paragraph("! " + safe(alert), normalFont));
            }
        }

        document.add(Chunk.NEWLINE);
    }

    private void addUltrasound(Document document, MedicalEncounterDTO view,
                               Font headerFont, Font normalFont) throws DocumentException {
        document.add(sectionTitle("V. Sieu am bung", headerFont));
        for (Map<String, String> field : view.getUltrasound().getFields()) {
            document.add(new Paragraph(
                    safe(field.get("label")) + ": " + safe(field.get("value")), normalFont));
        }
        document.add(Chunk.NEWLINE);
    }

    private void addFooter(Document document, MedicalEncounterDTO view,
                           Font footerFont, Font smallFont) throws DocumentException {

        document.add(new Paragraph(" ", smallFont));
        document.add(new Paragraph("Bac si phu trach: " + safe(view.getDoctorName()), smallFont));
        document.add(new Paragraph(
                "Thoi gian xuat: " + LocalDateTime.now().format(EXPORT_TIME), smallFont));
        document.add(new Paragraph(
                "Disclaimer: Bao cao chi mang tinh tham khao y khoa. "
                        + "Quyet dinh dieu tri can duoc bac si xac nhan.", footerFont));
    }

    private void addFieldTable(Document document, List<Map<String, Object>> items, Font font)
            throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        for (Map<String, Object> item : items) {
            if (item == null) {
                continue;
            }
            addRow(table,
                    fieldLabel(item) + ":",
                    fieldDisplayValue(item),
                    font);
        }

        document.add(table);
    }

    private void addLabRow(PdfPTable table, Map<String, Object> item, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(fieldLabel(item), font));
        PdfPCell valueCell = new PdfPCell(new Phrase(fieldDisplayValue(item), font));
        PdfPCell rangeCell = new PdfPCell(new Phrase(fieldReferenceRange(item), font));

        if (isAbnormal(item)) {
            valueCell.setBackgroundColor(new java.awt.Color(255, 237, 237));
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
        table.addCell(rangeCell);
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
        BaseFont baseFont = loadBaseFont();
        return new Font(baseFont, size, style);
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

    private boolean isAbnormal(Map<String, Object> field) {
        return field != null && Boolean.TRUE.equals(field.get("abnormal"));
    }

    private String fieldLabel(Map<String, Object> field) {
        return field == null ? "" : String.valueOf(field.getOrDefault("label", ""));
    }

    private String fieldDisplayValue(Map<String, Object> field) {
        return field == null ? "â€”" : String.valueOf(field.getOrDefault("displayValue", "â€”"));
    }

    private String fieldReferenceRange(Map<String, Object> field) {
        Object range = field == null ? null : field.get("referenceRange");
        return range == null ? "" : range.toString();
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "â€”";
        }
        return value;
    }
}
