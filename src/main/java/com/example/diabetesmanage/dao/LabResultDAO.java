package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.dto.EncounterCreateDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LabResultDAO {

    public LabResult getByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        try (Connection con = DBContext.getConnection()) {
            return getByEncounterId(con, encounterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Bản dùng trong transaction: đọc bằng connection đang mở để thấy cả dữ liệu chưa commit. */
    public LabResult getByEncounterId(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }

        // Mỗi encounter chỉ có một row (UNIQUE INDEX uq_lab_results_encounter)
        String sql = "SELECT * FROM lab_results WHERE encounter_id = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        }
        return null;
    }

    /**
     * Tổng hợp chỉ số xét nghiệm MỚI NHẤT theo TỪNG TRƯỜNG trên toàn bộ lab_results
     * của bệnh nhân — dùng cho patient profile / dashboard (KHÔNG dùng cho encounter detail).
     *
     * Mỗi encounter có thể chỉ chứa một nhóm chỉ số (encounter cũ có CBC, encounter mới
     * có sinh hóa), nên lấy theo encounter mới nhất sẽ thiếu giá trị. Method này lấy
     * giá trị non-null gần nhất của từng cột theo ngay_xet_nghiem DESC
     * (fallback ngay_tao DESC), độc lập giữa các cột.
     */
    public LabResult getLatestSummaryByPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }

        String[] fields = {
                "glucose_mau", "hba1c", "cholesterol_tp", "triglyceride", "hdl_c", "ldl_c",
                "ast", "alt", "ure", "creatinine",
                "wbc", "rbc", "hgb", "hct", "plt"
        };

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(SELECT ").append(fields[i])
                    .append(" FROM lab_results WHERE patient_id = ? AND ").append(fields[i])
                    .append(" IS NOT NULL ")
                    .append("ORDER BY COALESCE(ngay_xet_nghiem, ngay_tao) DESC, id DESC LIMIT 1) AS ")
                    .append(fields[i]);
        }
        sql.append(", (SELECT COALESCE(ngay_xet_nghiem, ngay_tao) FROM lab_results ")
                .append("WHERE patient_id = ? ")
                .append("ORDER BY COALESCE(ngay_xet_nghiem, ngay_tao) DESC, id DESC LIMIT 1) AS latest_time");

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            String trimmed = patientId.trim();
            for (int i = 1; i <= fields.length + 1; i++) {
                ps.setString(i, trimmed);
            }
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            Timestamp latest = rs.getTimestamp("latest_time");
            if (latest == null) {
                // Bệnh nhân chưa có bất kỳ lab_results nào
                return null;
            }

            LabResult lab = new LabResult();
            lab.setPatientId(trimmed);
            lab.setNgayXetNghiem(latest.toLocalDateTime());
            lab.setGlucoseMau(optDouble(rs, "glucose_mau"));
            lab.setHba1c(optDouble(rs, "hba1c"));
            lab.setCholesterolTp(optDouble(rs, "cholesterol_tp"));
            lab.setTriglyceride(optDouble(rs, "triglyceride"));
            lab.setHdlC(optDouble(rs, "hdl_c"));
            lab.setLdlC(optDouble(rs, "ldl_c"));
            lab.setAst(optDouble(rs, "ast"));
            lab.setAlt(optDouble(rs, "alt"));
            lab.setUre(optDouble(rs, "ure"));
            lab.setCreatinine(optDouble(rs, "creatinine"));
            lab.setWbc(optDouble(rs, "wbc"));
            lab.setRbc(optDouble(rs, "rbc"));
            lab.setHgb(optDouble(rs, "hgb"));
            lab.setHct(optDouble(rs, "hct"));
            lab.setPlt(optDouble(rs, "plt"));
            return lab;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Upsert chỉ số máu tổng quát (CBC). Mỗi encounter chỉ có đúng MỘT row lab_results:
     * nếu row của encounter đã tồn tại (ví dụ đã lưu sinh hóa trước đó) thì UPDATE bổ sung
     * các cột CBC vào row đó thay vì INSERT row thứ hai.
     */
    public void insertBloodCount(Connection con, EncounterCreateDTO form, String patientId, String encounterId)
            throws SQLException {
        if (!form.hasBloodCountData()) {
            return;
        }

        LabResult old = getByEncounterId(con, encounterId);
        if (old != null) {
            updateBloodCountRow(con, encounterId, form);
            return;
        }

        String id = java.util.UUID.randomUUID().toString();
        String sql =
                "INSERT INTO lab_results " +
                        "(id, patient_id, encounter_id, ngay_xet_nghiem, ngay_tao, " +
                        "wbc, rbc, hgb, hct, plt, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, encounterId);
            Timestamp visitTime = Timestamp.valueOf(form.resolveNgayKham());
            ps.setTimestamp(4, visitTime);
            ps.setTimestamp(5, visitTime);
            JdbcUtil.setNullableDouble(ps, 6, form.getLabWbc());
            JdbcUtil.setNullableDouble(ps, 7, form.getLabRbc());
            JdbcUtil.setNullableDouble(ps, 8, form.getLabHgb());
            JdbcUtil.setNullableDouble(ps, 9, form.getLabHct());
            JdbcUtil.setNullableDouble(ps, 10, form.getLabPlt());
            JdbcUtil.setString(ps, 11, form.getLabGhiChu());
            ps.executeUpdate();
        }
    }

    public void insertBiochemistry(Connection con,
                                   EncounterCreateDTO form,
                                   String patientId,
                                   String encounterId)
            throws SQLException {

        if (!form.hasBiochemistryData()) {
            return;
        }

        // Lab result gắn với đúng encounter hiện tại — không tra theo patientId
        // vì một bệnh nhân có nhiều lần khám, mỗi encounter có lab riêng.
        // Nếu row của encounter đã tồn tại (kể cả row CBC) thì UPDATE bổ sung
        // các cột sinh hóa vào row đó — mỗi encounter chỉ có MỘT row lab_results.
        LabResult old = getByEncounterId(con, encounterId);

        if (old == null) {
            String id = java.util.UUID.randomUUID().toString();

            String sql =
                    "INSERT INTO lab_results " +
                            "(id, patient_id, encounter_id, ngay_xet_nghiem, ngay_tao, " +
                            "glucose_mau, hba1c, cholesterol_tp, triglyceride, hdl_c, ldl_c, " +
                            "ast, alt, ure, creatinine, ghi_chu) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, patientId);
                ps.setString(3, encounterId);

                Timestamp time = Timestamp.valueOf(form.resolveNgayKham());
                ps.setTimestamp(4, time);
                ps.setTimestamp(5, time);

                JdbcUtil.setDouble(ps, 6, form.getLabGlucoseMau());
                JdbcUtil.setDouble(ps, 7, form.getLabHba1c());
                JdbcUtil.setDouble(ps, 8, form.getLabCholesterol());
                JdbcUtil.setDouble(ps, 9, form.getLabTriglyceride());
                JdbcUtil.setDouble(ps, 10, form.getLabHdl());
                JdbcUtil.setDouble(ps, 11, form.getLabLdl());
                JdbcUtil.setDouble(ps, 12, form.getLabAst());
                JdbcUtil.setDouble(ps, 13, form.getLabAlt());
                JdbcUtil.setDouble(ps, 14, form.getLabUre());
                JdbcUtil.setDouble(ps, 15, form.getLabCreatinine());
                JdbcUtil.setString(ps, 16, form.getLabGhiChu());

                ps.executeUpdate();
            }
        } else {
            updateBiochemistryRow(con, encounterId, form);
        }
    }

    /** Cập nhật các cột CBC trên row lab_results duy nhất của encounter. */
    private void updateBloodCountRow(Connection con, String encounterId, EncounterCreateDTO form)
            throws SQLException {
        String sql =
                "UPDATE lab_results SET " +
                        "wbc = COALESCE(?, wbc), rbc = COALESCE(?, rbc), " +
                        "hgb = COALESCE(?, hgb), hct = COALESCE(?, hct), " +
                        "plt = COALESCE(?, plt), ghi_chu = COALESCE(?, ghi_chu) " +
                        "WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            JdbcUtil.setNullableDouble(ps, 1, form.getLabWbc());
            JdbcUtil.setNullableDouble(ps, 2, form.getLabRbc());
            JdbcUtil.setNullableDouble(ps, 3, form.getLabHgb());
            JdbcUtil.setNullableDouble(ps, 4, form.getLabHct());
            JdbcUtil.setNullableDouble(ps, 5, form.getLabPlt());
            JdbcUtil.setString(ps, 6, form.getLabGhiChu());
            ps.setString(7, encounterId);
            ps.executeUpdate();
        }
    }

    /** Cập nhật các cột sinh hóa trên row lab_results duy nhất của encounter. */
    private void updateBiochemistryRow(Connection con, String encounterId, EncounterCreateDTO form)
            throws SQLException {
        String sql =
                "UPDATE lab_results SET " +
                        "glucose_mau = COALESCE(?, glucose_mau), " +
                        "hba1c = COALESCE(?, hba1c), " +
                        "cholesterol_tp = COALESCE(?, cholesterol_tp), " +
                        "triglyceride = COALESCE(?, triglyceride), " +
                        "hdl_c = COALESCE(?, hdl_c), ldl_c = COALESCE(?, ldl_c), " +
                        "ast = COALESCE(?, ast), alt = COALESCE(?, alt), " +
                        "ure = COALESCE(?, ure), creatinine = COALESCE(?, creatinine), " +
                        "ghi_chu = COALESCE(?, ghi_chu) WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            JdbcUtil.setDouble(ps, 1, form.getLabGlucoseMau());
            JdbcUtil.setDouble(ps, 2, form.getLabHba1c());
            JdbcUtil.setDouble(ps, 3, form.getLabCholesterol());
            JdbcUtil.setDouble(ps, 4, form.getLabTriglyceride());
            JdbcUtil.setDouble(ps, 5, form.getLabHdl());
            JdbcUtil.setDouble(ps, 6, form.getLabLdl());
            JdbcUtil.setDouble(ps, 7, form.getLabAst());
            JdbcUtil.setDouble(ps, 8, form.getLabAlt());
            JdbcUtil.setDouble(ps, 9, form.getLabUre());
            JdbcUtil.setDouble(ps, 10, form.getLabCreatinine());
            JdbcUtil.setString(ps, 11, form.getLabGhiChu());
            ps.setString(12, encounterId);
            ps.executeUpdate();
        }
    }

    public void deleteByEncounterId(Connection con, String encounterId) throws SQLException {
        String sql = "DELETE FROM lab_results WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    private LabResult map(ResultSet rs) throws SQLException {
        LabResult lab = new LabResult();
        lab.setId(rs.getString("id"));
        String displayCode = rs.getString("lab_result_code");
        if (displayCode == null || displayCode.isBlank()) {
            String fallbackId = rs.getString("patient_id");
            if (fallbackId == null || fallbackId.isBlank()) {
                fallbackId = rs.getString("id");
            }
            displayCode = fallbackId != null && fallbackId.length() >= 8
                    ? fallbackId.substring(0, 8).toUpperCase() : "N/A";
        }
        lab.setDisplayCode(displayCode);
        lab.setPatientId(rs.getString("patient_id"));
        lab.setEncounterId(rs.getString("encounter_id"));

        Timestamp ts = rs.getTimestamp("ngay_xet_nghiem");
        if (ts != null) {
            lab.setNgayXetNghiem(ts.toLocalDateTime());
        }

        lab.setGlucoseMau(optDouble(rs, "glucose_mau"));
        lab.setHba1c(optDouble(rs, "hba1c"));
        lab.setCholesterolTp(optDouble(rs, "cholesterol_tp"));
        lab.setTriglyceride(optDouble(rs, "triglyceride"));
        lab.setHdlC(optDouble(rs, "hdl_c"));
        lab.setLdlC(optDouble(rs, "ldl_c"));
        lab.setAst(optDouble(rs, "ast"));
        lab.setAlt(optDouble(rs, "alt"));
        lab.setUre(optDouble(rs, "ure"));
        lab.setCreatinine(optDouble(rs, "creatinine"));
        lab.setWbc(optDouble(rs, "wbc"));
        lab.setRbc(optDouble(rs, "rbc"));
        lab.setHgb(optDouble(rs, "hgb"));
        lab.setHct(optDouble(rs, "hct"));
        lab.setPlt(optDouble(rs, "plt"));
        lab.setGhiChu(rs.getString("ghi_chu"));
        return lab;
    }

    private Double optDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }
}
