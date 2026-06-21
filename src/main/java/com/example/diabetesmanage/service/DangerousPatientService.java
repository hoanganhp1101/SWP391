package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.DangerousPatientAnalysisResult;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.PatientHealthSnapshot;
import com.example.diabetesmanage.model.UrgentPatientAlert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DangerousPatientService {

    private static final int RECORDS_PER_PATIENT = 20;
    private static final int MAX_GEMINI_CANDIDATES = 15;
    private static final int DISPLAY_LIMIT = 8;

    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final HealthRiskRuleAnalyzer ruleAnalyzer = new HealthRiskRuleAnalyzer();
    private final GeminiHealthAnalysisService geminiService = new GeminiHealthAnalysisService();

    public DangerousPatientAnalysisResult analyzeDangerousPatients(String doctorEmail) {

        DangerousPatientAnalysisResult result = new DangerousPatientAnalysisResult();

        List<Patient> patients = patientDAO.getPatients();
        Map<String, List<HealthRecord>> recordsByPatient =
                healthRecordDAO.getRecentRecordsGroupedByPatient(
                        doctorEmail,
                        RECORDS_PER_PATIENT
                );

        List<PatientHealthSnapshot> dangerousSnapshots = new ArrayList<>();

        for (Patient patient : patients) {

            PatientHealthSnapshot snapshot = new PatientHealthSnapshot();
            snapshot.setPatientId(patient.getId());
            snapshot.setPatientCode(patient.getPatientCode());
            snapshot.setPatientName(
                    patient.getUser() != null ? patient.getUser().getHoTen() : "Không rõ"
            );
            snapshot.setLoaiTieuDuong(patient.getLoaiTieuDuong());

            List<HealthRecord> records =
                    recordsByPatient.getOrDefault(patient.getId(), new ArrayList<>());
            snapshot.setRecentRecords(records);

            ruleAnalyzer.analyze(snapshot);

            if (snapshot.isDangerous()) {
                dangerousSnapshots.add(snapshot);
            }
        }

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
        } else {
            result.setAiInsights(buildFallbackInsights(dangerousSnapshots));
        }

        List<UrgentPatientAlert> alerts = new ArrayList<>();
        int displayCount = Math.min(dangerousSnapshots.size(), DISPLAY_LIMIT);

        for (int i = 0; i < displayCount; i++) {
            PatientHealthSnapshot snapshot = dangerousSnapshots.get(i);
            alerts.add(toUrgentAlert(snapshot, geminiAnalysis));
        }

        if (geminiAnalysis.isUsed()) {
            alerts.sort((a, b) -> Integer.compare(b.getRiskScore(), a.getRiskScore()));
        }

        result.setDangerousPatients(alerts);
        return result;
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

        GeminiHealthAnalysisService.PatientGeminiInsight geminiInsight =
                geminiAnalysis.getPatientInsights().get(snapshot.getPatientCode());

        if (geminiInsight != null) {
            alert.setAiSummary(geminiInsight.getSummary());
            if ("critical".equalsIgnoreCase(geminiInsight.getRiskLevel())) {
                alert.setCritical(true);
            }
            if (geminiInsight.getPriorityScore() > 0) {
                alert.setRiskScore(
                        Math.max(snapshot.getRiskScore(), geminiInsight.getPriorityScore())
                );
            }
        }

        List<HealthRecord> records = snapshot.getRecentRecords();
        if (!records.isEmpty()) {
            HealthRecord latest = records.get(0);
            alert.setDuongHuyetGanNhat(latest.getDuongHuyetMgdl());
            alert.setHuyetApTamThu(latest.getHuyetApTamThu());
            alert.setHuyetApTamTruong(latest.getHuyetApTamTruong());
            alert.setVitalDisplay(buildVitalDisplay(alert));
            alert.setDetectedAgo(formatDetectedAgo(latest.getThoiGianDo()));
        } else {
            alert.setVitalDisplay("Chưa có chỉ số");
            alert.setDetectedAgo("Chưa có dữ liệu gần đây");
        }

        return alert;
    }

    private List<String> buildFallbackInsights(List<PatientHealthSnapshot> snapshots) {

        List<String> insights = new ArrayList<>();

        long criticalCount = snapshots.stream().filter(PatientHealthSnapshot::isCritical).count();
        long noMonitoring = snapshots.stream()
                .filter(s -> s.getRecentRecords().isEmpty())
                .count();

        if (criticalCount > 0) {
            insights.add("Phát hiện " + criticalCount + " bệnh nhân có chỉ số nguy hiểm cần can thiệp ngay.");
        }
        if (noMonitoring > 0) {
            insights.add(noMonitoring + " bệnh nhân chưa có hoặc thiếu hồ sơ theo dõi sức khỏe.");
        }
        if (insights.isEmpty() && !snapshots.isEmpty()) {
            insights.add("Có " + snapshots.size() + " hồ sơ cần bác sĩ xem xét dựa trên chỉ số bất thường.");
        }

        return insights;
    }

    private String buildVitalDisplay(UrgentPatientAlert alert) {

        if (alert.getDuongHuyetGanNhat() != null) {
            if (alert.getDuongHuyetGanNhat() < 70) {
                return String.format("Đường huyết thấp: %.0f mg/dL", alert.getDuongHuyetGanNhat());
            }
            if (alert.getDuongHuyetGanNhat() >= 180) {
                return String.format("Đường huyết: %.0f mg/dL", alert.getDuongHuyetGanNhat());
            }
        }

        if (alert.getHuyetApTamThu() != null && alert.getHuyetApTamTruong() != null) {
            return String.format(
                    "Huyết áp: %d/%d",
                    alert.getHuyetApTamThu(),
                    alert.getHuyetApTamTruong()
            );
        }

        if (alert.getDuongHuyetGanNhat() != null) {
            return String.format("Đường huyết: %.0f mg/dL", alert.getDuongHuyetGanNhat());
        }

        return alert.getRiskReasons().isEmpty()
                ? "Chỉ số bất thường"
                : alert.getRiskReasons().get(0);
    }

    private String formatDetectedAgo(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "Chưa có dữ liệu gần đây";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "Vừa phát hiện";
        }
        if (minutes < 60) {
            return "Phát hiện cách đây " + minutes + " phút";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return "Phát hiện cách đây " + hours + " giờ";
        }

        return "Phát hiện cách đây " + duration.toDays() + " ngày";
    }
}
