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

    // ── Doctor portal methods ────────────────────────────────────────────────

    private static final String SELECT_BASE =
            "SELECT a.*, " +
                    "COALESCE(p.patient_code, LEFT(p.id, 8)) AS patient_code, " +
                    "u.ho_ten AS patient_name, " +
                    "bs.ho_ten AS doctor_name, " +
                    "a.tieu_de AS noi_dung_kham " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "LEFT JOIN users bs ON a.bac_si_id = bs.id " +
                    "WHERE 1=1 ";

    public List<Appointment> findAll(String scopeDoctorId, String status, String keyword) {
        List<Appointment> list = new ArrayList<>();
        String normalizedStatus = Appointment.normalizeStatusFilter(status);

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        appendDoctorScope(sql, scopeDoctorId);
        if (normalizedStatus != null) {
            sql.append("AND a.trang_thai = ? ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (a.tieu_de LIKE ? " +
                    "OR u.ho_ten LIKE ? " +
                    "OR COALESCE(p.patient_code, LEFT(p.id, 8)) LIKE ?) ");
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
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDoctorAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            list.addAll(findAllFallback(scopeDoctorId, normalizedStatus, keyword));
        }
        return list;
    }

    private List<Appointment> findAllFallback(String scopeDoctorId, String status, String keyword) {
        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, " +
                        "COALESCE(p.patient_code, LEFT(p.id, 8)) AS patient_code, " +
                        "u.ho_ten AS patient_name, bs.ho_ten AS doctor_name, " +
                        "a.tieu_de AS noi_dung_kham " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "JOIN users u ON p.user_id = u.id " +
                        "LEFT JOIN users bs ON a.bac_si_id = bs.id " +
                        "WHERE 1=1 ");
        appendDoctorScope(sql, scopeDoctorId);
        if (status != null) {
            sql.append("AND a.trang_thai = ? ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (u.ho_ten LIKE ?) ");
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
            if (status != null) {
                String dbStatus = Appointment.STATUS_HUY.equals(status) ? "huy" : status;
                ps.setString(idx++, dbStatus);
            }
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(idx, "%" + keyword.trim() + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDoctorAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(String appointmentId, String newStatus, String scopeDoctorId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            return false;
        }
        newStatus = Appointment.normalizeStatusFilter(newStatus);
        if (!Appointment.STATUS_DA_KHAM.equals(newStatus) && !Appointment.STATUS_HUY.equals(newStatus)) {
            return false;
        }

        try (Connection con = DBContext.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(buildUpdateSql(scopeDoctorId))) {
                bindUpdate(ps, appointmentId, newStatus, scopeDoctorId);
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }
            if (Appointment.STATUS_HUY.equals(newStatus)) {
                try (PreparedStatement ps = con.prepareStatement(buildUpdateSql(scopeDoctorId))) {
                    bindUpdate(ps, appointmentId, "huy", scopeDoctorId);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Appointment findById(String appointmentId, String scopeDoctorId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            return null;
        }

        StringBuilder sql = new StringBuilder(SELECT_BASE + "AND a.id = ? ");
        appendDoctorScope(sql, scopeDoctorId);

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            if (con == null) {
                return null;
            }
            int idx = 1;
            ps.setString(idx++, appointmentId);
            bindDoctorScope(ps, idx, scopeDoctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDoctorAppointment(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatusInTransaction(
            Connection con,
            String appointmentId,
            String newStatus,
            String scopeDoctorId
    ) throws java.sql.SQLException {
        if (appointmentId == null || appointmentId.isBlank()) {
            return false;
        }
        if (!Appointment.STATUS_DA_KHAM.equals(newStatus) && !Appointment.STATUS_HUY.equals(newStatus)) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(buildUpdateSql(scopeDoctorId))) {
            bindUpdate(ps, appointmentId, newStatus, scopeDoctorId);
            if (ps.executeUpdate() > 0) {
                return true;
            }
            if (Appointment.STATUS_HUY.equals(newStatus)) {
                bindUpdate(ps, appointmentId, "huy", scopeDoctorId);
                return ps.executeUpdate() > 0;
            }
        }
        return false;
    }

    private String buildUpdateSql(String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(
                "UPDATE appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "SET a.trang_thai = ? " +
                        "WHERE a.id = ? AND a.trang_thai = ? ");
        appendDoctorScope(sql, scopeDoctorId);
        return sql.toString();
    }

    private void bindUpdate(
            PreparedStatement ps,
            String appointmentId,
            String newStatus,
            String scopeDoctorId
    ) throws java.sql.SQLException {
        ps.setString(1, newStatus);
        ps.setString(2, appointmentId);
        ps.setString(3, Appointment.STATUS_CHO_KHAM);
        if (scopeDoctorId != null) {
            ps.setString(4, scopeDoctorId);
            ps.setString(5, scopeDoctorId);
        }
    }

    private void appendDoctorScope(StringBuilder sql, String scopeDoctorId) {
        if (scopeDoctorId != null) {
            sql.append("AND (a.bac_si_id = ? OR p.bac_si_id = ?) ");
        }
    }

    private int bindDoctorScope(PreparedStatement ps, int startIdx, String scopeDoctorId) throws java.sql.SQLException {
        if (scopeDoctorId == null) {
            return startIdx;
        }
        ps.setString(startIdx++, scopeDoctorId);
        ps.setString(startIdx++, scopeDoctorId);
        return startIdx;
    }

    private Appointment mapDoctorAppointment(ResultSet rs) throws java.sql.SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getString("id"));
        a.setPatientId(rs.getString("patient_id"));
        a.setPatientCode(PatientDAO.resolveCode(rs, "patient_code"));
        a.setPatientName(rs.getString("patient_name"));
        a.setBacSiId(rs.getString("bac_si_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setBacSiName(rs.getString("doctor_name"));

        String noiDung = rs.getString("noi_dung_kham");
        if (noiDung == null || noiDung.isBlank()) {
            try {
                noiDung = rs.getString("tieu_de");
            } catch (java.sql.SQLException ignored) {
                // cột tieu_de có thể không tồn tại trên schema cũ
            }
        }
        a.setNoiDungKham(noiDung);
        a.setTieuDe(noiDung);

        a.setThoiGianHen(rs.getTimestamp("thoi_gian_hen"));
        a.setDiaDiem(rs.getString("dia_diem"));
        a.setTrangThai(rs.getString("trang_thai"));
        a.setNgayTao(rs.getTimestamp("ngay_tao"));
        return a;
    }
}
