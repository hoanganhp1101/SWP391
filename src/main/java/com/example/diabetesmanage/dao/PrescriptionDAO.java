package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrescriptionDAO {

    public boolean createPrescription(Prescription prescription, List<PrescriptionDetail> details) {
        String insertPrescriptionSQL = "INSERT INTO prescriptions (id, patient_id, doctor_id, ghi_chu) VALUES (?, ?, ?, ?)";
        String insertDetailSQL = "INSERT INTO prescription_details (id, prescription_id, medication_id, lieu_luong, tan_suat) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try (PreparedStatement psPrescription = conn.prepareStatement(insertPrescriptionSQL);
                 PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL)) {

                // 1. Lưu thông tin chung của đơn thuốc
                String prescriptionId = UUID.randomUUID().toString();
                psPrescription.setString(1, prescriptionId);
                psPrescription.setString(2, prescription.getPatientId());
                psPrescription.setString(3, prescription.getBacSiId());
                psPrescription.executeUpdate();

                // 2. Lưu từng loại thuốc trong đơn
                for (PrescriptionDetail detail : details) {
                    psDetail.setString(1, UUID.randomUUID().toString());
                    psDetail.setString(2, prescriptionId);
                    psDetail.setString(3, detail.getMedicationId());
                    psDetail.setString(4, detail.getLieuLuong());
                    psDetail.setString(5, detail.getTanSuat());
                    psDetail.addBatch(); // Đưa vào lô để chạy 1 lần cho nhanh
                }

                psDetail.executeBatch(); // Thực thi lô
                conn.commit(); // Hoàn tất Transaction
                return true;

            } catch (Exception e) {
                conn.rollback(); // Nếu có lỗi thì hoàn tác (không lưu gì cả)
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public List<Prescription> getPrescriptionsForPatient(String patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.ho_ten as bac_si_name " +
                     "FROM prescriptions p " +
                     "JOIN users u ON p.bac_si_id = u.id " +
                     "WHERE p.patient_id = ? " +
                     "ORDER BY p.ngay_ke_don DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
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

                // Load medications for this prescription
                String medSql = "SELECT * FROM medications WHERE prescription_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(medSql)) {
                    ps2.setString(1, p.getId());
                    ResultSet rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        Medication m = new Medication();
                        m.setId(rs2.getString("id"));
                        m.setTenThuoc(rs2.getString("ten_thuoc"));
                        m.setLieuLuong(rs2.getString("lieu_luong"));
                        m.setDonVi(rs2.getString("don_vi"));
                        m.setTanSuat(rs2.getString("tan_suat"));
                        p.getMedications().add(m);
                    }
                }

                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
