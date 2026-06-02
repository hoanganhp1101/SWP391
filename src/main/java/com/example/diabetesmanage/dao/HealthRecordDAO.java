package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealthRecordDAO {

    public void insertHealthRecord(HealthRecord record) {
        String sql = "INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, carbs_g, ghi_chu, lieu_luong_insulin_ui, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong, thoi_diem_do_duong) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, record.getPatientId());
            if (record.getDuongHuyetMgdl() != null)
                ps.setDouble(3, record.getDuongHuyetMgdl());
            else
                ps.setNull(3, java.sql.Types.DECIMAL);
            if (record.getCarbsG() != null)
                ps.setDouble(4, record.getCarbsG());
            else
                ps.setNull(4, java.sql.Types.DECIMAL);
            ps.setString(5, record.getGhiChu());
            if (record.getLieuLuongInsulinUi() != null)
                ps.setInt(6, record.getLieuLuongInsulinUi());
            else
                ps.setNull(6, java.sql.Types.INTEGER);
            
            if (record.getNhipTim() != null)
                ps.setInt(7, record.getNhipTim());
            else
                ps.setNull(7, java.sql.Types.INTEGER);
                
            if (record.getHuyetApTamThu() != null)
                ps.setInt(8, record.getHuyetApTamThu());
            else
                ps.setNull(8, java.sql.Types.INTEGER);
                
            if (record.getHuyetApTamTruong() != null)
                ps.setInt(9, record.getHuyetApTamTruong());
            else
                ps.setNull(9, java.sql.Types.INTEGER);
                
            ps.setString(10, record.getThoiDiemDoDuong());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HealthRecord getLatestHealthRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND duong_huyet_mgdl IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setId(rs.getString("id"));
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                if (rs.wasNull()) hr.setDuongHuyetMgdl(null);
                
                hr.setHba1cPercent(rs.getDouble("hba1c_percent"));
                if (rs.wasNull()) hr.setHba1cPercent(null);
                
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HealthRecord getLatestHeartRateRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND nhip_tim IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setNhipTim(rs.getInt("nhip_tim"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HealthRecord getLatestBloodPressureRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND huyet_ap_tam_thu IS NOT NULL AND huyet_ap_tam_truong IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setHuyetApTamThu(rs.getInt("huyet_ap_tam_thu"));
                hr.setHuyetApTamTruong(rs.getInt("huyet_ap_tam_truong"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HealthRecord> getAllRecordsForChart(String patientId) {
        String sql = "SELECT thoi_gian_do, duong_huyet_mgdl, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong " +
                     "FROM health_records WHERE patient_id = ? ORDER BY thoi_gian_do ASC";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
                
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                if (rs.wasNull()) hr.setDuongHuyetMgdl(null);
                
                hr.setNhipTim(rs.getInt("nhip_tim"));
                if (rs.wasNull()) hr.setNhipTim(null);
                
                hr.setHuyetApTamThu(rs.getInt("huyet_ap_tam_thu"));
                if (rs.wasNull()) hr.setHuyetApTamThu(null);
                
                hr.setHuyetApTamTruong(rs.getInt("huyet_ap_tam_truong"));
                if (rs.wasNull()) hr.setHuyetApTamTruong(null);
                
                list.add(hr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getDailyCarbsToday(String patientId) {
        String sql = "SELECT SUM(carbs_g) as total_carbs FROM health_records WHERE patient_id = ? AND DATE(thoi_gian_do) = CURRENT_DATE";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_carbs");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get average daily glucose and insulin for the last 7 days
    public List<HealthRecord> getRecentDailyRecords(String patientId) {
        String sql = "SELECT DATE(thoi_gian_do) as record_date, AVG(duong_huyet_mgdl) as avg_glucose, SUM(lieu_luong_insulin_ui) as total_insulin "
                +
                "FROM health_records " +
                "WHERE patient_id = ? " +
                "GROUP BY DATE(thoi_gian_do) " +
                "ORDER BY record_date DESC LIMIT 7";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setDuongHuyetMgdl(rs.getDouble("avg_glucose"));
                hr.setLieuLuongInsulinUi(rs.getInt("total_insulin"));
                hr.setThoiGianDo(rs.getTimestamp("record_date"));
                list.add(hr);
            }
            java.util.Collections.reverse(list); // Reverse to make it chronological for the chart
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
