package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.dto.EncounterCreateDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrescriptionDAO {

    public String getIdByEncounterId(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        String sql = "SELECT id FROM prescriptions WHERE encounter_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("id") : null;
        }
    }

    public String getIdByEncounterId(String encounterId) {
        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection()) {
            return getIdByEncounterId(con, encounterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo đơn thuốc mới gắn với lần khám.
     */
    public String insert(Connection con, EncounterCreateDTO form, String patientId,
                         String doctorId, String encounterId) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();

        String sql =
                "INSERT INTO prescriptions " +
                        "(id, patient_id, bac_si_id, encounter_id, chan_doan, huong_dieu_tri, " +
                        "che_do_an, luyen_tap, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, doctorId);
            ps.setString(4, encounterId);
            JdbcUtil.setString(ps, 5, form.getChanDoanChinh());
            JdbcUtil.setString(ps, 6, form.getKhuyenNghiDieuTri());
            JdbcUtil.setString(ps, 7, form.getCheDoAn());
            JdbcUtil.setString(ps, 8, form.getLuyenTap());
            JdbcUtil.setString(ps, 9, null);
            ps.executeUpdate();
        }
        return id;
    }

    public void deleteByEncounterId(Connection con, String encounterId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    public Map<String, String> getAdviceByEncounterId(Connection con, String encounterId) throws SQLException {
        Map<String, String> advice = new LinkedHashMap<>();
        if (encounterId == null || encounterId.isBlank()) {
            return advice;
        }

        String sql =
                "SELECT huong_dieu_tri, che_do_an, luyen_tap, ghi_chu " +
                        "FROM prescriptions " +
                        "WHERE encounter_id = ? " +
                        "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                putIfPresent(advice, "huong_dieu_tri", rs.getString("huong_dieu_tri"));
                putIfPresent(advice, "che_do_an", rs.getString("che_do_an"));
                putIfPresent(advice, "luyen_tap", rs.getString("luyen_tap"));
                putIfPresent(advice, "ghi_chu", rs.getString("ghi_chu"));
            }
        }
        return advice;
    }

    public Map<String, String> getAdviceByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return new LinkedHashMap<>();
        }
        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection()) {
            return getAdviceByEncounterId(con, encounterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    /**
     * Prefer the prescription linked to this encounter; for each field still missing,
     * fall back to the patient's most recent prescription that has that field.
     * Never mixes prescriptions from other patients.
     */
    public Map<String, String> getAdviceForEncounterOrLatestPatient(
            String encounterId, String patientId) {
        Map<String, String> advice = new LinkedHashMap<>();
        if ((encounterId == null || encounterId.isBlank())
                && (patientId == null || patientId.isBlank())) {
            return advice;
        }

        String sql =
                "SELECT huong_dieu_tri, che_do_an, luyen_tap " +
                        "FROM prescriptions " +
                        "WHERE encounter_id = ? OR patient_id = ? " +
                        "ORDER BY CASE WHEN encounter_id = ? THEN 0 ELSE 1 END, " +
                        "COALESCE(ngay_tao, TIMESTAMP(ngay_ke_don)) DESC, id DESC";

        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.setString(2, patientId);
            ps.setString(3, encounterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()
                    && (!advice.containsKey("huong_dieu_tri")
                        || !advice.containsKey("che_do_an")
                        || !advice.containsKey("luyen_tap"))) {
                putIfAbsent(advice, "huong_dieu_tri", rs.getString("huong_dieu_tri"));
                putIfAbsent(advice, "che_do_an", rs.getString("che_do_an"));
                putIfAbsent(advice, "luyen_tap", rs.getString("luyen_tap"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advice;
    }

    private void putIfAbsent(Map<String, String> target, String key, String value) {
        if (!target.containsKey(key)) {
            putIfPresent(target, key, value);
        }
    }

    private void putIfPresent(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }
}
