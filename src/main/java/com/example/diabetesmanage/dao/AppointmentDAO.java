package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentDAO {

    // ---- Patient portal methods ----

    public List<Appointment> getUpcomingAppointments(String patientId) {
        String sql = "SELECT a.*, u.ho_ten AS bac_si_name " +
                "FROM appointments a LEFT JOIN doctors d ON a.bac_si_id = d.id "
                + "LEFT JOIN users u ON d.id = u.id " +
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
                "FROM appointments a LEFT JOIN doctors d ON a.bac_si_id = d.id "
                + "LEFT JOIN users u ON d.id = u.id " +
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
        String sql = "SELECT d.id, u.ho_ten, u.email, u.so_dien_thoai, u.anh_dai_dien, u.kich_hoat, "
                + "u.ngay_tao, u.ngay_cap_nhat, u.lan_dang_nhap_cuoi, 'bac_si' AS vai_tro "
                + "FROM doctors d JOIN users u ON d.id = u.id WHERE u.kich_hoat = 1 ORDER BY u.ho_ten";
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

    // ---- Doctor portal methods (from doctor-dashboard) ----

    private static final String SELECT_BASE =
            "SELECT a.*, " +
                    "p.patient_code AS patient_code, " +
                    "pu.ho_ten AS patient_name, " +
                    "du.ho_ten AS doctor_name, " +
                    "a.tieu_de AS noi_dung_kham " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users pu ON p.id = pu.id " +
                    "LEFT JOIN doctors bs ON a.bac_si_id = bs.id " +
                    "LEFT JOIN users du ON bs.id = du.id " +
                    "WHERE 1=1 ";

    public List<Appointment> findAll(
            String scopeDoctorId, String status, String keyword, String fromDate, String toDate, String type
    ) {
        List<Appointment> list = new ArrayList<>();
        String normalizedStatus = Appointment.normalizeStatusFilter(status);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasDate = fromDate != null && !fromDate.isBlank()
                && toDate != null && !toDate.isBlank();

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        if (scopeDoctorId != null) {
            sql.append("AND (a.bac_si_id = ? OR p.bac_si_id = ?) ");
        }
        if (normalizedStatus != null) {
            sql.append("AND a.trang_thai = ? ");
        }
        if (hasDate) {
            sql.append("AND DATE(a.thoi_gian_hen) BETWEEN ? AND ? ");
        }
        if (hasKeyword) {
            sql.append("AND (a.tieu_de LIKE ? " +
                    "OR pu.ho_ten LIKE ? " +
                    "OR p.patient_code LIKE ?) ");
        }
        sql.append("ORDER BY a.thoi_gian_hen DESC");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            if (con == null) {
                return list;
            }
            int idx = bindDoctorScope(ps, 1, scopeDoctorId);
            if (normalizedStatus != null) {
                ps.setString(idx++, normalizedStatus);
            }
            if (hasDate) {
                ps.setString(idx++, fromDate);
                ps.setString(idx++, toDate);
            }
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appointments", e);
        }
        return list;
    }

    public boolean updateStatus(String appointmentId, String newStatus, String scopeDoctorId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            return false;
        }
        String normalizedStatus = Appointment.normalizeStatusFilter(newStatus);
        if (!Appointment.isAllowedStatusUpdate(normalizedStatus)) {
            return false;
        }

        try (Connection con = DBContext.getConnection()) {
            if (con == null) {
                return false;
            }
            if (executeStatusUpdate(con, appointmentId, normalizedStatus, scopeDoctorId)) {
                return true;
            }
            // Legacy fallback: một số bản ghi cũ dùng mã "huy" thay vì "da_huy"
            if (Appointment.STATUS_HUY.equals(normalizedStatus)) {
                return executeStatusUpdate(con, appointmentId, "huy", scopeDoctorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean executeStatusUpdate(
            Connection con,
            String appointmentId,
            String newStatus,
            String scopeDoctorId
    ) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(buildUpdateSql(scopeDoctorId))) {
            bindUpdate(ps, appointmentId, newStatus, scopeDoctorId);
            return ps.executeUpdate() > 0;
        }
    }

    private String buildUpdateSql(String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(
                "UPDATE appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "SET a.trang_thai = ? " +
                        "WHERE a.id = ? AND a.trang_thai = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND (a.bac_si_id = ? OR p.bac_si_id = ?) ");
        }
        return sql.toString();
    }

    private void bindUpdate(
            PreparedStatement ps,
            String appointmentId,
            String newStatus,
            String scopeDoctorId
    ) throws SQLException {
        ps.setString(1, newStatus);
        ps.setString(2, appointmentId);
        ps.setString(3, Appointment.STATUS_CHO_KHAM);
        if (scopeDoctorId != null) {
            ps.setString(4, scopeDoctorId);
            ps.setString(5, scopeDoctorId);
        }
    }

    private int bindDoctorScope(PreparedStatement ps, int startIdx, String scopeDoctorId) throws SQLException {
        if (scopeDoctorId == null) {
            return startIdx;
        }
        ps.setString(startIdx++, scopeDoctorId);
        ps.setString(startIdx++, scopeDoctorId);
        return startIdx;
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getString("id"));
        a.setPatientId(rs.getString("patient_id"));
        String patientCode = rs.getString("patient_code");
        a.setPatientCode(patientCode == null || patientCode.isBlank() ? null : patientCode.trim());
        a.setPatientName(rs.getString("patient_name"));
        a.setBacSiId(rs.getString("bac_si_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setBacSiName(rs.getString("doctor_name"));

        String noiDung = rs.getString("noi_dung_kham");
        if (noiDung == null || noiDung.isBlank()) {
            try {
                noiDung = rs.getString("tieu_de");
            } catch (SQLException ignored) {
                // cot tieu_de co the khong ton tai tren schema cu
            }
        }
        a.setNoiDungKham(noiDung);
        a.setTieuDe(noiDung);

        // Keep Timestamp (Appointment model + patient portal)
        a.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
        a.setDiaDiem(rs.getString("dia_diem"));
        a.setTrangThai(rs.getString("trang_thai"));
        a.setNgayTao(rs.getTimestamp("ngay_tao"));
        return a;
    }
}
