package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.PatientAssignment;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PatientAssignmentDAO {

    // Phân công bác sĩ mới cho bệnh nhân
    public boolean assignDoctor(String patientId, String doctorId) {
        if (isBlank(patientId) || isBlank(doctorId)) {
            return false;
        }

        String disableOldSql = "UPDATE patient_assignments SET trang_thai = false WHERE patient_id = ?";
        String insertNewSql = "INSERT INTO patient_assignments (id, patient_id, doctor_id, trang_thai) VALUES (?, ?, ?, true)";
        String updatePatientSql = "UPDATE patients SET bac_si_id = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try (PreparedStatement psDisable = conn.prepareStatement(disableOldSql);
                 PreparedStatement psInsert = conn.prepareStatement(insertNewSql);
                 PreparedStatement psUpdatePatient = conn.prepareStatement(updatePatientSql)) {

                // 1. Vô hiệu hóa phân công cũ
                psDisable.setString(1, patientId);
                psDisable.executeUpdate();

                // 2. Thêm phân công mới
                psInsert.setString(1, UUID.randomUUID().toString());
                psInsert.setString(2, patientId);
                psInsert.setString(3, doctorId);
                if (psInsert.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                // Keep the denormalized doctor reference used by patient lists and risk reports in sync.
                psUpdatePatient.setString(1, doctorId);
                psUpdatePatient.setString(2, patientId);
                if (psUpdatePatient.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit(); // Hoàn tất Transaction
                return true;
            } catch (Exception e) {
                conn.rollback(); // Rollback nếu có lỗi
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Lấy Map chứa <PatientID, Tên Bác sĩ đang điều trị> để hiển thị ra bảng
    public Map<String, String> getActiveAssignments() {
        Map<String, String> map = new HashMap<>();
        // Kết hợp (JOIN) bảng patient_assignments với bảng users (chứa thông tin bác sĩ)
        String sql = "SELECT pa.patient_id, u.ho_ten FROM patient_assignments pa " +
                "JOIN users u ON pa.doctor_id = u.id " +
                "WHERE pa.trang_thai = true";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("patient_id"), rs.getString("ho_ten"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
