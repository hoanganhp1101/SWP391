package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dto.EncounterCreateDTO.MedicationLineItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicationDAO {

    public void insertAll(Connection con, String prescriptionId, List<MedicationLineItem> medications)
            throws SQLException {
        if (medications == null || medications.isEmpty()) {
            return;
        }

        String sql =
                "INSERT INTO medications " +
                        "(id, prescription_id, ten_thuoc, hoat_chat, lieu_luong, don_vi, " +
                        "tan_suat, thoi_diem_uong, thoi_gian_dung_ngay, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (MedicationLineItem med : medications) {
                if (!med.hasContent()) {
                    continue;
                }
                ps.setString(1, java.util.UUID.randomUUID().toString());
                ps.setString(2, prescriptionId);
                JdbcUtil.setString(ps, 3, med.getTenThuoc());
                JdbcUtil.setString(ps, 4, med.getHoatChat());
                JdbcUtil.setString(ps, 5, med.getLieuLuong());
                JdbcUtil.setString(ps, 6, med.getDonVi());
                JdbcUtil.setString(ps, 7, med.getTanSuat());
                JdbcUtil.setString(ps, 8, med.getThoiDiemUong());
                JdbcUtil.setInteger(ps, 9, med.getThoiGianDungNgay());
                JdbcUtil.setString(ps, 10, med.getGhiChu());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void deleteByPrescriptionId(Connection con, String prescriptionId) throws SQLException {
        if (prescriptionId == null || prescriptionId.isBlank()) {
            return;
        }
        String sql = "DELETE FROM medications WHERE prescription_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prescriptionId);
            ps.executeUpdate();
        }
    }

    public List<Map<String, String>> getDetailsByPrescriptionId(String prescriptionId) {
        List<Map<String, String>> list = new ArrayList<>();
        if (prescriptionId == null || prescriptionId.isBlank()) {
            return list;
        }

        String sql =
                "SELECT ten_thuoc, hoat_chat, lieu_luong, don_vi, tan_suat, " +
                        "thoi_diem_uong, thoi_gian_dung_ngay, ghi_chu " +
                        "FROM medications WHERE prescription_id = ? ORDER BY ten_thuoc";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prescriptionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> med = new LinkedHashMap<>();
                med.put("name", display(rs.getString("ten_thuoc")));
                med.put("ingredient", display(rs.getString("hoat_chat")));
                med.put("dose", display(rs.getString("lieu_luong")));
                med.put("unit", display(rs.getString("don_vi")));
                med.put("route", "—");
                med.put("frequency", display(rs.getString("tan_suat")));
                med.put("usage", display(rs.getString("thoi_diem_uong")));
                Object days = rs.getObject("thoi_gian_dung_ngay");
                med.put("days", days != null
                        ? String.valueOf(rs.getInt("thoi_gian_dung_ngay")) : "—");
                med.put("note", display(rs.getString("ghi_chu")));
                list.add(med);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
