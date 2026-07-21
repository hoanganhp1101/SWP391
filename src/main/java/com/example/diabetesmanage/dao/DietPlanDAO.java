package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.DietPlan;
import com.example.diabetesmanage.model.DietPlanDetail;
import com.example.diabetesmanage.model.MasterFood;
import com.example.diabetesmanage.context.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DietPlanDAO {

    public boolean ensureDietTables() {
        String createPlans = "CREATE TABLE IF NOT EXISTS diet_plans ("
                + "id VARCHAR(50) PRIMARY KEY, "
                + "patient_id VARCHAR(50) NOT NULL, "
                + "doctor_id VARCHAR(50), "
                + "ngay_tao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "ghi_chu TEXT"
                + ")";
        String createDetails = "CREATE TABLE IF NOT EXISTS diet_plan_details ("
                + "id VARCHAR(50) PRIMARY KEY, "
                + "diet_plan_id VARCHAR(50) NOT NULL, "
                + "food_id VARCHAR(50) NOT NULL, "
                + "bua_an VARCHAR(50) NOT NULL, "
                + "ghi_chu TEXT"
                + ")";
        try (Connection conn = DBContext.getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.execute(createPlans);
            st.execute(createDetails);
            return true;
        } catch (Exception e) {
            System.err.println("[DietPlanDAO] ensureDietTables failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveDietPlan(DietPlan plan) {
        String insertPlanSql = "INSERT INTO diet_plans (id, patient_id, doctor_id, ngay_tao, ghi_chu) VALUES (?, ?, ?, ?, ?)";
        String insertDetailSql = "INSERT INTO diet_plan_details (id, diet_plan_id, food_id, bua_an, ghi_chu) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // Insert DietPlan
            try (PreparedStatement ps = conn.prepareStatement(insertPlanSql)) {
                ps.setString(1, plan.getId());
                ps.setString(2, plan.getPatientId());
                ps.setString(3, plan.getDoctorId());
                if (plan.getNgayTao() != null) {
                    ps.setTimestamp(4, plan.getNgayTao());
                } else {
                    ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                }
                ps.setString(5, plan.getGhiChu());
                ps.executeUpdate();
            }

            // Insert details
            if (plan.getChiTietThucPham() != null && !plan.getChiTietThucPham().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insertDetailSql)) {
                    for (DietPlanDetail detail : plan.getChiTietThucPham()) {
                        ps.setString(1, detail.getId());
                        ps.setString(2, plan.getId());
                        ps.setString(3, detail.getFoodId());
                        ps.setString(4, detail.getBuaAn());
                        ps.setString(5, detail.getGhiChu());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("[DietPlanDAO] saveDietPlan failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public DietPlan getTodayDietPlan(String patientId) {
        String sql = "SELECT dp.*, dpd.id as detail_id, dpd.food_id, dpd.bua_an, dpd.ghi_chu as detail_ghi_chu, " +
                     "mf.ten_thuc_pham, mf.don_vi_khau_phan, mf.carbs_g, mf.calo_kcal, mf.chi_so_gi " +
                     "FROM diet_plans dp " +
                     "LEFT JOIN diet_plan_details dpd ON dp.id = dpd.diet_plan_id " +
                     "LEFT JOIN master_foods mf ON dpd.food_id = mf.id " +
                     "WHERE dp.patient_id = ? AND DATE(dp.ngay_tao) = CURDATE() " +
                     "ORDER BY dp.ngay_tao DESC";
                     
        DietPlan plan = null;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                if (plan == null) {
                    plan = new DietPlan();
                    plan.setId(rs.getString("id"));
                    plan.setPatientId(rs.getString("patient_id"));
                    plan.setDoctorId(rs.getString("doctor_id"));
                    plan.setNgayTao(rs.getTimestamp("ngay_tao"));
                    plan.setGhiChu(rs.getString("ghi_chu"));
                    plan.setChiTietThucPham(new ArrayList<>());
                }
                
                // Bỏ qua các chi tiết của thực đơn cũ hơn trong cùng 1 ngày
                if (!plan.getId().equals(rs.getString("id"))) {
                    continue;
                }
                
                String detailId = rs.getString("detail_id");
                if (detailId != null) {
                    DietPlanDetail detail = new DietPlanDetail();
                    detail.setId(detailId);
                    detail.setDietPlanId(plan.getId());
                    detail.setFoodId(rs.getString("food_id"));
                    detail.setBuaAn(rs.getString("bua_an"));
                    detail.setGhiChu(rs.getString("detail_ghi_chu"));
                    
                    MasterFood food = new MasterFood();
                    food.setId(rs.getString("food_id"));
                    food.setTenThucPham(rs.getString("ten_thuc_pham"));
                    food.setDonViKhauPhan(rs.getString("don_vi_khau_phan"));
                    food.setCarbsG(rs.getDouble("carbs_g"));
                    food.setCaloKcal(rs.getDouble("calo_kcal"));
                    food.setChiSoGI(rs.getDouble("chi_so_gi"));
                    
                    detail.setThucPhamGoc(food);
                    plan.getChiTietThucPham().add(detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plan;
    }
}
