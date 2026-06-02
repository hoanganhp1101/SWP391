package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    public List<Alert> getRecentAlerts(String patientId) {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? ORDER BY thoi_gian_tao DESC LIMIT 5";
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
