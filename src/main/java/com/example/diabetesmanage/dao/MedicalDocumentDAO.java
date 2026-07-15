package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MedicalDocumentDAO {
    public List<MedicalDocument> getRecentDocuments(String patientId) {
        String sql = "SELECT d.*, u.ho_ten AS bac_si_name " +
                "FROM medical_documents d LEFT JOIN users u ON d.bac_si_id = u.id " +
                "WHERE d.patient_id = ? ORDER BY d.ngay_thuc_hien DESC, d.ngay_tao DESC LIMIT 5";
        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDocument(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MedicalDocument> getAllDocumentsByPatient(String patientId) {
        String sql = "SELECT d.*, u.ho_ten AS bac_si_name " +
                "FROM medical_documents d LEFT JOIN users u ON d.bac_si_id = u.id " +
                "WHERE d.patient_id = ? ORDER BY d.ngay_tao DESC, d.ngay_thuc_hien DESC";
        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDocument(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MedicalDocument> getDocumentsByPatient(String patientId, int offset, int limit) {
        String sql = "SELECT d.*, u.ho_ten AS bac_si_name " +
                "FROM medical_documents d LEFT JOIN users u ON d.bac_si_id = u.id " +
                "WHERE d.patient_id = ? ORDER BY d.ngay_tao DESC, d.ngay_thuc_hien DESC LIMIT ? OFFSET ?";// lấy từ databse
        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDocument(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countDocumentsByPatient(String patientId) {
        String sql = "SELECT COUNT(*) FROM medical_documents WHERE patient_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private MedicalDocument mapDocument(ResultSet rs) throws Exception {
        MedicalDocument d = new MedicalDocument();
        d.setId(rs.getString("id"));
        d.setPatientId(rs.getString("patient_id"));
        d.setBacSiId(rs.getString("bac_si_id"));
        d.setLoaiTaiLieu(rs.getString("loai_tai_lieu"));
        d.setTrangThai(rs.getString("trang_thai"));
        d.setFileUrl(rs.getString("file_url"));
        d.setNgayThucHien(rs.getDate("ngay_thuc_hien"));
        d.setNgayTao(rs.getTimestamp("ngay_tao"));
        d.setBacSiName(rs.getString("bac_si_name"));
        return d;
    }
}
