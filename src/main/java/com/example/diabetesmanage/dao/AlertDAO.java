package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlertDAO {

    public void insertAlert(Alert alert) {
        String sql = "INSERT INTO alerts (id, patient_id, ai_analysis_id, loai_canh_bao, muc_do, tieu_de, noi_dung) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alert.getId() != null ? alert.getId() : UUID.randomUUID().toString());
            ps.setString(2, alert.getPatientId());
            ps.setString(3, alert.getAiAnalysisId());
            ps.setString(4, alert.getLoaiCanhBao());
            ps.setString(5, alert.getMucDo());
            ps.setString(6, alert.getTieuDe());
            ps.setString(7, alert.getNoiDung());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Alert> getRecentAlerts(String patientId) {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? ORDER BY thoi_gian_tao DESC LIMIT 3";
        List<Alert> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Alert a = new Alert();
                a.setId(rs.getString("id"));
                a.setPatientId(rs.getString("patient_id"));
                a.setLoaiCanhBao(rs.getString("loai_canh_bao"));
                a.setMucDo(rs.getString("muc_do"));
                a.setTieuDe(rs.getString("tieu_de"));
                a.setNoiDung(rs.getString("noi_dung"));
                a.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
