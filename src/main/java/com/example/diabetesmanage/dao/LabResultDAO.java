package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.LabResult;

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

    private LabResult map(ResultSet rs) throws SQLException {
        LabResult lab = new LabResult();
        lab.setId(rs.getString("id"));
        lab.setDisplayCode(RecordCodeHelper.resolve(rs, "lab_result_code"));
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
