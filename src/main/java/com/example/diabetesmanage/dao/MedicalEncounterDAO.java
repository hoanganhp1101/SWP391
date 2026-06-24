package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.medical.MedicationChip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MedicalEncounterDAO {

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

    public List<MedicationChip> getMedicationsByPatientId(String patientId) {
        List<MedicationChip> list = new ArrayList<>();

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
                list.add(new MedicationChip(
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

    private MedicalEncounter map(ResultSet rs) throws SQLException {
        MedicalEncounter enc = new MedicalEncounter();
        enc.setId(rs.getString("id"));
        enc.setDisplayCode(RecordCodeHelper.resolve(rs, "encounter_code"));
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
        return enc;
    }
}
