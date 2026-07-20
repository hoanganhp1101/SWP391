package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

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
        if (scopeDoctorId != null) {
            sql.append("AND (a.bac_si_id = ? OR p.bac_si_id = ?) ");
        }

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
                return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        if (patientCode == null || patientCode.isBlank()) {
            String patientId = rs.getString("patient_id");
            patientCode = patientId != null && patientId.length() >= 8
                    ? patientId.substring(0, 8).toUpperCase() : "N/A";
        }
        a.setPatientCode(patientCode);
        a.setPatientName(rs.getString("patient_name"));
        a.setBacSiId(rs.getString("bac_si_id"));
        a.setDoctorName(rs.getString("doctor_name"));

        String noiDung = rs.getString("noi_dung_kham");
        if (noiDung == null || noiDung.isBlank()) {
            try {
                noiDung = rs.getString("tieu_de");
            } catch (SQLException ignored) {
                // cột tieu_de có thể không tồn tại trên schema cũ
            }
        }
        a.setNoiDungKham(noiDung);

        Timestamp ts = rs.getTimestamp("thoi_gian_hen");
        if (ts != null) {
            a.setThoiGianHen(ts.toLocalDateTime());
        }
        a.setDiaDiem(rs.getString("dia_diem"));
        a.setTrangThai(rs.getString("trang_thai"));
        return a;
    }
}
