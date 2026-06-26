package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.form.AddMedicalEncounterForm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalEncounterDAO {

    public List<MedicalEncounter> findByPatientId(String patientId) {
        List<MedicalEncounter> list = new ArrayList<>();
        String sql =
                "SELECT * FROM medical_encounters " +
                        "WHERE patient_id = ? " +
                        "ORDER BY ngay_kham DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Insert encounter trong transaction. bac_si_id lấy từ session (doctorId).
     */
    public String insert(Connection con, AddMedicalEncounterForm form, String doctorId) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();
        String khamLamSangJson = buildKhamLamSangJson(form.getKhamLamSang());

        String sql =
                "INSERT INTO medical_encounters " +
                        "(id, patient_id, bac_si_id, ngay_kham, ly_do_kham, qua_trinh_benh_ly, kham_lam_sang, " +
                        "chan_doan_chinh, chan_doan_phu, huong_xu_tri) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, form.getPatientId());
            ps.setString(3, doctorId);
            ps.setTimestamp(4, Timestamp.valueOf(form.resolveNgayKham()));
            JdbcUtil.setString(ps, 5, form.getLyDoKham());
            JdbcUtil.setString(ps, 6, form.getQuaTrinhBenhLy());
            JdbcUtil.setString(ps, 7, khamLamSangJson);
            JdbcUtil.setString(ps, 8, form.getChanDoanChinh());
            JdbcUtil.setString(ps, 9, form.getChanDoanPhu());
            JdbcUtil.setString(ps, 10, form.getHuongXuTri());
            ps.executeUpdate();
        }
        return id;
    }

    private String buildKhamLamSangJson(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"noi_dung\":\"" + escaped + "\"}";
    }

    public MedicalEncounter getClosestByPatientAndTime(String patientId, LocalDateTime recordTime) {
        if (patientId == null || recordTime == null) {
            return null;
        }

        String sql =
                "SELECT * FROM medical_encounters " +
                        "WHERE patient_id = ? " +
                        "ORDER BY ABS(TIMESTAMPDIFF(SECOND, ngay_kham, ?)) ASC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ps.setTimestamp(2, Timestamp.valueOf(recordTime));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDoctorNameById(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            return null;
        }
        String sql = "SELECT ho_ten FROM users WHERE id = ?";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ho_ten");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, String>> getMedicationsByEncounterId(String encounterId) {
        List<Map<String, String>> list = new ArrayList<>();
        if (encounterId == null || encounterId.isBlank()) {
            return list;
        }

        String sql =
                "SELECT m.ten_thuoc, m.lieu_luong, m.don_vi, m.tan_suat, m.ghi_chu " +
                        "FROM medications m " +
                        "JOIN prescriptions rx ON m.prescription_id = rx.id " +
                        "WHERE rx.encounter_id = ? " +
                        "ORDER BY m.ten_thuoc";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dose = rs.getString("lieu_luong");
                String unit = rs.getString("don_vi");
                String freq = rs.getString("tan_suat");
                String doseDisplay = dose + (unit != null ? " " + unit : "") + " · " + freq;
                list.add(toMedicationMap(
                        rs.getString("ten_thuoc"),
                        doseDisplay,
                        rs.getString("ghi_chu")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getRecommendationsByEncounterId(String encounterId) {
        List<String> list = new ArrayList<>();
        if (encounterId == null || encounterId.isBlank()) {
            return list;
        }

        String sql =
                "SELECT che_do_an, luyen_tap, huong_dieu_tri, ghi_chu " +
                        "FROM prescriptions " +
                        "WHERE encounter_id = ? " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                addIfPresent(list, rs.getString("huong_dieu_tri"));
                addIfPresent(list, rs.getString("che_do_an"));
                addIfPresent(list, rs.getString("luyen_tap"));
                addIfPresent(list, rs.getString("ghi_chu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteById(Connection con, String encounterId) throws SQLException {
        String sql = "DELETE FROM medical_encounters WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    public MedicalEncounter getLatestByPatientId(String patientId) {
        String sql =
                "SELECT * FROM medical_encounters " +
                        "WHERE patient_id = ? " +
                        "ORDER BY ngay_kham DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, String>> getMedicationsByPatientId(String patientId) {
        List<Map<String, String>> list = new ArrayList<>();

        String sql =
                "SELECT m.ten_thuoc, m.lieu_luong, m.don_vi, m.tan_suat, m.ghi_chu " +
                        "FROM medications m " +
                        "JOIN prescriptions rx ON m.prescription_id = rx.id " +
                        "WHERE rx.patient_id = ? " +
                        "ORDER BY rx.ngay_ke_don DESC, m.ten_thuoc";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dose = rs.getString("lieu_luong");
                String unit = rs.getString("don_vi");
                String freq = rs.getString("tan_suat");
                String doseDisplay = dose + (unit != null ? " " + unit : "") + " · " + freq;
                list.add(toMedicationMap(
                        rs.getString("ten_thuoc"),
                        doseDisplay,
                        rs.getString("ghi_chu")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<String> getRecommendationsByPatientId(String patientId) {
        List<String> list = new ArrayList<>();

        String sql =
                "SELECT che_do_an, luyen_tap, huong_dieu_tri, ghi_chu " +
                        "FROM prescriptions " +
                        "WHERE patient_id = ? " +
                        "ORDER BY ngay_ke_don DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                addIfPresent(list, rs.getString("huong_dieu_tri"));
                addIfPresent(list, rs.getString("che_do_an"));
                addIfPresent(list, rs.getString("luyen_tap"));
                addIfPresent(list, rs.getString("ghi_chu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void addIfPresent(List<String> list, String value) {
        if (value != null && !value.isBlank()) {
            list.add(value);
        }
    }

    private Map<String, String> toMedicationMap(String name, String dose, String note) {
        Map<String, String> med = new LinkedHashMap<>();
        med.put("name", name);
        med.put("dose", dose);
        if (note != null && !note.isBlank()) {
            med.put("note", note);
        }
        return med;
    }

    private MedicalEncounter map(ResultSet rs) throws SQLException {
        MedicalEncounter enc = new MedicalEncounter();
        enc.setId(rs.getString("id"));
        enc.setDisplayCode(PatientDAO.resolveCode(rs, "encounter_code"));
        enc.setPatientId(rs.getString("patient_id"));
        enc.setBacSiId(rs.getString("bac_si_id"));

        Timestamp ts = rs.getTimestamp("ngay_kham");
        if (ts != null) {
            enc.setNgayKham(ts.toLocalDateTime());
        }

        enc.setLyDoKham(rs.getString("ly_do_kham"));
        enc.setQuaTrinhBenhLy(rs.getString("qua_trinh_benh_ly"));
        enc.setChanDoanChinh(rs.getString("chan_doan_chinh"));
        enc.setChanDoanPhu(rs.getString("chan_doan_phu"));
        enc.setHuongXuTri(rs.getString("huong_xu_tri"));
        enc.setKhamLamSang(parseKhamLamSang(rs.getString("kham_lam_sang")));
        return enc;
    }

    private String parseKhamLamSang(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        int marker = json.indexOf("\"noi_dung\":\"");
        if (marker < 0) {
            return json;
        }
        int start = marker + "\"noi_dung\":\"".length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return json.substring(start);
        }
        return json.substring(start, end)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
