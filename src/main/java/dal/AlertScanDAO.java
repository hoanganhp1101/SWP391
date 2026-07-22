package dal;

import config.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;
import model.ThresholdSettings;

/**
 * Sinh cảnh báo từ health_records theo ngưỡng của bác sĩ, ghi vào bảng alerts.
 * Màn Alert chỉ đọc alerts — không query health_records để hiển thị danh sách.
 */
public class AlertScanDAO {

    private static final int SCAN_DAYS = 30;
    private static final String TYPE_GLUCOSE = "duong_huyet_cao";
    private static final String TYPE_MISSED = "khong_do_lien_tuc";

    public ScanResult scanAndCreateAlerts(String doctorId, ThresholdSettings t) {
        ScanResult result = new ScanResult();
        if (doctorId == null || doctorId.isBlank() || t == null) {
            return result;
        }

        try (Connection conn = new DBContext().getConnection()) {
            int[] glucose = scanGlucoseReadings(conn, doctorId, t);
            result.createdGlucose = glucose[0];
            result.skippedDuplicates += glucose[1];

            int[] missed = scanNotMeasured(conn, doctorId, t);
            result.createdMissed = missed[0];
            result.skippedDuplicates += missed[1];
        } catch (Exception e) {
            e.printStackTrace();
            result.error = true;
        }
        return result;
    }

    /** @return int[]{created, skipped} */
    private int[] scanGlucoseReadings(Connection conn, String doctorId, ThresholdSettings t) throws Exception {
        String sql = """
                SELECT hr.id, hr.patient_id, hr.duong_huyet_mgdl, hr.thoi_gian_do
                FROM health_records hr
                JOIN patients p ON hr.patient_id = p.id
                WHERE p.bac_si_id = ?
                  AND hr.duong_huyet_mgdl IS NOT NULL
                  AND hr.thoi_gian_do >= DATEADD(DAY, ?, GETDATE())
                  AND (hr.duong_huyet_mgdl < ? OR hr.duong_huyet_mgdl > ?)
                ORDER BY hr.thoi_gian_do DESC
                """;

        int created = 0;
        int skipped = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            ps.setInt(2, -SCAN_DAYS);
            ps.setInt(3, t.getGlucoseLow());
            ps.setInt(4, t.getGlucoseHigh());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String hrId = rs.getString("id");
                    String patientId = rs.getString("patient_id");
                    double glucose = rs.getDouble("duong_huyet_mgdl");
                    Timestamp measuredAt = rs.getTimestamp("thoi_gian_do");

                    if (alertExistsForHealthRecord(conn, hrId)) {
                        skipped++;
                        continue;
                    }

                    String mucDo;
                    String tieuDe;
                    String noiDung;
                    if (glucose < t.getGlucoseLow()) {
                        mucDo = "nguy_hiem";
                        tieuDe = "Hạ đường huyết";
                        noiDung = String.format(
                                "Đường huyết %.0f mg/dL (dưới ngưỡng %d). Thời điểm đo: %s. [HR:%s]",
                                glucose, t.getGlucoseLow(), measuredAt, hrId);
                    } else if (glucose >= t.getGlucoseDanger()) {
                        mucDo = "nguy_hiem";
                        tieuDe = "Đường huyết nguy hiểm";
                        noiDung = String.format(
                                "Đường huyết %.0f mg/dL (vượt ngưỡng nguy hiểm ≥ %d). Thời điểm đo: %s. [HR:%s]",
                                glucose, t.getGlucoseDanger(), measuredAt, hrId);
                    } else {
                        mucDo = "cao";
                        tieuDe = "Đường huyết cao";
                        noiDung = String.format(
                                "Đường huyết %.0f mg/dL (vượt ngưỡng cao > %d). Thời điểm đo: %s. [HR:%s]",
                                glucose, t.getGlucoseHigh(), measuredAt, hrId);
                    }

                    if (insertAlert(conn, patientId, TYPE_GLUCOSE, mucDo, tieuDe, noiDung, measuredAt)) {
                        created++;
                    }
                }
            }
        }
        return new int[]{created, skipped};
    }

    /** @return int[]{created, skipped} */
    private int[] scanNotMeasured(Connection conn, String doctorId, ThresholdSettings t) throws Exception {
        String sql = """
                SELECT p.id AS patient_id
                FROM patients p
                WHERE p.bac_si_id = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM health_records hr
                      WHERE hr.patient_id = p.id
                        AND hr.thoi_gian_do >= DATEADD(DAY, ?, GETDATE())
                  )
                """;

        int created = 0;
        int skipped = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            ps.setInt(2, -t.getDaysNoMeasure());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String patientId = rs.getString("patient_id");
                    String marker = "[MISS:" + patientId + ":" + t.getDaysNoMeasure() + "]";

                    if (openMissedAlertExists(conn, patientId, marker)) {
                        skipped++;
                        continue;
                    }

                    String tieuDe = "Không đo chỉ số > " + t.getDaysNoMeasure() + " ngày";
                    String noiDung = "Bệnh nhân không có lần đo nào trong "
                            + t.getDaysNoMeasure()
                            + " ngày gần nhất (theo ngưỡng bác sĩ cấu hình). " + marker;

                    if (insertAlert(conn, patientId, TYPE_MISSED, "cao", tieuDe, noiDung, null)) {
                        created++;
                    }
                }
            }
        }
        return new int[]{created, skipped};
    }

    private boolean alertExistsForHealthRecord(Connection conn, String healthRecordId) throws Exception {
        String sql = "SELECT 1 FROM alerts WHERE noi_dung LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%[HR:" + healthRecordId + "]%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean openMissedAlertExists(Connection conn, String patientId, String marker) throws Exception {
        String sql = """
                SELECT 1 FROM alerts
                WHERE patient_id = ?
                  AND loai_canh_bao = ?
                  AND thoi_gian_xu_ly IS NULL
                  AND noi_dung LIKE ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setString(2, TYPE_MISSED);
            ps.setString(3, "%" + marker + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean insertAlert(Connection conn, String patientId, String loai, String mucDo,
                                String tieuDe, String noiDung, Timestamp measuredAt) throws Exception {
        String sql = """
                INSERT INTO alerts (
                    id, patient_id, ai_analysis_id, loai_canh_bao, muc_do, tieu_de, noi_dung,
                    da_doc_bn, da_doc_bs, xu_ly_boi, ghi_chu_xu_ly, thoi_gian_tao, thoi_gian_xu_ly
                ) VALUES (?, ?, NULL, ?, ?, ?, ?, 0, 0, NULL, NULL, COALESCE(?, GETDATE()), NULL)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, patientId);
            ps.setString(3, loai);
            ps.setString(4, mucDo);
            ps.setString(5, tieuDe);
            ps.setString(6, noiDung);
            if (measuredAt != null) {
                ps.setTimestamp(7, measuredAt);
            } else {
                ps.setTimestamp(7, null);
            }
            return ps.executeUpdate() > 0;
        }
    }

    public static final class ScanResult {
        public int createdGlucose;
        public int createdMissed;
        public int skippedDuplicates;
        public boolean error;

        public int getTotalCreated() {
            return createdGlucose + createdMissed;
        }

        public boolean isError() {
            return error;
        }
    }
}
