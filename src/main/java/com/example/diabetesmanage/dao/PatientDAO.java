package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public Patient getPatientByIdAdmin(String id) {
        String sql = "SELECT * FROM patients WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient p = new Patient();
                    p.setId(rs.getString("id"));
                    p.setTenBenhNhan(rs.getString("ho_ten"));
                    p.setSoDienThoai(rs.getString("so_dien_thoai"));
                    p.setEmail(rs.getString("email"));
                    p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));
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
