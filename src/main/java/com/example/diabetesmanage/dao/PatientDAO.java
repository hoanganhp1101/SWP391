package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

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
                    "vps.huyet_ap_tam_thu AS huyet_ap_tam_thu, " +
                    "vps.huyet_ap_tam_truong AS huyet_ap_tam_truong, " +
                    "vps.muc_nguy_co AS muc_nguy_co, " +
                    "vps.diem_nguy_co AS diem_nguy_co, " +
                    "vps.lan_do_cuoi AS lan_do_cuoi, " +
                    "vps.canh_bao_chua_doc AS canh_bao_chua_doc, " +
                    "p.patient_code AS patient_code " +
                    "FROM v_patient_summary vps " +
                    "JOIN patients p ON vps.patient_id = p.id " +
                    "LEFT JOIN doctors doc ON p.bac_si_id = doc.id ";

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
                    "p.id AS patient_user_id, " +
                    "u.ho_ten AS patient_user_name, " +
                    "u.email AS patient_user_email, " +
                    "u.so_dien_thoai AS patient_user_phone, " +
                    "doc.id AS doctor_user_id " +
                    "FROM patients p " +
                    "JOIN users u ON p.id = u.id " +
                    "LEFT JOIN doctors doc ON p.bac_si_id = doc.id " +
                    "WHERE p.id = ? " +
                    "AND (? IS NULL OR doc.id = ?)";

    public boolean deletePatient(String patientId) {
        String sqlDelPatient = "DELETE FROM patients WHERE id=?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psPat = conn.prepareStatement(sqlDelPatient)) {
            psPat.setString(1, patientId);
            return psPat.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePatient(Patient p) {
        String sqlPatient = "UPDATE patients SET ngay_sinh=?, loai_tieu_duong=? WHERE id=?";
        String sqlUser = "UPDATE users SET ho_ten=?, email=?, so_dien_thoai=?, ngay_cap_nhat=NOW() WHERE id=?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPat = conn.prepareStatement(sqlPatient);
                 PreparedStatement psUser = conn.prepareStatement(sqlUser)) {

                psPat.setDate(1, p.getNgaySinh());
                psPat.setString(2, p.getLoaiTieuDuong());
                psPat.setString(3, p.getId());
                psPat.executeUpdate();

                psUser.setString(1, p.getTenBenhNhan() != null ? p.getTenBenhNhan() : p.getHoTen());
                psUser.setString(2, p.getEmail());
                psUser.setString(3, p.getSoDienThoai());
                psUser.setString(4, p.getId());
                psUser.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePatientMedicalProfile(Patient p) {
        String sql = "UPDATE patients SET gioi_tinh=?, chieu_cao_cm=?, dia_chi=?, bao_hiem_y_te=?, tien_su_benh=?, tien_su_gia_dinh=?, di_ung=?, nhom_mau=?, ngay_chan_doan_tieu_duong=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getGioiTinh());
            if (p.getChieuCaoCm() != null) {
                ps.setDouble(2, p.getChieuCaoCm());
            } else {
                ps.setNull(2, java.sql.Types.DOUBLE);
            }
            ps.setString(3, p.getDiaChi());
            ps.setString(4, p.getBaoHiemYTe());
            ps.setString(5, p.getTienSuBenh());
            ps.setString(6, p.getTienSuGiaDinh());
            ps.setString(7, p.getDiUng());
            ps.setString(8, p.getNhomMau());
            if (p.getNgayChanDoanTieuDuong() != null) {
                ps.setDate(9, p.getNgayChanDoanTieuDuong());
            } else {
                ps.setNull(9, java.sql.Types.DATE);
            }
            ps.setString(10, p.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Patient getPatientByIdAdmin(String id) {
        String sql = "SELECT p.*, u.ho_ten, u.email, u.so_dien_thoai, u.anh_dai_dien "
                + "FROM patients p JOIN users u ON p.id = u.id WHERE p.id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient p = new Patient();
                    p.setId(rs.getString("id"));
                    p.setUserId(rs.getString("id"));
                    p.setPatientCode(rs.getString("patient_code"));
                    p.setNgaySinh(rs.getDate("ngay_sinh"));
                    p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

                    p.setTenBenhNhan(rs.getString("ho_ten"));
                    p.setHoTen(rs.getString("ho_ten"));
                    p.setSoDienThoai(rs.getString("so_dien_thoai"));
                    p.setEmail(rs.getString("email"));
                    return p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, u.ho_ten AS ten_benh_nhan, u.email, u.so_dien_thoai, u.anh_dai_dien, "
                + "du.ho_ten AS ten_bac_si "
                + "FROM patients p "
                + "JOIN users u ON p.id = u.id "
                + "LEFT JOIN doctors doc ON p.bac_si_id = doc.id "
                + "LEFT JOIN users du ON doc.id = du.id "
                + "ORDER BY p.ngay_cap_nhat DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getString("id"));
                p.setUserId(rs.getString("id"));
                p.setPatientCode(rs.getString("patient_code"));
                p.setBacSiId(rs.getString("bac_si_id"));
                p.setNgaySinh(rs.getDate("ngay_sinh"));
                p.setGioiTinh(rs.getString("gioi_tinh"));
                p.setChieuCaoCm(rs.getDouble("chieu_cao_cm"));
                p.setDiaChi(rs.getString("dia_chi"));
                p.setBaoHiemYTe(rs.getString("bao_hiem_y_te"));
                p.setTienSuBenh(rs.getString("tien_su_benh"));
                p.setDiUng(rs.getString("di_ung"));
                p.setNhomMau(rs.getString("nhom_mau"));
                p.setNgayChanDoanTieuDuong(rs.getDate("ngay_chan_doan_tieu_duong"));
                p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

                p.setTenBenhNhan(rs.getString("ten_benh_nhan"));
                p.setHoTen(rs.getString("ten_benh_nhan"));
                p.setEmail(rs.getString("email"));
                p.setSoDienThoai(rs.getString("so_dien_thoai"));
                p.setAnhDaiDien(rs.getString("anh_dai_dien"));
                p.setTenBacSi(rs.getString("ten_bac_si"));

                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Patient getPatientById(String patientId) {
        String sql = "SELECT p.*, u.ho_ten, u.email, u.so_dien_thoai, u.anh_dai_dien "
                + "FROM patients p JOIN users u ON p.id = u.id WHERE p.id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getString("id"));
                p.setUserId(rs.getString("id"));
                p.setPatientCode(rs.getString("patient_code"));
                p.setBacSiId(rs.getString("bac_si_id"));
                p.setNgaySinh(rs.getDate("ngay_sinh"));
                p.setGioiTinh(rs.getString("gioi_tinh"));
                p.setChieuCaoCm(rs.getDouble("chieu_cao_cm"));
                p.setDiaChi(rs.getString("dia_chi"));
                p.setBaoHiemYTe(rs.getString("bao_hiem_y_te"));
                p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));
                p.setTienSuBenh(rs.getString("tien_su_benh"));
                p.setTienSuGiaDinh(rs.getString("tien_su_gia_dinh"));
                p.setDiUng(rs.getString("di_ung"));
                p.setNhomMau(rs.getString("nhom_mau"));
                p.setNgayChanDoanTieuDuong(rs.getDate("ngay_chan_doan_tieu_duong"));
                p.setHoTen(rs.getString("ho_ten"));
                p.setTenBenhNhan(rs.getString("ho_ten"));
                p.setEmail(rs.getString("email"));
                p.setSoDienThoai(rs.getString("so_dien_thoai"));
                p.setAnhDaiDien(rs.getString("anh_dai_dien"));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePatientProfile(Patient p) {
        String sqlMedical = "UPDATE patients SET ngay_sinh = ?, gioi_tinh = ?, dia_chi = ?, loai_tieu_duong = ?, "
                + "tien_su_benh = ?, di_ung = ? WHERE id = ?";
        String sqlUser = "UPDATE users SET ho_ten = ?, email = ?, so_dien_thoai = ?, anh_dai_dien = ?, ngay_cap_nhat = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psM = conn.prepareStatement(sqlMedical);
                 PreparedStatement psU = conn.prepareStatement(sqlUser)) {
                psM.setDate(1, p.getNgaySinh());
                psM.setString(2, p.getGioiTinh());
                psM.setString(3, p.getDiaChi());
                psM.setString(4, p.getLoaiTieuDuong());
                psM.setString(5, p.getTienSuBenh());
                psM.setString(6, p.getDiUng());
                psM.setString(7, p.getId());
                psM.executeUpdate();

                psU.setString(1, p.getHoTen());
                psU.setString(2, p.getEmail());
                psU.setString(3, p.getSoDienThoai());
                psU.setString(4, p.getAnhDaiDien());
                psU.setString(5, p.getId());
                psU.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getDemoPatientId() {
        String sql = "SELECT id FROM patients LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPatientIdByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String sql = "SELECT id FROM patients WHERE id = ? LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Đảm bảo user bệnh nhân có dòng trong {@code patients} (cùng UUID).
     */
    public String ensurePatientProfileForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String existing = getPatientIdByUserId(userId);
        if (existing != null) {
            return existing;
        }
        User account = UserDAO.getInstance().getUserById(userId);
        if (account == null || !"benh_nhan".equalsIgnoreCase(account.getVaiTro())) {
            return null;
        }
        String sql = "INSERT INTO patients (id, patient_code, ngay_sinh, loai_tieu_duong, ngay_tao) "
                + "VALUES (?, ?, '2000-01-01', 'Type 2', NOW())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            ps.setString(2, "BN" + userId.trim().replace("-", "").substring(0, Math.min(8, userId.trim().replace("-", "").length())).toUpperCase());
            if (ps.executeUpdate() > 0) {
                return userId.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getPatientIdByUserId(userId);
    }

    // ── Doctor portal methods ──────────────────────────────────────────────────

    public List<Patient> getPatients(String scopeDoctorId) {
        return searchPatients(null, null, null, null, null, null, null, null, null, scopeDoctorId);
    }

    public List<Patient> searchPatients(String keyword, String glucose, String hba1c,
                                        String bmi, String bloodPressure, String age,
                                        String gender, String diabetesType,
                                        String action, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(SUMMARY_SELECT);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE doc.id = ? ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (vps.ho_ten LIKE ? OR vps.email LIKE ? OR p.patient_code LIKE ?) ");
        }

        appendGlucoseFilter(sql, glucose);
        appendHba1cFilter(sql, hba1c);
        appendBmiFilter(sql, bmi);
        appendBloodPressureFilter(sql, bloodPressure);
        appendAgeFilter(sql, age);
        appendGenderFilter(sql, gender);
        appendDiabetesTypeFilter(sql, diabetesType);
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
                        "JOIN doctors doc ON p.bac_si_id = doc.id " +
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
        p.setPatientCode(rs.getString("patient_code"));

        User user = new User();
        user.setId(rs.getString("patient_user_id"));
        user.setHoTen(optionalString(rs, "patient_user_name"));
        user.setEmail(optionalString(rs, "patient_user_email"));
        user.setSoDienThoai(optionalString(rs, "patient_user_phone"));
        p.setUser(user);
        p.setHoTen(user.getHoTen());
        p.setEmail(user.getEmail());
        p.setSoDienThoai(user.getSoDienThoai());

        Integer age = optionalInt(rs, "patient_age");
        if (age != null) {
            p.setTuoi(age);
        }
        java.sql.Date birthDate = rs.getDate("patient_birth_date");
        if (birthDate != null) {
            p.setNgaySinh(birthDate);
        }
        p.setGioiTinh(optionalString(rs, "patient_gender"));
        p.setChieuCaoCm(optDouble(rs, "patient_height_cm"));
        p.setDiaChi(optionalString(rs, "patient_address"));
        p.setBaoHiemYTe(optionalString(rs, "patient_insurance"));
        p.setTienSuBenh(optionalString(rs, "patient_medical_history"));
        p.setDiUng(optionalString(rs, "patient_allergies"));
        p.setNhomMau(optionalString(rs, "patient_blood_type"));
        p.setLoaiTieuDuong(optionalString(rs, "patient_diabetes_type"));

        java.sql.Date diagnosisDate = rs.getDate("patient_diabetes_diagnosis_date");
        if (diagnosisDate != null) {
            p.setNgayChanDoanTieuDuong(diagnosisDate);
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("patient_updated_at");
        if (updatedAt != null) {
            p.setNgayCapNhat(updatedAt);
        }

        String doctorUserId = optionalString(rs, "doctor_user_id");
        if (doctorUserId != null && !doctorUserId.isBlank()) {
            User doctor = new User();
            doctor.setId(doctorUserId);
            p.setDoctor(doctor);
        }
        return p;
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

    private void appendBloodPressureFilter(StringBuilder sql, String bloodPressure) {
        if ("normal".equalsIgnoreCase(bloodPressure)) {
            sql.append("AND vps.huyet_ap_tam_thu IS NOT NULL AND vps.huyet_ap_tam_truong IS NOT NULL ")
                    .append("AND vps.huyet_ap_tam_thu < 140 AND vps.huyet_ap_tam_truong < 90 ");
        } else if ("high".equalsIgnoreCase(bloodPressure)) {
            sql.append("AND (vps.huyet_ap_tam_thu >= 140 OR vps.huyet_ap_tam_truong >= 90) ");
        } else if ("low".equalsIgnoreCase(bloodPressure)) {
            sql.append("AND vps.huyet_ap_tam_thu IS NOT NULL AND vps.huyet_ap_tam_thu < 90 ");
        } else if ("missing".equalsIgnoreCase(bloodPressure)) {
            sql.append("AND (vps.huyet_ap_tam_thu IS NULL OR vps.huyet_ap_tam_truong IS NULL) ");
        }
    }

    private void appendAgeFilter(StringBuilder sql, String age) {
        if ("child".equalsIgnoreCase(age)) {
            sql.append("AND vps.tuoi < 18 ");
        } else if ("adult".equalsIgnoreCase(age)) {
            sql.append("AND vps.tuoi BETWEEN 18 AND 39 ");
        } else if ("middle".equalsIgnoreCase(age)) {
            sql.append("AND vps.tuoi BETWEEN 40 AND 59 ");
        } else if ("senior".equalsIgnoreCase(age)) {
            sql.append("AND vps.tuoi >= 60 ");
        }
    }

    private void appendGenderFilter(StringBuilder sql, String gender) {
        if ("nam".equalsIgnoreCase(gender)) {
            sql.append("AND vps.gioi_tinh = 'nam' ");
        } else if ("nu".equalsIgnoreCase(gender)) {
            sql.append("AND vps.gioi_tinh = 'nu' ");
        } else if ("khac".equalsIgnoreCase(gender)) {
            sql.append("AND vps.gioi_tinh = 'khac' ");
        }
    }

    private void appendDiabetesTypeFilter(StringBuilder sql, String diabetesType) {
        if ("Type 1".equals(diabetesType)) {
            sql.append("AND vps.loai_tieu_duong = 'Type 1' ");
        } else if ("Type 2".equals(diabetesType)) {
            sql.append("AND vps.loai_tieu_duong = 'Type 2' ");
        } else if ("Thai kỳ".equals(diabetesType)) {
            sql.append("AND vps.loai_tieu_duong = 'Thai kỳ' ");
        } else if ("Khác".equals(diabetesType)) {
            sql.append("AND vps.loai_tieu_duong = 'Khác' ");
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
        p.setPatientCode(rs.getString("patient_code"));
        p.setTuoi(rs.getInt("tuoi"));
        p.setGioiTinh(rs.getString("gioi_tinh"));
        p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));
        p.setNgayCapNhat(rs.getTimestamp("lan_do_cuoi"));
        p.setDuongHuyetGanNhat(optDouble(rs, "duong_huyet_gan_nhat"));
        p.setBmiGanNhat(optDouble(rs, "bmi_gan_nhat"));
        p.setHba1cGanNhat(optDouble(rs, "hba1c_gan_nhat"));
        p.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
        p.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
        p.setMucNguyCo(rs.getString("muc_nguy_co"));
        p.setDiemNguyCo(optDouble(rs, "diem_nguy_co"));
        p.setLanDoCuoi(rs.getTimestamp("lan_do_cuoi"));
        p.setCanhBaoChuaDoc(rs.getInt("canh_bao_chua_doc"));

        User user = new User();
        user.setHoTen(rs.getString("ho_ten"));
        user.setEmail(rs.getString("email"));
        user.setSoDienThoai(rs.getString("so_dien_thoai"));
        p.setUser(user);
        p.setHoTen(user.getHoTen());
        p.setEmail(user.getEmail());
        p.setSoDienThoai(user.getSoDienThoai());

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

    /**
     * Cập nhật chiều cao / cân nặng mới nhất trên hồ sơ bệnh nhân (không tạo bản ghi mới).
     * Chỉ ghi các giá trị không null; giữ nguyên cột còn lại nếu không được gửi.
     */
    public boolean updateHeightAndWeight(Connection con, String patientId, Double height, Double weight)
            throws SQLException {
        if (con == null) {
            throw new SQLException("Connection is required for updateHeightAndWeight");
        }
        if (patientId == null || patientId.isBlank()) {
            return false;
        }
        if (height == null && weight == null) {
            return false;
        }
        String sql =
                "UPDATE patients SET " +
                        "chieu_cao_cm = COALESCE(?, chieu_cao_cm), " +
                        "can_nang_kg = COALESCE(?, can_nang_kg), " +
                        "ngay_cap_nhat = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (height != null) {
                ps.setDouble(1, height);
            } else {
                ps.setNull(1, Types.DECIMAL);
            }
            if (weight != null) {
                ps.setDouble(2, weight);
            } else {
                ps.setNull(2, Types.DECIMAL);
            }
            ps.setString(3, patientId.trim());
            return ps.executeUpdate() > 0;
        }
    }
}
