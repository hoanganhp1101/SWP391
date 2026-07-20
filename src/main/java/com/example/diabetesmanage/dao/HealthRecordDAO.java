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
        String sql = "INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, carbs_g, ghi_chu, lieu_luong_insulin_ui, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong, thoi_diem_do_duong, chest_pain, dizziness, fatigue, thoi_gian_do) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
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
            
            if (record.getChestPain() != null)
                ps.setInt(11, record.getChestPain());
            else
                ps.setInt(11, 0);

            if (record.getDizziness() != null)
                ps.setInt(12, record.getDizziness());
            else
                ps.setInt(12, 0);

            if (record.getFatigue() != null)
                ps.setInt(13, record.getFatigue());
            else
                ps.setInt(13, 0);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertExtractedHealthRecord(String patientId, Double weight, Double bmi, Integer systole, Integer diastole, Integer heartRate, Double glucose, Double hba1c, Double cholesterol, Double triglyceride) {
        String sql = "INSERT INTO health_records (id, patient_id, can_nang_kg, bmi, huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim, duong_huyet_mgdl, hba1c_percent, cholesterol_mmol, triglyceride_mmol, thoi_gian_do) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, patientId);
            
            if (weight != null) ps.setDouble(3, weight); else ps.setNull(3, java.sql.Types.DECIMAL);
            if (bmi != null) ps.setDouble(4, bmi); else ps.setNull(4, java.sql.Types.DECIMAL);
            if (systole != null) ps.setInt(5, systole); else ps.setNull(5, java.sql.Types.INTEGER);
            if (diastole != null) ps.setInt(6, diastole); else ps.setNull(6, java.sql.Types.INTEGER);
            if (heartRate != null) ps.setInt(7, heartRate); else ps.setNull(7, java.sql.Types.INTEGER);
            if (glucose != null) ps.setDouble(8, glucose); else ps.setNull(8, java.sql.Types.DECIMAL);
            if (hba1c != null) ps.setDouble(9, hba1c); else ps.setNull(9, java.sql.Types.DECIMAL);
            if (cholesterol != null) ps.setDouble(10, cholesterol); else ps.setNull(10, java.sql.Types.DECIMAL);
            if (triglyceride != null) ps.setDouble(11, triglyceride); else ps.setNull(11, java.sql.Types.DECIMAL);
            
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

    public HealthRecord getLatestComprehensiveRecord(String patientId) {
        String sql = "SELECT " +
                     "(SELECT duong_huyet_mgdl FROM health_records WHERE patient_id = p.id AND duong_huyet_mgdl IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as duong_huyet_mgdl, " +
                     "(SELECT huyet_ap_tam_thu FROM health_records WHERE patient_id = p.id AND huyet_ap_tam_thu IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as huyet_ap_tam_thu, " +
                     "(SELECT huyet_ap_tam_truong FROM health_records WHERE patient_id = p.id AND huyet_ap_tam_truong IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as huyet_ap_tam_truong, " +
                     "(SELECT nhip_tim FROM health_records WHERE patient_id = p.id AND nhip_tim IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as nhip_tim, " +
                     "(SELECT can_nang_kg FROM health_records WHERE patient_id = p.id AND can_nang_kg IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as can_nang_kg, " +
                     "(SELECT bmi FROM health_records WHERE patient_id = p.id AND bmi IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as bmi, " +
                     "(SELECT thoi_gian_do FROM health_records WHERE patient_id = p.id ORDER BY thoi_gian_do DESC LIMIT 1) as thoi_gian_do, " +
                     "(SELECT hba1c_percent FROM health_records WHERE patient_id = p.id AND hba1c_percent IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as hba1c_percent, " +
                     "(SELECT cholesterol_mmol FROM health_records WHERE patient_id = p.id AND cholesterol_mmol IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as cholesterol_mmol, " +
                     "(SELECT triglyceride_mmol FROM health_records WHERE patient_id = p.id AND triglyceride_mmol IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as triglyceride_mmol " +
                     "FROM (SELECT ? as id) p";
        HealthRecord hr = null;
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hr = new HealthRecord();
                // hr.setId(rs.getString("id")); -- Not selecting ID since it's an aggregate of records
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                if (rs.wasNull()) hr.setDuongHuyetMgdl(null);
                hr.setHba1cPercent(rs.getDouble("hba1c_percent"));
                if (rs.wasNull()) hr.setHba1cPercent(null);
                hr.setCanNangKg(rs.getDouble("can_nang_kg"));
                if (rs.wasNull()) hr.setCanNangKg(null);
                hr.setBmi(rs.getDouble("bmi"));
                if (rs.wasNull()) hr.setBmi(null);
                hr.setCholesterolMmol(rs.getDouble("cholesterol_mmol"));
                if (rs.wasNull()) hr.setCholesterolMmol(null);
                hr.setTriglycerideMmol(rs.getDouble("triglyceride_mmol"));
                if (rs.wasNull()) hr.setTriglycerideMmol(null);
                hr.setNhipTim(rs.getInt("nhip_tim"));
                if (rs.wasNull()) hr.setNhipTim(null);
                hr.setHuyetApTamThu(rs.getInt("huyet_ap_tam_thu"));
                if (rs.wasNull()) hr.setHuyetApTamThu(null);
                hr.setHuyetApTamTruong(rs.getInt("huyet_ap_tam_truong"));
                if (rs.wasNull()) hr.setHuyetApTamTruong(null);
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Enrich with data from lab_results if hr is found
        if (hr != null) {
            String labSql = "SELECT hba1c, cholesterol_tp, triglyceride FROM lab_results WHERE patient_id = ? ORDER BY ngay_xet_nghiem DESC LIMIT 1";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(labSql)) {
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (hr.getHba1cPercent() == null && rs.getObject("hba1c") != null) hr.setHba1cPercent(rs.getDouble("hba1c"));
                    if (hr.getCholesterolMmol() == null && rs.getObject("cholesterol_tp") != null) hr.setCholesterolMmol(rs.getDouble("cholesterol_tp"));
                    if (hr.getTriglycerideMmol() == null && rs.getObject("triglyceride") != null) hr.setTriglycerideMmol(rs.getDouble("triglyceride"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hr;
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
