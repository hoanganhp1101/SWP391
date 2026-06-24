package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentDAO {
    public List<Appointment> getUpcomingAppointments(String patientId) {
        String sql = "SELECT a.*, u.ho_ten AS bac_si_name " +
                "FROM appointments a LEFT JOIN users u ON a.bac_si_id = u.id " +
                "WHERE a.patient_id = ? AND a.trang_thai = 'cho_kham' " +
                "ORDER BY a.thoi_gian_hen ASC LIMIT 5";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        String sql = "SELECT a.*, u.ho_ten AS bac_si_name " +
                "FROM appointments a LEFT JOIN users u ON a.bac_si_id = u.id " +
                "WHERE a.patient_id = ? ORDER BY a.thoi_gian_hen DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (id, patient_id, bac_si_id, tieu_de, thoi_gian_hen, dia_diem, trang_thai) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'cho_kham')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, appointment.getPatientId());
            ps.setString(3, appointment.getBacSiId());
            ps.setString(4, appointment.getTieuDe());
            ps.setTimestamp(5, appointment.getThoiGianHen());
            ps.setString(6, appointment.getDiaDiem());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointments SET bac_si_id = ?, tieu_de = ?, thoi_gian_hen = ?, dia_diem = ? " +
                "WHERE id = ? AND patient_id = ? AND trang_thai = 'cho_kham'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointment.getBacSiId());
            ps.setString(2, appointment.getTieuDe());
            ps.setTimestamp(3, appointment.getThoiGianHen());
            ps.setString(4, appointment.getDiaDiem());
            ps.setString(5, appointment.getId());
            ps.setString(6, appointment.getPatientId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getAvailableDoctors() {
        String sql = "SELECT id, ho_ten, email, so_dien_thoai, vai_tro, anh_dai_dien, kich_hoat, ngay_tao, ngay_cap_nhat, lan_dang_nhap_cuoi " +
                "FROM users WHERE vai_tro = 'bac_si' AND kich_hoat = 1 ORDER BY ho_ten";
        List<User> doctors = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User doctor = new User();
                doctor.setId(rs.getString("id"));
                doctor.setHoTen(rs.getString("ho_ten"));
                doctor.setEmail(rs.getString("email"));
                doctor.setSoDienThoai(rs.getString("so_dien_thoai"));
                doctor.setVaiTro(rs.getString("vai_tro"));
                doctor.setAnhDaiDien(rs.getString("anh_dai_dien"));
                doctor.setKichHoat(rs.getInt("kich_hoat"));
                doctor.setNgayTao(rs.getTimestamp("ngay_tao"));
                doctor.setNgayCapNhat(rs.getTimestamp("ngay_cap_nhat"));
                doctor.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));
                doctors.add(doctor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctors;
    }

    private Appointment mapAppointment(ResultSet rs) throws Exception {
        Appointment a = new Appointment();
        a.setId(rs.getString("id"));
        a.setPatientId(rs.getString("patient_id"));
        a.setBacSiId(rs.getString("bac_si_id"));
        a.setTieuDe(rs.getString("tieu_de"));
        a.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
        a.setDiaDiem(rs.getString("dia_diem"));
        a.setTrangThai(rs.getString("trang_thai"));
        a.setNgayTao(rs.getTimestamp("ngay_tao"));
        a.setBacSiName(rs.getString("bac_si_name"));
        return a;
    }
}
