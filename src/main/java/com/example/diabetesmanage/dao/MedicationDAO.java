package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest.MedicationLineItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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

    public void deleteByEncounterId(Connection con, String encounterId) throws SQLException {
        String sql =
                "DELETE m FROM medications m " +
                        "JOIN prescriptions rx ON m.prescription_id = rx.id " +
                        "WHERE rx.encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }
}
