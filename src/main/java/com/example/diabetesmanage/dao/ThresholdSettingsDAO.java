package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.ThresholdSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ThresholdSettingsDAO {

    private static final String SQL_GET_FOR_DOCTOR =
            "SELECT id, bac_si_id, glucose_low, glucose_high, glucose_danger, "
            + "hba1c_target, hba1c_poor, days_no_measure, ngay_cap_nhat "
            + "FROM threshold_settings WHERE bac_si_id = ?";

    private static final String SQL_UPDATE =
            "UPDATE threshold_settings "
            + "SET glucose_low = ?, glucose_high = ?, glucose_danger = ?, "
            + "hba1c_target = ?, hba1c_poor = ?, days_no_measure = ?, "
            + "ngay_cap_nhat = NOW() WHERE bac_si_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO threshold_settings ("
            + "id, bac_si_id, glucose_low, glucose_high, glucose_danger, "
            + "hba1c_target, hba1c_poor, days_no_measure"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public ThresholdSettings getForDoctor(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            return ThresholdSettings.defaults(null);
        }
        String bacSiId = doctorId.trim();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_FOR_DOCTOR)) {
            ps.setString(1, bacSiId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ThresholdSettings.defaults(bacSiId);
    }

    public boolean save(ThresholdSettings settings) {
        if (settings == null || settings.getBacSiId() == null || settings.getBacSiId().isBlank()) {
            return false;
        }

        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                bindSettings(ps, settings);
                ps.setString(7, settings.getBacSiId());
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, settings.getBacSiId());
                ps.setInt(3, settings.getGlucoseLow());
                ps.setInt(4, settings.getGlucoseHigh());
                ps.setInt(5, settings.getGlucoseDanger());
                ps.setDouble(6, settings.getHba1cTarget());
                ps.setDouble(7, settings.getHba1cPoor());
                ps.setInt(8, settings.getDaysNoMeasure());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetToDefaults(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            return false;
        }
        return save(ThresholdSettings.defaults(doctorId.trim()));
    }

    private void bindSettings(PreparedStatement ps, ThresholdSettings s) throws SQLException {
        ps.setInt(1, s.getGlucoseLow());
        ps.setInt(2, s.getGlucoseHigh());
        ps.setInt(3, s.getGlucoseDanger());
        ps.setDouble(4, s.getHba1cTarget());
        ps.setDouble(5, s.getHba1cPoor());
        ps.setInt(6, s.getDaysNoMeasure());
    }

    private ThresholdSettings mapRow(ResultSet rs) throws SQLException {
        ThresholdSettings s = new ThresholdSettings();
        s.setId(rs.getString("id"));
        s.setBacSiId(rs.getString("bac_si_id"));
        s.setGlucoseLow(rs.getInt("glucose_low"));
        s.setGlucoseHigh(rs.getInt("glucose_high"));
        s.setGlucoseDanger(rs.getInt("glucose_danger"));
        s.setHba1cTarget(rs.getDouble("hba1c_target"));
        s.setHba1cPoor(rs.getDouble("hba1c_poor"));
        s.setDaysNoMeasure(rs.getInt("days_no_measure"));
        s.setNgayCapNhat(rs.getTimestamp("ngay_cap_nhat"));
        return s;
    }
}
