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

/**
 * Rule-based AI engine: đọc health_records + threshold_settings → ghi AIAnalysis.
 */
public class AIRecommendationScanDAO {

    private static final int SCAN_DAYS = 30;
    private static final String MODEL_VERSION = "rule-engine-v1";

    private final DoctorAIRecommendationDAO recommendationDAO = new DoctorAIRecommendationDAO();

    public ScanResult scan(String doctorId, ThresholdSettings t) {
        ScanResult result = new ScanResult();
        if (doctorId == null || doctorId.isBlank() || t == null) {
            return result;
        }

        try (Connection conn = new DBContext().getConnection()) {
            List<String> patientIds = listPatientIds(conn, doctorId);
            result.patientsScanned = patientIds.size();

            for (String patientId : patientIds) {
                PatientMetrics m = computeMetrics(conn, patientId, t);
                if (m == null || !m.needsRecommendation()) {
                    result.patientsNoRisk++;
                    continue;
                }

                String syncKey = "[SYNC:" + patientId + "]";
                if (recommendationDAO.hasOpenRecommendationToday(patientId, syncKey)) {
                    result.skipped++;
                    continue;
                }

                AIAnalysis item = buildRecommendation(patientId, m, t, syncKey);
                String insertError = recommendationDAO.insert(item);
                if (insertError == null) {
                    result.created++;
                } else {
                    result.insertFailed++;
                    result.error = true;
                    result.lastError = insertError;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error = true;
        }
        return result;
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

        // Điểm nguy cơ 0–100
        double score = 0;
        if (m.totalReadings > 0 && m.tir < 70) {
            score += Math.min(35, (70 - m.tir) * 0.7);
        }
        score += Math.min(25, m.hypoCount * 8);
        score += Math.min(25, m.dangerCount * 10);
        if (m.hyperCount > 0 && m.dangerCount == 0) {
            score += Math.min(15, m.hyperCount * 3);
        }
        if (m.hasHba1c && m.hba1c >= t.getHba1cPoor()) {
            score += 20;
        } else if (m.hasHba1c && m.hba1c >= t.getHba1cTarget()) {
            score += 10;
        }
        if (!m.measuredRecently) {
            score += 25;
        }
        if (m.totalReadings == 0) {
            score += 20;
        }
        m.score = Math.min(100, Math.round(score));

        if (m.score >= 70) {
            m.level = "nguy_hiem";
        } else if (m.score >= 40) {
            m.level = "cao";
        } else {
            m.level = "trung_binh";
        }
        return m;
    }

    private AIAnalysis buildRecommendation(String patientId, PatientMetrics m, ThresholdSettings t, String syncKey) {
        List<String> factors = new ArrayList<>();
        List<String> advice = new ArrayList<>();
        List<String> analysis = new ArrayList<>();

        if (m.totalReadings > 0) {
            analysis.add(String.format("Trong %d ngày gần nhất có %d lần đo, đường huyết TB %.0f mg/dL, TIR %.1f%% (mục tiêu ≥70%%, khoảng %d–%d).",
                    SCAN_DAYS, m.totalReadings, m.avgGlucose, m.tir, t.getGlucoseLow(), t.getGlucoseHigh()));
            if (m.tir < 70) {
                factors.add(String.format("TIR thấp (%.1f%%)", m.tir));
                advice.add("Tăng tần suất đo đúng khung giờ và rà soát chế độ ăn / carb.");
            }
        } else {
            analysis.add("Chưa có dữ liệu đường huyết trong khoảng thời gian phân tích.");
            factors.add("Thiếu dữ liệu đường huyết");
        }

        if (m.hypoCount > 0) {
            factors.add(m.hypoCount + " lần hạ đường huyết (<" + t.getGlucoseLow() + ")");
            advice.add("Xem lại liều insulin / thuốc hạ đường huyết; nhắc bệnh nhân mang đường hấp thu nhanh.");
        }
        if (m.dangerCount > 0) {
            factors.add(m.dangerCount + " lần đường huyết nguy hiểm (≥" + t.getGlucoseDanger() + ")");
            advice.add("Ưu tiên liên hệ bệnh nhân và kiểm tra tuân thủ điều trị.");
        } else if (m.hyperCount > 0) {
            factors.add(m.hyperCount + " lần vượt ngưỡng cao (>" + t.getGlucoseHigh() + ")");
            advice.add("Theo dõi đường huyết sau ăn và cân nhắc điều chỉnh bolus nếu phù hợp.");
        }

        if (m.hasHba1c) {
            analysis.add(String.format("HbA1c gần nhất: %.1f%% (mục tiêu <%s%%, kém ≥%s%%).",
                    m.hba1c, formatNum(t.getHba1cTarget()), formatNum(t.getHba1cPoor())));
            if (m.hba1c >= t.getHba1cPoor()) {
                factors.add(String.format("HbA1c cao (%.1f%%)", m.hba1c));
                advice.add("Ưu tiên tái khám sớm và đánh giá lại phác đồ dài hạn.");
            } else if (m.hba1c >= t.getHba1cTarget()) {
                factors.add(String.format("HbA1c chưa đạt mục tiêu (%.1f%%)", m.hba1c));
                advice.add("Duy trì theo dõi sát và củng cố tuân thủ điều trị.");
            }
        }

        if (!m.measuredRecently) {
            factors.add("Không đo chỉ số > " + t.getDaysNoMeasure() + " ngày");
            advice.add("Nhắc bệnh nhân đo và đồng bộ chỉ số đúng lịch.");
        }

        if (advice.isEmpty()) {
            advice.add("Duy trì theo dõi hiện tại và tái đánh giá khi có chỉ số mới.");
        }

        StringBuilder khuyenNghi = new StringBuilder();
        for (int i = 0; i < advice.size(); i++) {
            khuyenNghi.append(i + 1).append(". ").append(advice.get(i));
            if (i < advice.size() - 1) {
                khuyenNghi.append("\n");
            }
        }

        AIAnalysis item = new AIAnalysis();
        item.setId(UUID.randomUUID());
        item.setPatientId(UUID.fromString(patientId));
        item.setDiemNguyCo((double) m.score);
        item.setMucCanhBao(m.level);
        item.setDoTinCay(0.75);
        item.setPhanTichChiTiet(String.join(" ", analysis));
        item.setYeuToNguyCo(String.join("; ", factors));
        item.setKhuyenNghi(khuyenNghi.toString());
        item.setDuLieuDauVao("days=" + SCAN_DAYS
                + "; tir=" + Math.round(m.tir * 10) / 10.0
                + "; hypo=" + m.hypoCount
                + "; danger=" + m.dangerCount
                + "; hba1c=" + (m.hasHba1c ? m.hba1c : "n/a")
                + "; " + syncKey);
        item.setModelVersion(MODEL_VERSION);
        item.setTokensSuDung(0);
        item.setTrangThai("chua_xem");
        return item;
    }

    private String formatNum(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.001) {
            return String.valueOf((int) Math.rint(v));
        }
        return String.format("%.1f", v);
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
        long score;
        String level = "thap";

        boolean needsRecommendation() {
            if (score >= 20) {
                return true;
            }
            return hypoCount > 0
                    || dangerCount > 0
                    || hyperCount > 0
                    || !measuredRecently
                    || totalReadings == 0
                    || (hasHba1c && hba1c >= 7)
                    || (totalReadings > 0 && tir < 70);
        }
    }

    public static final class ScanResult {
        public int created;
        public int skipped;
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
