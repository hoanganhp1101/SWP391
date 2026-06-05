package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.MedicationLog;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MedicationLogDAO {

    public List<MedicationLog> getTodayChecklist(String patientId) {
        List<MedicationLog> checklist = new ArrayList<>();
        
        // Step 1: Get the latest prescription
        PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
        Prescription latestPrescription = prescriptionDAO.getLatestPrescription(patientId);
        
        if (latestPrescription == null || latestPrescription.getMedications() == null) {
            return checklist;
        }

        // Step 2: Check logs for each medication for today
        String sql = "SELECT * FROM medication_logs WHERE patient_id = ? AND medication_id = ? AND ngay_uong = CURDATE()";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            for (Medication med : latestPrescription.getMedications()) {
                ps.setString(1, patientId);
                ps.setString(2, med.getId());
                ResultSet rs = ps.executeQuery();
                
                MedicationLog log = new MedicationLog();
                // Attach Medication Info
                log.setPatientId(patientId);
                log.setMedicationId(med.getId());
                log.setTenThuoc(med.getTenThuoc());
                log.setLieuLuong(med.getLieuLuong());
                log.setDonVi(med.getDonVi());
                log.setTanSuat(med.getTanSuat());
                log.setThoiDiemUong(med.getThoiDiemUong());
                
                if (rs.next()) {
                    log.setId(rs.getString("id"));
                    log.setNgayUong(rs.getDate("ngay_uong"));
                    log.setThoiDiemDuKien(rs.getTime("thoi_diem_du_kien"));
                    log.setThoiGianThucTe(rs.getTimestamp("thoi_gian_thuc_te"));
                    log.setTrangThai(rs.getString("trang_thai"));
                    log.setGhiChu(rs.getString("ghi_chu"));
                } else {
                    // Not logged yet today -> default 'chua_uong'
                    log.setTrangThai("chua_uong");
                    log.setNgayUong(new Date(System.currentTimeMillis()));
                }
                checklist.add(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return checklist;
    }

    public boolean toggleMedicationStatus(String patientId, String medicationId) {
        // First check if an entry exists for today
        String checkSql = "SELECT id, trang_thai FROM medication_logs WHERE patient_id = ? AND medication_id = ? AND ngay_uong = CURDATE()";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            
            checkPs.setString(1, patientId);
            checkPs.setString(2, medicationId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                // Entry exists, toggle status
                String logId = rs.getString("id");
                String currentStatus = rs.getString("trang_thai");
                String newStatus = "da_uong".equals(currentStatus) ? "chua_uong" : "da_uong";
                
                String updateSql = "UPDATE medication_logs SET trang_thai = ?, thoi_gian_thuc_te = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setString(1, newStatus);
                    updatePs.setString(2, logId);
                    return updatePs.executeUpdate() > 0;
                }
            } else {
                // Entry does not exist, insert new as 'da_uong'
                String insertSql = "INSERT INTO medication_logs (patient_id, medication_id, ngay_uong, thoi_gian_thuc_te, trang_thai) " +
                                   "VALUES (?, ?, CURDATE(), CURRENT_TIMESTAMP, 'da_uong')";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, patientId);
                    insertPs.setString(2, medicationId);
                    return insertPs.executeUpdate() > 0;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
