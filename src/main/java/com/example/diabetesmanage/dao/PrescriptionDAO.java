package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.model.Medication;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrescriptionDAO {

    public boolean createPrescription(Prescription prescription, List<PrescriptionDetail> details) {
        // Khớp schema newdb.sql: bac_si_id + chan_doan (NOT NULL)
        String insertPrescriptionSQL =
                "INSERT INTO prescriptions (id, patient_id, bac_si_id, chan_doan, ghi_chu) VALUES (?, ?, ?, ?, ?)";
        String insertDetailSQL =
                "INSERT INTO prescription_details (id, prescription_id, medication_id, lieu_luong, tan_suat) VALUES (?, ?, ?, ?, ?)";
        // Ghi song song vào bảng medications (nguồn checklist tuân thủ của bệnh nhân)
        // để đơn kê từ admin đi cùng luồng với đơn của bác sĩ.
        String insertMedicationSQL =
                "INSERT INTO medications (id, prescription_id, ten_thuoc, hoat_chat, lieu_luong, don_vi, tan_suat) " +
                "SELECT ?, ?, mm.ten_thuoc, mm.hoat_chat, ?, mm.don_vi_tinh, ? " +
                "FROM master_medications mm WHERE mm.id = ?";

        try (Connection conn = DBContext.getConnection()) {
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            try (PreparedStatement psPrescription = conn.prepareStatement(insertPrescriptionSQL);
                 PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL);
                 PreparedStatement psMedication = conn.prepareStatement(insertMedicationSQL)) {

                String prescriptionId = UUID.randomUUID().toString();
                String chanDoan = prescription.getChanDoan();
                if (chanDoan == null || chanDoan.isBlank()) {
                    chanDoan = "Kê đơn từ quản trị";
                }

                psPrescription.setString(1, prescriptionId);
                psPrescription.setString(2, prescription.getPatientId());
                psPrescription.setString(3, prescription.getBacSiId());
                psPrescription.setString(4, chanDoan.trim());
                psPrescription.setString(5, prescription.getGhiChu());
                psPrescription.executeUpdate();

                for (PrescriptionDetail detail : details) {
                    psDetail.setString(1, UUID.randomUUID().toString());
                    psDetail.setString(2, prescriptionId);
                    psDetail.setString(3, detail.getMedicationId());
                    psDetail.setString(4, detail.getLieuLuong());
                    psDetail.setString(5, detail.getTanSuat());
                    psDetail.addBatch();

                    psMedication.setString(1, UUID.randomUUID().toString());
                    psMedication.setString(2, prescriptionId);
                    psMedication.setString(3, detail.getLieuLuong());
                    psMedication.setString(4, detail.getTanSuat());
                    psMedication.setString(5, detail.getMedicationId());
                    psMedication.addBatch();
                }

                psDetail.executeBatch();
                psMedication.executeBatch();
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


    public String getIdByEncounterId(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        String sql = "SELECT id FROM prescriptions WHERE encounter_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("id") : null;
        }
    }

    public String getIdByEncounterId(String encounterId) {
        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection()) {
            return getIdByEncounterId(con, encounterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo đơn thuốc mới gắn với lần khám.
     */
    public String insert(Connection con, EncounterCreateDTO form, String patientId,
                         String doctorId, String encounterId) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();

        String sql =
                "INSERT INTO prescriptions " +
                        "(id, patient_id, bac_si_id, encounter_id, chan_doan, huong_dieu_tri, " +
                        "che_do_an, luyen_tap, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, doctorId);
            ps.setString(4, encounterId);
            JdbcUtil.setString(ps, 5, form.getChanDoanChinh());
            JdbcUtil.setString(ps, 6, form.getKhuyenNghiDieuTri());
            JdbcUtil.setString(ps, 7, form.getCheDoAn());
            JdbcUtil.setString(ps, 8, form.getLuyenTap());
            JdbcUtil.setString(ps, 9, null);
            ps.executeUpdate();
        }
        return id;
    }

    public void deleteByEncounterId(Connection con, String encounterId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    public Map<String, String> getAdviceByEncounterId(Connection con, String encounterId) throws SQLException {
        Map<String, String> advice = new LinkedHashMap<>();
        if (encounterId == null || encounterId.isBlank()) {
            return advice;
        }

        String sql =
                "SELECT huong_dieu_tri, che_do_an, luyen_tap, ghi_chu " +
                        "FROM prescriptions " +
                        "WHERE encounter_id = ? " +
                        "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                putIfPresent(advice, "huong_dieu_tri", rs.getString("huong_dieu_tri"));
                putIfPresent(advice, "che_do_an", rs.getString("che_do_an"));
                putIfPresent(advice, "luyen_tap", rs.getString("luyen_tap"));
                putIfPresent(advice, "ghi_chu", rs.getString("ghi_chu"));
            }
        }
        return advice;
    }

    public Map<String, String> getAdviceByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return new LinkedHashMap<>();
        }
        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection()) {
            return getAdviceByEncounterId(con, encounterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    /**
     * Prefer the prescription linked to this encounter; for each field still missing,
     * fall back to the patient's most recent prescription that has that field.
     * Never mixes prescriptions from other patients.
     */
    public Map<String, String> getAdviceForEncounterOrLatestPatient(
            String encounterId, String patientId) {
        Map<String, String> advice = new LinkedHashMap<>();
        if ((encounterId == null || encounterId.isBlank())
                && (patientId == null || patientId.isBlank())) {
            return advice;
        }

        String sql =
                "SELECT huong_dieu_tri, che_do_an, luyen_tap " +
                        "FROM prescriptions " +
                        "WHERE encounter_id = ? OR patient_id = ? " +
                        "ORDER BY CASE WHEN encounter_id = ? THEN 0 ELSE 1 END, " +
                        "COALESCE(ngay_tao, TIMESTAMP(ngay_ke_don)) DESC, id DESC";

        try (Connection con = com.example.diabetesmanage.context.DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.setString(2, patientId);
            ps.setString(3, encounterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()
                    && (!advice.containsKey("huong_dieu_tri")
                        || !advice.containsKey("che_do_an")
                        || !advice.containsKey("luyen_tap"))) {
                putIfAbsent(advice, "huong_dieu_tri", rs.getString("huong_dieu_tri"));
                putIfAbsent(advice, "che_do_an", rs.getString("che_do_an"));
                putIfAbsent(advice, "luyen_tap", rs.getString("luyen_tap"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return advice;
    }

    private void putIfAbsent(Map<String, String> target, String key, String value) {
        if (!target.containsKey(key)) {
            putIfPresent(target, key, value);
        }
    }

    private void putIfPresent(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }
}
