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
        String id = rs.getString("patient_id");
        if (id == null || id.isBlank()) {
            id = rs.getString("id");
        }
        return id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "N/A";
    }

    private static final String SUMMARY_SELECT =
            "SELECT vps.patient_id AS patient_id, " +
                    "vps.ho_ten AS ho_ten, " +
                    "vps.email AS email, " +
                    "vps.so_dien_thoai AS so_dien_thoai, " +
                    "vps.gioi_tinh AS gioi_tinh, " +
                    "vps.loai_tieu_duong AS loai_tieu_duong, " +
                    "vps.tuoi AS tuoi, " +
                    "vps.duong_huyet_gan_nhat AS duong_huyet_gan_nhat, " +
                    "vps.bmi_gan_nhat AS bmi_gan_nhat, " +
                    "vps.hba1c_gan_nhat AS hba1c_gan_nhat, " +
                    "vps.muc_nguy_co AS muc_nguy_co, " +
                    "vps.diem_nguy_co AS diem_nguy_co, " +
                    "vps.lan_do_cuoi AS lan_do_cuoi, " +
                    "vps.canh_bao_chua_doc AS canh_bao_chua_doc, " +
                    "p.patient_code AS patient_code " +
                    "FROM v_patient_summary vps " +
                    "JOIN patients p ON vps.patient_id = p.id " +
                    "LEFT JOIN users doc ON p.bac_si_id = doc.id ";

    public List<Patient> getPatients(String scopeDoctorId) {
        return searchPatients(null, null, null, null, null, scopeDoctorId);
    }

    public List<Patient> searchPatients(String keyword, String glucose, String hba1c,
                                        String bmi, String action, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(SUMMARY_SELECT);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE doc.id = ? ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (vps.ho_ten LIKE ? OR vps.email LIKE ? OR p.patient_code LIKE ?) ");
        }

        appendGlucoseFilter(sql, glucose);
        appendHba1cFilter(sql, hba1c);
        appendBmiFilter(sql, bmi);
        appendActionFilter(sql, action);
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
        if (patientId == null || patientId.isBlank() || doctorId == null || doctorId.isBlank()) {
            return false;
        }
        String sql =
                "SELECT 1 FROM patients p " +
                        "JOIN users doc ON p.bac_si_id = doc.id " +
                        "WHERE p.id = ? AND doc.id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId.trim());
            ps.setString(2, doctorId.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final String PATIENT_BY_ID_SQL =
            "SELECT p.id AS patient_id, " +
                    "p.patient_code AS patient_code, " +
                    "p.ngay_sinh AS patient_birth_date, " +
                    "p.gioi_tinh AS patient_gender, " +
                    "p.chieu_cao_cm AS patient_height_cm, " +
                    "p.dia_chi AS patient_address, " +
                    "p.bao_hiem_y_te AS patient_insurance, " +
                    "p.tien_su_benh AS patient_medical_history, " +
                    "p.di_ung AS patient_allergies, " +
                    "p.nhom_mau AS patient_blood_type, " +
                    "p.loai_tieu_duong AS patient_diabetes_type, " +
                    "p.ngay_chan_doan_tieu_duong AS patient_diabetes_diagnosis_date, " +
                    "p.ngay_cap_nhat AS patient_updated_at, " +
                    "p.bac_si_id AS assigned_doctor_id, " +
                    "TIMESTAMPDIFF(YEAR, p.ngay_sinh, CURDATE()) AS patient_age, " +
                    "pu.id AS patient_user_id, " +
                    "pu.ho_ten AS patient_user_name, " +
                    "pu.email AS patient_user_email, " +
                    "pu.so_dien_thoai AS patient_user_phone, " +
                    "doc.id AS doctor_user_id " +
                    "FROM patients p " +
                    "LEFT JOIN users pu ON p.user_id = pu.id " +
                    "LEFT JOIN users doc ON p.bac_si_id = doc.id " +
                    "WHERE p.id = ? " +
                    "AND (? IS NULL OR doc.id = ?)";

    public Patient getPatientById(String patientId, String scopeDoctorId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(PATIENT_BY_ID_SQL)
        ) {
            ps.setString(1, patientId.trim());
            bindDoctorScopeParams(ps, 2, 3, scopeDoctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPatientByIdRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void bindDoctorScopeParams(
            PreparedStatement ps,
            int nullCheckIndex,
            int valueIndex,
            String scopeDoctorId
    ) throws SQLException {
        if (scopeDoctorId != null && !scopeDoctorId.isBlank()) {
            String doctorUserId = scopeDoctorId.trim();
            ps.setString(nullCheckIndex, doctorUserId);
            ps.setString(valueIndex, doctorUserId);
        } else {
            ps.setNull(nullCheckIndex, Types.VARCHAR);
            ps.setNull(valueIndex, Types.VARCHAR);
        }
    }

    private Patient mapPatientByIdRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getString("patient_id"));
        p.setPatientCode(PatientDAO.resolveCode(rs, "patient_code"));

        User user = new User();
        parseUuid(rs.getString("patient_user_id")).ifPresent(user::setId);
        user.setHoTen(optionalString(rs, "patient_user_name"));
        user.setEmail(optionalString(rs, "patient_user_email"));
        user.setSoDienThoai(optionalString(rs, "patient_user_phone"));
        p.setUser(user);

        Integer age = optionalInt(rs, "patient_age");
        if (age != null) {
            p.setTuoi(age);
        }
        Date birthDate = rs.getDate("patient_birth_date");
        if (birthDate != null) {
            p.setNgaySinh(birthDate.toLocalDate());
        }
        p.setGioiTinh(optionalString(rs, "patient_gender"));
        p.setChieuCaoCm(optDouble(rs, "patient_height_cm"));
        p.setDiaChi(optionalString(rs, "patient_address"));
        p.setBaoHiemYTe(optionalString(rs, "patient_insurance"));
        p.setTienSuBenh(optionalString(rs, "patient_medical_history"));
        p.setDiUng(optionalString(rs, "patient_allergies"));
        p.setNhomMau(optionalString(rs, "patient_blood_type"));
        p.setLoaiTieuDuong(optionalString(rs, "patient_diabetes_type"));

        Date diagnosisDate = rs.getDate("patient_diabetes_diagnosis_date");
        if (diagnosisDate != null) {
            p.setNgayChanDoanTieuDuong(diagnosisDate.toLocalDate());
        }

        Timestamp updatedAt = rs.getTimestamp("patient_updated_at");
        if (updatedAt != null) {
            p.setNgayCapNhat(updatedAt);
        }

        String doctorUserId = optionalString(rs, "doctor_user_id");
        if (doctorUserId != null && !doctorUserId.isBlank()) {
            User doctor = new User();
            parseUuid(doctorUserId).ifPresent(doctor::setId);
            p.setDoctor(doctor);
        }
        return p;
    }

    private static java.util.Optional<UUID> parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(UUID.fromString(raw.trim()));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private static String optionalString(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer optionalInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private void appendGlucoseFilter(StringBuilder sql, String glucose) {
        if ("normal".equalsIgnoreCase(glucose)) {
            sql.append("AND vps.duong_huyet_gan_nhat < 140 ");
        } else if ("high".equalsIgnoreCase(glucose)) {
            sql.append("AND vps.duong_huyet_gan_nhat BETWEEN 140 AND 249 ");
        } else if ("critical".equalsIgnoreCase(glucose)) {
            sql.append("AND vps.duong_huyet_gan_nhat >= 250 ");
        } else if ("missing".equalsIgnoreCase(glucose)) {
            sql.append("AND vps.duong_huyet_gan_nhat IS NULL ");
        }
    }

    private void appendHba1cFilter(StringBuilder sql, String hba1c) {
        if ("normal".equalsIgnoreCase(hba1c)) {
            sql.append("AND vps.hba1c_gan_nhat < 5.7 ");
        } else if ("prediabetes".equalsIgnoreCase(hba1c)) {
            sql.append("AND vps.hba1c_gan_nhat BETWEEN 5.7 AND 6.4 ");
        } else if ("high".equalsIgnoreCase(hba1c)) {
            sql.append("AND vps.hba1c_gan_nhat >= 6.5 ");
        } else if ("missing".equalsIgnoreCase(hba1c)) {
            sql.append("AND vps.hba1c_gan_nhat IS NULL ");
        }
    }

    private void appendBmiFilter(StringBuilder sql, String bmi) {
        if ("normal".equalsIgnoreCase(bmi)) {
            sql.append("AND vps.bmi_gan_nhat < 25 ");
        } else if ("overweight".equalsIgnoreCase(bmi)) {
            sql.append("AND vps.bmi_gan_nhat BETWEEN 25 AND 29.9 ");
        } else if ("obese".equalsIgnoreCase(bmi)) {
            sql.append("AND vps.bmi_gan_nhat >= 30 ");
        } else if ("missing".equalsIgnoreCase(bmi)) {
            sql.append("AND vps.bmi_gan_nhat IS NULL ");
        }
    }

    private void appendActionFilter(StringBuilder sql, String action) {
        if ("no-update".equalsIgnoreCase(action)) {
            sql.append("AND (vps.lan_do_cuoi IS NULL " +
                    "OR vps.lan_do_cuoi < DATE_SUB(NOW(), INTERVAL 7 DAY)) ");
        } else if ("no-followup".equalsIgnoreCase(action)) {
            sql.append("AND (vps.lan_do_cuoi IS NULL " +
                    "OR vps.lan_do_cuoi < DATE_SUB(NOW(), INTERVAL 30 DAY)) ");
        }
    }

    private List<Patient> queryPatients(String sql, String scopeDoctorId, String keyword) {
        List<Patient> list = new ArrayList<>();

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            int index = 1;
            if (scopeDoctorId != null && !scopeDoctorId.isBlank()) {
                ps.setString(index++, scopeDoctorId.trim());
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
