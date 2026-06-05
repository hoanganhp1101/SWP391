package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public Prescription getNextAppointment(String patientId) {
        String sql = "SELECT p.*, u.ho_ten as bac_si_name " +
                     "FROM prescriptions p " +
                     "JOIN users u ON p.bac_si_id = u.id " +
                     "WHERE p.patient_id = ? AND p.ngay_tai_kham >= CURRENT_DATE " +
                     "ORDER BY p.ngay_tai_kham ASC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getString("id"));
                p.setPatientId(rs.getString("patient_id"));
                p.setBacSiId(rs.getString("bac_si_id"));
                p.setNgayKeDon(rs.getDate("ngay_ke_don"));
                p.setNgayTaiKham(rs.getTimestamp("ngay_tai_kham"));
                p.setBacSiName(rs.getString("bac_si_name"));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Prescription getLatestPrescription(String patientId) {
        String sql = "SELECT p.*, u.ho_ten as bac_si_name " +
                     "FROM prescriptions p " +
                     "JOIN users u ON p.bac_si_id = u.id " +
                     "WHERE p.patient_id = ? " +
                     "ORDER BY p.ngay_ke_don DESC LIMIT 1";
        
        Prescription p = null;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Prescription();
                p.setId(rs.getString("id"));
                p.setPatientId(rs.getString("patient_id"));
                p.setBacSiId(rs.getString("bac_si_id"));
                p.setNgayKeDon(rs.getDate("ngay_ke_don"));
                p.setChanDoan(rs.getString("chan_doan"));
                p.setHuongDieuTri(rs.getString("huong_dieu_tri"));
                p.setCheDoAn(rs.getString("che_do_an"));
                p.setLuyenTap(rs.getString("luyen_tap"));
                p.setNgayTaiKham(rs.getTimestamp("ngay_tai_kham"));
                p.setBacSiName(rs.getString("bac_si_name"));
                p.setMedications(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (p != null) {
            String medSql = "SELECT * FROM medications WHERE prescription_id = ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(medSql)) {
                ps.setString(1, p.getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Medication m = new Medication();
                    m.setId(rs.getString("id"));
                    m.setPrescriptionId(rs.getString("prescription_id"));
                    m.setTenThuoc(rs.getString("ten_thuoc"));
                    m.setHoatChat(rs.getString("hoat_chat"));
                    m.setLieuLuong(rs.getString("lieu_luong"));
                    m.setDonVi(rs.getString("don_vi"));
                    m.setTanSuat(rs.getString("tan_suat"));
                    m.setThoiDiemUong(rs.getString("thoi_diem_uong"));
                    m.setThoiGianDungNgay(rs.getInt("thoi_gian_dung_ngay"));
                    if (rs.wasNull()) m.setThoiGianDungNgay(null);
                    m.setGhiChu(rs.getString("ghi_chu"));
                    p.getMedications().add(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return p;
    }
}
