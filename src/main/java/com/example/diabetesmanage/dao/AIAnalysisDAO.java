package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AIAnalysisDAO {

    public void insertAnalysis(AIAnalysis analysis) {
        String sql = "INSERT INTO ai_analysis (id, patient_id, health_record_id, diem_nguy_co, muc_canh_bao, " +
                     "do_tin_cay, phan_tich_chi_tiet, yeu_to_nguy_co, khuyen_nghi, du_lieu_dau_vao, " +
                     "model_version, tokens_su_dung) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = analysis.getId() != null ? analysis.getId() : UUID.randomUUID().toString();
            ps.setString(1, id);
            ps.setString(2, analysis.getPatientId());
            ps.setString(3, analysis.getHealthRecordId());
            ps.setDouble(4, analysis.getDiemNguyCo());
            ps.setString(5, analysis.getMucCanhBao());
            if (analysis.getDoTinCay() != null) ps.setDouble(6, analysis.getDoTinCay());
            else ps.setNull(6, java.sql.Types.DECIMAL);
            ps.setString(7, analysis.getPhanTichChiTiet());
            ps.setString(8, analysis.getYeuToNguyCo());
            ps.setString(9, analysis.getKhuyenNghi());
            ps.setString(10, analysis.getDuLieuDauVao());
            ps.setString(11, analysis.getModelVersion());
            if (analysis.getTokensSuDung() != null) ps.setInt(12, analysis.getTokensSuDung());
            else ps.setNull(12, java.sql.Types.INTEGER);
            ps.executeUpdate();
            analysis.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AIAnalysis getLatestAnalysis(String patientId) {
        String sql = "SELECT * FROM ai_analysis WHERE patient_id = ? ORDER BY thoi_gian_phan_tich DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AIAnalysis> getAnalysisHistory(String patientId) {
        String sql = "SELECT * FROM ai_analysis WHERE patient_id = ? ORDER BY thoi_gian_phan_tich DESC LIMIT 10";
        List<AIAnalysis> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTodayAnalysis() {
        String sql = "SELECT COUNT(*) FROM ai_analysis WHERE DATE(thoi_gian_phan_tich) = CURRENT_DATE";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private AIAnalysis mapResultSet(ResultSet rs) throws Exception {
        AIAnalysis a = new AIAnalysis();
        a.setId(rs.getString("id"));
        a.setPatientId(rs.getString("patient_id"));
        a.setHealthRecordId(rs.getString("health_record_id"));
        a.setDiemNguyCo(rs.getDouble("diem_nguy_co"));
        a.setMucCanhBao(rs.getString("muc_canh_bao"));
        a.setDoTinCay(rs.getDouble("do_tin_cay"));
        if (rs.wasNull()) a.setDoTinCay(null);
        a.setPhanTichChiTiet(rs.getString("phan_tich_chi_tiet"));
        a.setYeuToNguyCo(rs.getString("yeu_to_nguy_co"));
        a.setKhuyenNghi(rs.getString("khuyen_nghi"));
        a.setDuLieuDauVao(rs.getString("du_lieu_dau_vao"));
        a.setModelVersion(rs.getString("model_version"));
        a.setThoiGianPhanTich(rs.getTimestamp("thoi_gian_phan_tich"));
        a.setTokensSuDung(rs.getInt("tokens_su_dung"));
        if (rs.wasNull()) a.setTokensSuDung(null);
        return a;
    }
}
