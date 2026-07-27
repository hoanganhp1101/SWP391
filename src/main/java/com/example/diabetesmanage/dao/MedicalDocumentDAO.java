package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.MedicalDocument;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalDocumentDAO {
    public List<MedicalDocument> getRecentDocuments(String patientId) {
        String sql = "SELECT d.*, u.ho_ten AS bac_si_name " +
                "FROM medical_documents d LEFT JOIN doctors u ON d.bac_si_id = u.id " +
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

    public List<MedicalDocument> getAllDocumentsByPatient(String patientId) {
        String sql = "SELECT d.*, u.ho_ten AS bac_si_name " +
                "FROM medical_documents d LEFT JOIN doctors u ON d.bac_si_id = u.id " +
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
                "FROM medical_documents d LEFT JOIN doctors u ON d.bac_si_id = u.id " +
                "WHERE d.patient_id = ? ORDER BY d.ngay_tao DESC, d.ngay_thuc_hien DESC LIMIT ? OFFSET ?";
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

    public Map<String, String> findUltrasoundByEncounterId(String encounterId) {
        Map<String, String> data = new LinkedHashMap<>();
        if (encounterId == null || encounterId.isBlank()) {
            return data;
        }

        String sql =
                "SELECT * FROM medical_documents " +
                        "WHERE patient_id = (SELECT patient_id FROM medical_encounters WHERE id = ? LIMIT 1) " +
                        "AND (loai_tai_lieu LIKE '%sieu%' OR loai_tai_lieu LIKE '%ultrasound%' " +
                        "OR loai_tai_lieu LIKE '%Sieu%') " +
                        "ORDER BY ngay_thuc_hien DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mapUltrasoundRow(rs, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
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

    private void mapUltrasoundRow(ResultSet rs, Map<String, String> data) throws SQLException {
        putIfPresent(data, "Gan", firstNonBlank(
                optionalString(rs, "gan"),
                extractJsonValue(rs.getString("noi_dung"), "gan")));
        putIfPresent(data, "Mật", firstNonBlank(
                optionalString(rs, "mat"),
                extractJsonValue(rs.getString("noi_dung"), "mat")));
        putIfPresent(data, "Tụy", firstNonBlank(
                optionalString(rs, "tuy"),
                extractJsonValue(rs.getString("noi_dung"), "tuy")));
        putIfPresent(data, "Lách", firstNonBlank(
                optionalString(rs, "lach"),
                extractJsonValue(rs.getString("noi_dung"), "lach")));
        putIfPresent(data, "Thận", firstNonBlank(
                optionalString(rs, "than"),
                extractJsonValue(rs.getString("noi_dung"), "than")));
        putIfPresent(data, "Kết luận", firstNonBlank(
                optionalString(rs, "ket_luan"),
                extractJsonValue(rs.getString("noi_dung"), "ket_luan"),
                rs.getString("noi_dung")));
    }

    private String optionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || json.isBlank() || key == null) {
            return null;
        }
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return json.substring(start);
        }
        return json.substring(start, end);
    }

    private void putIfPresent(Map<String, String> data, String label, String value) {
        if (value != null && !value.isBlank()) {
            data.put(label, value);
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
