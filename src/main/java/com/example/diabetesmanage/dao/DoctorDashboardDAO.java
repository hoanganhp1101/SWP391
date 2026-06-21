package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.DoctorDashboardStats;
import com.example.diabetesmanage.model.UrgentPatientAlert;
import com.example.diabetesmanage.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardDAO {

    public static final String DOCTOR_EMAIL = "bacsi@example.com";

    public User getDoctorByEmail(String email) {

        String sql =
                "SELECT id, ho_ten, email, anh_dai_dien, vai_tro " +
                        "FROM users " +
                        "WHERE email = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                User doctor = new User();
                doctor.setId(rs.getString("id"));
                doctor.setHoTen(rs.getString("ho_ten"));
                doctor.setEmail(rs.getString("email"));
                doctor.setAnhDaiDien(rs.getString("anh_dai_dien"));
                doctor.setVaiTro(rs.getString("vai_tro"));
                return doctor;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public DoctorDashboardStats getDashboardStats(String doctorEmail) {

        DoctorDashboardStats stats = new DoctorDashboardStats();

        String riskSql =
                "SELECT " +
                        "COUNT(*) AS total_patients, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat IS NULL OR vps.duong_huyet_gan_nhat < 140 THEN 1 ELSE 0 END) AS risk_low, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 140 AND 179 THEN 1 ELSE 0 END) AS risk_medium, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 180 AND 249 THEN 1 ELSE 0 END) AS risk_high, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat >= 250 THEN 1 ELSE 0 END) AS risk_critical, " +
                        "COALESCE(SUM(vps.canh_bao_chua_doc), 0) AS active_alerts " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(riskSql)
        ) {

            ps.setString(1, doctorEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats.setTotalPatients(rs.getInt("total_patients"));
                stats.setRiskLow(rs.getInt("risk_low"));
                stats.setRiskMedium(rs.getInt("risk_medium"));
                stats.setRiskHigh(rs.getInt("risk_high"));
                stats.setRiskCritical(rs.getInt("risk_critical"));
                stats.setPriorityLevel1Count(rs.getInt("risk_critical"));
                stats.setActiveAlerts(rs.getInt("active_alerts"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            loadStatsWithoutAlertsColumn(doctorEmail, stats);
        }

        stats.setTodayHealthRecords(countTodayHealthRecords(doctorEmail));

        if (stats.getActiveAlerts() == 0) {
            stats.setActiveAlerts(stats.getRiskHigh() + stats.getRiskCritical());
        }

        return stats;
    }

    private void loadStatsWithoutAlertsColumn(String doctorEmail, DoctorDashboardStats stats) {

        String riskSql =
                "SELECT " +
                        "COUNT(*) AS total_patients, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat IS NULL OR vps.duong_huyet_gan_nhat < 140 THEN 1 ELSE 0 END) AS risk_low, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 140 AND 179 THEN 1 ELSE 0 END) AS risk_medium, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat BETWEEN 180 AND 249 THEN 1 ELSE 0 END) AS risk_high, " +
                        "SUM(CASE WHEN vps.duong_huyet_gan_nhat >= 250 THEN 1 ELSE 0 END) AS risk_critical " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(riskSql)
        ) {

            ps.setString(1, doctorEmail);
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

    private int countTodayHealthRecords(String doctorEmail) {

        String sql =
                "SELECT COUNT(*) AS total " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ? " +
                        "AND DATE(hr.thoi_gian_do) = CURDATE()";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, doctorEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<UrgentPatientAlert> getUrgentPatients(String doctorEmail, int limit) {

        List<UrgentPatientAlert> list = new ArrayList<>();

        String sql =
                "SELECT " +
                        "vps.patient_id, " +
                        "vps.patient_code, " +
                        "vps.ho_ten, " +
                        "vps.loai_tieu_duong, " +
                        "vps.duong_huyet_gan_nhat, " +
                        "vps.lan_do_cuoi, " +
                        "hr.huyet_ap_tam_thu, " +
                        "hr.huyet_ap_tam_truong " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "LEFT JOIN health_records hr ON hr.id = ( " +
                        "    SELECT hr2.id " +
                        "    FROM health_records hr2 " +
                        "    WHERE hr2.patient_id = p.id " +
                        "    ORDER BY hr2.thoi_gian_do DESC " +
                        "    LIMIT 1 " +
                        ") " +
                        "WHERE d.email = ? " +
                        "AND ( " +
                        "    vps.duong_huyet_gan_nhat >= 180 " +
                        "    OR hr.huyet_ap_tam_thu >= 140 " +
                        ") " +
                        "ORDER BY " +
                        "    CASE " +
                        "        WHEN vps.duong_huyet_gan_nhat >= 250 THEN 0 " +
                        "        WHEN vps.duong_huyet_gan_nhat >= 180 THEN 1 " +
                        "        ELSE 2 " +
                        "    END, " +
                        "    vps.lan_do_cuoi DESC " +
                        "LIMIT ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, doctorEmail);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                UrgentPatientAlert alert = new UrgentPatientAlert();

                alert.setPatientId(rs.getString("patient_id"));
                alert.setPatientCode(rs.getString("patient_code"));
                alert.setPatientName(rs.getString("ho_ten"));
                alert.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

                double glucose = rs.getDouble("duong_huyet_gan_nhat");
                if (!rs.wasNull()) {
                    alert.setDuongHuyetGanNhat(glucose);
                    alert.setCritical(glucose >= 250);
                }

                int systolic = rs.getInt("huyet_ap_tam_thu");
                if (!rs.wasNull()) {
                    alert.setHuyetApTamThu(systolic);
                }

                int diastolic = rs.getInt("huyet_ap_tam_truong");
                if (!rs.wasNull()) {
                    alert.setHuyetApTamTruong(diastolic);
                }

                alert.setVitalDisplay(buildVitalDisplay(alert));
                alert.setDetectedAgo(formatDetectedAgo(rs.getTimestamp("lan_do_cuoi")));

                list.add(alert);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private String buildVitalDisplay(UrgentPatientAlert alert) {

        if (alert.getDuongHuyetGanNhat() != null && alert.getDuongHuyetGanNhat() >= 180) {
            return String.format("Đường huyết: %.0f mg/dL", alert.getDuongHuyetGanNhat());
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

        return "Chỉ số bất thường";
    }

    private String formatDetectedAgo(Timestamp timestamp) {

        if (timestamp == null) {
            return "Chưa có dữ liệu gần đây";
        }

        LocalDateTime detectedAt = timestamp.toLocalDateTime();
        Duration duration = Duration.between(detectedAt, LocalDateTime.now());

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

        long days = duration.toDays();
        return "Phát hiện cách đây " + days + " ngày";
    }
}
