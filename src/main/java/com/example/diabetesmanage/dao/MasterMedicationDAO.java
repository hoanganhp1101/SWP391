package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.MasterMedication;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MasterMedicationDAO {

    // Lấy danh sách tất cả các loại thuốc
    public List<MasterMedication> getAllMedications() {
        List<MasterMedication> list = new ArrayList<>();
        String sql = "SELECT * FROM master_medications ORDER BY ngay_tao DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MasterMedication med = new MasterMedication();
                med.setId(rs.getString("id"));
                med.setTenThuoc(rs.getString("ten_thuoc"));
                med.setHoatChat(rs.getString("hoat_chat"));
                med.setDonViTinh(rs.getString("don_vi_tinh"));
                med.setLoaiThuoc(rs.getString("loai_thuoc"));
                med.setHuongDanGoc(rs.getString("huong_dan_goc"));
                med.setTrangThai(rs.getBoolean("trang_thai"));
                med.setNgayTao(rs.getTimestamp("ngay_tao"));
                list.add(med);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm thuốc mới
    public boolean addMedication(MasterMedication med) {
        String sql = "INSERT INTO master_medications (id, ten_thuoc, hoat_chat, don_vi_tinh, loai_thuoc, huong_dan_goc, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, med.getTenThuoc());
            ps.setString(3, med.getHoatChat());
            ps.setString(4, med.getDonViTinh());
            ps.setString(5, med.getLoaiThuoc());
            ps.setString(6, med.getHuongDanGoc());
            ps.setBoolean(7, med.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin thuốc
    public boolean updateMedication(MasterMedication med) {
        String sql = "UPDATE master_medications SET ten_thuoc=?, hoat_chat=?, don_vi_tinh=?, loai_thuoc=?, huong_dan_goc=?, trang_thai=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, med.getTenThuoc());
            ps.setString(2, med.getHoatChat());
            ps.setString(3, med.getDonViTinh());
            ps.setString(4, med.getLoaiThuoc());
            ps.setString(5, med.getHuongDanGoc());
            ps.setBoolean(6, med.isTrangThai());
            ps.setString(7, med.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa thuốc
    public boolean deleteMedication(String id) {
        String sql = "DELETE FROM master_medications WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}