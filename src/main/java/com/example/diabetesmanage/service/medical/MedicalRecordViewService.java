package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.medical.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedicalRecordViewService {

    private static final double UMOL_TO_MGDL = 1.0 / 88.4;

    private final MedicalLabAnalyzer analyzer = new MedicalLabAnalyzer();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MedicalRecordDetailView buildView(HealthRecord record) {
        return buildView(record, null, null, List.of(), List.of());
    }

    public MedicalRecordDetailView buildView(
            HealthRecord record,
            LabResult lab,
            MedicalEncounter encounter,
            List<MedicationChip> medications,
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

    private InternalMedicineSection buildInternalMedicine(
            HealthRecord record,
            MedicalEncounter encounter,
            List<MedicationChip> medications,
            List<String> recommendations
    ) {
        InternalMedicineSection section = new InternalMedicineSection();
        Patient patient = record.getPatient();

        String symptoms = firstNonBlank(
                encounter != null ? encounter.getLyDoKham() : null,
                encounter != null ? encounter.getQuaTrinhBenhLy() : null,
                record.getGhiChu(),
                "Không ghi nhận triệu chứng đặc biệt"
        );

        section.getClinicalInfo().add(analyzer.textField("Triệu chứng", symptoms));
        section.getClinicalInfo().add(analyzer.bmi(record.getBmi()));
        section.getClinicalInfo().add(analyzer.bloodPressure(
                record.getHuyetApTamThu(),
                record.getHuyetApTamTruong()
        ));

        String diagnosis = firstNonBlank(
                encounter != null ? encounter.getChanDoanChinh() : null,
                record.getChanDoanChinh(),
                patient != null ? patient.getLoaiTieuDuong() : null,
                "Theo dõi đái tháo đường"
        );
        section.getDiagnosisInfo().add(analyzer.textField("Chẩn đoán chính", diagnosis));

        String secondary = encounter != null ? encounter.getChanDoanPhu() : null;
        if (secondary != null && !secondary.isBlank()) {
            section.getDiagnosisInfo().add(analyzer.textField("Chẩn đoán phụ", secondary));
        }

        section.getDiagnosisInfo().add(analyzer.textField(
                "Phân loại tiểu đường",
                patient != null ? patient.getLoaiTieuDuong() : "—"
        ));

        if (encounter != null && encounter.getHuongXuTri() != null && !encounter.getHuongXuTri().isBlank()) {
            section.getDiagnosisInfo().add(analyzer.textField("Hướng xử trí", encounter.getHuongXuTri()));
        }

        section.setMedications(buildMedications(record, medications));
        section.setRecommendations(buildRecommendations(record, recommendations));

        return section;
    }

    private List<MedicationChip> buildMedications(HealthRecord record, List<MedicationChip> prescriptions) {
        if (prescriptions != null && !prescriptions.isEmpty()) {
            return prescriptions;
        }

        List<MedicationChip> meds = new ArrayList<>();

        if (record.getLoaiInsulinTiem() != null && !record.getLoaiInsulinTiem().isBlank()) {
            String dose = record.getLieuLuongInsulinUi() != null
                    ? record.getLieuLuongInsulinUi() + " UI"
                    : "—";
            meds.add(new MedicationChip(record.getLoaiInsulinTiem(), dose));
        } else if (record.getLieuLuongInsulinUi() != null && record.getLieuLuongInsulinUi() > 0) {
            meds.add(new MedicationChip("Insulin", record.getLieuLuongInsulinUi() + " UI"));
        }

        if (meds.isEmpty()) {
            meds.add(new MedicationChip("Chưa ghi nhận đơn thuốc", "—", "Cập nhật tại tái khám"));
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

    private BloodCountSection buildBloodCount(LabResult lab) {
        BloodCountSection section = new BloodCountSection();
        section.getItems().add(analyzer.lab("WBC", lab != null ? lab.getWbc() : null, "G/L", "4.0-10.0", 4.0, 10.0));
        section.getItems().add(analyzer.lab("RBC", lab != null ? lab.getRbc() : null, "T/L", "4.0-5.5", 4.0, 5.5));
        section.getItems().add(analyzer.lab("HGB", lab != null ? lab.getHgb() : null, "g/dL", "12-16", 12, 16));
        section.getItems().add(analyzer.lab("HCT", lab != null ? lab.getHct() : null, "%", "36-46", 36, 46));
        section.getItems().add(analyzer.lab("PLT", lab != null ? lab.getPlt() : null, "G/L", "150-400", 150, 400));
        return section;
    }

    private BiochemistrySection buildBiochemistry(HealthRecord record, LabResult lab) {
        BiochemistrySection section = new BiochemistrySection();

        Double glucoseMgdl = record.getDuongHuyetMgdl();
        if (glucoseMgdl == null && lab != null) {
            glucoseMgdl = lab.getGlucoseMgdl();
        }

        Double hba1c = record.getHba1cPercent();
        if (hba1c == null && lab != null) {
            hba1c = lab.getHba1c();
        }

        section.setGlucose(analyzer.glucose(glucoseMgdl, record.getThoiDiemDoDuong()));
        section.setHba1c(analyzer.hba1c(hba1c));

        Double cholesterol = firstNonNull(
                record.getCholesterolMmol(),
                lab != null ? lab.getCholesterolTp() : null
        );
        Double triglyceride = firstNonNull(
                record.getTriglycerideMmol(),
                lab != null ? lab.getTriglyceride() : null
        );

        section.getLipidProfile().add(analyzer.lab(
                "Cholesterol", cholesterol, "mmol/L", "< 5.2", 0, 5.2));
        section.getLipidProfile().add(analyzer.lab(
                "Triglyceride", triglyceride, "mmol/L", "< 1.7", 0, 1.7));
        section.getLipidProfile().add(analyzer.lab(
                "HDL", lab != null ? lab.getHdlC() : record.getHdlMmol(), "mmol/L", "> 1.0", 1.0, 99));
        section.getLipidProfile().add(analyzer.lab(
                "LDL", lab != null ? lab.getLdlC() : record.getLdlMmol(), "mmol/L", "< 3.4", 0, 3.4));

        Double ast = lab != null ? lab.getAst() : record.getAst();
        Double alt = lab != null ? lab.getAlt() : record.getAlt();
        section.getLiverEnzymes().add(analyzer.lab("AST", ast, "U/L", "< 40", 0, 40));
        section.getLiverEnzymes().add(analyzer.lab("ALT", alt, "U/L", "< 41", 0, 41));

        Double creatinineMgdl = null;
        if (lab != null && lab.getCreatinine() != null) {
            creatinineMgdl = lab.getCreatinine() * UMOL_TO_MGDL;
        } else if (record.getCreatinine() != null) {
            creatinineMgdl = record.getCreatinine();
        }

        section.getKidneyFunction().add(analyzer.lab(
                "Creatinine", creatinineMgdl, "mg/dL", "0.6-1.2", 0.6, 1.2));

        if (lab != null && lab.getUre() != null) {
            section.getKidneyFunction().add(analyzer.lab(
                    "Ure", lab.getUre(), "mmol/L", "2.5-7.5", 2.5, 7.5));
        }

        String alert = analyzer.diabetesAlert(glucoseMgdl, hba1c);
        if (alert != null) {
            section.getAlerts().add(alert);
        }

        if (section.getGlucose().isAbnormal()) {
            section.getAlerts().add("Đường huyết bất thường — cần can thiệp điều trị");
        }

        return section;
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
