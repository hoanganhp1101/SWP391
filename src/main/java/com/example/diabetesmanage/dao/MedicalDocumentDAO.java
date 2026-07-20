package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MedicalDocumentDAO {
    public List<MedicalDocument> getRecentDocuments(String patientId) {
        String sql = "SELECT * FROM medical_documents WHERE patient_id = ? ORDER BY ngay_thuc_hien DESC LIMIT 5";
        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MedicalDocument d = new MedicalDocument();
                d.setId(rs.getString("id"));
                d.setPatientId(rs.getString("patient_id"));
                d.setBacSiId(rs.getString("bac_si_id"));
                d.setLoaiTaiLieu(rs.getString("loai_tai_lieu"));
                d.setTrangThai(rs.getString("trang_thai"));
                d.setFileUrl(rs.getString("file_url"));
                d.setNgayThucHien(rs.getDate("ngay_thuc_hien"));
                d.setNgayTao(rs.getTimestamp("ngay_tao"));
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addDocument(MedicalDocument doc) {
        String sql = "INSERT INTO medical_documents (id, patient_id, bac_si_id, loai_tai_lieu, trang_thai, file_url, ngay_thuc_hien) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.util.UUID.randomUUID().toString());
            ps.setString(2, doc.getPatientId());
            ps.setString(3, doc.getBacSiId());
            ps.setString(4, doc.getLoaiTaiLieu());
            ps.setString(5, doc.getTrangThai());
            ps.setString(6, doc.getFileUrl());
            if (doc.getNgayThucHien() != null) {
                ps.setDate(7, doc.getNgayThucHien());
            } else {
                ps.setDate(7, new java.sql.Date(System.currentTimeMillis()));
            }

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
