package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.model.Patient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.diabetesmanage.util.GeminiJsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EncounterAiAnalysis {

    private boolean used;         // true nếu kết quả đến từ Gemini
    private boolean configured;   // true nếu Gemini API key đã cấu hình
    private String error;         // thông báo lỗi (nếu có), không chặn workflow

    private String riskLevel;                 // low | medium | high | critical
    private int riskScore;                    // 0-100
    private String possibleDisease;           // bệnh khả năng
    private List<String> riskFactors = new ArrayList<>();
    private List<String> recommendedTests = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String shortExplanation;          // giải thích ngắn

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getPossibleDisease() {
        return possibleDisease;
    }

    public void setPossibleDisease(String possibleDisease) {
        this.possibleDisease = possibleDisease;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors != null ? riskFactors : new ArrayList<>();
    }

    public List<String> getRecommendedTests() {
        return recommendedTests;
    }

    public void setRecommendedTests(List<String> recommendedTests) {
        this.recommendedTests = recommendedTests != null ? recommendedTests : new ArrayList<>();
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public String getShortExplanation() {
        return shortExplanation;
    }

    public void setShortExplanation(String shortExplanation) {
        this.shortExplanation = shortExplanation;
    }

    /** Tóm tắt dạng văn bản để hiển thị readonly ở Bước 2 (Treatment Plan). */
    public String buildSummaryText() {
        StringBuilder sb = new StringBuilder();
        if (riskLevel != null && !riskLevel.isBlank()) {
            sb.append("Mức độ rủi ro: ").append(riskLevel.toUpperCase());
            sb.append(" (").append(riskScore).append("/100)\n");
        }
        if (possibleDisease != null && !possibleDisease.isBlank()) {
            sb.append("Bệnh khả năng: ").append(possibleDisease).append("\n");
        }
        appendList(sb, "Yếu tố nguy cơ", riskFactors);
        appendList(sb, "Xét nghiệm đề xuất", recommendedTests);
        appendList(sb, "Khuyến nghị", recommendations);
        if (shortExplanation != null && !shortExplanation.isBlank()) {
            sb.append("Giải thích: ").append(shortExplanation);
        }
        return sb.toString().trim();
    }

    private void appendList(StringBuilder sb, String label, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(label).append(": ");
        sb.append(String.join("; ", items));
        sb.append("\n");
    }
    private static final Logger LOG = Logger.getLogger(EncounterAiAnalysis.class.getName());

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final int GEMINI_MAX_ATTEMPTS = 3;

    private final GeminiConfig geminiConfig = GeminiConfig.load();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public EncounterAiAnalysis analyze(EncounterCreateDTO form, Patient patient) {
        EncounterAiAnalysis analysis = new EncounterAiAnalysis();
        analysis.setConfigured(geminiConfig.isConfigured());

        if (form != null) {
            form.clearFieldsOutsideEncounterType();
            form.syncLabToHealthMetrics();
            form.calculateBmiIfNeeded();
        }

        if (!geminiConfig.isConfigured()) {
            applyRuleBasedFallback(analysis, form);
            analysis.setError("Chưa cấu hình Gemini API key — đang dùng phân tích theo quy tắc y khoa.");
            return analysis;
        }

        Exception lastError = null;
        boolean useCompact = false;
        String lastRaw = null;

        for (int attempt = 1; attempt <= GEMINI_MAX_ATTEMPTS; attempt++) {
            try {
                String prompt = buildPrompt(form, patient, useCompact);
                GeminiCallResult result = callGemini(prompt);
                lastRaw = result.text;
                LOG.info("Gemini raw response (attempt " + attempt + ", compact=" + useCompact
                        + ", finishReason=" + result.finishReason + "): " + result.text);

                if (result.isTruncated()) {
                    useCompact = true;
                    throw new IllegalStateException(
                            "Gemini bị cắt (finishReason=" + result.finishReason + ")");
                }

                if (!applyGeminiTextToAnalysis(result.text, analysis)) {
                    throw new IllegalStateException("Không parse được JSON từ Gemini response");
                }
                analysis.setUsed(true);
                return analysis;
            } catch (Exception e) {
                lastError = e;
                LOG.log(Level.WARNING,
                        "Gemini attempt " + attempt + "/" + GEMINI_MAX_ATTEMPTS + " failed: "
                                + (e.getMessage() != null ? e.getMessage() : e), e);

                // Parse lỗi → self-repair một lần với response cũ (không fallback ngay).
                if (lastRaw != null && !isTruncationError(e)) {
                    try {
                        String repaired = callGemini(buildSelfRepairPrompt(lastRaw)).text;
                        LOG.info("Gemini self-repair raw: " + repaired);
                        if (applyGeminiTextToAnalysis(repaired, analysis)) {
                            analysis.setUsed(true);
                            return analysis;
                        }
                        throw new IllegalStateException("Self-repair không parse được JSON");
                    } catch (Exception repairError) {
                        lastError = repairError;
                        LOG.log(Level.WARNING, "Gemini self-repair failed: "
                                + repairError.getMessage(), repairError);
                        lastRaw = null; // tránh self-repair lặp vô hạn
                    }
                }

                // Truncation hoặc lỗi khác → lần sau dùng prompt rút gọn.
                useCompact = true;
            }
        }

        String message = lastError != null && lastError.getMessage() != null
                ? lastError.getMessage() : "unknown";
        applyRuleBasedFallback(analysis, form);
        analysis.setError("Gemini lỗi (" + message + ") — đang dùng phân tích theo quy tắc y khoa.");
        return analysis;
    }

    private static boolean isTruncationError(Exception e) {
        String msg = e.getMessage();
        return msg != null && msg.contains("bị cắt");
    }

    private boolean applyGeminiTextToAnalysis(String raw, EncounterAiAnalysis analysis) {
        GeminiJsonUtil.ParseResult parseResult = GeminiJsonUtil.parse(raw, true);
        if (!parseResult.isSuccess() || parseResult.getJsonNode() == null) {
            System.err.println("Gemini JSON parse error: "
                    + (parseResult.getError() != null ? parseResult.getError() : "unknown"));
            return false;
        }
        parseResponseSafely(parseResult.getJsonNode(), analysis);
        return true;
    }

    /**
     * @param compact true = bỏ Symptoms / Medical History / Clinical Findings (giảm token khi retry).
     */
    private String buildPrompt(EncounterCreateDTO form, Patient patient, boolean compact) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là AI hỗ trợ bác sĩ nội tiết đánh giá bệnh nhân đái tháo đường.\n\n");

        sb.append("Schema:\n");
        sb.append("{\"riskLevel\":\"low|medium|high|critical\",\"riskScore\":0,\"possibleDisease\":\"\",");
        sb.append("\"riskFactors\":[],\"recommendedTests\":[],\"recommendations\":[],\"shortExplanation\":\"\"}\n\n");

        sb.append("Patient:\n");
        if (patient != null) {
            appendIfPresent(sb, "Age", patient.getTuoi() != null ? String.valueOf(patient.getTuoi()) : null);
            appendIfPresent(sb, "Gender", patient.getGioiTinh());
            appendIfPresent(sb, "Diabetes Type", patient.getLoaiTieuDuong());
        }

        sb.append("Encounter:\n");
        appendIfPresent(sb, "Type", encounterTypeLabel(form.resolveEncounterType()));
        if (!compact) {
            appendIfPresent(sb, "Symptoms", form.getTrieuChung());
            appendIfPresent(sb, "Medical History", form.getTienSuBenh());
            appendIfPresent(sb, "Clinical Findings", form.getKhamLamSang());
        }

        appendLabsAndVitals(sb, form);

        sb.append("\nONLY RETURN VALID JSON.\n");
        sb.append("DO NOT RETURN MARKDOWN.\n");
        sb.append("DO NOT RETURN EXPLANATION.\n");
        sb.append("DO NOT RETURN ANY TEXT.\n");
        sb.append("Return ONLY one JSON object.\n");
        sb.append("No markdown.\n");
        sb.append("No code block.\n");
        sb.append("No explanation.\n");
        sb.append("No comments.\n");
        sb.append("No extra text.\n");
        return sb.toString();
    }

    private static void appendLabsAndVitals(StringBuilder sb, EncounterCreateDTO form) {
        sb.append("Laboratory Results:\n");
        Double glucose = resolveGlucoseMgdl(form);
        if (glucose != null) {
            appendIfPresent(sb, "Glucose (mg/dL)", fmt(glucose));
        }
        Double hba1c = firstNonNull(form.getHba1cPercent(), form.getLabHba1c());
        if (hba1c != null) {
            appendIfPresent(sb, "HbA1c (%)", fmt(hba1c));
        }
        appendMetric(sb, "BMI", form.getBmi(), "");
        if (form.getHuyetApTamThu() != null || form.getHuyetApTamTruong() != null) {
            appendIfPresent(sb, "Blood Pressure (mmHg)",
                    nz(form.getHuyetApTamThu()) + "/" + nz(form.getHuyetApTamTruong()));
        }
        appendMetric(sb, "Heart Rate (bpm)", form.getNhipTim(), "");
        appendMetric(sb, "Temperature (C)", form.getNhietDoC(), "");
        appendMetric(sb, "Cholesterol (mmol/L)",
                firstNonNull(form.getCholesterolMmol(), form.getLabCholesterol()), "");
        appendMetric(sb, "Triglyceride (mmol/L)",
                firstNonNull(form.getTriglycerideMmol(), form.getLabTriglyceride()), "");
        appendMetric(sb, "HDL (mmol/L)", form.getLabHdl(), "");
        appendMetric(sb, "LDL (mmol/L)", form.getLabLdl(), "");
        appendMetric(sb, "Creatinine (umol/L)", form.getLabCreatinine(), "");
        appendMetric(sb, "AST (U/L)", form.getLabAst(), "");
        appendMetric(sb, "ALT (U/L)", form.getLabAlt(), "");
        appendMetric(sb, "Urea (mmol/L)", form.getLabUre(), "");
        appendMetric(sb, "WBC (G/L)", form.getLabWbc(), "");
        appendMetric(sb, "RBC (T/L)", form.getLabRbc(), "");
        appendMetric(sb, "HGB (g/dL)", form.getLabHgb(), "");
        appendMetric(sb, "HCT (%)", form.getLabHct(), "");
        appendMetric(sb, "PLT (G/L)", form.getLabPlt(), "");
    }

    private static String buildSelfRepairPrompt(String previousResponse) {
        return "ONLY RETURN VALID JSON.\n"
                + "DO NOT RETURN MARKDOWN.\n"
                + "DO NOT RETURN EXPLANATION.\n"
                + "DO NOT RETURN ANY TEXT.\n"
                + "Return ONLY one JSON object.\n\n"
                + "Previous response was not valid JSON.\n"
                + "Previous response:\n"
                + previousResponse;
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("- ").append(label).append(": ").append(value).append("\n");
        }
    }

    private static void appendMetric(StringBuilder sb, String label, Number value, String ignoredUnit) {
        if (value != null) {
            sb.append("- ").append(label).append(": ").append(value).append("\n");
        }
    }

    private enum AbnormalTier {
        NORMAL(0),
        MILD(8),
        MODERATE(18),
        HIGH(30),
        CRITICAL(70);

        private final int score;

        AbnormalTier(int score) {
            this.score = score;
        }

        int score() {
            return score;
        }
    }

    private static final class RuleHit {
        private final String factor;
        private final String recommendation;
        private final AbnormalTier tier;

        private RuleHit(String factor, String recommendation, AbnormalTier tier) {
            this.factor = factor;
            this.recommendation = recommendation;
            this.tier = tier;
        }
    }

    private static Double firstNonNull(Double first, Double second) {
        return first != null ? first : second;
    }

    /** Glucose mg/dL: ưu tiên chỉ số tái khám, không thì quy đổi từ mmol/L lab. */
    private static Double resolveGlucoseMgdl(EncounterCreateDTO form) {
        if (form.getDuongHuyetMgdl() != null) {
            return form.getDuongHuyetMgdl();
        }
        if (form.getLabGlucoseMau() != null) {
            return Math.round(form.getLabGlucoseMau() * 18.0182 * 10.0) / 10.0;
        }
        return null;
    }

    private static void applyHit(List<String> factors, List<String> recommendations,
                                 int[] score, boolean lockScore, RuleHit hit) {
        if (hit == null || hit.tier == AbnormalTier.NORMAL) {
            return;
        }
        factors.add(hit.factor);
        if (hit.recommendation != null && !hit.recommendation.isBlank()) {
            recommendations.add(hit.recommendation);
        }
        if (!lockScore) {
            score[0] += hit.tier.score();
        }
    }

    /** Chỉ số càng cao càng xấu (WBC tăng, cholesterol...). */
    private static RuleHit evaluateHighOnly(String label, String unit, double value,
                                            double normalMax, double mildMax, double moderateMax,
                                            String mildRec, String moderateRec, String criticalRec) {
        if (value <= normalMax) {
            return null;
        }
        if (value <= mildMax) {
            return new RuleHit(label + " tăng nhẹ (" + fmt(value) + unit + ")",
                    mildRec, AbnormalTier.MILD);
        }
        if (value <= moderateMax) {
            return new RuleHit(label + " tăng cao (" + fmt(value) + unit + ")",
                    moderateRec, AbnormalTier.HIGH);
        }
        return new RuleHit(label + " cực kỳ cao (" + fmt(value) + unit + ")",
                criticalRec, AbnormalTier.CRITICAL);
    }

    /** Chỉ số càng thấp càng xấu (HDL, HGB, RBC...). */
    private static RuleHit evaluateLowOnly(String label, String unit, double value,
                                           double normalMin, double mildMin, double moderateMin,
                                           String mildRec, String moderateRec, String criticalRec) {
        if (value >= normalMin) {
            return null;
        }
        if (value >= mildMin) {
            return new RuleHit(label + " thấp nhẹ (" + fmt(value) + unit + ")",
                    mildRec, AbnormalTier.MILD);
        }
        if (value >= moderateMin) {
            return new RuleHit(label + " thấp (" + fmt(value) + unit + ")",
                    moderateRec, AbnormalTier.HIGH);
        }
        return new RuleHit(label + " rất thấp (" + fmt(value) + unit + ")",
                criticalRec, AbnormalTier.CRITICAL);
    }

    /** Chỉ số hai chiều (WBC, PLT...). */
    private static RuleHit evaluateBidirectional(String label, String unit, double value,
                                                 double lowNormal, double highNormal,
                                                 double lowMild, double highMild,
                                                 double lowModerate, double highModerate,
                                                 String highMildRec, String highModRec, String highCritRec,
                                                 String lowMildRec, String lowModRec, String lowCritRec) {
        if (value >= lowNormal && value <= highNormal) {
            return null;
        }
        if (value > highNormal) {
            if (value <= highMild) {
                return new RuleHit(label + " tăng nhẹ (" + fmt(value) + unit + ")",
                        highMildRec, AbnormalTier.MILD);
            }
            if (value <= highModerate) {
                return new RuleHit(label + " tăng cao (" + fmt(value) + unit + ")",
                        highModRec, AbnormalTier.HIGH);
            }
            return new RuleHit(label + " cực kỳ cao (" + fmt(value) + unit + ")",
                    highCritRec, AbnormalTier.CRITICAL);
        }
        if (value >= lowMild) {
            return new RuleHit(label + " giảm nhẹ (" + fmt(value) + unit + ")",
                    lowMildRec, AbnormalTier.MILD);
        }
        if (value >= lowModerate) {
            return new RuleHit(label + " giảm (" + fmt(value) + unit + ")",
                    lowModRec, AbnormalTier.HIGH);
        }
        return new RuleHit(label + " rất thấp (" + fmt(value) + unit + ")",
                lowCritRec, AbnormalTier.CRITICAL);
    }

    private static RuleHit evaluateGlucose(double glucoseMgdl) {
        if (glucoseMgdl >= 600) {
            return new RuleHit("Đường huyết cực kỳ cao (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Đánh giá ngay HHS/DKA.", AbnormalTier.CRITICAL);
        }
        if (glucoseMgdl >= 300) {
            return new RuleHit("Đường huyết rất cao (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Theo dõi sát và xem xét điều trị tích cực đường huyết.", AbnormalTier.HIGH);
        }
        if (glucoseMgdl >= 200) {
            return new RuleHit("Đường huyết cao (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Điều chỉnh chế độ ăn và thuốc theo phác đồ.", AbnormalTier.MODERATE);
        }
        if (glucoseMgdl >= 126) {
            return new RuleHit("Đường huyết tăng (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Theo dõi đường huyết và tái đánh giá.", AbnormalTier.MILD);
        }
        if (glucoseMgdl >= 100) {
            return new RuleHit("Đường huyết tiền đái tháo đường (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Khuyến khích thay đổi lối sống.", AbnormalTier.MILD);
        }
        if (glucoseMgdl < 54) {
            return new RuleHit("Đường huyết rất thấp (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Xử trí hạ đường huyết cấp và điều chỉnh liều insulin/thuốc.", AbnormalTier.CRITICAL);
        }
        if (glucoseMgdl < 70) {
            return new RuleHit("Đường huyết thấp (" + fmt(glucoseMgdl) + " mg/dL)",
                    "Theo dõi triệu chứng hạ đường huyết.", AbnormalTier.HIGH);
        }
        return null;
    }

    private static RuleHit evaluateHba1c(double hba1c) {
        if (hba1c >= 14) {
            return new RuleHit("HbA1c cực kỳ cao (" + fmt(hba1c) + "%)",
                    "Đánh giá lại phác đồ điều trị và kiểm soát đường huyết dài hạn.", AbnormalTier.CRITICAL);
        }
        if (hba1c >= 10) {
            return new RuleHit("HbA1c rất cao (" + fmt(hba1c) + "%)",
                    "Tăng cường kiểm soát đường huyết và tái khám sớm.", AbnormalTier.HIGH);
        }
        if (hba1c >= 8) {
            return new RuleHit("HbA1c cao (" + fmt(hba1c) + "%)",
                    "Xem xét điều chỉnh thuốc/chế độ ăn.", AbnormalTier.MODERATE);
        }
        if (hba1c >= 6.5) {
            return new RuleHit("HbA1c tăng (" + fmt(hba1c) + "%)",
                    "Theo dõi HbA1c định kỳ.", AbnormalTier.MILD);
        }
        if (hba1c >= 5.7) {
            return new RuleHit("HbA1c tiền đái tháo đường (" + fmt(hba1c) + "%)",
                    "Khuyến khích thay đổi lối sống.", AbnormalTier.MILD);
        }
        return null;
    }

    private static RuleHit evaluateBmi(double bmi) {
        if (bmi >= 40) {
            return new RuleHit("BMI béo phì độ III (" + fmt(bmi) + ")",
                    "Can thiệp giảm cân và đánh giá biến chứng chuyển hóa.", AbnormalTier.CRITICAL);
        }
        if (bmi >= 35) {
            return new RuleHit("BMI béo phì độ II (" + fmt(bmi) + ")",
                    "Lập kế hoạch giảm cân và vận động.", AbnormalTier.HIGH);
        }
        if (bmi >= 30) {
            return new RuleHit("BMI cao (" + fmt(bmi) + ")",
                    "Điều chỉnh chế độ ăn và tăng vận động.", AbnormalTier.MODERATE);
        }
        if (bmi >= 25) {
            return new RuleHit("BMI thừa cân (" + fmt(bmi) + ")",
                    "Theo dõi cân nặng định kỳ.", AbnormalTier.MILD);
        }
        if (bmi < 16) {
            return new RuleHit("BMI suy dinh dưỡng nặng (" + fmt(bmi) + ")",
                    "Đánh giá dinh dưỡng và nguyên nhân sụt cân.", AbnormalTier.CRITICAL);
        }
        if (bmi < 18.5) {
            return new RuleHit("BMI thấp (" + fmt(bmi) + ")",
                    "Tư vấn dinh dưỡng.", AbnormalTier.MILD);
        }
        return null;
    }

    private static RuleHit evaluateBloodPressure(Integer sys, Integer dia) {
        if (sys == null && dia == null) {
            return null;
        }
        int s = sys != null ? sys : 0;
        int d = dia != null ? dia : 0;
        if (s >= 180 || d >= 120) {
            return new RuleHit("Huyết áp khủng hoàng (" + s + "/" + d + " mmHg)",
                    "Đánh giá cấp cứu tăng huyết áp nếu có triệu chứng.", AbnormalTier.CRITICAL);
        }
        if (s >= 160 || d >= 100) {
            return new RuleHit("Huyết áp rất cao (" + s + "/" + d + " mmHg)",
                    "Theo dõi sát và điều chỉnh điều trị tăng huyết áp.", AbnormalTier.HIGH);
        }
        if (s >= 140 || d >= 90) {
            return new RuleHit("Huyết áp cao (" + s + "/" + d + " mmHg)",
                    "Kiểm tra huyết áp tại nhà và tái khám.", AbnormalTier.MODERATE);
        }
        if (s >= 130 || d >= 80) {
            return new RuleHit("Huyết áp tăng nhẹ (" + s + "/" + d + " mmHg)",
                    "Thay đổi lối sống, hạn chế muối.", AbnormalTier.MILD);
        }
        if ((sys != null && s < 90) || (dia != null && d < 60)) {
            return new RuleHit("Huyết áp thấp (" + s + "/" + d + " mmHg)",
                    "Theo dõi triệu chứng chóng mặt/mệt.", AbnormalTier.MODERATE);
        }
        return null;
    }

    /**
     * Phân tích dự phòng theo ngưỡng y khoa khi Gemini không khả dụng.
     * Đánh giá đầy đủ CBC, sinh hóa, huyết áp, BMI; cộng dồn điểm và giới hạn 100.
     */
    private void applyRuleBasedFallback(EncounterAiAnalysis analysis, EncounterCreateDTO form) {
        List<String> factors = new ArrayList<>();
        List<String> tests = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int[] score = {0};
        boolean lockScore = false;

        Double glucose = resolveGlucoseMgdl(form);
        if (glucose != null) {
            RuleHit hit = evaluateGlucose(glucose);
            if (glucose >= 600) {
                lockScore = true;
                score[0] = 100;
            }
            applyHit(factors, recommendations, score, lockScore, hit);
        }

        Double hba1c = firstNonNull(form.getHba1cPercent(), form.getLabHba1c());
        applyHit(factors, recommendations, score, lockScore, hba1c != null ? evaluateHba1c(hba1c) : null);

        if (form.getBmi() != null) {
            applyHit(factors, recommendations, score, lockScore, evaluateBmi(form.getBmi()));
        }

        applyHit(factors, recommendations, score, lockScore,
                evaluateBloodPressure(form.getHuyetApTamThu(), form.getHuyetApTamTruong()));

        Double cholesterol = firstNonNull(form.getCholesterolMmol(), form.getLabCholesterol());
        applyHit(factors, recommendations, score, lockScore, cholesterol != null
                ? evaluateHighOnly("Cholesterol", " mmol/L", cholesterol, 5.2, 6.2, 7.8,
                "Theo dõi lipid máu.",
                "Xem xét statin và chế độ ăn.",
                "Đánh giá nguy cơ tim mạch và điều trị tích cực.") : null);

        Double triglyceride = firstNonNull(form.getTriglycerideMmol(), form.getLabTriglyceride());
        applyHit(factors, recommendations, score, lockScore, triglyceride != null
                ? evaluateHighOnly("Triglyceride", " mmol/L", triglyceride, 1.7, 2.3, 5.6,
                "Hạn chế tinh bột/đường.",
                "Điều chỉnh chế độ ăn và cân nhắc thuốc.",
                "Nguy cơ viêm tụy cấp — cần can thiệp sớm.") : null);

        if (form.getLabHdl() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateLowOnly("HDL", " mmol/L", form.getLabHdl(), 1.0, 0.9, 0.7,
                            "Tăng vận động thể chất.",
                            "Đánh giá nguy cơ tim mạch.",
                            "Nguy cơ tim mạch cao — cần can thiệp lipid."));
        }

        if (form.getLabLdl() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateHighOnly("LDL", " mmol/L", form.getLabLdl(), 3.0, 4.0, 4.9,
                            "Theo dõi lipid máu.",
                            "Xem xét statin theo nguy cơ tim mạch.",
                            "LDL rất cao — cần điều trị tích cực."));
        }

        if (form.getLabCreatinine() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateHighOnly("Creatinine", " µmol/L", form.getLabCreatinine(), 115, 150, 250,
                            "Theo dõi chức năng thận.",
                            "Đánh giá suy thận và điều chỉnh thuốc.",
                            "Suy thận nặng — cần đánh giá thận học khẩn."));
        }

        if (form.getLabAst() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateHighOnly("AST", " U/L", form.getLabAst(), 40, 80, 200,
                            "Theo dõi men gan.",
                            "Đánh giá tổn thương gan.",
                            "Men gan rất cao — cần đánh giá cấp cứu gan."));
        }

        if (form.getLabAlt() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateHighOnly("ALT", " U/L", form.getLabAlt(), 40, 80, 200,
                            "Theo dõi men gan.",
                            "Đánh giá tổn thương gan.",
                            "Men gan rất cao — cần đánh giá cấp cứu gan."));
        }

        if (form.getLabUre() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateHighOnly("Urê", " mmol/L", form.getLabUre(), 7.5, 10, 20,
                            "Theo dõi chức năng thận.",
                            "Đánh giá suy thận/tăng urê huyết.",
                            "Urê rất cao — cần đánh giá thận khẩn."));
        }

        // CBC — WBC (G/L): 4-10 bình thường; 10-20 nhẹ; 20-50 cao; >50 rất nguy hiểm
        if (form.getLabWbc() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateBidirectional("Bạch cầu (WBC)", " G/L", form.getLabWbc(),
                            4, 10, 2, 20, 1, 50,
                            "Theo dõi và đánh giá nguyên nhân tăng bạch cầu (nhiễm trùng, viêm).",
                            "Cần đánh giá lâm sàng thêm: nhiễm trùng, viêm hoặc phản ứng huyết học.",
                            "Bạch cầu tăng rất cao, cần đánh giá nguyên nhân như nhiễm trùng, viêm hoặc bệnh lý huyết học.",
                            "Theo dõi bạch cầu giảm nhẹ.",
                            "Cần đánh giá nguyên nhân giảm bạch cầu.",
                            "Bạch cầu giảm rất thấp, cần đánh giá nguy cơ nhiễm trùng và nguyên nhân huyết học."));
        }

        if (form.getLabRbc() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateBidirectional("Hồng cầu (RBC)", " T/L", form.getLabRbc(),
                            3.8, 5.5, 3.2, 6.0, 2.5, 6.5,
                            "Theo dõi bất thường hồng cầu.",
                            "Cần đánh giá nguyên nhân thiếu máu hoặc đa hồng cầu.",
                            "RBC bất thường rõ, cần đánh giá thêm xét nghiệm máu và nguyên nhân lâm sàng.",
                            "Theo dõi thiếu máu nhẹ.",
                            "Cần đánh giá nguyên nhân thiếu máu.",
                            "Thiếu máu rõ trên xét nghiệm, cần đánh giá lâm sàng và chỉ định thêm nếu phù hợp."));
        }

        if (form.getLabHgb() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateBidirectional("Huyết sắc tố (HGB)", " g/dL", form.getLabHgb(),
                            12, 17, 10, 18, 7, 20,
                            "Theo dõi thiếu máu.",
                            "Cần đánh giá thiếu máu và bổ sung dinh dưỡng nếu phù hợp.",
                            "Hb bất thường rõ, cần đánh giá nguyên nhân và mức độ lâm sàng.",
                            "Theo dõi thiếu máu nhẹ.",
                            "Cần đánh giá và theo dõi thiếu máu.",
                            "Hb thấp rõ, cần đánh giá lâm sàng; cân nhắc truyền máu chỉ khi có chỉ định."));
        }

        if (form.getLabHct() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateBidirectional("Hematocrit (HCT)", " %", form.getLabHct(),
                            36, 48, 30, 52, 21, 60,
                            "Theo dõi hematocrit.",
                            "Cần đánh giá thiếu máu hoặc tình trạng mất nước.",
                            "HCT bất thường rõ, cần đánh giá thêm theo lâm sàng.",
                            "Theo dõi thiếu máu nhẹ.",
                            "Cần đánh giá nguyên nhân thiếu máu.",
                            "HCT thấp rõ, cần đánh giá lâm sàng và xử trí theo chỉ định."));
        }

        if (form.getLabPlt() != null) {
            applyHit(factors, recommendations, score, lockScore,
                    evaluateBidirectional("Tiểu cầu (PLT)", " G/L", form.getLabPlt(),
                            150, 400, 100, 450, 50, 600,
                            "Theo dõi tiểu cầu.",
                            "Cần đánh giá nguyên nhân tăng/giảm tiểu cầu.",
                            "Tiểu cầu bất thường rõ, cần đánh giá nguy cơ chảy máu/huyết khối theo lâm sàng.",
                            "Theo dõi giảm tiểu cầu nhẹ.",
                            "Cần theo dõi dấu hiệu chảy máu và xem xét thuốc đang dùng.",
                            "Giảm tiểu cầu rõ, cần đánh giá lâm sàng và nguy cơ chảy máu."));
        }

        if (form.getNhipTim() != null) {
            int hr = form.getNhipTim();
            if (hr > 120) {
                applyHit(factors, recommendations, score, lockScore,
                        new RuleHit("Nhịp tim nhanh (" + hr + " bpm)",
                                "Đánh giá nguyên nhân tim nhanh.", AbnormalTier.MODERATE));
            } else if (hr < 50) {
                applyHit(factors, recommendations, score, lockScore,
                        new RuleHit("Nhịp tim chậm (" + hr + " bpm)",
                                "Theo dõi triệu chứng và ECG nếu cần.", AbnormalTier.MODERATE));
            }
        }

        if (form.getNhietDoC() != null) {
            double temp = form.getNhietDoC();
            if (temp >= 39) {
                applyHit(factors, recommendations, score, lockScore,
                        new RuleHit("Sốt cao (" + fmt(temp) + " °C)",
                                "Tìm nguyên nhân nhiễm trùng và điều trị.", AbnormalTier.HIGH));
            } else if (temp >= 38) {
                applyHit(factors, recommendations, score, lockScore,
                        new RuleHit("Sốt (" + fmt(temp) + " °C)",
                                "Theo dõi dấu hiệu nhiễm trùng.", AbnormalTier.MILD));
            }
        }

        tests.add("HbA1c định kỳ");
        tests.add("Đường huyết đói và sau ăn");
        tests.add("Lipid máu (cholesterol, triglyceride, HDL, LDL)");
        if (form.getLabWbc() != null || form.getLabRbc() != null || form.getLabPlt() != null) {
            tests.add("Tổng phân tích túi máu (CBC) theo dõi");
        }
        if (form.getLabCreatinine() != null || form.getLabUre() != null) {
            tests.add("Chức năng thận (creatinine, ure, eGFR)");
        }
        if (lockScore) {
            tests.add("Khí máu / ketone / điện giải (đánh giá HHS/DKA)");
        }

        if (recommendations.isEmpty() && !lockScore) {
            recommendations.add("Theo dõi đường huyết đều đặn.");
            recommendations.add("Điều chỉnh chế độ ăn và vận động phù hợp.");
        }
        if (score[0] >= 50 && !lockScore) {
            recommendations.add("Cân nhắc đánh giá lại phác đồ điều trị và tái khám sớm.");
        }
        if (lockScore) {
            recommendations.add("Theo dõi sát và chuyển cấp cứu nếu có dấu hiệu HHS/DKA.");
        }

        int finalScore = Math.min(score[0], 100);
        analysis.setRiskScore(finalScore);
        analysis.setRiskLevel(finalScore >= 90 ? "critical"
                : finalScore >= 70 ? "high"
                : finalScore >= 40 ? "medium" : "low");
        analysis.setPossibleDisease(encounterTypeLabel(form.resolveEncounterType()));
        analysis.setRiskFactors(factors.isEmpty()
                ? List.of("Chưa phát hiện bất thường rõ rệt từ chỉ số nhập vào.")
                : new ArrayList<>(new LinkedHashSet<>(factors)));
        analysis.setRecommendedTests(tests);
        analysis.setRecommendations(new ArrayList<>(new LinkedHashSet<>(recommendations)));
        analysis.setShortExplanation(buildFallbackExplanation(factors, finalScore, lockScore));
    }

    private static String buildFallbackExplanation(List<String> factors, int score, boolean extremeGlucose) {
        if (extremeGlucose) {
            return "Đường huyết cực kỳ cao — nghi HHS/DKA, cần đánh giá cấp cứu.";
        }
        if (factors.isEmpty()) {
            return "Phân tích theo quy tắc y khoa: các chỉ số nhập vào nằm trong giới hạn chấp nhận được.";
        }
        if (score >= 70) {
            return "Nhiều chỉ số bất thường nghiêm trọng — cần đánh giá lâm sàng và can thiệp sớm.";
        }
        return "Phân tích theo quy tắc y khoa dựa trên các chỉ số đã nhập. Bác sĩ cần đánh giá lâm sàng trước khi kê đơn.";
    }
    private void parseResponseSafely(JsonNode root, EncounterAiAnalysis analysis) {
        applyParseDefaults(analysis);
        if (root == null || !root.isObject()) {
            return;
        }

        analysis.setRiskLevel(readStringFromNode(root, "riskLevel", analysis.getRiskLevel()));
        analysis.setRiskScore(Math.min(100, Math.max(0, readIntFromNode(root, "riskScore", 0))));
        analysis.setPossibleDisease(readStringFromNode(root, "possibleDisease", ""));
        analysis.setRiskFactors(toStringListFromNode(root, "riskFactors"));
        analysis.setRecommendedTests(toStringListFromNode(root, "recommendedTests"));
        analysis.setRecommendations(toStringListFromNode(root, "recommendations"));
        analysis.setShortExplanation(readStringFromNode(root, "shortExplanation", ""));
    }

    private static String readStringFromNode(JsonNode root, String key, String defaultValue) {
        try {
            if (root == null || !root.has(key) || root.get(key).isNull()) {
                return defaultValue;
            }
            return root.get(key).asText(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static int readIntFromNode(JsonNode root, String key, int defaultValue) {
        try {
            if (root == null || !root.has(key) || root.get(key).isNull()) {
                return defaultValue;
            }
            JsonNode node = root.get(key);
            if (node.isInt() || node.isLong()) {
                return node.asInt();
            }
            if (node.isFloatingPointNumber()) {
                return (int) Math.round(node.asDouble());
            }
            String raw = node.asText("").trim();
            if (!raw.isEmpty()) {
                return (int) Math.round(Double.parseDouble(raw));
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private List<String> toStringListFromNode(JsonNode root, String key) {
        List<String> list = new ArrayList<>();
        try {
            if (root == null || !root.has(key) || root.get(key).isNull()) {
                return list;
            }
            JsonNode array = root.get(key);
            if (!array.isArray()) {
                return list;
            }
            for (JsonNode element : array) {
                if (element != null && !element.isNull()) {
                    list.add(element.asText());
                }
            }
        } catch (Exception ignored) {
            // return empty/partial list
        }
        return list;
    }

    private void applyParseDefaults(EncounterAiAnalysis analysis) {
        analysis.setRiskLevel("medium");
        analysis.setRiskScore(0);
        analysis.setPossibleDisease("");
        analysis.setRiskFactors(new ArrayList<>());
        analysis.setRecommendedTests(new ArrayList<>());
        analysis.setRecommendations(new ArrayList<>());
        analysis.setShortExplanation("");
    }

    // ---- Gemini HTTP ----

    private static final class GeminiCallResult {
        private final String text;
        private final String finishReason;

        private GeminiCallResult(String text, String finishReason) {
            this.text = text != null ? text : "";
            this.finishReason = finishReason;
        }

        private boolean isTruncated() {
            return "MAX_TOKENS".equalsIgnoreCase(finishReason)
                    || "LENGTH".equalsIgnoreCase(finishReason);
        }
    }

    private String resolveModel() {
        String configured = geminiConfig.getModel();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return DEFAULT_MODEL;
    }

    private GeminiCallResult callGemini(String prompt) throws Exception {
        String model = resolveModel();
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        requestBody.add("generationConfig", buildGenerationConfig());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(GENERATE_URL, model)))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API lỗi (" + response.statusCode() + ", model=" + model + ")");
        }
        return parseGeminiHttpBody(response.body());
    }

    private static JsonObject buildGenerationConfig() {
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0);
        generationConfig.addProperty("topP", 0.1);
        generationConfig.addProperty("topK", 1);
        generationConfig.addProperty("candidateCount", 1);
        generationConfig.addProperty("maxOutputTokens", 2048);
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.add("responseSchema", buildResponseSchema());
        return generationConfig;
    }

    private static JsonObject buildResponseSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "OBJECT");
        JsonObject properties = new JsonObject();
        properties.add("riskLevel", schemaType("STRING"));
        properties.add("riskScore", schemaType("NUMBER"));
        properties.add("possibleDisease", schemaType("STRING"));
        properties.add("riskFactors", stringArraySchema());
        properties.add("recommendedTests", stringArraySchema());
        properties.add("recommendations", stringArraySchema());
        properties.add("shortExplanation", schemaType("STRING"));
        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("riskLevel");
        required.add("riskScore");
        required.add("possibleDisease");
        required.add("riskFactors");
        required.add("recommendedTests");
        required.add("recommendations");
        required.add("shortExplanation");
        schema.add("required", required);
        return schema;
    }

    private static JsonObject schemaType(String type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        return obj;
    }

    private static JsonObject stringArraySchema() {
        JsonObject arr = new JsonObject();
        arr.addProperty("type", "ARRAY");
        arr.add("items", schemaType("STRING"));
        return arr;
    }

    private GeminiCallResult parseGeminiHttpBody(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

        if (root.has("promptFeedback")) {
            JsonObject feedback = root.getAsJsonObject("promptFeedback");
            if (feedback != null && feedback.has("blockReason") && !feedback.get("blockReason").isJsonNull()) {
                throw new RuntimeException("Gemini bị block safety: "
                        + feedback.get("blockReason").getAsString());
            }
        }

        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini không trả về candidates (có thể bị block safety hoặc timeout)");
        }
        JsonObject candidate = candidates.get(0).getAsJsonObject();
        String finishReason = null;
        if (candidate.has("finishReason") && !candidate.get("finishReason").isJsonNull()) {
            finishReason = candidate.get("finishReason").getAsString();
            if ("SAFETY".equalsIgnoreCase(finishReason)
                    || "RECITATION".equalsIgnoreCase(finishReason)
                    || "BLOCKLIST".equalsIgnoreCase(finishReason)) {
                throw new RuntimeException("Gemini bị block (" + finishReason + ")");
            }
        }
        JsonObject content = candidate.getAsJsonObject("content");
        if (content == null || !content.has("parts") || content.get("parts").isJsonNull()) {
            throw new RuntimeException("Gemini không trả về content/parts");
        }
        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini không trả về text part");
        }
        StringBuilder sb = new StringBuilder();
        for (JsonElement element : parts) {
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            JsonObject part = element.getAsJsonObject();
            if (part.has("text") && !part.get("text").isJsonNull()) {
                sb.append(part.get("text").getAsString());
            }
        }
        String text = sb.toString();
        if (text.isBlank() && !("MAX_TOKENS".equalsIgnoreCase(finishReason)
                || "LENGTH".equalsIgnoreCase(finishReason))) {
            throw new RuntimeException("Gemini trả text rỗng hoặc null");
        }
        return new GeminiCallResult(text, finishReason);
    }

    private static String encounterTypeLabel(String typeCode) {
        if ("mau_tong_quat".equalsIgnoreCase(typeCode)) {
            return "Kết quả xét nghiệm máu tổng quát";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(typeCode)) {
            return "Kết quả sinh hóa máu";
        }
        return "Bệnh án tái khám Nội tiết";
    }

    private static String nz(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private static String fmt(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }
}
