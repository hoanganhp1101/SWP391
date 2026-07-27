package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.ThresholdSettingsDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.ThresholdSettings;

import java.util.UUID;

/**
 * Nguồn ngưỡng lâm sàng và quy tắc rủi ro DÙNG CHUNG cho toàn hệ thống.
 *
 * Ngưỡng mặc định (hằng số) khớp {@link ThresholdSettings#defaults}.
 * Khi bệnh nhân đã được gán bác sĩ, ưu tiên ngưỡng riêng trong bảng
 * {@code threshold_settings} — cùng nguồn với quét cảnh báo / khuyến nghị AI.
 */
public final class ClinicalRiskService {

    public static final class PersistResult {
        private final boolean analysisSaved;
        private final boolean alertCreated;
        private final String alertTitle;
        private final String mucCanhBao;

        public PersistResult(boolean analysisSaved, boolean alertCreated, String alertTitle, String mucCanhBao) {
            this.analysisSaved = analysisSaved;
            this.alertCreated = alertCreated;
            this.alertTitle = alertTitle;
            this.mucCanhBao = mucCanhBao;
        }

        public boolean isAnalysisSaved() { return analysisSaved; }
        public boolean isAlertCreated() { return alertCreated; }
        public String getAlertTitle() { return alertTitle; }
        public String getMucCanhBao() { return mucCanhBao; }

        public static PersistResult failed() {
            return new PersistResult(false, false, null, null);
        }
    }

    // ==== Ngưỡng đường huyết (mg/dL) — khớp ThresholdSettings.defaults ====
    public static final double GLUCOSE_LOW_MGDL = 70;
    public static final double GLUCOSE_HIGH_MGDL = 180;
    public static final double GLUCOSE_CRITICAL_MGDL = 250;

    // ==== Ngưỡng đường huyết (mmol/L) — red flag y khoa tuyệt đối ====
    public static final double GLUCOSE_HYPO_MMOL = 3.9;
    public static final double GLUCOSE_HYPER_MMOL = 16.7;

    // ==== Ngưỡng huyết áp (mmHg) ====
    public static final int BP_SYS_WATCH = 140;
    public static final int BP_DIA_WATCH = 90;
    public static final int BP_SYS_DANGER = 160;
    public static final int BP_DIA_DANGER = 100;

    // ==== Ngưỡng HbA1c (%) ====
    public static final double HBA1C_HIGH = 7.0;
    public static final double HBA1C_CRITICAL = 9.0;

    // ==== Ngưỡng BMI ====
    public static final double BMI_OVERWEIGHT = 25;
    public static final double BMI_HIGH = 30;

    // ==== Theo dõi định kỳ ====
    public static final int MONITORING_GAP_DAYS = 7;

    // ==== Thang điểm rủi ro 0-100 → mức cảnh báo (ngưỡng nghiêm hơn) ====
    public static final double SCORE_DANGER = 65.0;
    public static final double SCORE_HIGH = 30.0;
    public static final double SCORE_MEDIUM = 12.0;

    private ClinicalRiskService() {
    }

