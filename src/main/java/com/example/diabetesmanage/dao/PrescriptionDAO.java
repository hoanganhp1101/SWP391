package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;
import com.example.diabetesmanage.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrescriptionDAO {

    public boolean createPrescription(Prescription prescription, List<PrescriptionDetail> details) {
        if (prescription == null || isBlank(prescription.getPatientId())
                || isBlank(prescription.getBacSiId()) || details == null || details.isEmpty()) {
            return false;
        }

        String insertPrescriptionSql =
                "INSERT INTO prescriptions (id, patient_id, bac_si_id, chan_doan, ghi_chu) VALUES (?, ?, ?, ?, ?)";
        String insertMedicationSql =
                "INSERT INTO medications (id, prescription_id, ten_thuoc, hoat_chat, lieu_luong, don_vi, tan_suat, ghi_chu) " +
                "SELECT ?, ?, ten_thuoc, hoat_chat, ?, don_vi_tinh, ?, ? FROM master_medications WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPrescription = conn.prepareStatement(insertPrescriptionSql);
                 PreparedStatement psMedication = conn.prepareStatement(insertMedicationSql)) {

                String prescriptionId = UUID.randomUUID().toString();
                psPrescription.setString(1, prescriptionId);
                psPrescription.setString(2, prescription.getPatientId());
                psPrescription.setString(3, prescription.getBacSiId());
                psPrescription.setString(4, "Ke don thuoc");
                psPrescription.setString(5, prescription.getGhiChu());
                if (psPrescription.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                for (PrescriptionDetail detail : details) {
                    psMedication.setString(1, UUID.randomUUID().toString());
                    psMedication.setString(2, prescriptionId);
                    psMedication.setString(3, detail.getLieuLuong());
                    psMedication.setString(4, detail.getTanSuat());
                    psMedication.setString(5, prescription.getGhiChu());
                    psMedication.setString(6, detail.getMedicationId());
                    psMedication.addBatch();
                }

                int[] insertedRows = psMedication.executeBatch();
                for (int insertedRow : insertedRows) {
                    if (insertedRow != 1 && insertedRow != Statement.SUCCESS_NO_INFO) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
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
                p.setGhiChu(rs.getString("ghi_chu"));
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
                p = mapPrescription(rs);
                p.setMedications(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (p != null) {
            loadMedications(p);
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
                Prescription p = mapPrescription(rs);
                p.setMedications(new ArrayList<>());
                loadMedications(conn, p);
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Prescription mapPrescription(ResultSet rs) throws Exception {
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
        p.setGhiChu(rs.getString("ghi_chu"));
        p.setBacSiName(rs.getString("bac_si_name"));
        return p;
    }

    private void loadMedications(Prescription prescription) {
        try (Connection conn = DBContext.getConnection()) {
            loadMedications(conn, prescription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMedications(Connection conn, Prescription prescription) throws Exception {
        String medSql = "SELECT * FROM medications WHERE prescription_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(medSql)) {
            ps.setString(1, prescription.getId());
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
                if (rs.wasNull()) {
                    m.setThoiGianDungNgay(null);
                }
                m.setGhiChu(rs.getString("ghi_chu"));
                prescription.getMedications().add(m);
            }
        }
    }
}
