package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DoctorDashboardDAO {

    public DashboardStats getDashboardStats(String doctorId, String startDate, String endDate) {

        DashboardStats stats = new DashboardStats();
        boolean hasDate = startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank();

        String sql =
                "SELECT " +
                        "COUNT(*) AS total_patients, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat IS NULL OR vps.duong_huyet_gan_nhat < 140 THEN 1 ELSE 0 END) AS risk_low, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 140 AND 179 THEN 1 ELSE 0 END) AS risk_medium, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 180 AND 249 THEN 1 ELSE 0 END) AS risk_high, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat >= 250 THEN 1 ELSE 0 END) AS risk_critical, " +
                        "COALESCE(SUM(vps.canh_bao_chua_doc), 0) AS active_alerts " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "WHERE p.bac_si_id = ?" +
                        (hasDate ? " AND p.ngay_tao BETWEEN ? AND ?" : "");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, doctorId);
            if (hasDate) {
                ps.setString(2, startDate);
                ps.setString(3, endDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalPatients(rs.getInt("total_patients"));
                    stats.setRiskLow(rs.getInt("risk_low"));
                    stats.setRiskMedium(rs.getInt("risk_medium"));
                    stats.setRiskHigh(rs.getInt("risk_high"));
                    stats.setRiskCritical(rs.getInt("risk_critical"));
                    stats.setActiveAlerts(rs.getInt("active_alerts"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            loadStatsWithoutAlertsColumn(doctorId, stats);
        }

        stats.setTodayHealthRecords(countEncounters(doctorId, startDate, endDate, hasDate));

        if (stats.getActiveAlerts() == 0) {
            stats.setActiveAlerts(stats.getRiskHigh() + stats.getRiskCritical());
        }

        return stats;
    }

    private void loadStatsWithoutAlertsColumn(String doctorId, DashboardStats stats) {

        String riskSql =
                "SELECT " +
                        "COUNT(*) AS total_patients, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat IS NULL OR vps.duong_huyet_gan_nhat < 140 THEN 1 ELSE 0 END) AS risk_low, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 140 AND 179 THEN 1 ELSE 0 END) AS risk_medium, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 180 AND 249 THEN 1 ELSE 0 END) AS risk_high, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat >= 250 THEN 1 ELSE 0 END) AS risk_critical " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "WHERE p.bac_si_id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(riskSql)
        ) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats.setTotalPatients(rs.getInt("total_patients"));
                stats.setRiskLow(rs.getInt("risk_low"));
                stats.setRiskMedium(rs.getInt("risk_medium"));
                stats.setRiskHigh(rs.getInt("risk_high"));
                stats.setRiskCritical(rs.getInt("risk_critical"));
                stats.setPriorityLevel1Count(rs.getInt("risk_critical"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int countEncounters(String doctorId, String startDate, String endDate, boolean hasDate) {

        String sql =
                "SELECT COUNT(*) AS total " +
                        "FROM medical_encounters me " +
                        "JOIN patients p ON me.patient_id = p.id " +
                        "WHERE p.bac_si_id = ? " +
                        (hasDate
                                ? "AND DATE(COALESCE(me.ngay_tao, me.ngay_kham)) BETWEEN ? AND ?"
                                : "AND DATE(COALESCE(me.ngay_tao, me.ngay_kham)) = CURDATE()");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, doctorId);
            if (hasDate) {
                ps.setString(2, startDate);
                ps.setString(3, endDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static class DashboardStats {

        private int totalPatients;
        private int activeAlerts;
        private int todayHealthRecords;
        private int riskLow;
        private int riskMedium;
        private int riskHigh;
        private int riskCritical;
        private int priorityLevel1Count;

        public int getTotalPatients() {
            return totalPatients;
        }

        public void setTotalPatients(int totalPatients) {
            this.totalPatients = totalPatients;
        }

        public int getActiveAlerts() {
            return activeAlerts;
        }

        public void setActiveAlerts(int activeAlerts) {
            this.activeAlerts = activeAlerts;
        }

        public int getTodayHealthRecords() {
            return todayHealthRecords;
        }

        public void setTodayHealthRecords(int todayHealthRecords) {
            this.todayHealthRecords = todayHealthRecords;
        }

        public int getRiskLow() {
            return riskLow;
        }

        public void setRiskLow(int riskLow) {
            this.riskLow = riskLow;
        }

        public int getRiskMedium() {
            return riskMedium;
        }

        public void setRiskMedium(int riskMedium) {
            this.riskMedium = riskMedium;
        }

        public int getRiskHigh() {
            return riskHigh;
        }

        public void setRiskHigh(int riskHigh) {
            this.riskHigh = riskHigh;
        }

        public int getRiskCritical() {
            return riskCritical;
        }

        public void setRiskCritical(int riskCritical) {
            this.riskCritical = riskCritical;
        }

        public int getPriorityLevel1Count() {
            return priorityLevel1Count;
        }

        public void setPriorityLevel1Count(int priorityLevel1Count) {
            this.priorityLevel1Count = priorityLevel1Count;
        }
    }
}
