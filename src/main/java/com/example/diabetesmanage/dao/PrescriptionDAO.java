package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PrescriptionDAO {

    public Prescription getNextAppointment(String patientId) {
        String sql = "SELECT p.*, u.ho_ten as bac_si_name " +
                     "FROM prescriptions p " +
                     "JOIN users u ON p.bac_si_id = u.id " +
                     "WHERE p.patient_id = ? AND p.ngay_tai_kham >= CURRENT_DATE " +
                     "ORDER BY p.ngay_tai_kham ASC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getString("id"));
                p.setPatientId(rs.getString("patient_id"));
                p.setBacSiId(rs.getString("bac_si_id"));
                p.setNgayKeDon(rs.getDate("ngay_ke_don"));
                p.setNgayTaiKham(rs.getTimestamp("ngay_tai_kham"));
                p.setBacSiName(rs.getString("bac_si_name"));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
