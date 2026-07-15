package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.EncounterClinicalJson;
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

    private static final double UMOL_TO_MGDL = 1.0 / 88.4;

    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EXPORT_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EncounterDetail loadDetailViewByEncounterId(String encounterId, String scopeDoctorId) {
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            encounter = encounterDAO.getEncounterById(encounterId, null);
        }
        if (encounter == null) {
            return null;
        }
        return buildDetailViewFromEncounter(encounter, scopeDoctorId);
    }

    public EncounterDetail loadDetailViewByPatientId(String patientId, String scopeDoctorId) {
        MedicalEncounter encounter = encounterDAO.getLatestByPatientId(patientId, scopeDoctorId);
        if (encounter == null) {
            return null;
        }
        return buildDetailViewFromEncounter(encounter, scopeDoctorId);
    }

    public MedicalEncounter getLatestEncounterByPatientId(String patientId, String scopeDoctorId) {
        return encounterDAO.getLatestByPatientId(patientId, scopeDoctorId);
    }

    public byte[] generateMedicalRecordPdf(EncounterDetail view, PdfExportType type)
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

    private EncounterDetail buildDetailViewFromEncounter(
            MedicalEncounter encounter, String scopeDoctorId) {
        Patient patient = patientDAO.getPatientById(encounter.getPatientId(), scopeDoctorId);
        LabResult lab = labResultDAO.getByEncounterId(encounter.getId());
        Map<String, String> prescriptionAdvice =
                encounterDAO.getPrescriptionAdviceByEncounterId(encounter.getId());
        List<Map<String, String>> prescriptionItems =
                encounterDAO.getMedicationDetailsByEncounterId(encounter.getId());

        EncounterDetail view = new EncounterDetail();
        view.setRecordId(encounter.getId());
        view.setRecordCode(encounter.getDisplayCode());

        if (patient != null) {
            view.setPatientCode(patient.getPatientCode());
            if (patient.getUser() != null) {
                view.setPatientName(patient.getUser().getHoTen());
            }
        } else {
            view.setPatientCode(encounter.getPatientCode());
            view.setPatientName(encounter.getPatientName());
        }

        if (encounter.getNgayKham() != null) {
            view.setExamDate(encounter.getNgayKham().format(DATE_FMT));
        }

        EncounterType encounterType = resolveEncounterType(encounter);
        view.setEncounterType(encounterType.getCode());
        view.setEncounterTypeLabel(encounterType.getLabel());

        if (encounter.getDoctorName() != null && !encounter.getDoctorName().isBlank()) {
            view.setDoctorName(encounter.getDoctorName());
        } else if (encounter.getBacSiId() != null) {
            String doctorName = encounterDAO.getDoctorNameById(encounter.getBacSiId());
            if (doctorName != null && !doctorName.isBlank()) {
                view.setDoctorName(doctorName);
            }
        }

        String khoaKham = EncounterClinicalJson.parseString(encounter.getKhamLamSang(), "khoa_kham");
        if (khoaKham != null && !khoaKham.isBlank()) {
            view.setDepartment(khoaKham);
        }

        view.setInternalMedicine(buildInternalMedicineFromEncounter(
                encounter, patient, prescriptionAdvice));
        if (encounterType.isTaiKhamNoiTiet()) {
            view.setPrescriptionDetail(buildPrescriptionDetail(prescriptionItems));
        } else {
            view.setPrescriptionDetail(new EncounterDetail.PrescriptionDetailSection());
        }
        if (encounterType.isMauTongQuat()) {
            view.setBloodCount(buildBloodCount(lab));
        } else {
            view.setBloodCount(new EncounterDetail.BloodCountSection());
        }
        if (encounterType.isSinhHoaMau()) {
            view.setBiochemistry(buildBiochemistryFromLab(lab));
        } else {
            view.setBiochemistry(new EncounterDetail.BiochemistrySection());
        }
        view.setUltrasound(new EncounterDetail.UltrasoundSection());
        return view;
    }

    private void addSectionsForEncounterType(
            Document document,
            EncounterDetail view,
            Font headerFont,
            Font normalFont
    ) throws DocumentException {
        EncounterType type = view.resolveEncounterType();
        if (type.isTaiKhamNoiTiet()) {
            addInternalMedicine(document, view, headerFont, normalFont);
            addPrescriptionDetail(document, view, headerFont, normalFont);
        } else if (type.isMauTongQuat()) {
            addBloodCount(document, view, headerFont, normalFont);
        } else if (type.isSinhHoaMau()) {
            addBiochemistry(document, view, headerFont, normalFont);
        }
    }

    private EncounterType resolveEncounterType(MedicalEncounter encounter) {
        if (encounter.getLoaiEncounter() != null && !encounter.getLoaiEncounter().isBlank()) {
            return EncounterType.fromCode(encounter.getLoaiEncounter());
        }
        String json = encounter.getKhamLamSang();
        String fromJson = EncounterClinicalJson.parseString(json, "loai_encounter");
        if (fromJson != null && !fromJson.isBlank()) {
            return EncounterType.fromCode(fromJson);
        }
        return encounter.getEncounterType();
    }

    private EncounterDetail.InternalMedicineSection buildInternalMedicineFromEncounter(
            MedicalEncounter encounter,
            Patient patient,
            Map<String, String> prescriptionAdvice
    ) {
        EncounterDetail.InternalMedicineSection section =
                new EncounterDetail.InternalMedicineSection();
        String json = encounter.getKhamLamSang();

        String trieuChung = firstNonBlank(
                EncounterClinicalJson.parseString(json, "trieu_chung"),
                encounter.getLyDoKham()
        );
        section.getClinicalInfo().add(textField(
                "Triệu chứng", trieuChung != null ? trieuChung : "—"));

        String tienSu = firstNonBlank(encounter.getQuaTrinhBenhLy());
        section.getClinicalInfo().add(textField(
                "Tiền sử bệnh", tienSu != null ? tienSu : "—"));

        String khamLamSang = EncounterClinicalJson.parseString(json, "noi_dung");
        section.getClinicalInfo().add(textField(
                "Khám lâm sàng", khamLamSang != null ? khamLamSang : "—"));

        Double height = EncounterClinicalJson.parseDouble(json, "chieu_cao_cm");
        section.getHealthMetrics().add(field(
                "Chiều cao", height != null ? format(height) : "—", "cm", null));

        Double weight = EncounterClinicalJson.parseDouble(json, "can_nang_kg");
        section.getHealthMetrics().add(field(
                "Cân nặng", weight != null ? format(weight) : "—", "kg", null));

        Double bmi = EncounterClinicalJson.parseDouble(json, "bmi");
        if (bmi != null) {
            section.getHealthMetrics().add(bmi(bmi));
        } else {
            section.getHealthMetrics().add(field("BMI", "—", "kg/m²", "18.5-24.9"));
        }

        Integer systolic = EncounterClinicalJson.parseInteger(json, "huyet_ap_tam_thu");
        Integer diastolic = EncounterClinicalJson.parseInteger(json, "huyet_ap_tam_truong");
        if (systolic != null || diastolic != null) {
            section.getHealthMetrics().add(bloodPressure(systolic, diastolic));
        } else {
            section.getHealthMetrics().add(field("Huyết áp", "—", "mmHg", "< 120/80"));
        }

        Integer heartRate = EncounterClinicalJson.parseInteger(json, "nhip_tim");
        section.getHealthMetrics().add(field(
                "Nhịp tim",
                heartRate != null ? String.valueOf(heartRate) : "—",
                "bpm", "60-100"));

        Double temperature = EncounterClinicalJson.parseDouble(json, "nhiet_do_c");
        section.getHealthMetrics().add(field(
                "Nhiệt độ",
                temperature != null ? format(temperature) : "—",
                "°C", "36.0-37.5"));

        Integer respiratoryRate = EncounterClinicalJson.parseInteger(json, "nhip_tho");
        section.getHealthMetrics().add(field(
                "Nhịp thở",
                respiratoryRate != null ? String.valueOf(respiratoryRate) : "—",
                "lần/phút", "12-20"));

        String diagnosis = encounter.getChanDoanChinh();
        section.getDiagnosisInfo().add(textField(
                "Chẩn đoán chính",
                diagnosis != null && !diagnosis.isBlank() ? diagnosis : "—"
        ));
        section.getDiagnosisInfo().add(textField(
                "Chẩn đoán phụ",
                encounter.getChanDoanPhu() != null && !encounter.getChanDoanPhu().isBlank()
                        ? encounter.getChanDoanPhu() : "—"
        ));
        section.getDiagnosisInfo().add(textField(
                "Phân loại tiểu đường",
                patient != null && patient.getLoaiTieuDuong() != null
                        ? patient.getLoaiTieuDuong() : "—"
        ));
        section.getDiagnosisInfo().add(textField(
                "Hướng xử trí",
                encounter.getHuongXuTri() != null && !encounter.getHuongXuTri().isBlank()
                        ? encounter.getHuongXuTri() : "—"
        ));

        section.getRecommendationFields().add(textField(
                "Khuyến nghị điều trị",
                prescriptionAdvice != null ? prescriptionAdvice.get("huong_dieu_tri") : null
        ));
        section.getRecommendationFields().add(textField(
                "Chế độ ăn",
                prescriptionAdvice != null ? prescriptionAdvice.get("che_do_an") : null
        ));
        section.getRecommendationFields().add(textField(
                "Luyện tập",
                prescriptionAdvice != null ? prescriptionAdvice.get("luyen_tap") : null
        ));

        List<String> recommendations = new ArrayList<>();
        if (prescriptionAdvice != null) {
            appendRecommendation(recommendations, prescriptionAdvice.get("huong_dieu_tri"));
            appendRecommendation(recommendations, prescriptionAdvice.get("che_do_an"));
            appendRecommendation(recommendations, prescriptionAdvice.get("luyen_tap"));
        }
        section.setMedications(List.of());
        section.setRecommendations(recommendations);
        return section;
    }

    private void appendRecommendation(List<String> target, String item) {
        if (item == null || item.isBlank()) {
            return;
        }
        String trimmed = item.trim();
        if (!target.contains(trimmed)) {
            target.add(trimmed);
        }
    }

    private EncounterDetail.PrescriptionDetailSection buildPrescriptionDetail(
            List<Map<String, String>> items) {
        EncounterDetail.PrescriptionDetailSection section =
                new EncounterDetail.PrescriptionDetailSection();
        section.setItems(items != null ? items : List.of());
        return section;
    }

    private EncounterDetail.BiochemistrySection buildBiochemistryFromLab(LabResult lab) {
        EncounterDetail.BiochemistrySection section =
                new EncounterDetail.BiochemistrySection();

        Double glucoseMgdl = lab != null ? lab.getGlucoseMgdl() : null;
        Double hba1c = lab != null ? lab.getHba1c() : null;

        section.setGlucose(glucose(glucoseMgdl, null));
        section.setHba1c(hba1c(hba1c));

        section.getLipidProfile().add(lab(
                "Cholesterol", lab != null ? lab.getCholesterolTp() : null, "mmol/L", "< 5.2", 0, 5.2));
        section.getLipidProfile().add(lab(
                "Triglyceride", lab != null ? lab.getTriglyceride() : null, "mmol/L", "< 1.7", 0, 1.7));
        section.getLipidProfile().add(lab(
                "HDL", lab != null ? lab.getHdlC() : null, "mmol/L", "> 1.0", 1.0, 99));
        section.getLipidProfile().add(lab(
                "LDL", lab != null ? lab.getLdlC() : null, "mmol/L", "< 3.4", 0, 3.4));

        section.getLiverEnzymes().add(lab("AST", lab != null ? lab.getAst() : null, "U/L", "< 40", 0, 40));
        section.getLiverEnzymes().add(lab("ALT", lab != null ? lab.getAlt() : null, "U/L", "< 41", 0, 41));

        Double creatinineMgdl = null;
        if (lab != null && lab.getCreatinine() != null) {
            creatinineMgdl = lab.getCreatinine() * UMOL_TO_MGDL;
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

    private EncounterDetail.BloodCountSection buildBloodCount(LabResult lab) {
        EncounterDetail.BloodCountSection section =
                new EncounterDetail.BloodCountSection();
        section.getItems().add(lab("WBC", lab != null ? lab.getWbc() : null, "G/L", "4.0-10.0", 4.0, 10.0));
        section.getItems().add(lab("RBC", lab != null ? lab.getRbc() : null, "T/L", "4.0-5.5", 4.0, 5.5));
        section.getItems().add(lab("HGB", lab != null ? lab.getHgb() : null, "g/dL", "12-16", 12, 16));
        section.getItems().add(lab("HCT", lab != null ? lab.getHct() : null, "%", "36-46", 36, 46));
        section.getItems().add(lab("PLT", lab != null ? lab.getPlt() : null, "G/L", "150-400", 150, 400));
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

    private void addHeader(Document document, EncounterDetail view,
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

    private void addInternalMedicine(Document document, EncounterDetail view,
                                     Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("I. Benh an tai kham noi tiet", headerFont));

        EncounterDetail.InternalMedicineSection section = view.getInternalMedicine();

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

    private void addPrescriptionDetail(Document document, EncounterDetail view,
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

    private void addBloodCount(Document document, EncounterDetail view,
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

    private void addBiochemistry(Document document, EncounterDetail view,
                                 Font headerFont, Font normalFont) throws DocumentException {

        document.add(sectionTitle("IV. Sinh hoa mau", headerFont));

        EncounterDetail.BiochemistrySection section = view.getBiochemistry();

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

    private void addUltrasound(Document document, EncounterDetail view,
                               Font headerFont, Font normalFont) throws DocumentException {
        document.add(sectionTitle("V. Sieu am bung", headerFont));
        for (Map<String, String> field : view.getUltrasound().getFields()) {
            document.add(new Paragraph(
                    safe(field.get("label")) + ": " + safe(field.get("value")), normalFont));
        }
        document.add(Chunk.NEWLINE);
    }

    private void addFooter(Document document, EncounterDetail view,
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
