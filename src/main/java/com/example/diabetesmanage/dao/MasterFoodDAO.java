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
                food.setLoaiMon(rs.getString("loai_mon"));
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

    /**
     * Nếu bảng trống (hoặc chưa seed), thêm món mặc định ID ổn định f1..fN
     * để AI thực đơn luôn có dữ liệu chọn.
     */
    public int ensureDefaultFoods() {
        try (Connection conn = DBContext.getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS master_foods ("
                    + "id VARCHAR(50) PRIMARY KEY, "
                    + "ten_thuc_pham VARCHAR(255) NOT NULL, "
                    + "don_vi_khau_phan VARCHAR(100), "
                    + "carbs_g DOUBLE, "
                    + "calo_kcal DOUBLE, "
                    + "chi_so_gi DOUBLE, "
                    + "trang_thai BOOLEAN DEFAULT TRUE, "
                    + "ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) {
            System.err.println("[MasterFoodDAO] ensure master_foods table: " + e.getMessage());
        }

        List<MasterFood> existing = getAllFoods();
        if (!existing.isEmpty()) {
            return 0;
        }

        String[][] defaults = {
                {"f1", "Phở bò chín", "1 Bát vừa", "55", "430", "55"},
                {"f2", "Cơm gạo lứt", "1 Bát con", "30", "150", "55"},
                {"f3", "Cá hồi áp chảo", "1 Khúc (150g)", "0", "280", "0"},
                {"f4", "Súp lơ xanh luộc", "1 Đĩa con", "5", "30", "15"},
                {"f5", "Trứng ốp la", "1 Quả", "1", "90", "0"},
                {"f6", "Bánh mì đen nguyên cám", "2 Lát", "24", "130", "55"},
                {"f7", "Ức gà luộc", "100g", "0", "165", "0"},
                {"f8", "Salad dưa chuột cà chua", "1 Đĩa", "8", "45", "20"},
                {"f9", "Sữa chua không đường", "1 Hộp (100g)", "6", "60", "35"},
                {"f10", "Táo tây", "1 Quả vừa", "20", "80", "36"},
                {"f11", "Cháo yến mạch", "1 Bát con", "27", "150", "60"},
                {"f12", "Khoai lang luộc", "1 Củ vừa", "26", "112", "50"},
                {"f13", "Canh bí xanh nấu tôm", "1 Bát", "6", "70", "15"},
                {"f14", "Đậu phụ sốt cà chua", "1 Đĩa nhỏ", "10", "130", "30"},
                {"f15", "Rau muống xào tỏi", "1 Đĩa", "6", "100", "15"}
        };

        String sql = "INSERT INTO master_foods (id, ten_thuc_pham, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        int inserted = 0;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : defaults) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.setString(3, row[2]);
                ps.setDouble(4, Double.parseDouble(row[3]));
                ps.setDouble(5, Double.parseDouble(row[4]));
                ps.setDouble(6, Double.parseDouble(row[5]));
                inserted += ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[MasterFoodDAO] ensureDefaultFoods failed: " + e.getMessage());
            e.printStackTrace();
        }
        return inserted;
    }

    // Thêm thực phẩm mới
    public boolean addFood(MasterFood food) {
        String sql = "INSERT INTO master_foods (id, ten_thuc_pham, loai_mon, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString()); // Tự động generate UUID
            ps.setString(2, food.getTenThucPham());
            ps.setString(3, food.getLoaiMon());
            ps.setString(4, food.getDonViKhauPhan());
            ps.setDouble(5, food.getCarbsG());
            if (food.getCaloKcal() != null) ps.setDouble(6, food.getCaloKcal()); else ps.setNull(6, java.sql.Types.DOUBLE);
            if (food.getChiSoGI() != null) ps.setDouble(7, food.getChiSoGI()); else ps.setNull(7, java.sql.Types.DOUBLE);
            ps.setBoolean(8, food.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thực phẩm
    public boolean updateFood(MasterFood food) {
        String sql = "UPDATE master_foods SET ten_thuc_pham=?, loai_mon=?, don_vi_khau_phan=?, carbs_g=?, calo_kcal=?, chi_so_gi=?, trang_thai=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.getTenThucPham());
            ps.setString(2, food.getLoaiMon());
            ps.setString(3, food.getDonViKhauPhan());
            ps.setDouble(4, food.getCarbsG());
            if (food.getCaloKcal() != null) ps.setDouble(5, food.getCaloKcal()); else ps.setNull(5, java.sql.Types.DOUBLE);
            if (food.getChiSoGI() != null) ps.setDouble(6, food.getChiSoGI()); else ps.setNull(6, java.sql.Types.DOUBLE);
            ps.setBoolean(7, food.isTrangThai());
            ps.setString(8, food.getId());

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