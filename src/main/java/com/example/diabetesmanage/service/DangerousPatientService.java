package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dto.HighRiskPatientDTO;
import com.example.diabetesmanage.dto.PatientRiskAssessmentDTO;
import com.example.diabetesmanage.dto.CriticalPatientAlertDTO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.Patient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class DangerousPatientService {

    private static final int RECORDS_PER_PATIENT = 20;
    private static final int MAX_GEMINI_CANDIDATES = 15;
    private static final int DISPLAY_LIMIT = 20;

    private static final double GLUCOSE_LOW = 70;
    private static final double GLUCOSE_HIGH = 180;
    private static final double GLUCOSE_CRITICAL = 250;
    private static final double HBA1C_HIGH = 7.0;
    private static final double HBA1C_CRITICAL = 9.0;
    private static final double BMI_HIGH = 30;
    private static final int BP_SYSTOLIC_HIGH = 140;
    private static final int BP_DIASTOLIC_HIGH = 90;
    private static final int MONITORING_GAP_DAYS = 7;
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final String LIST_MODELS_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";

    private static final List<String> MODEL_FALLBACKS = Arrays.asList(
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite",
            "gemini-3-flash",
            "gemini-flash-latest"
    );

    private final PatientDAO patientDAO = new PatientDAO();
    private final GeminiConfig geminiConfig = GeminiConfig.load();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public AnalysisResult analyzeDangerousPatients(String doctorId) {

        AnalysisResult result = new AnalysisResult();
        List<PatientRiskAssessmentDTO> dangerousProfiles = collectDangerousProfiles(doctorId);

        dangerousProfiles.sort(Comparator
                .comparingInt(PatientRiskAssessmentDTO::getRiskScore).reversed());

        result.setTotalDangerousCount(dangerousProfiles.size());

        List<PatientRiskAssessmentDTO> geminiCandidates = dangerousProfiles.subList(
                0,
                Math.min(dangerousProfiles.size(), MAX_GEMINI_CANDIDATES)
        );

        GeminiAnalysis geminiAnalysis = enrichWithGemini(geminiCandidates);

        result.setGeminiUsed(geminiAnalysis.isUsed());
        result.setGeminiConfigured(geminiAnalysis.isConfigured());
        result.setGeminiError(geminiAnalysis.getError());
        result.setGeminiConfigInfo(geminiAnalysis.getConfigInfo());
        result.setAiSummary(geminiAnalysis.getOverallSummary());

        if (!geminiAnalysis.getInsights().isEmpty()) {
            result.setAiInsights(geminiAnalysis.getInsights());
        }

        List<CriticalPatientAlertDTO> alerts = new ArrayList<>();
        int displayCount = Math.min(dangerousProfiles.size(), DISPLAY_LIMIT);

        for (int i = 0; i < displayCount; i++) {
            alerts.add(toUrgentAlert(dangerousProfiles.get(i), geminiAnalysis));
        }

        alerts.sort((a, b) -> Integer.compare(b.getRiskScore(), a.getRiskScore()));
        result.setDangerousPatients(alerts);
        return result;
    }

    public HighRiskPatientDTO getDangerousPatientDetail(String doctorId, String patientId) {

        Map<String, List<HealthRecord>> recordsByPatient =
                getRecordsGroupedByPatient(doctorId);

        List<HealthRecord> records = recordsByPatient.getOrDefault(patientId, new ArrayList<>());
        if (records.isEmpty()) {
            return null;
        }

        Patient patient = patientDAO.getPatientById(patientId, doctorId);
        if (patient == null) {
            return null;
        }

        PatientRiskAssessmentDTO profile = buildRiskProfile(patient, records);
        analyzeRiskRules(profile);

        if (!profile.isDangerous()) {
            return null;
        }

        HighRiskPatientDTO detail = new HighRiskPatientDTO();
        detail.setPatientId(profile.getPatientId());
        detail.setPatientCode(profile.getPatientCode());
        detail.setPatientName(profile.getPatientName());
        detail.setInitials(buildInitials(profile.getPatientName()));
        detail.setLoaiTieuDuong(profile.getLoaiTieuDuong());
        detail.setRiskLevel(resolveRiskLevel(profile));
        detail.setRiskScore(profile.getRiskScore());
        detail.setCritical(profile.isCritical());
        detail.setNeedsUrgentReview(profile.isCritical());
        detail.setRiskReasons(profile.getRiskReasons());
        detail.setMetricTags(buildMetricTags(records, profile.getRiskReasons()));
        detail.setRecentRecords(records);

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        HealthRecord latest = sorted.get(0);

        detail.setDuongHuyetGanNhat(latest.getDuongHuyetMgdl());
        detail.setHba1cGanNhat(findLatestHba1c(sorted));
        detail.setHuyetApTamThu(latest.getHuyetApTamThu());
        detail.setHuyetApTamTruong(latest.getHuyetApTamTruong());
        detail.setBmiGanNhat(latest.getBmi());
        detail.setInsulinGanNhat(latest.getLieuLuongInsulinUi());
        detail.setTimeAgo(formatTimeAgo(latest.getThoiGianDo()));

        PatientDetailGeminiAnalysis geminiDetail = analyzePatientDetail(profile);

        detail.setGeminiUsed(geminiDetail.isUsed());
        detail.setGeminiError(geminiDetail.getError());

        if (geminiDetail.isUsed()) {
            detail.setAiSummary(geminiDetail.getSummary());
            detail.setAiDetailAnalysis(geminiDetail.getDetailAnalysis());
            detail.setAiRecommendations(geminiDetail.getRecommendations());
            if (geminiDetail.getRiskLevel() != null) {
                detail.setRiskLevel(geminiDetail.getRiskLevel());
            }
            if (geminiDetail.getPriorityScore() > 0) {
                detail.setRiskScore(Math.max(profile.getRiskScore(), geminiDetail.getPriorityScore()));
            }
        } else {
            detail.setAiSummary(buildFallbackSummary(profile));
            detail.setAiDetailAnalysis(buildFallbackDetail(profile));
            detail.setAiRecommendations(buildFallbackRecommendations(profile));
        }

        return detail;
    }

    private List<PatientRiskAssessmentDTO> collectDangerousProfiles(String doctorId) {

        List<PatientRiskAssessmentDTO> dangerousProfiles = new ArrayList<>();
        List<Patient> patients = patientDAO.getPatients(doctorId);
        Map<String, List<HealthRecord>> recordsByPatient = getRecordsGroupedByPatient(doctorId);

        for (Patient patient : patients) {
            List<HealthRecord> records =
                    recordsByPatient.getOrDefault(patient.getId(), new ArrayList<>());

            if (records.isEmpty()) {
                continue;
            }

            PatientRiskAssessmentDTO profile = buildRiskProfile(patient, records);
            analyzeRiskRules(profile);

            if (profile.isDangerous()) {
                dangerousProfiles.add(profile);
            }
        }

        return dangerousProfiles;
    }

    private PatientRiskAssessmentDTO buildRiskProfile(Patient patient, List<HealthRecord> records) {
        PatientRiskAssessmentDTO profile = new PatientRiskAssessmentDTO();
        profile.setPatientId(patient.getId());
        profile.setPatientCode(patient.getPatientCode());
        profile.setPatientName(
                patient.getUser() != null ? patient.getUser().getHoTen() : "Không rõ"
        );
        profile.setLoaiTieuDuong(patient.getLoaiTieuDuong());
        profile.setRecentRecords(records);
        return profile;
    }

    private CriticalPatientAlertDTO toUrgentAlert(
            PatientRiskAssessmentDTO profile,
            GeminiAnalysis geminiAnalysis) {

        CriticalPatientAlertDTO alert = new CriticalPatientAlertDTO();
        alert.setPatientId(profile.getPatientId());
        alert.setPatientCode(profile.getPatientCode());
        alert.setPatientName(profile.getPatientName());
        alert.setLoaiTieuDuong(profile.getLoaiTieuDuong());
        alert.setRiskReasons(profile.getRiskReasons());
        alert.setRiskScore(profile.getRiskScore());
        alert.setCritical(profile.isCritical());

        populateAlertMetrics(alert, profile);

        PatientGeminiInsight geminiInsight =
                geminiAnalysis.getPatientInsights().get(profile.getPatientCode());

        if (geminiInsight != null) {
            alert.setAiSummary(geminiInsight.getSummary());
            if (geminiInsight.getRiskLevel() != null) {
                alert.setRiskLevel(geminiInsight.getRiskLevel());
            }
            if ("critical".equalsIgnoreCase(geminiInsight.getRiskLevel())) {
                alert.setCritical(true);
                alert.setNeedsUrgentReview(true);
            }
            if (geminiInsight.getPriorityScore() > 0) {
                alert.setRiskScore(
                        Math.max(profile.getRiskScore(), geminiInsight.getPriorityScore())
                );
            }
        } else {
            alert.setAiSummary(buildFallbackSummary(profile));
        }

        return alert;
    }

    private String buildFallbackSummary(PatientRiskAssessmentDTO profile) {
        if (profile.getRiskReasons().isEmpty()) {
            return "Bệnh nhân có chỉ số cần theo dõi thêm.";
        }
        return String.join(". ", profile.getRiskReasons()) + ".";
    }

    private String buildFallbackDetail(PatientRiskAssessmentDTO profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hệ thống ghi nhận ").append(profile.getRiskReasons().size())
                .append(" dấu hiệu bất thường từ ").append(profile.getRecentRecords().size())
                .append(" hồ sơ gần đây. ");
        if (!profile.getRiskReasons().isEmpty()) {
            sb.append("Các vấn đề chính: ").append(String.join(", ", profile.getRiskReasons())).append(".");
        }
        return sb.toString();
    }

    private List<String> buildFallbackRecommendations(PatientRiskAssessmentDTO profile) {
        List<String> recommendations = new ArrayList<>();
        for (String reason : profile.getRiskReasons()) {
            if (reason.contains("đường huyết") || reason.contains("Đường huyết")) {
                recommendations.add("Theo dõi đường huyết nhiều lần trong ngày và điều chỉnh chế độ ăn.");
            } else if (reason.contains("HbA1c")) {
                recommendations.add("Đánh giá lại phác đồ điều trị và tăng cường kiểm soát đường huyết dài hạn.");
            } else if (reason.contains("Huyết áp")) {
                recommendations.add("Kiểm tra huyết áp định kỳ và cân nhắc can thiệp dược lý.");
            } else if (reason.contains("Insulin")) {
                recommendations.add("Xem xét điều chỉnh liều insulin và đánh giá tuân thủ điều trị.");
            } else if (reason.contains("theo dõi")) {
                recommendations.add("Nhắc bệnh nhân cập nhật hồ sơ sức khỏe đều đặn hơn.");
            }
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Theo dõi sát các chỉ số và tái khám sớm nếu triệu chứng xấu đi.");
        }
        return recommendations;
    }

    private Double findLatestHba1c(List<HealthRecord> records) {
        for (HealthRecord record : records) {
            if (record.getHba1cPercent() != null) {
                return record.getHba1cPercent();
            }
        }
        return null;
    }

    private void analyzeRiskRules(PatientRiskAssessmentDTO profile) {

        List<String> reasons = new ArrayList<>();
        int score = 0;
        boolean critical = false;

        List<HealthRecord> records = profile.getRecentRecords();
        if (records == null || records.isEmpty()) {
            profile.setRiskReasons(new ArrayList<>());
            profile.setRiskScore(0);
            profile.setCritical(false);
            return;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        HealthRecord latest = sorted.get(sorted.size() - 1);

        if (latest.getDaysSinceLastVisit() > MONITORING_GAP_DAYS) {
            reasons.add("Không theo dõi sức khỏe đều đặn ("
                    + latest.getDaysSinceLastVisit() + " ngày không cập nhật)");
            score += 35;
        }

        for (HealthRecord record : sorted) {
            Double glucose = record.getDuongHuyetMgdl();
            if (glucose != null && glucose < GLUCOSE_LOW) {
                reasons.add("Đường huyết quá thấp (" + formatRiskNumber(glucose) + " mg/dL)");
                score += 90;
                critical = true;
                break;
            }
        }

        Double latestGlucose = latest.getDuongHuyetMgdl();
        if (latestGlucose != null) {
            if (latestGlucose >= GLUCOSE_CRITICAL) {
                reasons.add("Đường huyết quá cao (" + formatRiskNumber(latestGlucose) + " mg/dL)");
                score += 100;
                critical = true;
            } else if (latestGlucose >= GLUCOSE_HIGH) {
                reasons.add("Đường huyết cao (" + formatRiskNumber(latestGlucose) + " mg/dL)");
                score += 70;
            }
        }

        Double latestHba1c = findLatestHba1cAscending(sorted);
        if (latestHba1c != null) {
            if (latestHba1c >= HBA1C_CRITICAL) {
                reasons.add("HbA1c rất cao (" + formatRiskNumber(latestHba1c) + "%)");
                score += 85;
                critical = true;
            } else if (latestHba1c >= HBA1C_HIGH) {
                reasons.add("HbA1c cao (" + formatRiskNumber(latestHba1c) + "%)");
                score += 55;
            }
        }

        Integer systolic = latest.getHuyetApTamThu();
        Integer diastolic = latest.getHuyetApTamTruong();
        if ((systolic != null && systolic >= BP_SYSTOLIC_HIGH)
                || (diastolic != null && diastolic >= BP_DIASTOLIC_HIGH)) {
            reasons.add("Huyết áp cao ("
                    + (systolic != null ? systolic : "?")
                    + "/"
                    + (diastolic != null ? diastolic : "?")
                    + ")");
            score += 60;
        }

        Double latestBmi = findLatestBmi(sorted);
        if (latestBmi != null && latestBmi >= BMI_HIGH) {
            reasons.add("BMI cao (" + formatRiskNumber(latestBmi) + ")");
            score += 40;
        }

        if (hasRisingGlucoseTrend(sorted)) {
            reasons.add("Đường huyết tăng liên tục nhiều ngày");
            score += 65;
        }

        if (hasInsulinIneffective(sorted)) {
            reasons.add("Insulin tăng nhưng đường huyết không cải thiện");
            score += 75;
        }

        if (hasIrregularMonitoring(sorted)) {
            reasons.add("Khoảng cách giữa các lần đo quá dài, theo dõi không đều");
            score += 45;
        }

        profile.setRiskReasons(deduplicateReasons(reasons));
        profile.setRiskScore(score);
        profile.setCritical(critical || score >= 85);
    }

    private boolean hasRisingGlucoseTrend(List<HealthRecord> sorted) {

        List<Double> dailyGlucose = new ArrayList<>();
        LocalDate lastDate = null;

        for (HealthRecord record : sorted) {
            if (record.getDuongHuyetMgdl() == null || record.getThoiGianDo() == null) {
                continue;
            }
            LocalDate date = record.getThoiGianDo().toLocalDate();
            if (lastDate == null || !lastDate.equals(date)) {
                dailyGlucose.add(record.getDuongHuyetMgdl());
                lastDate = date;
            }
        }

        if (dailyGlucose.size() < 3) {
            return false;
        }

        int risingDays = 0;
        for (int i = 1; i < dailyGlucose.size(); i++) {
            if (dailyGlucose.get(i) > dailyGlucose.get(i - 1)) {
                risingDays++;
            } else {
                risingDays = 0;
            }
            if (risingDays >= 2) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInsulinIneffective(List<HealthRecord> sorted) {

        List<HealthRecord> withInsulin = new ArrayList<>();
        for (HealthRecord record : sorted) {
            if (record.getLieuLuongInsulinUi() != null
                    && record.getLieuLuongInsulinUi() > 0
                    && record.getDuongHuyetMgdl() != null) {
                withInsulin.add(record);
            }
        }

        if (withInsulin.size() < 2) {
            return false;
        }

        HealthRecord older = withInsulin.get(0);
        HealthRecord newer = withInsulin.get(withInsulin.size() - 1);

        boolean insulinIncreased =
                newer.getLieuLuongInsulinUi() > older.getLieuLuongInsulinUi();

        boolean glucoseNotImproved =
                newer.getDuongHuyetMgdl() >= older.getDuongHuyetMgdl() - 10;

        return insulinIncreased && glucoseNotImproved
                && newer.getDuongHuyetMgdl() >= GLUCOSE_HIGH;
    }

    private boolean hasIrregularMonitoring(List<HealthRecord> sorted) {

        if (sorted.size() < 2) {
            return false;
        }

        LocalDateTime previous = null;
        int gapsOverLimit = 0;

        for (HealthRecord record : sorted) {
            if (record.getThoiGianDo() == null) {
                continue;
            }
            if (previous != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        record.getThoiGianDo().toLocalDate(),
                        previous.toLocalDate()
                );
                if (days > MONITORING_GAP_DAYS) {
                    gapsOverLimit++;
                }
            }
            previous = record.getThoiGianDo();
        }

        return gapsOverLimit >= 1;
    }

    private Double findLatestHba1cAscending(List<HealthRecord> sorted) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i).getHba1cPercent() != null) {
                return sorted.get(i).getHba1cPercent();
            }
        }
        return null;
    }

    private Double findLatestBmi(List<HealthRecord> sorted) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i).getBmi() != null) {
                return sorted.get(i).getBmi();
            }
        }
        return null;
    }

    private List<String> deduplicateReasons(List<String> reasons) {
        return new ArrayList<>(new LinkedHashSet<>(reasons));
    }

    private String formatRiskNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    private static String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase();
        return first + last;
    }

    private static String resolveRiskLevel(PatientRiskAssessmentDTO profile) {
        if (profile.isCritical() || profile.getRiskScore() >= 85) {
            return "critical";
        }
        if (profile.getRiskScore() >= 55) {
            return "high";
        }
        return "medium";
    }

    private static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Chưa cập nhật";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "Vừa xong";
        }
        if (minutes < 60) {
            return minutes + " phút trước";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }

        long days = duration.toDays();
        return days + " ngày trước";
    }

    private static List<Map<String, Object>> buildMetricTags(
            List<HealthRecord> records,
            List<String> riskReasons) {

        List<Map<String, Object>> tags = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return tags;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        HealthRecord latest = sorted.get(0);
        boolean glucoseRising = isGlucoseRising(sorted);

        if (latest.getDuongHuyetMgdl() != null) {
            double glucose = latest.getDuongHuyetMgdl();
            String label = glucose >= GLUCOSE_CRITICAL ? "Đường huyết rất cao"
                    : glucose >= GLUCOSE_HIGH ? "Đường huyết cao"
                    : glucose < 70 ? "Đường huyết thấp"
                    : "Đường huyết";
            tags.add(metricTag(
                    label,
                    String.format("%.0f mg/dL", glucose),
                    "glucose",
                    glucoseRising
            ));
        }

        Double hba1c = findLatestHba1cInSortedRecords(sorted);
        if (hba1c != null) {
            tags.add(metricTag(
                    hba1c >= HBA1C_HIGH ? "HbA1c cao" : "HbA1c",
                    String.format("%.1f%%", hba1c),
                    "hba1c",
                    false
            ));
        }

        if (latest.getHuyetApTamThu() != null && latest.getHuyetApTamTruong() != null) {
            tags.add(metricTag(
                    "Huyết áp",
                    latest.getHuyetApTamThu() + "/" + latest.getHuyetApTamTruong(),
                    "bp",
                    false
            ));
        }

        if (latest.getBmi() != null && latest.getBmi() >= 30) {
            tags.add(metricTag(
                    "BMI cao",
                    String.format("%.1f", latest.getBmi()),
                    "bmi",
                    false
            ));
        }

        if (latest.getLieuLuongInsulinUi() != null && latest.getLieuLuongInsulinUi() > 0) {
            tags.add(metricTag(
                    "Insulin",
                    latest.getLieuLuongInsulinUi() + " UI",
                    "insulin",
                    false
            ));
        }

        for (String reason : riskReasons) {
            if (reason.contains("Insulin tăng")) {
                tags.add(metricTag(
                        "Can thiệp insulin",
                        "Không cải thiện",
                        "warning",
                        true
                ));
                break;
            }
            if (reason.contains("tăng liên tục")) {
                tags.add(metricTag(
                        "Xu hướng",
                        "Đường huyết tăng",
                        "trend",
                        true
                ));
                break;
            }
        }

        return tags;
    }

    private static void populateAlertMetrics(
            CriticalPatientAlertDTO alert,
            PatientRiskAssessmentDTO profile) {

        List<HealthRecord> records = profile.getRecentRecords();
        alert.setInitials(buildInitials(profile.getPatientName()));
        alert.setRiskLevel(resolveRiskLevel(profile));
        alert.setNeedsUrgentReview(profile.isCritical());
        alert.setMetricTags(buildMetricTags(records, profile.getRiskReasons()));

        if (records.isEmpty()) {
            return;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        HealthRecord latest = sorted.get(0);
        alert.setDuongHuyetGanNhat(latest.getDuongHuyetMgdl());
        alert.setHba1cGanNhat(findLatestHba1cInSortedRecords(sorted));
        alert.setHuyetApTamThu(latest.getHuyetApTamThu());
        alert.setHuyetApTamTruong(latest.getHuyetApTamTruong());
        alert.setBmiGanNhat(latest.getBmi());
        alert.setInsulinGanNhat(latest.getLieuLuongInsulinUi());
        alert.setTimeAgo(formatTimeAgo(latest.getThoiGianDo()));
        alert.setDetectedAgo("Phát hiện " + formatTimeAgo(latest.getThoiGianDo()));
        alert.setVitalDisplay(buildVitalDisplay(alert));
    }

    private static Map<String, Object> metricTag(
            String label, String value, String type, boolean trending) {
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("label", label);
        tag.put("value", value);
        tag.put("type", type);
        tag.put("trending", trending);
        return tag;
    }

    private static boolean isGlucoseRising(List<HealthRecord> sortedNewestFirst) {
        List<Double> values = new ArrayList<>();
        for (HealthRecord record : sortedNewestFirst) {
            if (record.getDuongHuyetMgdl() != null) {
                values.add(record.getDuongHuyetMgdl());
                if (values.size() >= 3) {
                    break;
                }
            }
        }

        if (values.size() < 2) {
            return false;
        }

        return values.get(0) > values.get(1);
    }

    private static Double findLatestHba1cInSortedRecords(List<HealthRecord> records) {
        for (HealthRecord record : records) {
            if (record.getHba1cPercent() != null) {
                return record.getHba1cPercent();
            }
        }
        return null;
    }

    private static String buildVitalDisplay(CriticalPatientAlertDTO alert) {
        if (alert.getDuongHuyetGanNhat() != null) {
            return String.format("%.0f mg/dL", alert.getDuongHuyetGanNhat());
        }
        if (alert.getHba1cGanNhat() != null) {
            return String.format("HbA1c %.1f%%", alert.getHba1cGanNhat());
        }
        return "—";
    }

    private GeminiAnalysis enrichWithGemini(List<PatientRiskAssessmentDTO> candidates) {

        GeminiAnalysis result = new GeminiAnalysis();
        result.setConfigured(geminiConfig.isConfigured());
        result.setConfigInfo(
                "Nguồn key: " + geminiConfig.getConfigSource()
                        + ", file: " + (geminiConfig.isPropertiesFileFound() ? "đã tìm thấy" : "không tìm thấy")
                        + ", key: " + geminiConfig.getMaskedApiKey()
        );

        if (!geminiConfig.isConfigured()) {
            if (!geminiConfig.isPropertiesFileFound()) {
                result.setError(
                        "Không tìm thấy gemini.properties trong classpath. "
                                + "Tạo file tại src/main/resources/gemini.properties rồi Build/Rebuild project."
                );
            } else {
                result.setError("API key trống trong gemini.properties");
            }
            return result;
        }

        if (candidates.isEmpty()) {
            result.setError("Không có hồ sơ nguy hiểm để Gemini phân tích.");
            return result;
        }

        try {
            String prompt = buildGeminiPrompt(candidates);
            String jsonResponse = generateGeminiJsonResponse(prompt);
            parseGeminiResponse(jsonResponse, candidates, result);
            result.setUsed(true);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            result.setError(message);
            System.err.println("Gemini phân tích thất bại: " + message);
            e.printStackTrace();
        }

        return result;
    }

    private PatientDetailGeminiAnalysis analyzePatientDetail(PatientRiskAssessmentDTO profile) {

        PatientDetailGeminiAnalysis result = new PatientDetailGeminiAnalysis();
        result.setConfigured(geminiConfig.isConfigured());

        if (!geminiConfig.isConfigured()) {
            result.setError("Chưa cấu hình Gemini API key");
            return result;
        }

        try {
            String prompt = buildDetailPrompt(profile);
            String jsonResponse = generateGeminiJsonResponse(prompt);
            parseDetailResponse(jsonResponse, result);
            result.setUsed(true);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            result.setError(message);
            System.err.println("Gemini phân tích chi tiết thất bại: " + message);
        }

        return result;
    }

    private String buildDetailPrompt(PatientRiskAssessmentDTO profile) {

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là bác sĩ nội tiết chuyên tiểu đường. ");
        sb.append("Phân tích chi tiết hồ sơ bệnh nhân nguy hiểm và trả về JSON thuần (không markdown).\n\n");
        sb.append("Schema JSON:\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"tóm tắt ngắn 1-2 câu\",\n");
        sb.append("  \"detailAnalysis\": \"phân tích chi tiết 4-6 câu về xu hướng, nguy cơ, mối liên hệ giữa các chỉ số\",\n");
        sb.append("  \"riskLevel\": \"critical|high|medium\",\n");
        sb.append("  \"recommendations\": [\"khuyến nghị 1\", \"khuyến nghị 2\", \"khuyến nghị 3\"],\n");
        sb.append("  \"priorityScore\": 1-100\n");
        sb.append("}\n\n");
        sb.append("Bệnh nhân: ").append(profile.getPatientName());
        sb.append(" (").append(profile.getPatientCode()).append(")\n");
        sb.append("Loại tiểu đường: ").append(nullToDash(profile.getLoaiTieuDuong())).append("\n");
        sb.append("Điểm rủi ro: ").append(profile.getRiskScore()).append("\n");
        sb.append("Lý do nguy hiểm: ").append(String.join("; ", profile.getRiskReasons())).append("\n\n");
        sb.append("Lịch sử đo gần đây:\n");

        int limit = Math.min(10, profile.getRecentRecords().size());
        for (int i = 0; i < limit; i++) {
            var record = profile.getRecentRecords().get(i);
            sb.append("- ")
                    .append(record.getThoiGianDo() != null ? record.getThoiGianDo().toLocalDate() : "?")
                    .append(": DH=").append(format(record.getDuongHuyetMgdl()))
                    .append(", HbA1c=").append(format(record.getHba1cPercent()))
                    .append(", HA=").append(formatBp(record))
                    .append(", BMI=").append(format(record.getBmi()))
                    .append(", Insulin=").append(formatInt(record.getLieuLuongInsulinUi()))
                    .append("\n");
        }

        return sb.toString();
    }

    private void parseDetailResponse(String jsonResponse, PatientDetailGeminiAnalysis result) {
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (root.has("summary")) {
            result.setSummary(root.get("summary").getAsString());
        }
        if (root.has("detailAnalysis")) {
            result.setDetailAnalysis(root.get("detailAnalysis").getAsString());
        }
        if (root.has("riskLevel")) {
            result.setRiskLevel(root.get("riskLevel").getAsString());
        }
        if (root.has("priorityScore")) {
            result.setPriorityScore(root.get("priorityScore").getAsInt());
        }
        if (root.has("recommendations")) {
            JsonArray recommendations = root.getAsJsonArray("recommendations");
            List<String> list = new ArrayList<>();
            for (JsonElement element : recommendations) {
                list.add(element.getAsString());
            }
            result.setRecommendations(list);
        }
    }

    private String buildGeminiPrompt(List<PatientRiskAssessmentDTO> candidates) {

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là bác sĩ nội tiết chuyên về tiểu đường. ");
        sb.append("Phân tích các hồ sơ bệnh nhân nguy hiểm sau và trả về JSON thuần (không markdown).\n\n");
        sb.append("Tiêu chí nguy hiểm: đường huyết quá cao/thấp, HbA1c cao, huyết áp cao, BMI cao, ");
        sb.append("đường huyết tăng liên tục, insulin tăng nhưng đường huyết không cải thiện, ");
        sb.append("không theo dõi sức khỏe đều đặn.\n\n");
        sb.append("Schema JSON:\n");
        sb.append("{\n");
        sb.append("  \"overallSummary\": \"tóm tắt ngắn toàn hệ thống\",\n");
        sb.append("  \"insights\": [\"insight 1\", \"insight 2\"],\n");
        sb.append("  \"patients\": [\n");
        sb.append("    {\n");
        sb.append("      \"patientCode\": \"mã\",\n");
        sb.append("      \"riskLevel\": \"critical|high|medium\",\n");
        sb.append("      \"summary\": \"phân tích 2-3 câu bằng tiếng Việt về tình trạng bệnh nhân\",\n");
        sb.append("      \"priorityScore\": 1-100\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("Dữ liệu bệnh nhân:\n");

        for (PatientRiskAssessmentDTO profile : candidates) {
            sb.append("- Mã: ").append(profile.getPatientCode());
            sb.append(", Tên: ").append(profile.getPatientName());
            sb.append(", Loại tiểu đường: ").append(nullToDash(profile.getLoaiTieuDuong()));
            sb.append(", Điểm rủi ro: ").append(profile.getRiskScore());
            sb.append(", Lý do: ").append(String.join("; ", profile.getRiskReasons()));
            sb.append(", Số hồ sơ gần đây: ").append(profile.getRecentRecords().size());
            sb.append("\n");

            int limit = Math.min(5, profile.getRecentRecords().size());
            for (int i = 0; i < limit; i++) {
                var record = profile.getRecentRecords().get(i);
                sb.append("  + ")
                        .append(record.getThoiGianDo() != null ? record.getThoiGianDo().toLocalDate() : "?")
                        .append(": DH=").append(format(record.getDuongHuyetMgdl()))
                        .append(", HbA1c=").append(format(record.getHba1cPercent()))
                        .append(", HA=").append(formatBp(record))
                        .append(", BMI=").append(format(record.getBmi()))
                        .append(", Insulin=").append(formatInt(record.getLieuLuongInsulinUi()))
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void parseGeminiResponse(
            String jsonResponse,
            List<PatientRiskAssessmentDTO> candidates,
            GeminiAnalysis result) {

        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (root.has("overallSummary")) {
            result.setOverallSummary(root.get("overallSummary").getAsString());
        }

        if (root.has("insights")) {
            JsonArray insights = root.getAsJsonArray("insights");
            List<String> insightList = new ArrayList<>();
            for (JsonElement element : insights) {
                insightList.add(element.getAsString());
            }
            result.setInsights(insightList);
        }

        Map<String, PatientGeminiInsight> patientMap = new HashMap<>();
        if (root.has("patients")) {
            JsonArray patients = root.getAsJsonArray("patients");
            for (JsonElement element : patients) {
                JsonObject patient = element.getAsJsonObject();
                String code = patient.get("patientCode").getAsString();
                PatientGeminiInsight insight = new PatientGeminiInsight();
                insight.setSummary(patient.get("summary").getAsString());
                if (patient.has("riskLevel")) {
                    insight.setRiskLevel(patient.get("riskLevel").getAsString());
                }
                if (patient.has("priorityScore")) {
                    insight.setPriorityScore(patient.get("priorityScore").getAsInt());
                }
                patientMap.put(code, insight);
            }
        }

        result.setPatientInsights(patientMap);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String format(Double value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatInt(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatBp(HealthRecord record) {
        if (record.getHuyetApTamThu() == null && record.getHuyetApTamTruong() == null) {
            return "-";
        }
        return (record.getHuyetApTamThu() != null ? record.getHuyetApTamThu() : "?")
                + "/"
                + (record.getHuyetApTamTruong() != null ? record.getHuyetApTamTruong() : "?");
    }

    private String generateGeminiJsonResponse(String prompt) throws Exception {

        if (!geminiConfig.isConfigured()) {
            if (!geminiConfig.isPropertiesFileFound()) {
                throw new IllegalStateException(
                        "Không tìm thấy file src/main/resources/gemini.properties. "
                                + "Hãy copy từ gemini.properties.example và rebuild project."
                );
            }
            throw new IllegalStateException("Gemini API key chưa được cấu hình trong gemini.properties");
        }

        List<String> modelsToTry = buildModelList();
        List<String> errors = new ArrayList<>();

        for (String model : modelsToTry) {
            try {
                return callGeminiModel(model, prompt);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                errors.add(model + ": " + message);
                System.err.println("Gemini model " + model + " thất bại: " + message);
            }
        }

        String discoveredModel = discoverGenerateContentModel();
        if (discoveredModel != null && !modelsToTry.contains(discoveredModel)) {
            try {
                return callGeminiModel(discoveredModel, prompt);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                errors.add(discoveredModel + ": " + message);
            }
        }

        throw new RuntimeException(
                "Tất cả model Gemini đều thất bại. "
                        + "Hãy đổi gemini.model trong gemini.properties (gợi ý: gemini-3.5-flash). "
                        + "Chi tiết: " + String.join(" | ", errors)
        );
    }

    private List<String> buildModelList() {
        String preferred = geminiConfig.getModel();
        LinkedHashSet<String> models = new LinkedHashSet<>();
        if (preferred != null && !preferred.isBlank()) {
            models.add(preferred.trim());
        }
        models.addAll(MODEL_FALLBACKS);
        return new ArrayList<>(models);
    }

    private String discoverGenerateContentModel() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIST_MODELS_URL + "?pageSize=100"))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-goog-api-key", geminiConfig.getApiKey())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                return null;
            }

            JsonArray models = JsonParser.parseString(response.body())
                    .getAsJsonObject()
                    .getAsJsonArray("models");

            if (models == null) {
                return null;
            }

            for (JsonElement element : models) {
                JsonObject model = element.getAsJsonObject();
                String name = model.get("name").getAsString();
                if (!name.contains("gemini")) {
                    continue;
                }

                JsonArray methods = model.getAsJsonArray("supportedGenerationMethods");
                if (methods == null) {
                    continue;
                }

                boolean supportsGenerate = false;
                for (JsonElement method : methods) {
                    if ("generateContent".equals(method.getAsString())) {
                        supportsGenerate = true;
                        break;
                    }
                }

                if (supportsGenerate) {
                    return name.replace("models/", "");
                }
            }
        } catch (Exception e) {
            System.err.println("Không lấy được danh sách model Gemini: " + e.getMessage());
        }

        return null;
    }

    private String callGeminiModel(String model, String prompt) throws Exception {

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

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.addProperty("temperature", 0.2);
        requestBody.add("generationConfig", generationConfig);

        String url = String.format(GENERATE_URL, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(
                        requestBody.toString(),
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Gemini API lỗi (" + response.statusCode() + ", model=" + model + "): "
                            + shortenGeminiResponse(response.body())
            );
        }

        return extractTextFromGeminiResponse(response.body());
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini không trả về kết quả: " + shortenGeminiResponse(responseBody));
        }

        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
        JsonObject content = firstCandidate.getAsJsonObject("content");
        JsonArray parts = content.getAsJsonArray("parts");
        JsonObject firstPart = parts.get(0).getAsJsonObject();
        return firstPart.get("text").getAsString();
    }

    private String shortenGeminiResponse(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }

    public Map<String, List<HealthRecord>> getRecordsGroupedByPatient(String scopeDoctorId) {
        Map<String, List<HealthRecord>> grouped = new LinkedHashMap<>();
        for (HealthRecord record : healthRecordDAO.getLatestPerPatient(scopeDoctorId)) {
            String patientKey = record.getPatient() != null ? record.getPatient().getId() : null;
            if (patientKey == null || patientKey.isBlank()) {
                patientKey = record.getId();
            }
            if (patientKey == null || patientKey.isBlank()) {
                continue;
            }
            // Phân tích rủi ro cần bức tranh đầy đủ: gộp giá trị mới nhất của từng
            // chỉ số trên mọi encounter thay vì chỉ đọc lab của encounter gần nhất.
            String labPatientId = record.getPatient() != null ? record.getPatient().getId() : null;
            LabResult lab = labResultDAO.getLatestSummaryByPatientId(labPatientId);
            if (lab != null) {
                record.setHba1cPercent(lab.getHba1c());
                record.setCholesterolMmol(lab.getCholesterolTp());
                record.setTriglycerideMmol(lab.getTriglyceride());
                record.setHdlMmol(lab.getHdlC());
                record.setLdlMmol(lab.getLdlC());
                record.setWbc(lab.getWbc());
                record.setRbc(lab.getRbc());
                record.setHgb(lab.getHgb());
                record.setHct(lab.getHct());
                record.setPlt(lab.getPlt());
                record.setAst(lab.getAst());
                record.setAlt(lab.getAlt());
                record.setUre(lab.getUre());
                record.setCreatinine(lab.getCreatinine());
            }
            grouped.put(patientKey, List.of(record));
        }
        return grouped;
    }

    public static class AnalysisResult {

        private List<CriticalPatientAlertDTO> dangerousPatients = new ArrayList<>();
        private List<String> aiInsights = new ArrayList<>();
        private String aiSummary;
        private boolean geminiUsed;
        private int totalDangerousCount;
        private boolean geminiConfigured;
        private String geminiError;
        private String geminiConfigInfo;

        public List<CriticalPatientAlertDTO> getDangerousPatients() {
            return dangerousPatients;
        }

        public void setDangerousPatients(List<CriticalPatientAlertDTO> dangerousPatients) {
            this.dangerousPatients = dangerousPatients;
        }

        public List<String> getAiInsights() {
            return aiInsights;
        }

        public void setAiInsights(List<String> aiInsights) {
            this.aiInsights = aiInsights;
        }

        public String getAiSummary() {
            return aiSummary;
        }

        public void setAiSummary(String aiSummary) {
            this.aiSummary = aiSummary;
        }

        public boolean isGeminiUsed() {
            return geminiUsed;
        }

        public void setGeminiUsed(boolean geminiUsed) {
            this.geminiUsed = geminiUsed;
        }

        public int getTotalDangerousCount() {
            return totalDangerousCount;
        }

        public void setTotalDangerousCount(int totalDangerousCount) {
            this.totalDangerousCount = totalDangerousCount;
        }

        public boolean isGeminiConfigured() {
            return geminiConfigured;
        }

        public void setGeminiConfigured(boolean geminiConfigured) {
            this.geminiConfigured = geminiConfigured;
        }

        public String getGeminiError() {
            return geminiError;
        }

        public void setGeminiError(String geminiError) {
            this.geminiError = geminiError;
        }

        public String getGeminiConfigInfo() {
            return geminiConfigInfo;
        }

        public void setGeminiConfigInfo(String geminiConfigInfo) {
            this.geminiConfigInfo = geminiConfigInfo;
        }
    }

    public static class GeminiAnalysis {
        private boolean used;
        private boolean configured;
        private String error;
        private String configInfo;
        private String overallSummary;
        private List<String> insights = new ArrayList<>();
        private Map<String, PatientGeminiInsight> patientInsights = new HashMap<>();

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

        public String getConfigInfo() {
            return configInfo;
        }

        public void setConfigInfo(String configInfo) {
            this.configInfo = configInfo;
        }

        public String getOverallSummary() {
            return overallSummary;
        }

        public void setOverallSummary(String overallSummary) {
            this.overallSummary = overallSummary;
        }

        public List<String> getInsights() {
            return insights;
        }

        public void setInsights(List<String> insights) {
            this.insights = insights;
        }

        public Map<String, PatientGeminiInsight> getPatientInsights() {
            return patientInsights;
        }

        public void setPatientInsights(Map<String, PatientGeminiInsight> patientInsights) {
            this.patientInsights = patientInsights;
        }
    }

    public static class PatientGeminiInsight {
        private String summary;
        private String riskLevel;
        private int priorityScore;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public int getPriorityScore() {
            return priorityScore;
        }

        public void setPriorityScore(int priorityScore) {
            this.priorityScore = priorityScore;
        }
    }

    public static class PatientDetailGeminiAnalysis {
        private boolean used;
        private boolean configured;
        private String error;
        private String summary;
        private String detailAnalysis;
        private String riskLevel;
        private int priorityScore;
        private List<String> recommendations = new ArrayList<>();

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

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDetailAnalysis() {
            return detailAnalysis;
        }

        public void setDetailAnalysis(String detailAnalysis) {
            this.detailAnalysis = detailAnalysis;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public int getPriorityScore() {
            return priorityScore;
        }

        public void setPriorityScore(int priorityScore) {
            this.priorityScore = priorityScore;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
    }
}
