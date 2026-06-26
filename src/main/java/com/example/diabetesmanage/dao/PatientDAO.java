package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatientDAO {

    public static String resolveCode(ResultSet rs, String codeColumn) throws SQLException {
        String code = rs.getString(codeColumn);
        if (code != null && !code.isBlank()) {
            return code;
        }
        String id = rs.getString("id");
        return id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "N/A";
    }

    private static final String SUMMARY_SELECT =
            "SELECT vps.*, p.patient_code " +
                    "FROM v_patient_summary vps " +
                    "JOIN patients p ON vps.patient_id = p.id ";

    public List<Patient> getPatients(String scopeDoctorId) {
        return searchPatients(null, null, scopeDoctorId);
    }

    public List<Patient> searchPatients(String keyword, String risk, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(SUMMARY_SELECT);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (vps.ho_ten LIKE ? OR vps.email LIKE ? OR p.patient_code LIKE ?) ");
        }

        appendRiskFilter(sql, risk);
        return queryPatients(sql.toString(), scopeDoctorId, keyword);
    }

    public boolean exists(String patientId) {
        String sql = "SELECT 1 FROM patients WHERE id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAssignedToDoctor(String patientId, String doctorId) {
        String sql = "SELECT 1 FROM patients WHERE id = ? AND bac_si_id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ps.setString(2, doctorId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Patient getPatientById(String patientId, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, " +
                        "TIMESTAMPDIFF(YEAR, p.ngay_sinh, CURDATE()) AS tuoi, " +
                        "u.id AS user_id, u.ho_ten, u.email, u.so_dien_thoai " +
                        "FROM patients p " +
                        "JOIN users u ON p.user_id = u.id " +
                        "WHERE p.id = ? "
        );
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, patientId);
            if (scopeDoctorId != null) {
                ps.setString(2, scopeDoctorId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDetailPatient(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void appendRiskFilter(StringBuilder sql, String risk) {
        if ("low".equalsIgnoreCase(risk)) {
            sql.append("AND (vps.muc_nguy_co = 'an_toan' OR vps.duong_huyet_gan_nhat < 140) ");
        } else if ("medium".equalsIgnoreCase(risk)) {
            sql.append("AND (vps.muc_nguy_co = 'trung_binh' " +
                    "OR vps.duong_huyet_gan_nhat BETWEEN 140 AND 179) ");
        } else if ("high".equalsIgnoreCase(risk)) {
            sql.append("AND (vps.muc_nguy_co = 'cao' " +
                    "OR vps.duong_huyet_gan_nhat BETWEEN 180 AND 249) ");
        } else if ("critical".equalsIgnoreCase(risk)) {
            sql.append("AND (vps.muc_nguy_co = 'nguy_hiem' OR vps.duong_huyet_gan_nhat >= 250) ");
        }
    }

    private List<Patient> queryPatients(String sql, String scopeDoctorId, String keyword) {
        List<Patient> list = new ArrayList<>();

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            int index = 1;
            if (scopeDoctorId != null) {
                ps.setString(index++, scopeDoctorId);
            }

            if (keyword != null && !keyword.isBlank()) {
                String search = "%" + keyword + "%";
                ps.setString(index++, search);
                ps.setString(index++, search);
                ps.setString(index++, search);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapSummaryPatient(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Patient mapSummaryPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getString("patient_id"));
        p.setPatientCode(resolveCode(rs, "patient_code"));
        p.setTuoi(rs.getInt("tuoi"));
        p.setGioiTinh(rs.getString("gioi_tinh"));
        p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));
        p.setNgayCapNhat(rs.getTimestamp("lan_do_cuoi"));
        p.setDuongHuyetGanNhat(optDouble(rs, "duong_huyet_gan_nhat"));
        p.setBmiGanNhat(optDouble(rs, "bmi_gan_nhat"));
        p.setHba1cGanNhat(optDouble(rs, "hba1c_gan_nhat"));
        p.setMucNguyCo(rs.getString("muc_nguy_co"));
        p.setDiemNguyCo(optDouble(rs, "diem_nguy_co"));
        p.setLanDoCuoi(rs.getTimestamp("lan_do_cuoi"));
        p.setCanhBaoChuaDoc(rs.getInt("canh_bao_chua_doc"));

        User user = new User();
        user.setHoTen(rs.getString("ho_ten"));
        user.setEmail(rs.getString("email"));
        user.setSoDienThoai(rs.getString("so_dien_thoai"));
        p.setUser(user);

        return p;
    }

    private Patient mapDetailPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getString("id"));
        p.setPatientCode(resolveCode(rs, "patient_code"));

        User user = new User();
        user.setId(UUID.fromString(rs.getString("user_id")));
        user.setHoTen(rs.getString("ho_ten"));
        user.setEmail(rs.getString("email"));
        user.setSoDienThoai(rs.getString("so_dien_thoai"));
        p.setUser(user);

        p.setTuoi(rs.getInt("tuoi"));
        if (rs.getDate("ngay_sinh") != null) {
            p.setNgaySinh(rs.getDate("ngay_sinh").toLocalDate());
        }
        p.setGioiTinh(rs.getString("gioi_tinh"));
        p.setChieuCaoCm(optDouble(rs, "chieu_cao_cm"));
        p.setDiaChi(rs.getString("dia_chi"));
        p.setBaoHiemYTe(rs.getString("bao_hiem_y_te"));
        p.setTienSuBenh(rs.getString("tien_su_benh"));
        p.setDiUng(rs.getString("di_ung"));
        p.setNhomMau(rs.getString("nhom_mau"));
        p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

        Date diagnosisDate = rs.getDate("ngay_chan_doan_tieu_duong");
        if (diagnosisDate != null) {
            p.setNgayChanDoanTieuDuong(diagnosisDate.toLocalDate());
        }

        p.setNgayCapNhat(rs.getTimestamp("ngay_cap_nhat"));
        return p;
    }

    private Double optDouble(ResultSet rs, String col) throws SQLException {
        try {
            double v = rs.getDouble(col);
            return rs.wasNull() ? null : v;
        } catch (SQLException e) {
            return null;
        }
    }

    public void updateLoaiTieuDuong(Connection con, String patientId, String loaiTieuDuong)
            throws SQLException {
        if (patientId == null || loaiTieuDuong == null || loaiTieuDuong.isBlank()) {
            return;
        }
        String sql = "UPDATE patients SET loai_tieu_duong = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiTieuDuong);
            ps.setString(2, patientId);
            ps.executeUpdate();
        }
    }
}
