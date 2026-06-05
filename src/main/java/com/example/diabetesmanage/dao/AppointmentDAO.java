package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    public List<Appointment> getUpcomingAppointments(String patientId) {
        String sql = "SELECT * FROM appointments WHERE patient_id = ? AND trang_thai = 'cho_kham' ORDER BY thoi_gian_hen ASC LIMIT 5";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getString("id"));
                a.setPatientId(rs.getString("patient_id"));
                a.setBacSiId(rs.getString("bac_si_id"));
                a.setTieuDe(rs.getString("tieu_de"));
                a.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
                a.setDiaDiem(rs.getString("dia_diem"));
                a.setTrangThai(rs.getString("trang_thai"));
                a.setNgayTao(rs.getTimestamp("ngay_tao"));
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
