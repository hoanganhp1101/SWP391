package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.MasterFood;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MasterFoodDAO {

    // Lấy danh sách tất cả thực phẩm
    public List<MasterFood> getAllFoods() {
        List<MasterFood> list = new ArrayList<>();
        String sql = "SELECT * FROM master_foods ORDER BY ngay_tao DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MasterFood food = new MasterFood();
                food.setId(rs.getString("id"));
                food.setTenThucPham(rs.getString("ten_thuc_pham"));
                food.setDonViKhauPhan(rs.getString("don_vi_khau_phan"));
                food.setCarbsG(rs.getDouble("carbs_g"));
                food.setCaloKcal(rs.getDouble("calo_kcal"));
                food.setChiSoGI(rs.getDouble("chi_so_gi"));
                food.setTrangThai(rs.getBoolean("trang_thai"));
                food.setNgayTao(rs.getTimestamp("ngay_tao"));
                list.add(food);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm thực phẩm mới
    public boolean addFood(MasterFood food) {
        String sql = "INSERT INTO master_foods (id, ten_thuc_pham, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString()); // Tự động generate UUID
            ps.setString(2, food.getTenThucPham());
            ps.setString(3, food.getDonViKhauPhan());
            ps.setDouble(4, food.getCarbsG());
            if (food.getCaloKcal() != null) ps.setDouble(5, food.getCaloKcal()); else ps.setNull(5, java.sql.Types.DOUBLE);
            if (food.getChiSoGI() != null) ps.setDouble(6, food.getChiSoGI()); else ps.setNull(6, java.sql.Types.DOUBLE);
            ps.setBoolean(7, food.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thực phẩm
    public boolean updateFood(MasterFood food) {
        String sql = "UPDATE master_foods SET ten_thuc_pham=?, don_vi_khau_phan=?, carbs_g=?, calo_kcal=?, chi_so_gi=?, trang_thai=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.getTenThucPham());
            ps.setString(2, food.getDonViKhauPhan());
            ps.setDouble(3, food.getCarbsG());
            if (food.getCaloKcal() != null) ps.setDouble(4, food.getCaloKcal()); else ps.setNull(4, java.sql.Types.DOUBLE);
            if (food.getChiSoGI() != null) ps.setDouble(5, food.getChiSoGI()); else ps.setNull(5, java.sql.Types.DOUBLE);
            ps.setBoolean(6, food.isTrangThai());
            ps.setString(7, food.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa thực phẩm
    public boolean deleteFood(String id) {
        String sql = "DELETE FROM master_foods WHERE id=?";
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