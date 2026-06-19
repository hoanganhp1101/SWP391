package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardDAO {

    public int getTotalPatients() {
        String sql = "SELECT COUNT(id) FROM patients";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2. Đếm số nhân viên y tế (Bác sĩ, y tá, quản trị viên) đang hoạt động
    public int getActiveStaffCount() {
        String sql = "SELECT COUNT(id) FROM users WHERE vai_tro IN ('bac_si', 'y_ta', 'quan_tri_vien') AND kich_hoat = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 3. Đếm số cảnh báo nguy hiểm chưa được bác sĩ xử lý
    public int getCriticalAlertsCount() {
        String sql = "SELECT COUNT(DISTINCT patient_id) FROM alerts WHERE muc_do IN ('cao', 'nguy_hiem') AND da_doc_bs = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}