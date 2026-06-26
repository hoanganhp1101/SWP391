package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.medical.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordViewService {

    public enum PdfExportType {
        FULL("full", "Toan bo ho so"),
        INTERNAL_MEDICINE("internal", "Benh an noi tiet"),
        BLOOD_COUNT("blood", "Xet nghiem mau tong quat"),
        BIOCHEMISTRY("biochemistry", "Sinh hoa mau");

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

    private static final double UMOL_TO_MGDL = 1.0 / 88.4;

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EXPORT_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MedicalRecordDetailView loadDetailViewByRecordId(String recordId, String scopeDoctorId) {
        HealthRecord record = healthRecordDAO.getHealthRecordRecordById(recordId, scopeDoctorId);
        if (record == null) {
            return null;
        }
        return buildDetailView(record);
    }

    public MedicalRecordDetailView loadDetailViewByPatientId(String patientId, String scopeDoctorId) {
        HealthRecord record = healthRecordDAO.getLatestHealthRecordByPatientId(patientId, scopeDoctorId);
        if (record == null) {
            return null;
        }
        return buildDetailView(record);
    }

    public HealthRecord getRecordById(String recordId, String scopeDoctorId) {
        return healthRecordDAO.getHealthRecordRecordById(recordId, scopeDoctorId);
    }

    public HealthRecord getLatestRecordByPatientId(String patientId, String scopeDoctorId) {
        return healthRecordDAO.getLatestHealthRecordByPatientId(patientId, scopeDoctorId);
    }

    public byte[] generateMedicalRecordPdf(MedicalRecordDetailView view, PdfExportType type)
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
            default:
                addInternalMedicine(document, view, headerFont, normalFont);
                addBloodCount(document, view, headerFont, normalFont);
                addBiochemistry(document, view, headerFont, normalFont);
        }

        addFooter(document, view, footerFont, smallFont);

        document.close();
        return output.toByteArray();
    }

    private MedicalRecordDetailView buildDetailView(HealthRecord record) {
        String patientId = record.getPatient() != null ? record.getPatient().getId() : null;

        MedicalEncounter encounter = null;
        if (patientId != null && record.getThoiGianDo() != null) {
            encounter = encounterDAO.getClosestByPatientAndTime(patientId, record.getThoiGianDo());
        }
        if (encounter == null && patientId != null) {
            encounter = encounterDAO.getLatestByPatientId(patientId);
        }

        LabResult lab = null;
        if (encounter != null && encounter.getId() != null) {
            lab = labResultDAO.getByEncounterId(encounter.getId());
        }
        if (lab == null && patientId != null) {
            lab = labResultDAO.getLatestByPatientId(patientId);
        }

        List<Map<String, String>> medications = encounter != null && encounter.getId() != null
                ? encounterDAO.getMedicationsByEncounterId(encounter.getId())
                : (patientId != null ? encounterDAO.getMedicationsByPatientId(patientId) : List.of());

        List<String> recommendations = encounter != null && encounter.getId() != null
                ? encounterDAO.getRecommendationsByEncounterId(encounter.getId())
                : (patientId != null ? encounterDAO.getRecommendationsByPatientId(patientId) : List.of());

        MedicalRecordDetailView view = buildView(record, lab, encounter, medications, recommendations);
        if (encounter != null && encounter.getBacSiId() != null) {
            String doctorName = encounterDAO.getDoctorNameById(encounter.getBacSiId());
            if (doctorName != null && !doctorName.isBlank()) {
                view.setDoctorName(doctorName);
            }
        }
        return view;
    }

    public MedicalRecordDetailView buildView(HealthRecord record) {
        return buildView(record, null, null, List.of(), List.of());
    }

    public MedicalRecordDetailView buildView(
            HealthRecord record,
            LabResult lab,
            MedicalEncounter encounter,
            List<Map<String, String>> medications,
            List<String> recommendations
    ) {
        MedicalRecordDetailView view = new MedicalRecordDetailView();

        if (record == null) {
            return view;
        }

        view.setRecordId(record.getId());
        view.setRecordCode(record.getHealthRecordId());

        if (record.getPatient() != null) {
            view.setPatientCode(record.getPatient().getPatientCode());
            if (record.getPatient().getUser() != null) {
                view.setPatientName(record.getPatient().getUser().getHoTen());
            }
        }

        if (encounter != null && encounter.getNgayKham() != null) {
            view.setExamDate(encounter.getNgayKham().format(DATE_FMT));
        } else if (record.getThoiGianDo() != null) {
            view.setExamDate(record.getThoiGianDo().format(DATE_FMT));
        }

        view.setInternalMedicine(buildInternalMedicine(record, encounter, medications, recommendations));
        view.setBloodCount(buildBloodCount(lab));
        view.setBiochemistry(buildBiochemistry(record, lab));

        return view;
    }

    private MedicalRecordDetailView.InternalMedicineSection buildInternalMedicine(
            HealthRecord record,
            MedicalEncounter encounter,
            List<Map<String, String>> medications,
            List<String> recommendations
    ) {
        MedicalRecordDetailView.InternalMedicineSection section =
                new MedicalRecordDetailView.InternalMedicineSection();
        Patient patient = record.getPatient();

        String symptoms = firstNonBlank(
                encounter != null ? encounter.getLyDoKham() : null,
                encounter != null ? encounter.getQuaTrinhBenhLy() : null,
                record.getGhiChu(),
                "Không ghi nhận triệu chứng đặc biệt"
        );

        section.getClinicalInfo().add(textField("Triệu chứng", symptoms));
        if (encounter != null && encounter.getKhamLamSang() != null && !encounter.getKhamLamSang().isBlank()) {
            section.getClinicalInfo().add(textField("Khám lâm sàng", encounter.getKhamLamSang()));
        }
        if (encounter != null && encounter.getQuaTrinhBenhLy() != null && !encounter.getQuaTrinhBenhLy().isBlank()) {
            section.getClinicalInfo().add(textField("Tiền sử bệnh", encounter.getQuaTrinhBenhLy()));
        }
        section.getClinicalInfo().add(bmi(record.getBmi()));
        section.getClinicalInfo().add(bloodPressure(
                record.getHuyetApTamThu(),
                record.getHuyetApTamTruong()
        ));

        String diagnosis = firstNonBlank(
                encounter != null ? encounter.getChanDoanChinh() : null,
                record.getChanDoanChinh(),
                patient != null ? patient.getLoaiTieuDuong() : null,
                "Theo dõi đái tháo đường"
        );
        section.getDiagnosisInfo().add(textField("Chẩn đoán chính", diagnosis));

        String secondary = encounter != null ? encounter.getChanDoanPhu() : null;
        if (secondary != null && !secondary.isBlank()) {
            section.getDiagnosisInfo().add(textField("Chẩn đoán phụ", secondary));
        }

        section.getDiagnosisInfo().add(textField(
                "Phân loại tiểu đường",
                patient != null ? patient.getLoaiTieuDuong() : "—"
        ));

        if (encounter != null && encounter.getHuongXuTri() != null && !encounter.getHuongXuTri().isBlank()) {
            section.getDiagnosisInfo().add(textField("Hướng xử trí", encounter.getHuongXuTri()));
        }

        section.setMedications(buildMedications(record, medications));
        section.setRecommendations(buildRecommendations(record, recommendations));

        return section;
    }

    private List<Map<String, String>> buildMedications(
            HealthRecord record, List<Map<String, String>> prescriptions) {
        if (prescriptions != null && !prescriptions.isEmpty()) {
            return prescriptions;
        }

        List<Map<String, String>> meds = new ArrayList<>();

        if (record.getLoaiInsulinTiem() != null && !record.getLoaiInsulinTiem().isBlank()) {
            String dose = record.getLieuLuongInsulinUi() != null
                    ? record.getLieuLuongInsulinUi() + " UI"
                    : "—";
            meds.add(medication(record.getLoaiInsulinTiem(), dose));
        } else if (record.getLieuLuongInsulinUi() != null && record.getLieuLuongInsulinUi() > 0) {
            meds.add(medication("Insulin", record.getLieuLuongInsulinUi() + " UI"));
        }

        if (meds.isEmpty()) {
            meds.add(medication(
                    "Chưa ghi nhận đơn thuốc", "—", "Cập nhật tại tái khám"));
        }

        return meds;
    }

    private List<String> buildRecommendations(HealthRecord record, List<String> fromPrescription) {
        if (fromPrescription != null && !fromPrescription.isEmpty()) {
            return fromPrescription;
        }

        if (record.getKhuyenNghi() != null && !record.getKhuyenNghi().isBlank()) {
            return Arrays.asList(record.getKhuyenNghi().split("\\n|;"));
        }

        List<String> recs = new ArrayList<>();
        recs.add("Duy trì chế độ ăn kiểm soát carbohydrate và đường huyết.");
        recs.add("Tập thể dục đều đặn 30 phút/ngày, ít nhất 5 ngày/tuần.");

        if (record.getSoBuocChan() != null && record.getSoBuocChan() < 5000) {
            recs.add("Tăng hoạt động thể chất — mục tiêu ≥ 7.000 bước/ngày.");
        }
        if (record.getSoGioNgu() != null && record.getSoGioNgu() < 7) {
            recs.add("Ngủ đủ 7-8 giờ/ngày để ổn định đường huyết.");
        }
        if (record.getCarbsG() != null && record.getCarbsG() > 200) {
            recs.add("Giảm lượng carbohydrate trong bữa ăn hàng ngày.");
        }

        return recs;
    }

    private MedicalRecordDetailView.BloodCountSection buildBloodCount(LabResult lab) {
        MedicalRecordDetailView.BloodCountSection section =
                new MedicalRecordDetailView.BloodCountSection();
        section.getItems().add(lab("WBC", lab != null ? lab.getWbc() : null, "G/L", "4.0-10.0", 4.0, 10.0));
        section.getItems().add(lab("RBC", lab != null ? lab.getRbc() : null, "T/L", "4.0-5.5", 4.0, 5.5));
        section.getItems().add(lab("HGB", lab != null ? lab.getHgb() : null, "g/dL", "12-16", 12, 16));
        section.getItems().add(lab("HCT", lab != null ? lab.getHct() : null, "%", "36-46", 36, 46));
        section.getItems().add(lab("PLT", lab != null ? lab.getPlt() : null, "G/L", "150-400", 150, 400));
        return section;
    }

    private MedicalRecordDetailView.BiochemistrySection buildBiochemistry(
            HealthRecord record, LabResult lab) {
        MedicalRecordDetailView.BiochemistrySection section =
                new MedicalRecordDetailView.BiochemistrySection();

        Double glucoseMgdl = record.getDuongHuyetMgdl();
        if (glucoseMgdl == null && lab != null) {
            glucoseMgdl = lab.getGlucoseMgdl();
        }

        Double hba1c = record.getHba1cPercent();
        if (hba1c == null && lab != null) {
            hba1c = lab.getHba1c();
        }

        section.setGlucose(glucose(glucoseMgdl, record.getThoiDiemDoDuong()));
        section.setHba1c(hba1c(hba1c));

        Double cholesterol = firstNonNull(
                record.getCholesterolMmol(),
                lab != null ? lab.getCholesterolTp() : null
        );
        Double triglyceride = firstNonNull(
                record.getTriglycerideMmol(),
                lab != null ? lab.getTriglyceride() : null
        );

        section.getLipidProfile().add(lab(
                "Cholesterol", cholesterol, "mmol/L", "< 5.2", 0, 5.2));
        section.getLipidProfile().add(lab(
                "Triglyceride", triglyceride, "mmol/L", "< 1.7", 0, 1.7));
        section.getLipidProfile().add(lab(
                "HDL", lab != null ? lab.getHdlC() : record.getHdlMmol(), "mmol/L", "> 1.0", 1.0, 99));
        section.getLipidProfile().add(lab(
                "LDL", lab != null ? lab.getLdlC() : record.getLdlMmol(), "mmol/L", "< 3.4", 0, 3.4));

        Double ast = lab != null ? lab.getAst() : record.getAst();
        Double alt = lab != null ? lab.getAlt() : record.getAlt();
        section.getLiverEnzymes().add(lab("AST", ast, "U/L", "< 40", 0, 40));
        section.getLiverEnzymes().add(lab("ALT", alt, "U/L", "< 41", 0, 41));

        Double creatinineMgdl = null;
        if (lab != null && lab.getCreatinine() != null) {
            creatinineMgdl = lab.getCreatinine() * UMOL_TO_MGDL;
        } else if (record.getCreatinine() != null) {
            creatinineMgdl = record.getCreatinine();
        }

        section.getKidneyFunction().add(lab(
                "Creatinine", creatinineMgdl, "mg/dL", "0.6-1.2", 0.6, 1.2));

        if (lab != null && lab.getUre() != null) {
            section.getKidneyFunction().add(lab(
                    "Ure", lab.getUre(), "mmol/L", "2.5-7.5", 2.5, 7.5));
        }

        String alert = diabetesAlert(glucoseMgdl, hba1c);
        if (alert != null) {
            section.getAlerts().add(alert);
        }

        if (isAbnormal(section.getGlucose())) {
            section.getAlerts().add("Đường huyết bất thường — cần can thiệp điều trị");
        }

        return section;
    }

    private Map<String, Object> glucose(Double value, String timing) {
        Map<String, Object> item = field("Glucose", format(value), "mg/dL", "70-99 (lúc đói)");
        if (value == null) {
            return item;
        }
        item.put("highlightLevel", "CORE");
        if (value < 70) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 126) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 100) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        if (timing != null && !timing.isBlank()) {
            item.put("label", "Glucose (" + timing + ")");
        }
        return item;
    }

    private Map<String, Object> hba1c(Double value) {
        Map<String, Object> item = field("HbA1c", format(value), "%", "< 5.7");
        item.put("highlightLevel", "CORE");
        if (value == null) {
            return item;
        }
        if (value >= 6.5) {
            item.put("abnormal", true);
            item.put("highlightLevel", "CRITICAL");
        } else if (value >= 5.7) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> bmi(Double value) {
        Map<String, Object> item = field("BMI", format(value), "kg/m²", "18.5-24.9");
        if (value != null && value >= 30) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> bloodPressure(Integer systolic, Integer diastolic) {
        String value = "—";
        if (systolic != null && diastolic != null) {
            value = systolic + "/" + diastolic;
        }
        Map<String, Object> item = field("Huyết áp", value, "mmHg", "< 120/80");
        if (systolic != null && systolic >= 140) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        if (diastolic != null && diastolic >= 90) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> lab(String label, Double value, String unit, String range,
                                   double low, double high) {
        Map<String, Object> item = field(label, format(value), unit, range);
        if (value != null && (value < low || value > high)) {
            item.put("abnormal", true);
            item.put("highlightLevel", "WARNING");
        }
        return item;
    }

    private Map<String, Object> textField(String label, String value) {
        return field(label, value == null || value.isBlank() ? "—" : value, null, null);
    }

    public static Map<String, String> medication(String name, String dose) {
        return medication(name, dose, null);
    }

    public static Map<String, String> medication(String name, String dose, String note) {
        Map<String, String> med = new LinkedHashMap<>();
        med.put("name", name);
        med.put("dose", dose);
        if (note != null && !note.isBlank()) {
            med.put("note", note);
        }
        return med;
    }

    public static boolean isAbnormal(Map<String, Object> field) {
        return field != null && Boolean.TRUE.equals(field.get("abnormal"));
    }

    public static String fieldLabel(Map<String, Object> field) {
        return field == null ? "" : String.valueOf(field.getOrDefault("label", ""));
    }

    public static String fieldDisplayValue(Map<String, Object> field) {
        return field == null ? "—" : String.valueOf(field.getOrDefault("displayValue", "—"));
    }

    public static String fieldReferenceRange(Map<String, Object> field) {
        Object range = field == null ? null : field.get("referenceRange");
        return range == null ? "" : range.toString();
    }

    private String diabetesAlert(Double glucoseMgdl, Double hba1c) {
        if (hba1c != null && hba1c >= 6.5) {
            return "HbA1c ≥ 6.5% — tiêu chí chẩn đoán Đái tháo đường";
        }
        if (glucoseMgdl != null && glucoseMgdl >= 126) {
            return "Glucose ≥ 126 mg/dL — đường huyết bất thường";
        }
        if (hba1c != null && hba1c >= 5.7) {
            return "HbA1c 5.7-6.4% — tiền đái tháo đường";
        }
        return null;
    }

    private String diabetesAlert(HealthRecord record) {
        return diabetesAlert(record.getDuongHuyetMgdl(), record.getHba1cPercent());
    }

    private Map<String, Object> field(String label, String value, String unit, String range) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("unit", unit);
        item.put("referenceRange", range);
        item.put("abnormal", false);
        item.put("highlightLevel", "NORMAL");
        item.put("displayValue", buildDisplayValue(value, unit));
        return item;
    }

    private String buildDisplayValue(String value, String unit) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        if (unit == null || unit.isBlank()) {
            return value;
        }
        return value + " " + unit;
    }

    private String format(Double value) {
        if (value == null) {
            return "—";
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value.doubleValue());
        }
        return String.format("%.1f", value);
    }

    private void addHeader(Document document, MedicalRecordDetailView view,
                           Font titleFont, Font normalFont) throws DocumentException {

        Paragraph title = new Paragraph("HO SO SUC KHOE - NOI TIET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(16);

        addRow(table, "Benh nhan:", safe(view.getPatientName()), normalFont);
        addRow(table, "Ma ho so:", safe(view.getRecordCode()), normalFont);
        addRow(table, "Ma benh nhan:", safe(view.getPatientCode()), normalFont);
        addRow(table, "Ngay kham:", safe(view.getExamDate()), normalFont);
        addRow(table, "Khoa:", safe(view.getDepartment()), normalFont);

        document.add(table);
        document.add(new Paragraph(" ", normalFont));
    }

    private void addInternalMedicine(Document document, MedicalRecordDetailView view,
                                     Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("I. Benh an tai kham noi tiet", headerFont));

        MedicalRecordDetailView.InternalMedicineSection section = view.getInternalMedicine();

        document.add(subTitle("Thong tin lam sang", normalFont));
        addFieldTable(document, section.getClinicalInfo(), normalFont);

        document.add(subTitle("Chan doan", normalFont));
        addFieldTable(document, section.getDiagnosisInfo(), normalFont);

        document.add(subTitle("Don thuoc", normalFont));
        for (Map<String, String> med : section.getMedications()) {
            document.add(new Paragraph(
                    "- " + safe(med.get("name")) + ": " + safe(med.get("dose")), normalFont));
        }

        document.add(subTitle("Khuyen nghi", normalFont));
        for (String rec : section.getRecommendations()) {
            document.add(new Paragraph("- " + safe(rec), normalFont));
        }

        document.add(Chunk.NEWLINE);
    }

    private void addBloodCount(Document document, MedicalRecordDetailView view,
                               Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("II. Xet nghiem mau tong quat", headerFont));

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

    private void addBiochemistry(Document document, MedicalRecordDetailView view,
                                 Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("III. Sinh hoa mau", headerFont));

        MedicalRecordDetailView.BiochemistrySection section = view.getBiochemistry();

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

    private void addFooter(Document document, MedicalRecordDetailView view,
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
        PdfPCell rangeCell = new PdfPCell(new Phrase(
                fieldReferenceRange(item), font));

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

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value;
    }

    private Double firstNonNull(Double first, Double second) {
        return first != null ? first : second;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "—";
    }
}