    /**
     * Lấy ngưỡng của bác sĩ phụ trách bệnh nhân; nếu chưa gán thì dùng mặc định hệ thống.
     */
    public static ThresholdSettings resolveThresholdsForPatient(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return ThresholdSettings.defaults(null);
        }
        try {
            Patient patient = new PatientDAO().getPatientById(patientId);
            if (patient != null && patient.getBacSiId() != null && !patient.getBacSiId().isBlank()) {
                return new ThresholdSettingsDAO().getForDoctor(patient.getBacSiId());
            }
        } catch (Exception e) {
            System.err.println("[ClinicalRiskService] Không đọc được ngưỡng bác sĩ: " + e.getMessage());
        }
        return ThresholdSettings.defaults(null);
    }

    /**
     * Pipeline chuẩn sau khi AI phân tích một bản ghi vitals:
     * áp quy tắc động (theo ngưỡng bác sĩ nếu có) → lưu ai_analysis → tạo alert.
     */
    public static PersistResult applyRulesAndPersist(String patientId, HealthRecord record, AIAnalysis analysis) {
        ThresholdSettings thresholds = resolveThresholdsForPatient(patientId);
        return applyRulesAndPersist(patientId, record, analysis, thresholds);
    }

    public static PersistResult applyRulesAndPersist(
            String patientId, HealthRecord record, AIAnalysis analysis, ThresholdSettings thresholds) {
        try {
            if (analysis == null) {
                return PersistResult.failed();
            }
            if (thresholds == null) {
                thresholds = ThresholdSettings.defaults(null);
            }
            if (record != null && record.getId() != null) {
                analysis.setHealthRecordId(record.getId());
            }
            analysis.setPatientId(patientId);

            boolean redFlag = finalizeAnalysis(record, analysis, thresholds);
            boolean analysisSaved = new AIAnalysisDAO().insertAnalysis(analysis);
            if (!analysisSaved) {
                return new PersistResult(false, false, null, analysis.getMucCanhBao());
            }

            String mucCanhBao = analysis.getMucCanhBao();
            Alert alert = buildPatientAlert(patientId, record, analysis, thresholds, redFlag);
            boolean alertCreated = new AlertDAO().insertAlert(alert);
            return new PersistResult(true, alertCreated, alert.getTieuDe(), mucCanhBao);
        } catch (Exception e) {
            System.err.println("[ClinicalRiskService] applyRulesAndPersist failed: " + e.getMessage());
            e.printStackTrace();
            return PersistResult.failed();
        }
    }

    /**
     * Tạo cảnh báo sau mỗi lần phân tích — kể cả chỉ số an toàn (ghi nhận đo mới).
     */
    private static Alert buildPatientAlert(
            String patientId,
            HealthRecord record,
            AIAnalysis analysis,
            ThresholdSettings thresholds,
            boolean redFlag) {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setPatientId(patientId);
        alert.setAiAnalysisId(analysis.getId());
        alert.setLoaiCanhBao(determineAlertType(record, thresholds));

        String mucCanhBao = analysis.getMucCanhBao() != null ? analysis.getMucCanhBao() : "an_toan";
        alert.setMucDo(mucCanhBao);

        if (redFlag || "nguy_hiem".equals(mucCanhBao)) {
            alert.setTieuDe("🚨 CẢNH BÁO Y TẾ KHẨN CẤP");
            alert.setMucDo("nguy_hiem");
        } else if ("cao".equals(mucCanhBao)) {
            alert.setTieuDe("🚨 [RED FLAG] Chỉ số bất thường — cần can thiệp y tế");
            alert.setMucDo("cao");
        } else if ("trung_binh".equals(mucCanhBao)) {
            alert.setTieuDe("⚠️ Cảnh báo: chỉ số lệch ngưỡng — theo dõi sát");
            alert.setMucDo("trung_binh");
        } else {
            alert.setTieuDe("✓ Ghi nhận chỉ số mới");
            alert.setMucDo("an_toan");
        }

        String noiDung = analysis.getPhanTichChiTiet();
        if (noiDung == null || noiDung.isBlank()) {
            noiDung = "Hệ thống đã ghi nhận lần đo mới vào hồ sơ sức khỏe.";
        }
        noiDung = prependUrgencyNotice(mucCanhBao, redFlag, noiDung);
        alert.setNoiDung(noiDung);
        return alert;
    }

    private static String prependUrgencyNotice(String mucCanhBao, boolean redFlag, String noiDung) {
        if (redFlag || "nguy_hiem".equals(mucCanhBao)) {
            return "⛔ Liên hệ cơ sở y tế / bác sĩ NGAY LẬP TỨC. Không tự ý thay đổi liều thuốc.\n\n" + noiDung;
        }
        if ("cao".equals(mucCanhBao)) {
            return "⚠️ Chỉ số bất thường — cần báo bác sĩ trong thời gian sớm nhất và theo dõi sát.\n\n" + noiDung;
        }
        if ("trung_binh".equals(mucCanhBao)) {
            return "ℹ️ Chỉ số hơi lệch ngưỡng — nên đo lại sau 2 giờ và ghi chú triệu chứng.\n\n" + noiDung;
        }
        return noiDung;
    }

    /**
     * Áp quy tắc rủi ro động: ưu tiên ngưỡng mg/dL của bác sĩ, kèm red-flag mmol tuyệt đối.
     *
     * @return true nếu bản ghi rơi vào red flag
     */
    public static boolean finalizeAnalysis(HealthRecord record, AIAnalysis analysis) {
        return finalizeAnalysis(record, analysis, ThresholdSettings.defaults(null));
    }

    public static boolean finalizeAnalysis(
            HealthRecord record, AIAnalysis analysis, ThresholdSettings thresholds) {
        if (thresholds == null) {
            thresholds = ThresholdSettings.defaults(null);
        }

        double dynamicRiskScore = 0.0;
        boolean redFlag = false;

        double glucoseLow = thresholds.getGlucoseLow();
        double glucoseHigh = thresholds.getGlucoseHigh();
        double glucoseDanger = thresholds.getGlucoseDanger();

        if (record.getDuongHuyetMgdl() != null) {
            double glucoseMgdl = record.getDuongHuyetMgdl();
            double glucoseMmol = glucoseMgdl / 18.0;
            String td = record.getThoiDiemDoDuong() != null ? record.getThoiDiemDoDuong() : "";

            // Red flag tuyệt đối (mmol) HOẶC theo ngưỡng bác sĩ (mg/dL)
            if (glucoseMmol < GLUCOSE_HYPO_MMOL
                    || glucoseMmol > GLUCOSE_HYPER_MMOL
                    || glucoseMgdl < glucoseLow
                    || glucoseMgdl >= glucoseDanger) {
                redFlag = true;
            } else if (glucoseMgdl > glucoseHigh) {
                dynamicRiskScore += 30.0;
            } else if (isFastingMeasurement(td)) {
                if (glucoseMmol >= 7.3 && glucoseMmol <= 13.0) {
                    dynamicRiskScore += 15.0;
                } else if (glucoseMmol > 13.0 && glucoseMmol <= GLUCOSE_HYPER_MMOL) {
                    dynamicRiskScore += 30.0;
                }
            } else {
                if (glucoseMmol >= 10.1 && glucoseMmol <= 15.0) {
                    dynamicRiskScore += 15.0;
                } else if (glucoseMmol > 15.0 && glucoseMmol <= GLUCOSE_HYPER_MMOL) {
                    dynamicRiskScore += 30.0;
                }
            }
        }

        Integer sysBP = record.getHuyetApTamThu();
        Integer diaBP = record.getHuyetApTamTruong();
        if (sysBP != null || diaBP != null) {
            int sys = (sysBP != null) ? sysBP : 0;
            int dia = (diaBP != null) ? diaBP : 0;

            if (sys >= BP_SYS_DANGER || dia >= BP_DIA_DANGER) {
                redFlag = true;
            } else if ((sys >= BP_SYS_WATCH && sys < BP_SYS_DANGER)
                    || (dia >= BP_DIA_WATCH && dia < BP_DIA_DANGER)) {
                dynamicRiskScore += 15.0;
            }
        }

        double totalRisk = analysis.getDiemNguyCo() + dynamicRiskScore;
        if (totalRisk > 100.0) {
            totalRisk = 100.0;
        }
        analysis.setDiemNguyCo(totalRisk);

        if (redFlag) {
            analysis.setMucCanhBao("nguy_hiem");
            analysis.setPhanTichChiTiet(buildEmergencySummary(record));
        } else {
            analysis.setMucCanhBao(resolveMucCanhBao(totalRisk));
        }

        return redFlag;
    }

    public static String resolveMucCanhBao(double riskScore) {
        if (riskScore >= SCORE_DANGER) {
            return "nguy_hiem";
        }
        if (riskScore >= SCORE_HIGH) {
            return "cao";
        }
        if (riskScore >= SCORE_MEDIUM) {
            return "trung_binh";
        }
        return "an_toan";
    }

    public static String resolveRiskLevel(int riskScore) {
        if (riskScore >= SCORE_DANGER) {
            return "critical";
        }
        if (riskScore >= SCORE_HIGH) {
            return "high";
        }
        if (riskScore >= SCORE_MEDIUM) {
            return "medium";
        }
        return "low";
    }

    public static String determineAlertType(HealthRecord record) {
        return determineAlertType(record, ThresholdSettings.defaults(null));
    }

    public static String determineAlertType(HealthRecord record, ThresholdSettings thresholds) {
        double high = thresholds != null ? thresholds.getGlucoseHigh() : GLUCOSE_HIGH_MGDL;
        if (record.getDuongHuyetMgdl() != null && record.getDuongHuyetMgdl() > high) {
            return "duong_huyet_cao";
        }
        if (record.getHuyetApTamThu() != null && record.getHuyetApTamThu() >= BP_SYS_WATCH) {
            return "xu_huong_tang";
        }
        if (record.getHuyetApTamTruong() != null && record.getHuyetApTamTruong() >= BP_DIA_WATCH) {
            return "xu_huong_tang";
        }
        if (record.getDuongHuyetMgdl() != null) {
            return "duong_huyet_cao";
        }
        return "xu_huong_tang";
    }

    private static boolean isFastingMeasurement(String thoiDiemDo) {
        if (thoiDiemDo == null || thoiDiemDo.isBlank()) {
            return true;
        }
        return "luc_doi".equals(thoiDiemDo) || thoiDiemDo.contains("đói");
    }

    private static String buildEmergencySummary(HealthRecord record) {
        StringBuilder sb = new StringBuilder(
                "🚨 Chỉ số của bạn rơi vào mức NGUY HIỂM. Vui lòng liên hệ y tế ngay lập tức!");
        if (record == null) {
            return sb.toString();
        }
        if (record.getDuongHuyetMgdl() != null) {
            sb.append("\n- Đường huyết: ").append(record.getDuongHuyetMgdl()).append(" mg/dL");
        }
        if (record.getHuyetApTamThu() != null && record.getHuyetApTamTruong() != null) {
            sb.append("\n- Huyết áp: ").append(record.getHuyetApTamThu())
                    .append("/").append(record.getHuyetApTamTruong()).append(" mmHg");
        }
        if (record.getNhipTim() != null) {
            sb.append("\n- Nhịp tim: ").append(record.getNhipTim()).append(" BPM");
        }
        return sb.toString();
    }
}
