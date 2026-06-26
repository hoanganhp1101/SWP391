package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.form.AddMedicalEncounterForm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrescriptionDAO {

    /**
     * Tạo đơn thuốc mới gắn với lần khám.
     */
    public String insert(Connection con, AddMedicalEncounterForm form, String patientId,
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
}
