package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.ThresholdSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Quét bản ghi sức khỏe của bệnh nhân thuộc bác sĩ và tạo cảnh báo chưa đọc.
 */
public class AlertScanDAO {

    private static final int SCAN_DAYS = 30;
    private static final String TYPE_GLUCOSE = "duong_huyet_cao";
    private static final String TYPE_MISSED = "khong_do_lien_tuc";

    public ScanResult scanAndCreateAlerts(String doctorId, ThresholdSettings thresholds) {
        ScanResult result = new ScanResult();
        if (doctorId == null || doctorId.isBlank() || thresholds == null) {
            return result;
        }

        try (Connection connection = DBContext.getConnection()) {
            int[] glucose = scanGlucose(connection, doctorId, thresholds);
            result.createdGlucose = glucose[0];
            result.skippedDuplicates += glucose[1];

            int[] missed = scanNotMeasured(connection, doctorId, thresholds);
            result.createdMissed = missed[0];
            result.skippedDuplicates += missed[1];
        } catch (Exception e) {
            // Cảnh báo là tính năng bổ trợ; lỗi quét không được làm hỏng trang danh sách.
            System.err.println("Không thể quét cảnh báo tự động: " + e.getMessage());
            result.error = true;
        }
        return result;
    }

    private int[] scanGlucose(
            Connection connection, String doctorId, ThresholdSettings thresholds) throws Exception {
        String sql = """
                SELECT hr.id, hr.patient_id, hr.duong_huyet_mgdl, hr.thoi_gian_do
                FROM health_records hr
                JOIN patients p ON hr.patient_id = p.id
                WHERE p.bac_si_id = ?
                  AND hr.duong_huyet_mgdl IS NOT NULL
                  AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)
                  AND (hr.duong_huyet_mgdl < ? OR hr.duong_huyet_mgdl > ?)
                ORDER BY hr.thoi_gian_do DESC
                """;

        int created = 0;
        int skipped = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, doctorId);
            statement.setInt(2, SCAN_DAYS);
            statement.setInt(3, thresholds.getGlucoseLow());
            statement.setInt(4, thresholds.getGlucoseHigh());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String healthRecordId = resultSet.getString("id");
                    String patientId = resultSet.getString("patient_id");
                    double glucose = resultSet.getDouble("duong_huyet_mgdl");
                    Timestamp measuredAt = resultSet.getTimestamp("thoi_gian_do");
                    String marker = "[HR:" + healthRecordId + "]";

                    if (similarUnreadAlertExists(connection, patientId, TYPE_GLUCOSE, marker)) {
                        skipped++;
                        continue;
                    }

                    String severity;
                    String title;
                    String content;
                    if (glucose < thresholds.getGlucoseLow()) {
                        severity = "nguy_hiem";
                        title = "Hạ đường huyết";
                        content = String.format(
                                "Đường huyết %.0f mg/dL, dưới ngưỡng %d mg/dL. Thời điểm đo: %s. %s",
                                glucose, thresholds.getGlucoseLow(), measuredAt, marker);
                    } else if (glucose >= thresholds.getGlucoseDanger()) {
                        severity = "nguy_hiem";
                        title = "Đường huyết nguy hiểm";
                        content = String.format(
                                "Đường huyết %.0f mg/dL, đạt ngưỡng nguy hiểm %d mg/dL. Thời điểm đo: %s. %s",
                                glucose, thresholds.getGlucoseDanger(), measuredAt, marker);
                    } else {
                        severity = "cao";
                        title = "Đường huyết cao";
                        content = String.format(
                                "Đường huyết %.0f mg/dL, vượt ngưỡng %d mg/dL. Thời điểm đo: %s. %s",
                                glucose, thresholds.getGlucoseHigh(), measuredAt, marker);
                    }

                    if (insertAlert(
                            connection, patientId, TYPE_GLUCOSE, severity, title, content)) {
                        created++;
                    }
                }
            }
        }
        return new int[]{created, skipped};
    }

    private int[] scanNotMeasured(
            Connection connection, String doctorId, ThresholdSettings thresholds) throws Exception {
        String sql = """
                SELECT p.id AS patient_id
                FROM patients p
                WHERE p.bac_si_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM health_records hr
                      WHERE hr.patient_id = p.id
                        AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)
                  )
                """;

        int created = 0;
        int skipped = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, doctorId);
            statement.setInt(2, thresholds.getDaysNoMeasure());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String patientId = resultSet.getString("patient_id");
                    String marker = "[MISS:" + patientId + ":" + thresholds.getDaysNoMeasure() + "]";
                    if (similarUnreadAlertExists(connection, patientId, TYPE_MISSED, marker)) {
                        skipped++;
                        continue;
                    }

                    String title = "Không đo chỉ số quá "
                            + thresholds.getDaysNoMeasure() + " ngày";
                    String content = "Bệnh nhân không có lần đo nào trong "
                            + thresholds.getDaysNoMeasure()
                            + " ngày gần nhất theo ngưỡng của bác sĩ. " + marker;
                    if (insertAlert(
                            connection, patientId, TYPE_MISSED, "cao", title, content)) {
                        created++;
                    }
                }
            }
        }
        return new int[]{created, skipped};
    }

    private boolean similarUnreadAlertExists(
            Connection connection, String patientId, String type, String marker) throws Exception {
        // Dedup theo marker HR trong ngày — kể cả alert đã đọc — tránh tạo trùng khi quét lại.
        String sql = """
                SELECT 1
                FROM alerts
                WHERE patient_id = ?
                  AND loai_canh_bao = ?
                  AND DATE(thoi_gian_tao) = CURDATE()
                  AND noi_dung LIKE ?
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientId);
            statement.setString(2, type);
            statement.setString(3, "%" + marker + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean insertAlert(
            Connection connection,
            String patientId,
            String type,
            String severity,
            String title,
            String content
    ) throws Exception {
        String sql = """
                INSERT INTO alerts (
                    id, patient_id, ai_analysis_id, loai_canh_bao, muc_do, tieu_de, noi_dung,
                    da_doc_bn, da_doc_bs, xu_ly_boi, ghi_chu_xu_ly,
                    thoi_gian_tao, thoi_gian_xu_ly
                ) VALUES (?, ?, NULL, ?, ?, ?, ?, 0, 0, NULL, NULL, NOW(), NULL)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, patientId);
            statement.setString(3, type);
            statement.setString(4, severity);
            statement.setString(5, title);
            statement.setString(6, content);
            return statement.executeUpdate() > 0;
        }
    }

    public static final class ScanResult {
        private int createdGlucose;
        private int createdMissed;
        private int skippedDuplicates;
        private boolean error;

        public int getTotalCreated() {
            return createdGlucose + createdMissed;
        }

        public int getSkippedDuplicates() {
            return skippedDuplicates;
        }

        public boolean isError() {
            return error;
        }
    }
}
