package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MedicalDocumentDAO {

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
