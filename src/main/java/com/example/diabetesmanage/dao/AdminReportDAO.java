package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.AdminReportStats;
import com.example.diabetesmanage.model.ReportBucket;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminReportDAO {

    public AdminReportStats getReportStats(int periodDays) {
        AdminReportStats stats = new AdminReportStats();
        stats.setPeriodDays(periodDays);

        try (Connection conn = DBContext.getConnection()) {
            stats.setTotalPatients(count(conn, "SELECT COUNT(*) FROM patients"));
            loadDiseaseControlStats(conn, stats, periodDays);
            loadAlertStats(conn, stats, periodDays);
            loadAppointmentStats(conn, stats, periodDays);
            loadBuckets(conn, stats, periodDays);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stats;
    }

    private void loadDiseaseControlStats(Connection conn, AdminReportStats stats, int periodDays) throws Exception {
        String recentFilter = " AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL " + periodDays + " DAY)";

        String glucoseSql =
                "SELECT COUNT(*) AS measured, " +
                "AVG(latest_glucose) AS avg_glucose, " +
                "SUM(CASE WHEN latest_glucose BETWEEN 70 AND 180 THEN 1 ELSE 0 END) AS controlled, " +
                "SUM(CASE WHEN latest_glucose > 180 AND latest_glucose < 250 THEN 1 ELSE 0 END) AS high, " +
                "SUM(CASE WHEN latest_glucose >= 250 OR latest_glucose < 70 THEN 1 ELSE 0 END) AS critical " +
                "FROM (" +
                "  SELECT hr.patient_id, hr.duong_huyet_mgdl AS latest_glucose " +
                "  FROM health_records hr " +
                "  JOIN (" +
                "    SELECT patient_id, MAX(thoi_gian_do) AS latest_time " +
                "    FROM health_records " +
                "    WHERE duong_huyet_mgdl IS NOT NULL AND thoi_gian_do >= DATE_SUB(NOW(), INTERVAL " + periodDays + " DAY) " +
                "    GROUP BY patient_id" +
                "  ) latest ON latest.patient_id = hr.patient_id AND latest.latest_time = hr.thoi_gian_do " +
                "  WHERE hr.duong_huyet_mgdl IS NOT NULL" +
                ") latest_values";

        try (PreparedStatement ps = conn.prepareStatement(glucoseSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setPatientsWithRecentMeasurements(rs.getInt("measured"));
                stats.setAverageGlucose(rs.getDouble("avg_glucose"));
                stats.setControlledGlucoseCount(rs.getInt("controlled"));
                stats.setHighGlucoseCount(rs.getInt("high"));
                stats.setCriticalGlucoseCount(rs.getInt("critical"));
            }
        }

        String hba1cSql =
                "SELECT AVG(hr.hba1c_percent) AS avg_hba1c, " +
                "SUM(CASE WHEN hr.hba1c_percent < 7 THEN 1 ELSE 0 END) AS controlled, " +
                "SUM(CASE WHEN hr.hba1c_percent >= 8 THEN 1 ELSE 0 END) AS high " +
                "FROM health_records hr WHERE hr.hba1c_percent IS NOT NULL" + recentFilter;
        try (PreparedStatement ps = conn.prepareStatement(hba1cSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setAverageHba1c(rs.getDouble("avg_hba1c"));
                stats.setControlledHba1cCount(rs.getInt("controlled"));
                stats.setHighHba1cCount(rs.getInt("high"));
            }
        }

        String bpSql =
                "SELECT " +
                "SUM(CASE WHEN huyet_ap_tam_thu < 140 AND huyet_ap_tam_truong < 90 THEN 1 ELSE 0 END) AS controlled, " +
                "SUM(CASE WHEN huyet_ap_tam_thu >= 140 OR huyet_ap_tam_truong >= 90 THEN 1 ELSE 0 END) AS high " +
                "FROM health_records hr " +
                "WHERE hr.huyet_ap_tam_thu IS NOT NULL AND hr.huyet_ap_tam_truong IS NOT NULL" + recentFilter;
        try (PreparedStatement ps = conn.prepareStatement(bpSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setControlledBloodPressureCount(rs.getInt("controlled"));
                stats.setHighBloodPressureCount(rs.getInt("high"));
            }
        }
    }

    private void loadAlertStats(Connection conn, AdminReportStats stats, int periodDays) throws Exception {
        String sql =
                "SELECT COUNT(*) AS total_alerts, " +
                "SUM(CASE WHEN muc_do IN ('cao', 'nguy_hiem') THEN 1 ELSE 0 END) AS high_alerts, " +
                "SUM(CASE WHEN da_doc_bs = 0 THEN 1 ELSE 0 END) AS unread_alerts " +
                "FROM alerts WHERE thoi_gian_tao >= DATE_SUB(NOW(), INTERVAL " + periodDays + " DAY)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalAlerts(rs.getInt("total_alerts"));
                stats.setHighAlerts(rs.getInt("high_alerts"));
                stats.setUnreadDoctorAlerts(rs.getInt("unread_alerts"));
            }
        }
    }

    private void loadAppointmentStats(Connection conn, AdminReportStats stats, int periodDays) throws Exception {
        String sql =
                "SELECT COUNT(*) AS total_appointments, " +
                "SUM(CASE WHEN trang_thai = 'cho_kham' THEN 1 ELSE 0 END) AS pending, " +
                "SUM(CASE WHEN trang_thai = 'da_kham' THEN 1 ELSE 0 END) AS completed, " +
                "SUM(CASE WHEN trang_thai = 'da_huy' THEN 1 ELSE 0 END) AS cancelled, " +
                "SUM(CASE WHEN trang_thai = 'cho_kham' AND thoi_gian_hen BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS upcoming, " +
                "SUM(CASE WHEN LOWER(dia_diem) LIKE '%online%' OR LOWER(dia_diem) LIKE '%telehealth%' OR LOWER(dia_diem) LIKE '%video%' THEN 1 ELSE 0 END) AS telehealth " +
                "FROM appointments WHERE thoi_gian_hen >= DATE_SUB(NOW(), INTERVAL " + periodDays + " DAY)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalAppointments(rs.getInt("total_appointments"));
                stats.setPendingAppointments(rs.getInt("pending"));
                stats.setCompletedAppointments(rs.getInt("completed"));
                stats.setCancelledAppointments(rs.getInt("cancelled"));
                stats.setUpcomingAppointments(rs.getInt("upcoming"));
                stats.setTelehealthAppointments(rs.getInt("telehealth"));
            }
        }
    }

    private void loadBuckets(Connection conn, AdminReportStats stats, int periodDays) throws Exception {
        String glucoseBucketSql =
                "SELECT " +
                "SUM(CASE WHEN duong_huyet_mgdl < 70 THEN 1 ELSE 0 END) AS low_count, " +
                "SUM(CASE WHEN duong_huyet_mgdl BETWEEN 70 AND 180 THEN 1 ELSE 0 END) AS controlled_count, " +
                "SUM(CASE WHEN duong_huyet_mgdl > 180 AND duong_huyet_mgdl < 250 THEN 1 ELSE 0 END) AS high_count, " +
                "SUM(CASE WHEN duong_huyet_mgdl >= 250 THEN 1 ELSE 0 END) AS critical_count " +
                "FROM health_records WHERE duong_huyet_mgdl IS NOT NULL AND thoi_gian_do >= DATE_SUB(NOW(), INTERVAL " + periodDays + " DAY)";
        try (PreparedStatement ps = conn.prepareStatement(glucoseBucketSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.getGlucoseBuckets().add(new ReportBucket("Hạ đường huyết", rs.getInt("low_count")));
                stats.getGlucoseBuckets().add(new ReportBucket("Kiểm soát tốt", rs.getInt("controlled_count")));
                stats.getGlucoseBuckets().add(new ReportBucket("Cao", rs.getInt("high_count")));
                stats.getGlucoseBuckets().add(new ReportBucket("Nguy hiểm", rs.getInt("critical_count")));
            }
        }

        stats.getAppointmentBuckets().add(new ReportBucket("Chờ khám", stats.getPendingAppointments()));
        stats.getAppointmentBuckets().add(new ReportBucket("Đã khám", stats.getCompletedAppointments()));
        stats.getAppointmentBuckets().add(new ReportBucket("Đã hủy", stats.getCancelledAppointments()));
        stats.getAppointmentBuckets().add(new ReportBucket("Telehealth", stats.getTelehealthAppointments()));
    }

    private int count(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
