package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.model.form.AddMedicalEncounterForm;

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
                    "p.bac_si_id, " +
                    "p.loai_tieu_duong, " +
                    "u.ho_ten ";

    private static final String RECORD_FROM =
            "FROM health_records hr " +
                    "JOIN patients p ON hr.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id ";

    public List<HealthRecord> getHealthRecords(String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(RECORD_SELECT + RECORD_FROM);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");
        sql.append("ORDER BY hr.thoi_gian_do DESC");
        return queryRecords(sql.toString(), scopeDoctorId, null, null, null);
    }

    public HealthRecord getHealthRecordRecordById(String recordId, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(RECORD_SELECT + RECORD_FROM + "WHERE hr.id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, recordId);
            if (scopeDoctorId != null) {
                ps.setString(2, scopeDoctorId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDetailedHealthRecord(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean recordExists(String recordId) {
        String sql = "SELECT 1 FROM health_records WHERE id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, recordId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPatientIdByRecordId(String recordId) {
        String sql = "SELECT patient_id FROM health_records WHERE id = ?";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, recordId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("patient_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HealthRecord> searchHealthRecordRecords(
            String startDate, String endDate, String keyword, String scopeDoctorId
    ) {
        StringBuilder sql = new StringBuilder(RECORD_SELECT + RECORD_FROM);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");

        if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            sql.append("AND DATE(hr.thoi_gian_do) BETWEEN ? AND ? ");
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (hr.health_record_code LIKE ? OR p.patient_code LIKE ? OR u.ho_ten LIKE ?) ");
        }

        sql.append("ORDER BY hr.thoi_gian_do DESC");
        return queryRecords(sql.toString(), scopeDoctorId, startDate, endDate, keyword);
    }

    public Map<String, List<HealthRecord>> getRecentRecordsGroupedByPatient(
            String scopeDoctorId, int maxRecordsPerPatient
    ) {
        Map<String, List<HealthRecord>> grouped = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder(RECORD_SELECT + RECORD_FROM);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");
        sql.append("ORDER BY p.id, hr.thoi_gian_do DESC");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            if (scopeDoctorId != null) {
                ps.setString(1, scopeDoctorId);
            }
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

    public HealthRecord getLatestHealthRecordByPatientId(String patientId, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(RECORD_SELECT + RECORD_FROM + "WHERE p.id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }
        sql.append("ORDER BY hr.thoi_gian_do DESC LIMIT 1");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, patientId);
            if (scopeDoctorId != null) {
                ps.setString(2, scopeDoctorId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDetailedHealthRecord(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lưu chỉ số sức khỏe gắn với lần khám (trong transaction).
     */
    public String insert(Connection con, AddMedicalEncounterForm form, String patientId, String doctorId)
            throws SQLException {
        String id = java.util.UUID.randomUUID().toString();
        form.calculateBmiIfNeeded();

        String sql =
                "INSERT INTO health_records " +
                        "(id, patient_id, nhap_boi, thoi_gian_do, duong_huyet_mgdl, thoi_diem_do_duong, " +
                        "huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim, nhiet_do_c, nhip_tho, " +
                        "can_nang_kg, bmi, hba1c_percent, cholesterol_mmol, triglyceride_mmol, " +
                        "carbs_g, lieu_luong_insulin_ui, loai_insulin_tiem, ghi_chu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, doctorId);
            ps.setTimestamp(4, Timestamp.valueOf(form.resolveNgayKham()));
            JdbcUtil.setDouble(ps, 5, form.getDuongHuyetMgdl());
            JdbcUtil.setString(ps, 6, form.getThoiDiemDoDuong());
            JdbcUtil.setInteger(ps, 7, form.getHuyetApTamThu());
            JdbcUtil.setInteger(ps, 8, form.getHuyetApTamTruong());
            JdbcUtil.setInteger(ps, 9, form.getNhipTim());
            JdbcUtil.setDouble(ps, 10, form.getNhietDoC());
            JdbcUtil.setInteger(ps, 11, form.getNhipTho());
            JdbcUtil.setDouble(ps, 12, form.getCanNangKg());
            JdbcUtil.setDouble(ps, 13, form.getBmi());
            JdbcUtil.setDouble(ps, 14, form.getHba1cPercent());
            JdbcUtil.setDouble(ps, 15, form.getCholesterolMmol());
            JdbcUtil.setDouble(ps, 16, form.getTriglycerideMmol());
            JdbcUtil.setDouble(ps, 17, form.getCarbsG());
            JdbcUtil.setInteger(ps, 18, form.getLieuLuongInsulinUi());
            JdbcUtil.setString(ps, 19, form.getLoaiInsulinTiem());
            JdbcUtil.setString(ps, 20, form.getGhiChuSucKhoe());
            ps.executeUpdate();
        }
        return id;
    }

    public void deleteById(Connection con, String recordId) throws SQLException {
        String sql = "DELETE FROM health_records WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, recordId);
            ps.executeUpdate();
        }
    }

    private List<HealthRecord> queryRecords(
            String sql, String scopeDoctorId, String startDate, String endDate, String keyword
    ) {
        List<HealthRecord> list = new ArrayList<>();

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            int index = 1;
            if (scopeDoctorId != null) {
                ps.setString(index++, scopeDoctorId);
            }

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

    private HealthRecord mapDetailedHealthRecord(ResultSet rs) throws SQLException {
        HealthRecord hr = new HealthRecord();

        hr.setId(rs.getString("id"));
        hr.setHealthRecordId(PatientDAO.resolveCode(rs, "health_record_code"));
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
        patient.setPatientCode(PatientDAO.resolveCode(rs, "patient_code"));
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
