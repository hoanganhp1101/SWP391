package dal;

import config.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.AIAnalysis;
import model.ThresholdSettings;
import service.GeminiClient;
import service.GeminiClient.GeminiRecommendation;
import service.GeminiRawInput;

/**
 * Gemini-only: gom số liệu health_records → gọi Gemini sinh cả danh sách khuyến nghị.
 */
public class AIRecommendationScanDAO {

    private static final int SCAN_DAYS = 30;
    /** Nghỉ giữa các lần gọi để giảm 429 (free tier ~5 RPM). */
    private static final long DELAY_BETWEEN_CALLS_MS = 12_000L;

    private final DoctorAIRecommendationDAO recommendationDAO = new DoctorAIRecommendationDAO();
    private final GeminiClient geminiClient = new GeminiClient();

    public ScanResult scan(String doctorId, ThresholdSettings t) {
        return scan(doctorId, t, false);
    }

    /**
     * @param forceRefresh true = viết lại toàn bộ BN bằng Gemini; false = chỉ tạo BN chưa có bản hôm nay
     */
    public ScanResult scan(String doctorId, ThresholdSettings t, boolean forceRefresh) {
        ScanResult result = new ScanResult();
        if (doctorId == null || doctorId.isBlank() || t == null) {
            return result;
        }
        result.geminiEnabled = geminiClient.isEnabled();
        if (!result.geminiEnabled) {
            result.error = true;
            result.lastError = "Gemini đang tắt. Bật gemini.enabled=true và điền API key.";
            result.lastGeminiError = result.lastError;
            return result;
        }

        try (Connection conn = new DBContext().getConnection()) {
            List<String> patientIds = listPatientIds(conn, doctorId);
            result.patientsScanned = patientIds.size();
            boolean firstCall = true;

            for (String patientId : patientIds) {
                PatientMetrics m = computeMetrics(conn, patientId, t);
                if (m == null) {
                    result.patientsNoRisk++;
                    continue;
                }

                String syncKey = "[SYNC:" + patientId + "]";
                DoctorAIRecommendationDAO.TodayRow today = recommendationDAO.findTodayRow(patientId, syncKey);
                String todayId = today == null ? null : today.id;

                if (todayId != null && !forceRefresh) {
                    // Đã có bản Gemini hôm nay → giữ; bản rule cũ → ghi đè bằng Gemini
                    if (today != null && today.isGemini()) {
                        result.skipped++;
                        continue;
                    }
                }

                if (!firstCall) {
                    try {
                        Thread.sleep(DELAY_BETWEEN_CALLS_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                firstCall = false;

                AIAnalysis item = buildFromGemini(patientId, m, t, syncKey);
                if (item == null) {
                    result.insertFailed++;
                    result.error = true;
                    String err = geminiClient.getLastError();
                    result.lastGeminiError = err;
                    result.lastError = err;
                    // 429 → dừng các BN còn lại
                    if (err != null && (err.contains("429") || err.toLowerCase().contains("quota"))) {
                        result.lastGeminiError = err + " | Đã dừng các BN còn lại vì quota. Đợi rồi bấm Đồng bộ lại.";
                        break;
                    }
                    continue;
                }

                result.geminiUsed++;

                if (todayId != null) {
                    String err = recommendationDAO.updateNarrative(todayId, doctorId, item);
                    if (err == null) {
                        result.refreshed++;
                    } else {
                        result.insertFailed++;
                        result.error = true;
                        result.lastError = err;
                    }
                } else {
                    String insertError = recommendationDAO.insert(item);
                    if (insertError == null) {
                        result.created++;
                    } else {
                        result.insertFailed++;
                        result.error = true;
                        result.lastError = insertError;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error = true;
            result.lastError = e.getMessage();
        }
        return result;
    }

    private AIAnalysis buildFromGemini(String patientId, PatientMetrics m, ThresholdSettings t, String syncKey) {
        GeminiRecommendation g = geminiClient.generate(toGeminiRaw(m, t));
        if (g == null) {
            return null;
        }

        String metricsSummary = "days=" + SCAN_DAYS
                + "; readings=" + m.totalReadings
                + "; avgGlucose=" + Math.round(m.avgGlucose)
                + "; tir=" + Math.round(m.tir * 10) / 10.0
                + "; hypo=" + m.hypoCount
                + "; hyper=" + m.hyperCount
                + "; danger=" + m.dangerCount
                + "; hba1c=" + (m.hasHba1c ? m.hba1c : "n/a")
                + "; measuredRecently=" + m.measuredRecently;

        String usedModel = g.modelUsed == null ? geminiClient.getModelName() : g.modelUsed;

        AIAnalysis item = new AIAnalysis();
        item.setId(UUID.randomUUID());
        item.setPatientId(UUID.fromString(patientId));
        item.setDiemNguyCo(g.diemNguyCo);
        item.setMucCanhBao(g.mucCanhBao);
        item.setDoTinCay(0.85);
        item.setPhanTichChiTiet(g.phanTich);
        item.setYeuToNguyCo(g.yeuToNguyCo);
        item.setKhuyenNghi(g.khuyenNghi);
        item.setDuLieuDauVao(metricsSummary + "; " + syncKey);
        item.setModelVersion("gemini+" + usedModel);
        item.setTokensSuDung(g.tokens == null ? 0 : g.tokens);
        item.setTrangThai("chua_xem");
        return item;
    }

    private List<String> listPatientIds(Connection conn, String doctorId) throws Exception {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM patients WHERE bac_si_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
            }
        }
        return ids;
    }

    /** Chỉ gom số liệu — không chấm điểm / không sinh text rule. */
    private PatientMetrics computeMetrics(Connection conn, String patientId, ThresholdSettings t) throws Exception {
        PatientMetrics m = new PatientMetrics();

        String glucoseSql = """
                SELECT
                    COUNT(*) AS total,
                    SUM(CASE WHEN duong_huyet_mgdl BETWEEN ? AND ? THEN 1 ELSE 0 END) AS in_range,
                    SUM(CASE WHEN duong_huyet_mgdl < ? THEN 1 ELSE 0 END) AS hypo,
                    SUM(CASE WHEN duong_huyet_mgdl >= ? THEN 1 ELSE 0 END) AS danger,
                    SUM(CASE WHEN duong_huyet_mgdl > ? THEN 1 ELSE 0 END) AS hyper,
                    AVG(duong_huyet_mgdl) AS avg_g
                FROM health_records
                WHERE patient_id = ?
                  AND duong_huyet_mgdl IS NOT NULL
                  AND thoi_gian_do >= DATEADD(DAY, ?, GETDATE())
                """;

        try (PreparedStatement ps = conn.prepareStatement(glucoseSql)) {
            ps.setInt(1, t.getGlucoseLow());
            ps.setInt(2, t.getGlucoseHigh());
            ps.setInt(3, t.getGlucoseLow());
            ps.setInt(4, t.getGlucoseDanger());
            ps.setInt(5, t.getGlucoseHigh());
            ps.setString(6, patientId);
            ps.setInt(7, -SCAN_DAYS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.totalReadings = rs.getInt("total");
                    m.inRange = rs.getInt("in_range");
                    m.hypoCount = rs.getInt("hypo");
                    m.dangerCount = rs.getInt("danger");
                    m.hyperCount = rs.getInt("hyper");
                    double avg = rs.getDouble("avg_g");
                    m.avgGlucose = rs.wasNull() ? 0 : avg;
                }
            }
        }

        m.tir = m.totalReadings == 0 ? 0 : (m.inRange * 100.0 / m.totalReadings);

        String hba1cSql = """
                SELECT TOP 1 hba1c_percent
                FROM health_records
                WHERE patient_id = ? AND hba1c_percent IS NOT NULL
                ORDER BY thoi_gian_do DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(hba1cSql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.hba1c = rs.getDouble(1);
                    m.hasHba1c = true;
                }
            }
        }

        String measuredSql = """
                SELECT CASE WHEN EXISTS (
                    SELECT 1 FROM health_records
                    WHERE patient_id = ? AND thoi_gian_do >= DATEADD(DAY, ?, GETDATE())
                ) THEN 1 ELSE 0 END
                """;
        try (PreparedStatement ps = conn.prepareStatement(measuredSql)) {
            ps.setString(1, patientId);
            ps.setInt(2, -t.getDaysNoMeasure());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.measuredRecently = rs.getInt(1) == 1;
                }
            }
        }
        return m;
    }

    private GeminiRawInput toGeminiRaw(PatientMetrics m, ThresholdSettings t) {
        GeminiRawInput raw = new GeminiRawInput();
        raw.scanDays = SCAN_DAYS;
        raw.totalReadings = m.totalReadings;
        raw.avgGlucose = m.avgGlucose;
        raw.tirPercent = m.tir;
        raw.hypoCount = m.hypoCount;
        raw.hyperCount = m.hyperCount;
        raw.dangerCount = m.dangerCount;
        raw.hasHba1c = m.hasHba1c;
        raw.hba1c = m.hasHba1c ? m.hba1c : null;
        raw.measuredRecently = m.measuredRecently;
        raw.glucoseLow = t.getGlucoseLow();
        raw.glucoseHigh = t.getGlucoseHigh();
        raw.glucoseDanger = t.getGlucoseDanger();
        raw.hba1cTarget = t.getHba1cTarget();
        raw.hba1cPoor = t.getHba1cPoor();
        raw.daysNoMeasure = t.getDaysNoMeasure();
        return raw;
    }

    private static final class PatientMetrics {
        int totalReadings;
        int inRange;
        int hypoCount;
        int hyperCount;
        int dangerCount;
        double avgGlucose;
        double tir;
        double hba1c;
        boolean hasHba1c;
        boolean measuredRecently = true;
    }

    public static final class ScanResult {
        public int created;
        public int skipped;
        public int refreshed;
        public int geminiUsed;
        public boolean geminiEnabled;
        public String lastGeminiError;
        public int patientsScanned;
        public int patientsNoRisk;
        public int insertFailed;
        public boolean error;
        public String lastError;

        public int getCreated() {
            return created;
        }

        public int getSkipped() {
            return skipped;
        }

        public int getRefreshed() {
            return refreshed;
        }

        public int getGeminiUsed() {
            return geminiUsed;
        }

        public boolean isGeminiEnabled() {
            return geminiEnabled;
        }

        public String getLastGeminiError() {
            return lastGeminiError;
        }

        public int getPatientsScanned() {
            return patientsScanned;
        }

        public int getPatientsNoRisk() {
            return patientsNoRisk;
        }

        public int getInsertFailed() {
            return insertFailed;
        }

        public String getLastError() {
            return lastError;
        }

        public boolean isError() {
            return error;
        }
    }
}
