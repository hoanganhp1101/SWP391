package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LabResultDAO {

    public LabResult getLatestByPatientId(String patientId) {
        String sql =
                "SELECT * FROM lab_results " +
                        "WHERE patient_id = ? " +
                        "ORDER BY ngay_xet_nghiem DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LabResult getByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }

        String sql =
                "SELECT * FROM lab_results " +
                        "WHERE encounter_id = ? " +
                        "ORDER BY ngay_xet_nghiem DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Connection con, EncounterCreateRequest form, String patientId, String encounterId)
            throws SQLException {
        EncounterType type = form.resolveEncounterType();
        if (type.isMauTongQuat()) {
            insertBloodCount(con, form, patientId, encounterId);
        } else if (type.isSinhHoaMau()) {
            insertBiochemistry(con, form, patientId, encounterId);
        } else if (form.hasLabData()) {
            insertAll(con, form, patientId, encounterId);
        }
    }

    public void insertBloodCount(Connection con, EncounterCreateRequest form, String patientId, String encounterId)
            throws SQLException {
        if (!form.hasBloodCountData()) {
            return;
        }
        String id = java.util.UUID.randomUUID().toString();
        String sql =
                "INSERT INTO lab_results " +
                        "(id, patient_id, encounter_id, ngay_xet_nghiem, ngay_tao, wbc, rbc, hgb, hct, plt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.executeUpdate();
        }
    }

    public void insertBiochemistry(Connection con, EncounterCreateRequest form, String patientId, String encounterId)
            throws SQLException {
        if (!form.hasBiochemistryData()) {
            return;
        }
        String id = java.util.UUID.randomUUID().toString();
        String sql =
                "INSERT INTO lab_results " +
                        "(id, patient_id, encounter_id, ngay_xet_nghiem, ngay_tao, glucose_mau, hba1c, cholesterol_tp, triglyceride, " +
                        "hdl_c, ldl_c, ast, alt, ure, creatinine, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, encounterId);
            Timestamp visitTime = Timestamp.valueOf(form.resolveNgayKham());
            ps.setTimestamp(4, visitTime);
            ps.setTimestamp(5, visitTime);
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
    }

    private void insertAll(Connection con, EncounterCreateRequest form, String patientId, String encounterId)
            throws SQLException {
        if (!form.hasLabData()) {
            return;
        }

        String id = java.util.UUID.randomUUID().toString();
        String nuocTieuJson = buildNuocTieuJson(form.getLabNuocTieu());

        String sql =
                "INSERT INTO lab_results " +
                        "(id, patient_id, encounter_id, glucose_mau, hba1c, cholesterol_tp, triglyceride, " +
                        "hdl_c, ldl_c, ast, alt, ure, creatinine, hbsag, anti_hcv, nuoc_tieu, ghi_chu, " +
                        "wbc, rbc, hgb, hct, plt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, encounterId);
            JdbcUtil.setDouble(ps, 4, form.getLabGlucoseMau());
            JdbcUtil.setDouble(ps, 5, form.getLabHba1c());
            JdbcUtil.setDouble(ps, 6, form.getLabCholesterol());
            JdbcUtil.setDouble(ps, 7, form.getLabTriglyceride());
            JdbcUtil.setDouble(ps, 8, form.getLabHdl());
            JdbcUtil.setDouble(ps, 9, form.getLabLdl());
            JdbcUtil.setDouble(ps, 10, form.getLabAst());
            JdbcUtil.setDouble(ps, 11, form.getLabAlt());
            JdbcUtil.setDouble(ps, 12, form.getLabUre());
            JdbcUtil.setDouble(ps, 13, form.getLabCreatinine());
            JdbcUtil.setString(ps, 14, emptyToNull(form.getLabHbsag()));
            JdbcUtil.setString(ps, 15, emptyToNull(form.getLabAntiHcv()));
            JdbcUtil.setString(ps, 16, nuocTieuJson);
            JdbcUtil.setString(ps, 17, form.getLabGhiChu());
            JdbcUtil.setNullableDouble(ps, 18, form.getLabWbc());
            JdbcUtil.setNullableDouble(ps, 19, form.getLabRbc());
            JdbcUtil.setNullableDouble(ps, 20, form.getLabHgb());
            JdbcUtil.setNullableDouble(ps, 21, form.getLabHct());
            JdbcUtil.setNullableDouble(ps, 22, form.getLabPlt());
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

    private String buildNuocTieuJson(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"ket_qua\":\"" + escaped + "\"}";
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private LabResult map(ResultSet rs) throws SQLException {
        LabResult lab = new LabResult();
        lab.setId(rs.getString("id"));
        lab.setDisplayCode(PatientDAO.resolveCode(rs, "lab_result_code"));
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
