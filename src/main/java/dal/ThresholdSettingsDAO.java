package dal;

import config.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import model.ThresholdSettings;

public class ThresholdSettingsDAO {

    public ThresholdSettings getForDoctor(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            return ThresholdSettings.defaults(null);
        }

        UUID bacSiId;
        try {
            bacSiId = UUID.fromString(doctorId.trim());
        } catch (IllegalArgumentException ex) {
            return ThresholdSettings.defaults(null);
        }

        String sql = """
                SELECT id, bac_si_id, glucose_low, glucose_high, glucose_danger,
                       hba1c_target, hba1c_poor, days_no_measure, ngay_cap_nhat
                FROM threshold_settings
                WHERE bac_si_id = ?
                """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bacSiId.toString());
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
        if (settings == null || settings.getBacSiId() == null) {
            return false;
        }

        String updateSql = """
                UPDATE threshold_settings
                SET glucose_low = ?, glucose_high = ?, glucose_danger = ?,
                    hba1c_target = ?, hba1c_poor = ?, days_no_measure = ?,
                    ngay_cap_nhat = GETDATE()
                WHERE bac_si_id = ?
                """;

        String insertSql = """
                INSERT INTO threshold_settings (
                    id, bac_si_id, glucose_low, glucose_high, glucose_danger,
                    hba1c_target, hba1c_poor, days_no_measure
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = new DBContext().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                bindSettings(ps, settings);
                ps.setString(7, settings.getBacSiId().toString());
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, settings.getBacSiId().toString());
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
        UUID bacSiId;
        try {
            bacSiId = UUID.fromString(doctorId.trim());
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return save(ThresholdSettings.defaults(bacSiId));
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
        String id = rs.getString("id");
        s.setId(id == null ? null : UUID.fromString(id));
        String bacSiId = rs.getString("bac_si_id");
        s.setBacSiId(bacSiId == null ? null : UUID.fromString(bacSiId));
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
