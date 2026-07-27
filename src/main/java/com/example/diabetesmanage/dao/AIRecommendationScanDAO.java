package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.ThresholdSettings;
import com.example.diabetesmanage.service.DoctorRecommendationGeminiClient;
import com.example.diabetesmanage.service.DoctorRecommendationGeminiClient.GeminiRecommendation;
import com.example.diabetesmanage.service.GeminiRawInput;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tổng hợp dữ liệu sức khỏe và gọi Gemini để tạo khuyến nghị cho bệnh nhân của bác sĩ.
 */
public class AIRecommendationScanDAO {

    private static final int SCAN_DAYS = 30;
    /** Nghỉ ngắn giữa các lần gọi để giảm 429; chỉ chạy khi bác sĩ bấm Đồng bộ. */
    private static final long DELAY_BETWEEN_CALLS_MS = 2_000L;

    private final DoctorAIRecommendationDAO recommendationDAO = new DoctorAIRecommendationDAO();
    private final DoctorRecommendationGeminiClient geminiClient =
            new DoctorRecommendationGeminiClient();

    public ScanResult scan(String doctorId, ThresholdSettings thresholds) {
        return scan(doctorId, thresholds, false);
    }

    public ScanResult scan(String doctorId, ThresholdSettings thresholds, boolean forceRefresh) {
        ScanResult result = new ScanResult();
        if (doctorId == null || doctorId.isBlank() || thresholds == null) {
            return result;
        }

        result.geminiEnabled = geminiClient.isEnabled();
        if (!result.geminiEnabled) {
            result.error = true;
            result.lastError = "Gemini chưa được cấu hình API key.";
            result.lastGeminiError = result.lastError;
            return result;
        }

        try (Connection connection = DBContext.getConnection()) {
            List<String> patientIds = listPatientIds(connection, doctorId);
            result.patientsScanned = patientIds.size();
            boolean firstCall = true;

            for (String patientId : patientIds) {
                PatientMetrics metrics = computeMetrics(connection, patientId, thresholds);
                if (metrics == null) {
                    result.patientsNoRisk++;
                    continue;
                }

                String syncKey = "[SYNC:" + patientId + "]";
                DoctorAIRecommendationDAO.TodayRow today =
                        recommendationDAO.findTodayRow(patientId, syncKey);
                String todayId = today == null ? null : today.id;

                if (todayId != null && !forceRefresh && today.isGemini()) {
                    result.skipped++;
                    continue;
                }

                if (!firstCall) {
                    try {
                        Thread.sleep(DELAY_BETWEEN_CALLS_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                firstCall = false;

                AIAnalysis item = buildFromGemini(patientId, metrics, thresholds, syncKey);
                if (item == null) {
                    result.insertFailed++;
                    result.error = true;
                    String error = geminiClient.getLastError();
                    result.lastGeminiError = error;
                    result.lastError = error;
                    if (error != null
                            && (error.contains("429")
                            || error.toLowerCase().contains("quota"))) {
                        break;
                    }
                    continue;
                }

                result.geminiUsed++;
                String error;
                if (todayId != null) {
                    error = recommendationDAO.updateNarrative(todayId, doctorId, item);
                    if (error == null) {
                        result.refreshed++;
                    }
                } else {
                    error = recommendationDAO.insert(item);
                    if (error == null) {
                        result.created++;
                    }
                }

                if (error != null) {
                    result.insertFailed++;
                    result.error = true;
                    result.lastError = error;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error = true;
            result.lastError = e.getMessage();
        }
        return result;
    }

    private AIAnalysis buildFromGemini(
            String patientId, PatientMetrics metrics, ThresholdSettings thresholds, String syncKey) {
        GeminiRecommendation generated = geminiClient.generate(toGeminiRaw(metrics, thresholds));
        if (generated == null) {
            return null;
        }

        String metricsSummary = "days=" + SCAN_DAYS
                + "; readings=" + metrics.totalReadings
                + "; avgGlucose=" + Math.round(metrics.avgGlucose)
                + "; tir=" + Math.round(metrics.tir * 10) / 10.0
                + "; hypo=" + metrics.hypoCount
                + "; hyper=" + metrics.hyperCount
                + "; danger=" + metrics.dangerCount
                + "; hba1c=" + (metrics.hasHba1c ? metrics.hba1c : "n/a")
                + "; measuredRecently=" + metrics.measuredRecently;

        String usedModel = generated.modelUsed == null
                ? geminiClient.getModelName() : generated.modelUsed;

        AIAnalysis item = new AIAnalysis();
        item.setId(UUID.randomUUID().toString());
        item.setPatientId(patientId);
        item.setDiemNguyCo(generated.diemNguyCo);
        item.setMucCanhBao(generated.mucCanhBao);
        item.setDoTinCay(0.85);
        item.setPhanTichChiTiet(generated.phanTich);
        item.setYeuToNguyCo(generated.yeuToNguyCo);
        item.setKhuyenNghi(generated.khuyenNghi);
        item.setDuLieuDauVao(metricsSummary + "; " + syncKey);
        item.setModelVersion("gemini+" + usedModel);
        item.setTokensSuDung(generated.tokens == null ? 0 : generated.tokens);
        item.setTrangThai("chua_xem");
        return item;
    }

    private List<String> listPatientIds(Connection connection, String doctorId) throws Exception {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM patients WHERE bac_si_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, doctorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(resultSet.getString("id"));
                }
            }
        }
        return ids;
    }

    /**
     * Chỉ tổng hợp số liệu; không tự chấm điểm hoặc sinh nội dung theo luật.
     */
    private PatientMetrics computeMetrics(
            Connection connection, String patientId, ThresholdSettings thresholds) throws Exception {
        PatientMetrics metrics = new PatientMetrics();
        String glucoseSql = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN duong_huyet_mgdl BETWEEN ? AND ? THEN 1 ELSE 0 END) AS in_range,
                       SUM(CASE WHEN duong_huyet_mgdl < ? THEN 1 ELSE 0 END) AS hypo,
                       SUM(CASE WHEN duong_huyet_mgdl >= ? THEN 1 ELSE 0 END) AS danger,
                       SUM(CASE WHEN duong_huyet_mgdl > ? THEN 1 ELSE 0 END) AS hyper,
                       AVG(duong_huyet_mgdl) AS avg_g
                FROM health_records
                WHERE patient_id = ?
                  AND duong_huyet_mgdl IS NOT NULL
                  AND thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)
                """;
        try (PreparedStatement statement = connection.prepareStatement(glucoseSql)) {
            statement.setInt(1, thresholds.getGlucoseLow());
            statement.setInt(2, thresholds.getGlucoseHigh());
            statement.setInt(3, thresholds.getGlucoseLow());
            statement.setInt(4, thresholds.getGlucoseDanger());
            statement.setInt(5, thresholds.getGlucoseHigh());
            statement.setString(6, patientId);
            statement.setInt(7, SCAN_DAYS);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    metrics.totalReadings = resultSet.getInt("total");
                    metrics.inRange = resultSet.getInt("in_range");
                    metrics.hypoCount = resultSet.getInt("hypo");
                    metrics.dangerCount = resultSet.getInt("danger");
                    metrics.hyperCount = resultSet.getInt("hyper");
                    double average = resultSet.getDouble("avg_g");
                    metrics.avgGlucose = resultSet.wasNull() ? 0 : average;
                }
            }
        }
        metrics.tir = metrics.totalReadings == 0
                ? 0 : metrics.inRange * 100.0 / metrics.totalReadings;

        String hba1cSql = """
                SELECT hba1c_percent
                FROM health_records
                WHERE patient_id = ? AND hba1c_percent IS NOT NULL
                ORDER BY thoi_gian_do DESC
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(hba1cSql)) {
            statement.setString(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    metrics.hba1c = resultSet.getDouble(1);
                    metrics.hasHba1c = true;
                }
            }
        }

        String measuredSql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM health_records
                    WHERE patient_id = ?
                      AND thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(measuredSql)) {
            statement.setString(1, patientId);
            statement.setInt(2, thresholds.getDaysNoMeasure());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    metrics.measuredRecently = resultSet.getBoolean(1);
                }
            }
        }

        // Chỉ gọi Gemini khi có tín hiệu rủi ro theo ngưỡng bác sĩ.
        boolean hasGlucoseRisk = metrics.hypoCount > 0
                || metrics.hyperCount > 0
                || metrics.dangerCount > 0;
        boolean hasHba1cRisk = metrics.hasHba1c && metrics.hba1c >= thresholds.getHba1cPoor();
        boolean missedMonitoring = !metrics.measuredRecently;
        if (metrics.totalReadings == 0 && !hasHba1cRisk && !missedMonitoring) {
            return null;
        }
        if (!hasGlucoseRisk && !hasHba1cRisk && !missedMonitoring && metrics.tir >= 70.0) {
            return null;
        }
        return metrics;
    }

    private GeminiRawInput toGeminiRaw(PatientMetrics metrics, ThresholdSettings thresholds) {
        GeminiRawInput raw = new GeminiRawInput();
        raw.scanDays = SCAN_DAYS;
        raw.totalReadings = metrics.totalReadings;
        raw.avgGlucose = metrics.avgGlucose;
        raw.tirPercent = metrics.tir;
        raw.hypoCount = metrics.hypoCount;
        raw.hyperCount = metrics.hyperCount;
        raw.dangerCount = metrics.dangerCount;
        raw.hasHba1c = metrics.hasHba1c;
        raw.hba1c = metrics.hasHba1c ? metrics.hba1c : null;
        raw.measuredRecently = metrics.measuredRecently;
        raw.glucoseLow = thresholds.getGlucoseLow();
        raw.glucoseHigh = thresholds.getGlucoseHigh();
        raw.glucoseDanger = thresholds.getGlucoseDanger();
        raw.hba1cTarget = thresholds.getHba1cTarget();
        raw.hba1cPoor = thresholds.getHba1cPoor();
        raw.daysNoMeasure = thresholds.getDaysNoMeasure();
        return raw;
    }

    private static final class PatientMetrics {
        private int totalReadings;
        private int inRange;
        private int hypoCount;
        private int hyperCount;
        private int dangerCount;
        private double avgGlucose;
        private double tir;
        private double hba1c;
        private boolean hasHba1c;
        private boolean measuredRecently = true;
    }

    public static final class ScanResult {
        private int created;
        private int skipped;
        private int refreshed;
        private int geminiUsed;
        private boolean geminiEnabled;
        private String lastGeminiError;
        private int patientsScanned;
        private int patientsNoRisk;
        private int insertFailed;
        private boolean error;
        private String lastError;

        public int getCreated() { return created; }
        public int getSkipped() { return skipped; }
        public int getRefreshed() { return refreshed; }
        public int getGeminiUsed() { return geminiUsed; }
        public boolean isGeminiEnabled() { return geminiEnabled; }
        public String getLastGeminiError() { return lastGeminiError; }
        public int getPatientsScanned() { return patientsScanned; }
        public int getPatientsNoRisk() { return patientsNoRisk; }
        public int getInsertFailed() { return insertFailed; }
        public boolean isError() { return error; }
        public String getLastError() { return lastError; }
    }
}
