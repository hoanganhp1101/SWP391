package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.config.AppConstants;
import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HealthRecordDAO {

    private static final String RECORD_SELECT =
            "SELECT hr.*, " +
                    "p.id AS patient_id, " +
                    "p.patient_code, " +
                    "p.loai_tieu_duong, " +
                    "u.ho_ten ";

    private static final String RECORD_FROM =
            "FROM health_records hr " +
                    "JOIN patients p ON hr.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id ";

    public List<HealthRecord> getHealthRecord() {
        List<HealthRecord> list = new ArrayList<>();

        String sql = RECORD_SELECT + RECORD_FROM +
                "JOIN users d ON p.bac_si_id = d.id " +
                "WHERE d.email = ? " +
                "ORDER BY hr.thoi_gian_do DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, AppConstants.DOCTOR_EMAIL);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapDetailedHealthRecord(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public HealthRecord getHealthRecordRecordById(String recordId) {
        String sql = RECORD_SELECT + RECORD_FROM + "WHERE hr.id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, recordId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDetailedHealthRecord(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<HealthRecord> searchHealthRecordRecords(String startDate, String endDate, String keyword) {
        List<HealthRecord> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                RECORD_SELECT + RECORD_FROM +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ? "
        );

        if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            sql.append("AND DATE(hr.thoi_gian_do) BETWEEN ? AND ? ");
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (hr.health_record_code LIKE ? OR p.patient_code LIKE ? OR u.ho_ten LIKE ?) ");
        }

        sql.append("ORDER BY hr.thoi_gian_do DESC");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            int index = 1;
            ps.setString(index++, AppConstants.DOCTOR_EMAIL);

            if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
                ps.setString(index++, startDate);
                ps.setString(index++, endDate);
            }

            if (keyword != null && !keyword.isBlank()) {
                String search = "%" + keyword + "%";
                ps.setString(index++, search);
                ps.setString(index++, search);
                ps.setString(index++, search);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDetailedHealthRecord(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Map<String, List<HealthRecord>> getRecentRecordsGroupedByPatient(String doctorEmail, int maxRecordsPerPatient) {
        Map<String, List<HealthRecord>> grouped = new LinkedHashMap<>();

        String sql = RECORD_SELECT + RECORD_FROM +
                "JOIN users d ON p.bac_si_id = d.id " +
                "WHERE d.email = ? " +
                "ORDER BY p.id, hr.thoi_gian_do DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, doctorEmail);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String patientId = rs.getString("patient_id");
                List<HealthRecord> records = grouped.computeIfAbsent(patientId, k -> new ArrayList<>());
                if (records.size() >= maxRecordsPerPatient) {
                    continue;
                }
                records.add(mapDetailedHealthRecord(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return grouped;
    }

    public HealthRecord getLatestHealthRecordByPatientId(String patientId) {
        String sql = RECORD_SELECT + RECORD_FROM +
                "WHERE p.id = ? ORDER BY hr.thoi_gian_do DESC LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDetailedHealthRecord(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private HealthRecord mapDetailedHealthRecord(ResultSet rs) throws SQLException {
        HealthRecord hr = new HealthRecord();

        hr.setId(rs.getString("id"));
        hr.setHealthRecordId(RecordCodeHelper.resolve(rs, "health_record_code"));
        hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
        hr.setThoiDiemDoDuong(optionalString(rs, "thoi_diem_do_duong"));
        hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
        hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
        hr.setNhipTim(optionalInt(rs, "nhip_tim"));
        hr.setCanNangKg(optionalDouble(rs, "can_nang_kg"));
        hr.setBmi(optionalDouble(rs, "bmi"));
        hr.setHba1cPercent(optionalDouble(rs, "hba1c_percent"));
        hr.setCholesterolMmol(optionalDouble(rs, "cholesterol_mmol"));
        hr.setTriglycerideMmol(optionalDouble(rs, "triglyceride_mmol"));
        hr.setSoBuocChan(optionalInt(rs, "so_buoc_chan"));
        hr.setCarbsG(optionalDouble(rs, "carbs_g"));
        hr.setSoGioNgu(optionalDouble(rs, "so_gio_ngu"));
        hr.setLieuLuongInsulinUi(optionalInt(rs, "lieu_luong_insulin_ui"));
        hr.setLoaiInsulinTiem(optionalString(rs, "loai_insulin_tiem"));
        hr.setGhiChu(optionalString(rs, "ghi_chu"));

        Timestamp timestamp = rs.getTimestamp("thoi_gian_do");
        if (timestamp != null) {
            hr.setThoiGianDo(timestamp.toLocalDateTime());
            LocalDate lastVisitDate = timestamp.toLocalDateTime().toLocalDate();
            hr.setDaysSinceLastVisit((int) ChronoUnit.DAYS.between(lastVisitDate, LocalDate.now()));
        }

        Patient patient = new Patient();
        patient.setId(optionalString(rs, "patient_id"));
        patient.setPatientCode(RecordCodeHelper.resolve(rs, "patient_code"));
        patient.setLoaiTieuDuong(optionalString(rs, "loai_tieu_duong"));

        User user = new User();
        user.setHoTen(rs.getString("ho_ten"));
        patient.setUser(user);

        hr.setPatient(patient);
        return hr;
    }

    private Double optionalDouble(ResultSet rs, String column) {
        try {
            double value = rs.getDouble(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer optionalInt(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    private String optionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }
}
