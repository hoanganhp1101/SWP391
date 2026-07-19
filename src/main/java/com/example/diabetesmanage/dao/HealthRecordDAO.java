package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.dto.EncounterCreateDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO {
    public List<HealthRecord> getLatestPerPatient(String scopeDoctorId) {
        String sql =
                "SELECT hr.*, " +
                        "p.patient_code, " +
                        "p.loai_tieu_duong, " +
                        "pu.ho_ten AS patient_ho_ten, " +
                        "nu.ho_ten AS nhap_boi_ho_ten " +
                        "FROM health_records hr " +
                        "INNER JOIN ( " +
                        "    SELECT patient_id, " +
                        "           SUBSTRING_INDEX( " +
                        "               GROUP_CONCAT( " +
                        "                   id ORDER BY COALESCE(thoi_gian_do, ngay_tao) DESC, id DESC " +
                        "                   SEPARATOR ',' " +
                        "               ), ',', 1 " +
                        "           ) AS latest_id " +
                        "    FROM health_records " +
                        "    GROUP BY patient_id " +
                        ") hr_latest ON hr.id = hr_latest.latest_id " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "LEFT JOIN users pu ON p.user_id = pu.id " +
                        "LEFT JOIN users doc ON p.bac_si_id = doc.id " +
                        "LEFT JOIN users nu ON hr.nhap_boi = nu.id " +
                        "WHERE (? IS NULL OR doc.id = ?) " +
                        "ORDER BY COALESCE(hr.thoi_gian_do, hr.ngay_tao) DESC";


        List<HealthRecord> list = new ArrayList<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            if (scopeDoctorId != null && !scopeDoctorId.isBlank()) {
                ps.setString(1, scopeDoctorId);
                ps.setString(2, scopeDoctorId);
            } else {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    list.add(mapDetailedHealthRecord(rs));
                } catch (SQLException mapEx) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public HealthRecord getByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM health_records WHERE encounter_id = ? LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord record = mapHealthRecordFromBaseRow(
                        rs, optionalString(rs, "patient_id"));
                enrichHealthRecordOptionalJoins(record);
                return record;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HealthRecord mapHealthRecordFromBaseRow(ResultSet rs, String patientId) throws SQLException {
        HealthRecord hr = new HealthRecord();
        hr.setId(optionalString(rs, "id"));
        hr.setEncounterId(optionalString(rs, "encounter_id"));
        hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
        hr.setThoiDiemDoDuong(optionalString(rs, "thoi_diem_do_duong"));
        hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
        hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
        hr.setNhipTim(optionalInt(rs, "nhip_tim"));
        hr.setNhietDoC(optionalDouble(rs, "nhiet_do_c"));
        hr.setNhipTho(optionalInt(rs, "nhip_tho"));
        hr.setCanNangKg(optionalDouble(rs, "can_nang_kg"));
        hr.setBmi(optionalDouble(rs, "bmi"));
        hr.setSoBuocChan(optionalInt(rs, "so_buoc_chan"));
        hr.setCarbsG(optionalDouble(rs, "carbs_g"));
        hr.setSoGioNgu(optionalDouble(rs, "so_gio_ngu"));
        hr.setLieuLuongInsulinUi(optionalInt(rs, "lieu_luong_insulin_ui"));
        hr.setLoaiInsulinTiem(optionalString(rs, "loai_insulin_tiem"));
        hr.setGhiChu(optionalString(rs, "ghi_chu"));
        hr.setChestPain(optionalBoolean(rs, "chest_pain"));
        hr.setDizziness(optionalBoolean(rs, "dizziness"));
        hr.setFatigue(optionalBoolean(rs, "fatigue"));

        Timestamp timestamp = optionalTimestamp(rs, "thoi_gian_do");
        if (timestamp != null) {
            hr.setThoiGianDo(timestamp.toLocalDateTime());
            LocalDate lastVisitDate = timestamp.toLocalDateTime().toLocalDate();
            hr.setDaysSinceLastVisit((int) ChronoUnit.DAYS.between(lastVisitDate, LocalDate.now()));
        }

        Timestamp createdAt = optionalTimestamp(rs, "ngay_tao");
        if (createdAt != null) {
            hr.setNgayTao(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = optionalTimestamp(rs, "ngay_cap_nhat");
        if (updatedAt != null) {
            hr.setNgayCapNhat(updatedAt.toLocalDateTime());
        }

        Patient patient = new Patient();
        patient.setId(firstNonBlank(optionalString(rs, "patient_id"), patientId));
        hr.setPatient(patient);

        String nhapBoiId = optionalString(rs, "nhap_boi");
        if (nhapBoiId != null && !nhapBoiId.isBlank()) {
            User nhapBoi = new User();
            try {
                nhapBoi.setId(java.util.UUID.fromString(nhapBoiId.trim()));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid UUID
            }
            hr.setNhapBoi(nhapBoi);
        }

        return hr;
    }

    private void enrichHealthRecordOptionalJoins(HealthRecord record) {
        if (record == null || record.getId() == null || record.getId().isBlank()) {
            return;
        }

        String sql =
                "SELECT p.patient_code, p.loai_tieu_duong, " +
                        "pu.ho_ten AS patient_ho_ten, " +
                        "nu.ho_ten AS nhap_boi_ho_ten " +
                        "FROM health_records hr " +
                        "LEFT JOIN patients p ON hr.patient_id = p.id " +
                        "LEFT JOIN users pu ON p.user_id = pu.id " +
                        "LEFT JOIN users nu ON hr.nhap_boi = nu.id " +
                        "WHERE hr.id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, record.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return;
            }

            if (record.getPatient() == null) {
                record.setPatient(new Patient());
            }
            Patient patient = record.getPatient();
            String code = optionalString(rs, "patient_code");
            if (code != null && !code.isBlank()) {
                patient.setPatientCode(code);
            }
            String loaiTieuDuong = optionalString(rs, "loai_tieu_duong");
            if (loaiTieuDuong != null && !loaiTieuDuong.isBlank()) {
                patient.setLoaiTieuDuong(loaiTieuDuong);
            }
            String patientName = optionalString(rs, "patient_ho_ten");
            if (patientName != null && !patientName.isBlank()) {
                User patientUser = patient.getUser() != null ? patient.getUser() : new User();
                patientUser.setHoTen(patientName);
                patient.setUser(patientUser);
            }

            String nhapBoiName = optionalString(rs, "nhap_boi_ho_ten");
            if (nhapBoiName != null && !nhapBoiName.isBlank()) {
                User nhapBoi = record.getNhapBoi() != null ? record.getNhapBoi() : new User();
                nhapBoi.setHoTen(nhapBoiName);
                record.setNhapBoi(nhapBoi);
            }
        } catch (SQLException e) {
        }
    }

    public String insert(
            Connection con,
            EncounterCreateDTO form,
            String encounterId,
            String patientId,
            String doctorId,
            LocalDateTime visitTime
    ) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        String sql =
                "INSERT INTO health_records " +
                        "(id, encounter_id, patient_id, nhap_boi, thoi_gian_do, ngay_tao, " +
                        "duong_huyet_mgdl, thoi_diem_do_duong, huyet_ap_tam_thu, huyet_ap_tam_truong, " +
                        "nhip_tim, nhiet_do_c, nhip_tho, can_nang_kg, bmi, " +
                        "carbs_g, lieu_luong_insulin_ui, loai_insulin_tiem, ghi_chu) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, id);
            ps.setString(idx++, encounterId);
            ps.setString(idx++, patientId);
            ps.setString(idx++, doctorId);
            ps.setTimestamp(idx++, Timestamp.valueOf(visitTime));
            ps.setTimestamp(idx++, Timestamp.valueOf(now));
            JdbcUtil.setDouble(ps, idx++, form.getDuongHuyetMgdl());
            JdbcUtil.setString(ps, idx++, form.getThoiDiemDoDuong());
            JdbcUtil.setInteger(ps, idx++, form.getHuyetApTamThu());
            JdbcUtil.setInteger(ps, idx++, form.getHuyetApTamTruong());
            JdbcUtil.setInteger(ps, idx++, form.getNhipTim());
            JdbcUtil.setDouble(ps, idx++, form.getNhietDoC());
            JdbcUtil.setInteger(ps, idx++, form.getNhipTho());
            JdbcUtil.setDouble(ps, idx++, form.getCanNangKg());
            JdbcUtil.setDouble(ps, idx++, form.getBmi());
            JdbcUtil.setDouble(ps, idx++, form.getCarbsG());
            JdbcUtil.setInteger(ps, idx++, form.getLieuLuongInsulinUi());
            JdbcUtil.setString(ps, idx++, form.getLoaiInsulinTiem());
            JdbcUtil.setString(ps, idx, form.getGhiChuSucKhoe());
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("INSERT health_records affected " + rows + " row(s)");
            }
        }
        return id;
    }

    public void delete(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return;
        }
        String sql = "DELETE FROM health_records WHERE encounter_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    private HealthRecord mapDetailedHealthRecord(ResultSet rs) throws SQLException {
        HealthRecord hr = mapHealthRecordFromBaseRow(rs, resolvePatientId(rs));

        Patient patient = hr.getPatient() != null ? hr.getPatient() : new Patient();
        patient.setId(firstNonBlank(patient.getId(), resolvePatientId(rs)));
        String patientCode = optionalString(rs, "patient_code");
        if (patientCode != null && !patientCode.isBlank()) {
            patient.setPatientCode(patientCode);
        } else {
            String patientId = resolvePatientId(rs);
            patient.setPatientCode(patientId != null && patientId.length() >= 8
                    ? patientId.substring(0, 8).toUpperCase() : "N/A");
        }
        patient.setLoaiTieuDuong(optionalString(rs, "loai_tieu_duong"));

        User user = new User();
        String hoTen = optionalString(rs, "ho_ten");
        if (hoTen == null) {
            hoTen = optionalString(rs, "patient_ho_ten");
        }
        user.setHoTen(hoTen);
        patient.setUser(user);

        hr.setPatient(patient);

        String nhapBoiName = optionalString(rs, "nhap_boi_ho_ten");
        if (nhapBoiName != null && !nhapBoiName.isBlank()) {
            User nhapBoi = hr.getNhapBoi() != null ? hr.getNhapBoi() : new User();
            nhapBoi.setHoTen(nhapBoiName);
            hr.setNhapBoi(nhapBoi);
        }

        return hr;
    }

    private String resolvePatientId(ResultSet rs) throws SQLException {
        String patientId = optionalString(rs, "p_patient_id");
        if (patientId == null || patientId.isBlank()) {
            patientId = optionalString(rs, "hr_patient_id");
        }
        if (patientId == null || patientId.isBlank()) {
            patientId = optionalString(rs, "patient_id");
        }
        return patientId;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private Boolean optionalBoolean(ResultSet rs, String column) {
        try {
            boolean value = rs.getBoolean(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
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

    private Timestamp optionalTimestamp(ResultSet rs, String column) {
        try {
            return rs.getTimestamp(column);
        } catch (SQLException e) {
            return null;
        }
    }
}
