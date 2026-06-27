package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatientDAO {

    public boolean deletePatient(String patientId) {
        // Đã thêm logic xóa luôn tài khoản user tương ứng để tránh rác dữ liệu
        String sqlGetUserId = "SELECT user_id FROM patients WHERE id=?";
        String sqlDelPatient = "DELETE FROM patients WHERE id=?";
        String sqlDelUser = "DELETE FROM users WHERE id=?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            String userId = null;

            try (PreparedStatement psGet = conn.prepareStatement(sqlGetUserId)) {
                psGet.setString(1, patientId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getString("user_id");
                    }
                }
            }

            try (PreparedStatement psPat = conn.prepareStatement(sqlDelPatient)) {
                psPat.setString(1, patientId);
                psPat.executeUpdate();
            }

            if (userId != null) {
                try (PreparedStatement psUser = conn.prepareStatement(sqlDelUser)) {
                    psUser.setString(1, userId);
                    psUser.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePatient(Patient p) {
        // ĐÃ SỬA: Chỉ update thông tin y khoa vào bảng patients
        String sqlPatient = "UPDATE patients SET ngay_sinh=?, loai_tieu_duong=? WHERE id=?";
        // Cập nhật thông tin cá nhân vào bảng users dựa trên user_id của bệnh nhân
        String sqlUser = "UPDATE users SET ho_ten=?, email=?, so_dien_thoai=? WHERE id=(SELECT user_id FROM patients WHERE id=?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPat = conn.prepareStatement(sqlPatient);
                 PreparedStatement psUser = conn.prepareStatement(sqlUser)) {

                // Update bảng patients
                psPat.setDate(1, p.getNgaySinh());
                psPat.setString(2, p.getLoaiTieuDuong());
                psPat.setString(3, p.getId());
                psPat.executeUpdate();

                // Update bảng users
                psUser.setString(1, p.getTenBenhNhan());
                psUser.setString(2, p.getEmail());
                psUser.setString(3, p.getSoDienThoai());
                psUser.setString(4, p.getId());
                psUser.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addPatient(Patient p) {
        String userId = UUID.randomUUID().toString();
        String patientId = UUID.randomUUID().toString();

        String sqlUser = "INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash) VALUES (?, ?, ?, ?, 'benh_nhan', 'hash_mac_dinh_123')";

        // ĐÃ SỬA: Bỏ insert ho_ten, email, sdt vào bảng patients vì chúng thuộc về bảng users
        String sqlPatient = "INSERT INTO patients (id, user_id, ngay_sinh, loai_tieu_duong) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser);
                 PreparedStatement psPat = conn.prepareStatement(sqlPatient)) {

                psUser.setString(1, userId);
                psUser.setString(2, p.getTenBenhNhan());
                psUser.setString(3, p.getEmail());
                psUser.setString(4, p.getSoDienThoai());
                psUser.executeUpdate();

                psPat.setString(1, patientId);
                psPat.setString(2, userId);
                psPat.setDate(3, p.getNgaySinh());
                psPat.setString(4, p.getLoaiTieuDuong());
                psPat.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Patient getPatientByIdAdmin(String id) {
        // ĐÃ SỬA: JOIN với bảng users để lấy các cột ho_ten, email, so_dien_thoai
        String sql = "SELECT p.*, u.ho_ten, u.email, u.so_dien_thoai " +
                "FROM patients p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient p = new Patient();
                    p.setId(rs.getString("id"));
                    p.setUserId(rs.getString("user_id"));
                    p.setNgaySinh(rs.getDate("ngay_sinh"));
                    p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

                    // Lấy thông tin cá nhân từ bảng users
                    p.setTenBenhNhan(rs.getString("ho_ten"));
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
        String sql = "SELECT p.*, u1.ho_ten AS ten_benh_nhan, u1.email, u1.so_dien_thoai, u2.ho_ten AS ten_bac_si " +
                "FROM patients p " +
                "JOIN users u1 ON p.user_id = u1.id " +
                "LEFT JOIN users u2 ON p.bac_si_id = u2.id " +
                "ORDER BY p.ngay_cap_nhat DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getString("id"));
                p.setUserId(rs.getString("user_id"));
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
                p.setTenBacSi(rs.getString("ten_bac_si"));

                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Patient getPatientById(String patientId) {
        String sql = "SELECT p.*, u.ho_ten FROM patients p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getString("id"));
                p.setUserId(rs.getString("user_id"));
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
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}
