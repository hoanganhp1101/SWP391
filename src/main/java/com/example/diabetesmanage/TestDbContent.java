package com.example.diabetesmanage;

import com.example.diabetesmanage.context.DBContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDbContent {
    public static void main(String[] args) {
        try {
            try (Connection conn = DBContext.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("=== PATIENTS ===");
                ResultSet rs = stmt.executeQuery("SELECT p.id, u.ho_ten FROM patients p JOIN users u ON p.user_id = u.id");
                while (rs.next()) {
                    System.out.println("Patient ID: " + rs.getString("id") + " | Name: " + rs.getString("ho_ten"));
                }
                
                System.out.println("\n=== AI ANALYSIS ===");
                rs = stmt.executeQuery("SELECT id, patient_id, diem_nguy_co, muc_canh_bao, thoi_gian_phan_tich FROM ai_analysis");
                while (rs.next()) {
                    System.out.println("Analysis ID: " + rs.getString("id") + " | Patient ID: " + rs.getString("patient_id") + " | Risk: " + rs.getDouble("diem_nguy_co") + " | Warning: " + rs.getString("muc_canh_bao") + " | Time: " + rs.getTimestamp("thoi_gian_phan_tich"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
