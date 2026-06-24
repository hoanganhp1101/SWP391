package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.DangerousPatientAnalysisResult;
import com.example.diabetesmanage.model.DangerousPatientDetail;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.PatientHealthSnapshot;
import com.example.diabetesmanage.model.UrgentPatientAlert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DangerousPatientService {

    private static final int RECORDS_PER_PATIENT = 20;
    private static final int MAX_GEMINI_CANDIDATES = 15;
    private static final int DISPLAY_LIMIT = 20;

    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final HealthRiskRuleAnalyzer ruleAnalyzer = new HealthRiskRuleAnalyzer();
    private final GeminiHealthAnalysisService geminiService = new GeminiHealthAnalysisService();

    public DangerousPatientAnalysisResult analyzeDangerousPatients(String doctorEmail) {

        DangerousPatientAnalysisResult result = new DangerousPatientAnalysisResult();
        List<PatientHealthSnapshot> dangerousSnapshots = collectDangerousSnapshots(doctorEmail);

        dangerousSnapshots.sort(Comparator
                .comparingInt(PatientHealthSnapshot::getRiskScore).reversed());

        result.setTotalDangerousCount(dangerousSnapshots.size());

        List<PatientHealthSnapshot> geminiCandidates = dangerousSnapshots.subList(
                0,
                Math.min(dangerousSnapshots.size(), MAX_GEMINI_CANDIDATES)
        );

        GeminiHealthAnalysisService.GeminiAnalysis geminiAnalysis =
                geminiService.enrichWithGemini(geminiCandidates);

        result.setGeminiUsed(geminiAnalysis.isUsed());
        result.setGeminiConfigured(geminiAnalysis.isConfigured());
        result.setGeminiError(geminiAnalysis.getError());
        result.setGeminiConfigInfo(geminiAnalysis.getConfigInfo());
        result.setAiSummary(geminiAnalysis.getOverallSummary());

        if (!geminiAnalysis.getInsights().isEmpty()) {
            result.setAiInsights(geminiAnalysis.getInsights());
        }

        List<UrgentPatientAlert> alerts = new ArrayList<>();
        int displayCount = Math.min(dangerousSnapshots.size(), DISPLAY_LIMIT);

        for (int i = 0; i < displayCount; i++) {
            alerts.add(toUrgentAlert(dangerousSnapshots.get(i), geminiAnalysis));
        }

        alerts.sort((a, b) -> Integer.compare(b.getRiskScore(), a.getRiskScore()));
        result.setDangerousPatients(alerts);
        return result;
    }

    public DangerousPatientDetail getDangerousPatientDetail(String doctorEmail, String patientId) {

        Map<String, List<HealthRecord>> recordsByPatient =
                healthRecordDAO.getRecentRecordsGroupedByPatient(
                        doctorEmail,
                        RECORDS_PER_PATIENT
                );

        List<HealthRecord> records = recordsByPatient.getOrDefault(patientId, new ArrayList<>());
        if (records.isEmpty()) {
            return null;
        }

        Patient patient = patientDAO.getPatientByIdAndDoctor(patientId);
        if (patient == null) {
            return null;
        }

        PatientHealthSnapshot snapshot = buildSnapshot(patient, records);
        ruleAnalyzer.analyze(snapshot);

        if (!snapshot.isDangerous()) {
            return null;
        }

        DangerousPatientDetail detail = new DangerousPatientDetail();
        detail.setPatientId(snapshot.getPatientId());
        detail.setPatientCode(snapshot.getPatientCode());
        detail.setPatientName(snapshot.getPatientName());
        detail.setInitials(PatientAlertBuilder.buildInitials(snapshot.getPatientName()));
        detail.setLoaiTieuDuong(snapshot.getLoaiTieuDuong());
        detail.setRiskLevel(PatientAlertBuilder.resolveRiskLevel(snapshot));
        detail.setRiskScore(snapshot.getRiskScore());
        detail.setCritical(snapshot.isCritical());
        detail.setNeedsUrgentReview(snapshot.isCritical());
        detail.setRiskReasons(snapshot.getRiskReasons());
        detail.setMetricTags(PatientAlertBuilder.buildMetricTags(records, snapshot.getRiskReasons()));
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
        detail.setTimeAgo(PatientAlertBuilder.formatTimeAgo(latest.getThoiGianDo()));

        GeminiHealthAnalysisService.PatientDetailGeminiAnalysis geminiDetail =
                geminiService.analyzePatientDetail(snapshot);

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
                detail.setRiskScore(Math.max(snapshot.getRiskScore(), geminiDetail.getPriorityScore()));
            }
        } else {
            detail.setAiSummary(buildFallbackSummary(snapshot));
            detail.setAiDetailAnalysis(buildFallbackDetail(snapshot));
            detail.setAiRecommendations(buildFallbackRecommendations(snapshot));
        }

        return detail;
    }

    private List<PatientHealthSnapshot> collectDangerousSnapshots(String doctorEmail) {

        List<PatientHealthSnapshot> dangerousSnapshots = new ArrayList<>();
        List<Patient> patients = patientDAO.getPatients();
        Map<String, List<HealthRecord>> recordsByPatient =
                healthRecordDAO.getRecentRecordsGroupedByPatient(
                        doctorEmail,
                        RECORDS_PER_PATIENT
                );

        for (Patient patient : patients) {
            List<HealthRecord> records =
                    recordsByPatient.getOrDefault(patient.getId(), new ArrayList<>());

            if (records.isEmpty()) {
                continue;
            }

            PatientHealthSnapshot snapshot = buildSnapshot(patient, records);
            ruleAnalyzer.analyze(snapshot);

            if (snapshot.isDangerous()) {
                dangerousSnapshots.add(snapshot);
            }
        }

        return dangerousSnapshots;
    }

    private PatientHealthSnapshot buildSnapshot(Patient patient, List<HealthRecord> records) {
        PatientHealthSnapshot snapshot = new PatientHealthSnapshot();
        snapshot.setPatientId(patient.getId());
        snapshot.setPatientCode(patient.getPatientCode());
        snapshot.setPatientName(
                patient.getUser() != null ? patient.getUser().getHoTen() : "Không rõ"
        );
        snapshot.setLoaiTieuDuong(patient.getLoaiTieuDuong());
        snapshot.setRecentRecords(records);
        return snapshot;
    }

    private UrgentPatientAlert toUrgentAlert(
            PatientHealthSnapshot snapshot,
            GeminiHealthAnalysisService.GeminiAnalysis geminiAnalysis) {

        UrgentPatientAlert alert = new UrgentPatientAlert();
        alert.setPatientId(snapshot.getPatientId());
        alert.setPatientCode(snapshot.getPatientCode());
        alert.setPatientName(snapshot.getPatientName());
        alert.setLoaiTieuDuong(snapshot.getLoaiTieuDuong());
        alert.setRiskReasons(snapshot.getRiskReasons());
        alert.setRiskScore(snapshot.getRiskScore());
        alert.setCritical(snapshot.isCritical());

        PatientAlertBuilder.populateAlertMetrics(alert, snapshot);

        GeminiHealthAnalysisService.PatientGeminiInsight geminiInsight =
                geminiAnalysis.getPatientInsights().get(snapshot.getPatientCode());

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
                        Math.max(snapshot.getRiskScore(), geminiInsight.getPriorityScore())
                );
            }
        } else {
            alert.setAiSummary(buildFallbackSummary(snapshot));
        }

        return alert;
    }

    private String buildFallbackSummary(PatientHealthSnapshot snapshot) {
        if (snapshot.getRiskReasons().isEmpty()) {
            return "Bệnh nhân có chỉ số cần theo dõi thêm.";
        }
        return String.join(". ", snapshot.getRiskReasons()) + ".";
    }

    private String buildFallbackDetail(PatientHealthSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hệ thống ghi nhận ").append(snapshot.getRiskReasons().size())
                .append(" dấu hiệu bất thường từ ").append(snapshot.getRecentRecords().size())
                .append(" hồ sơ gần đây. ");
        if (!snapshot.getRiskReasons().isEmpty()) {
            sb.append("Các vấn đề chính: ").append(String.join(", ", snapshot.getRiskReasons())).append(".");
        }
        return sb.toString();
    }

    private List<String> buildFallbackRecommendations(PatientHealthSnapshot snapshot) {
        List<String> recommendations = new ArrayList<>();
        for (String reason : snapshot.getRiskReasons()) {
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
}
