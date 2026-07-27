package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthRecordDAO {

    private static final Logger LOG = Logger.getLogger(HealthRecordDAO.class.getName());

    public List<HealthRecord> getLatestPerPatient(String scopeDoctorId) {
        String sql =
                "SELECT hr.*, " +
                        "p.patient_code, " +
                        "p.loai_tieu_duong, " +
                        "pu.ho_ten AS patient_ho_ten, " +
                        "nuu.ho_ten AS nhap_boi_ho_ten " +
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
                        "JOIN users pu ON p.id = pu.id " +
                        "LEFT JOIN doctors doc ON p.bac_si_id = doc.id " +
                        "LEFT JOIN users nuu ON hr.nhap_boi = nuu.id " +
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
                    LOG.log(Level.WARNING, "Failed to map health record row", mapEx);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "getLatestPerPatient error", e);
        }
        return list;
    }

    public HealthRecord getByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        String sql =
                "SELECT hr.*, " +
                        "p.patient_code, p.loai_tieu_duong, p.tien_su_benh, p.chieu_cao_cm, " +
                        "pu.ho_ten AS patient_name, " +
                        "nuu.ho_ten AS nhap_boi_name, " +
                        "me.ly_do_kham, me.qua_trinh_benh_ly, me.kham_lam_sang, " +
                        "me.chan_doan_chinh, me.chan_doan_phu, me.huong_xu_tri, " +
                        "rx.huong_dieu_tri, rx.che_do_an, rx.luyen_tap, rx.ghi_chu AS rx_ghi_chu " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users pu ON p.id = pu.id " +
                        "LEFT JOIN users nuu ON hr.nhap_boi = nuu.id " +
                        "LEFT JOIN medical_encounters me ON hr.encounter_id = me.id " +
                        "LEFT JOIN prescriptions rx ON rx.encounter_id = me.id " +
                        "WHERE hr.encounter_id = ? " +
                        "ORDER BY rx.ngay_tao DESC " +
                        "LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord record = mapHealthRecordFromBaseRow(
                        rs, optionalString(rs, "patient_id"));
                enrichFromJoinedRow(record, rs);
                return record;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getByEncounterId error for encounterId=" + encounterId, e);
        }
        return null;
    }

    private HealthRecord mapHealthRecordFromBaseRow(ResultSet rs, String patientId) throws SQLException {
        HealthRecord hr = new HealthRecord();
        hr.setId(optionalString(rs, "id"));
        hr.setEncounterId(optionalString(rs, "encounter_id"));
        String resolvedPatientId = firstNonBlank(optionalString(rs, "patient_id"), patientId);
        if (resolvedPatientId != null) {
            hr.setPatientId(resolvedPatientId);
        }
        hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
        hr.setThoiDiemDoDuong(optionalString(rs, "thoi_diem_do_duong"));
        hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
        hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
        hr.setNhipTim(optionalInt(rs, "nhip_tim"));
        hr.setNhietDoC(optionalDouble(rs, "nhiet_do_c"));
        hr.setNhipTho(optionalInt(rs, "nhip_tho"));
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

        Patient patient = new Patient();
        patient.setId(firstNonBlank(optionalString(rs, "patient_id"), patientId));
        hr.setPatient(patient);

        String nhapBoiId = optionalString(rs, "nhap_boi");
        if (nhapBoiId != null && !nhapBoiId.isBlank()) {
            User nhapBoi = new User();
            nhapBoi.setId(nhapBoiId.trim());
            hr.setNhapBoi(nhapBoi);
        }

        return hr;
    }

    /**
     * Bản ghi health_records mới nhất của bệnh nhân (không phụ thuộc encounter_id,
     * vì dữ liệu bệnh nhân tự nhập không gắn với lần khám nào).
     */
    public HealthRecord getLatestByPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        String sql =
                "SELECT hr.*, " +
                        "p.patient_code, p.loai_tieu_duong, p.tien_su_benh, p.chieu_cao_cm, " +
                        "pu.ho_ten AS patient_name, " +
                        "nuu.ho_ten AS nhap_boi_name " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users pu ON p.id = pu.id " +
                        "LEFT JOIN users nuu ON hr.nhap_boi = nuu.id " +
                        "WHERE hr.patient_id = ? " +
                        "ORDER BY hr.ngay_tao DESC " +
                        "LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = mapHealthRecordFromBaseRow(rs, patientId);
                enrichFromJoinedRow(hr, rs);
                return hr;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "getLatestByPatientId error for patientId=" + patientId, e);
        }
        return null;
    }

    /**
     * Gắn dữ liệu từ các bảng liên quan (patients, users, medical_encounters,
     * prescriptions) đã được JOIN trong {@link #getByEncounterId} vào HealthRecord
     * để patient-detail.jsp hiển thị đủ thông tin. Không đọc/tạo column mới.
     */
    private void enrichFromJoinedRow(HealthRecord record, ResultSet rs) {
        Patient patient = record.getPatient() != null ? record.getPatient() : new Patient();
        String patientCode = optionalString(rs, "patient_code");
        if (patientCode != null && !patientCode.isBlank()) {
            patient.setPatientCode(patientCode);
        }
        patient.setLoaiTieuDuong(optionalString(rs, "loai_tieu_duong"));
        patient.setTienSuBenh(optionalString(rs, "tien_su_benh"));
        patient.setChieuCaoCm(optionalDouble(rs, "chieu_cao_cm"));
        User patientUser = patient.getUser() != null ? patient.getUser() : new User();
        patientUser.setHoTen(optionalString(rs, "patient_name"));
        patient.setUser(patientUser);
        record.setPatient(patient);

        // Các field HealthRecord mà patient-detail.jsp đọc trực tiếp (hr.*).
        record.setTienSuBenh(optionalString(rs, "tien_su_benh"));
        record.setChieuCaoCm(optionalDouble(rs, "chieu_cao_cm"));
        record.setPhanLoaiTieuDuong(optionalString(rs, "loai_tieu_duong"));

        String nhapBoiName = optionalString(rs, "nhap_boi_name");
        if (nhapBoiName != null && !nhapBoiName.isBlank()) {
            User nhapBoi = record.getNhapBoi() != null ? record.getNhapBoi() : new User();
            nhapBoi.setHoTen(nhapBoiName);
            record.setNhapBoi(nhapBoi);
        }

        // medical_encounters: kham_lam_sang là JSON -> parse để tránh hiển thị JSON thô.
        String khamJson = optionalString(rs, "kham_lam_sang");
        String noiDung = EncounterClinicalJson.parseString(khamJson, "noi_dung");
        if (noiDung != null && !noiDung.isBlank()) {
            record.setKhamLamSang(noiDung.trim());
        } else if (khamJson != null && !khamJson.trim().isEmpty()
                && !khamJson.trim().startsWith("{")) {
            record.setKhamLamSang(khamJson.trim());
        }
        // HealthRecord không có field lyDoKham/quaTrinhBenhLy -> map ly_do_kham vào trieuChung
        // (đúng ngữ nghĩa JSP + luồng enrich hiện tại của PatientListController).
        String trieuChung = EncounterClinicalJson.parseString(khamJson, "trieu_chung");
        if (trieuChung == null || trieuChung.isBlank()) {
            trieuChung = optionalString(rs, "ly_do_kham");
        }
        if (trieuChung != null && !trieuChung.isBlank()) {
            record.setTrieuChung(trieuChung.trim());
        }
        record.setChanDoanChinh(optionalString(rs, "chan_doan_chinh"));
        record.setChanDoanPhu(optionalString(rs, "chan_doan_phu"));
        record.setHuongXuTri(optionalString(rs, "huong_xu_tri"));

        // prescriptions: hướng điều trị / chế độ ăn / luyện tập.
        record.setKhuyenNghiDieuTri(optionalString(rs, "huong_dieu_tri"));
        record.setCheDoAn(optionalString(rs, "che_do_an"));
        record.setLuyenTap(optionalString(rs, "luyen_tap"));
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
                        "hba1c_percent, cholesterol_mmol, triglyceride_mmol, " +
                        "carbs_g, lieu_luong_insulin_ui, loai_insulin_tiem, ghi_chu) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
            JdbcUtil.setDouble(ps, idx++, form.getHba1cPercent());
            JdbcUtil.setDouble(ps, idx++, form.getCholesterolMmol());
            JdbcUtil.setDouble(ps, idx++, form.getTriglycerideMmol());
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
        HealthRecord hr = mapHealthRecordFromBaseRow(rs, optionalString(rs, "patient_id"));

        Patient patient = hr.getPatient() != null ? hr.getPatient() : new Patient();
        String patientCode = optionalString(rs, "patient_code");
        if (patientCode != null && !patientCode.isBlank()) {
            patient.setPatientCode(patientCode);
        }
        patient.setLoaiTieuDuong(optionalString(rs, "loai_tieu_duong"));

        User user = new User();
        user.setHoTen(optionalString(rs, "patient_ho_ten"));
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

    // ---- Patient portal methods (from main) ----

    public boolean insertHealthRecord(HealthRecord record) {
        String sql = "INSERT INTO health_records (id, patient_id, nhap_boi, duong_huyet_mgdl, carbs_g, ghi_chu, lieu_luong_insulin_ui, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong, thoi_diem_do_duong, chest_pain, dizziness, fatigue, thoi_gian_do) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) {
                throw new IllegalStateException("Không kết nối được MySQL.");
            }
            String recordId = java.util.UUID.randomUUID().toString();
            record.setId(recordId);
            ps.setString(1, recordId);
            ps.setString(2, record.getPatientId());
            String nhapBoi = record.getNhapBoi() != null ? record.getNhapBoi().getId() : record.getPatientId();
            ps.setString(3, nhapBoi);
            if (record.getDuongHuyetMgdl() != null) {
                ps.setDouble(4, record.getDuongHuyetMgdl());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            if (record.getCarbsG() != null) {
                ps.setDouble(5, record.getCarbsG());
            } else {
                ps.setNull(5, Types.DECIMAL);
            }
            ps.setString(6, record.getGhiChu());
            if (record.getLieuLuongInsulinUi() != null) {
                ps.setInt(7, record.getLieuLuongInsulinUi());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            if (record.getNhipTim() != null) {
                ps.setInt(8, record.getNhipTim());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (record.getHuyetApTamThu() != null) {
                ps.setInt(9, record.getHuyetApTamThu());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (record.getHuyetApTamTruong() != null) {
                ps.setInt(10, record.getHuyetApTamTruong());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setString(11, record.getThoiDiemDoDuong());
            ps.setInt(12, record.getChestPain() != null ? record.getChestPain() : 0);
            ps.setInt(13, record.getDizziness() != null ? record.getDizziness() : 0);
            ps.setInt(14, record.getFatigue() != null ? record.getFatigue() : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "insertHealthRecord failed", e);
            return false;
        }
    }

    public void insertExtractedHealthRecord(String patientId, Double weight, Double bmi, Integer systole, Integer diastole, Integer heartRate, Double glucose, Double hba1c, Double cholesterol, Double triglyceride) {
        String sql = "INSERT INTO health_records (id, patient_id, can_nang_kg, bmi, huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim, duong_huyet_mgdl, hba1c_percent, cholesterol_mmol, triglyceride_mmol, thoi_gian_do) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, patientId);
            
            if (weight != null) ps.setDouble(3, weight); else ps.setNull(3, java.sql.Types.DECIMAL);
            if (bmi != null) ps.setDouble(4, bmi); else ps.setNull(4, java.sql.Types.DECIMAL);
            if (systole != null) ps.setInt(5, systole); else ps.setNull(5, java.sql.Types.INTEGER);
            if (diastole != null) ps.setInt(6, diastole); else ps.setNull(6, java.sql.Types.INTEGER);
            if (heartRate != null) ps.setInt(7, heartRate); else ps.setNull(7, java.sql.Types.INTEGER);
            if (glucose != null) ps.setDouble(8, glucose); else ps.setNull(8, java.sql.Types.DECIMAL);
            if (hba1c != null) ps.setDouble(9, hba1c); else ps.setNull(9, java.sql.Types.DECIMAL);
            if (cholesterol != null) ps.setDouble(10, cholesterol); else ps.setNull(10, java.sql.Types.DECIMAL);
            if (triglyceride != null) ps.setDouble(11, triglyceride); else ps.setNull(11, java.sql.Types.DECIMAL);
            
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HealthRecord getLatestHealthRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND duong_huyet_mgdl IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setId(rs.getString("id"));
                hr.setPatientId(patientId);
                hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
                hr.setHba1cPercent(optionalDouble(rs, "hba1c_percent"));
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HealthRecord getLatestComprehensiveRecord(String patientId) {
        String sql = "SELECT " +
                     "(SELECT duong_huyet_mgdl FROM health_records WHERE patient_id = p.id AND duong_huyet_mgdl IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as duong_huyet_mgdl, " +
                     "(SELECT huyet_ap_tam_thu FROM health_records WHERE patient_id = p.id AND huyet_ap_tam_thu IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as huyet_ap_tam_thu, " +
                     "(SELECT huyet_ap_tam_truong FROM health_records WHERE patient_id = p.id AND huyet_ap_tam_truong IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as huyet_ap_tam_truong, " +
                     "(SELECT nhip_tim FROM health_records WHERE patient_id = p.id AND nhip_tim IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as nhip_tim, " +
                     "(SELECT can_nang_kg FROM health_records WHERE patient_id = p.id AND can_nang_kg IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as can_nang_kg, " +
                     "(SELECT bmi FROM health_records WHERE patient_id = p.id AND bmi IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as bmi, " +
                     "(SELECT thoi_gian_do FROM health_records WHERE patient_id = p.id ORDER BY thoi_gian_do DESC LIMIT 1) as thoi_gian_do, " +
                     "(SELECT hba1c_percent FROM health_records WHERE patient_id = p.id AND hba1c_percent IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as hba1c_percent, " +
                     "(SELECT cholesterol_mmol FROM health_records WHERE patient_id = p.id AND cholesterol_mmol IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as cholesterol_mmol, " +
                     "(SELECT triglyceride_mmol FROM health_records WHERE patient_id = p.id AND triglyceride_mmol IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1) as triglyceride_mmol " +
                     "FROM (SELECT ? as id) p";
        HealthRecord hr = null;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hr = new HealthRecord();
                hr.setPatientId(patientId);
                hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
                hr.setHba1cPercent(optionalDouble(rs, "hba1c_percent"));
                hr.setCanNangKg(optionalDouble(rs, "can_nang_kg"));
                hr.setBmi(optionalDouble(rs, "bmi"));
                hr.setCholesterolMmol(optionalDouble(rs, "cholesterol_mmol"));
                hr.setTriglycerideMmol(optionalDouble(rs, "triglyceride_mmol"));
                hr.setNhipTim(optionalInt(rs, "nhip_tim"));
                hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
                hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (hr != null) {
            String labSql = "SELECT hba1c, cholesterol_tp, triglyceride FROM lab_results WHERE patient_id = ? ORDER BY ngay_xet_nghiem DESC LIMIT 1";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(labSql)) {
                ps.setString(1, patientId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (hr.getHba1cPercent() == null && rs.getObject("hba1c") != null) {
                        hr.setHba1cPercent(rs.getDouble("hba1c"));
                    }
                    if (hr.getCholesterolMmol() == null && rs.getObject("cholesterol_tp") != null) {
                        hr.setCholesterolMmol(rs.getDouble("cholesterol_tp"));
                    }
                    if (hr.getTriglycerideMmol() == null && rs.getObject("triglyceride") != null) {
                        hr.setTriglycerideMmol(rs.getDouble("triglyceride"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hr;
    }

    public HealthRecord getLatestHeartRateRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND nhip_tim IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setNhipTim(optionalInt(rs, "nhip_tim"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HealthRecord getLatestBloodPressureRecord(String patientId) {
        String sql = "SELECT * FROM health_records WHERE patient_id = ? AND huyet_ap_tam_thu IS NOT NULL AND huyet_ap_tam_truong IS NOT NULL ORDER BY thoi_gian_do DESC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
                hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
                return hr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HealthRecord> getAllRecordsForChart(String patientId) {
        String sql = "SELECT thoi_gian_do, duong_huyet_mgdl, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong " +
                "FROM health_records WHERE patient_id = ? ORDER BY thoi_gian_do ASC";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
                hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
                hr.setNhipTim(optionalInt(rs, "nhip_tim"));
                hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
                hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
                list.add(hr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lịch sử đo chỉ số gần đây (IoT và/hoặc thủ công), mới nhất trước.
     *
     * @param iotOnly true = chỉ bản ghi mô phỏng IoT
     */
    public List<HealthRecord> getMeasurementHistory(String patientId, int limit, boolean iotOnly) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = "SELECT id, thoi_gian_do, duong_huyet_mgdl, thoi_diem_do_duong, nhip_tim, "
                + "huyet_ap_tam_thu, huyet_ap_tam_truong, ghi_chu "
                + "FROM health_records WHERE patient_id = ? "
                + (iotOnly ? "AND ghi_chu LIKE '%mô phỏng IoT%' " : "")
                + "AND (duong_huyet_mgdl IS NOT NULL OR nhip_tim IS NOT NULL "
                + "OR huyet_ap_tam_thu IS NOT NULL OR huyet_ap_tam_truong IS NOT NULL) "
                + "ORDER BY thoi_gian_do DESC LIMIT ?";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setInt(2, safeLimit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setId(optionalString(rs, "id"));
                hr.setThoiGianDo(rs.getTimestamp("thoi_gian_do"));
                hr.setDuongHuyetMgdl(optionalDouble(rs, "duong_huyet_mgdl"));
                hr.setThoiDiemDoDuong(optionalString(rs, "thoi_diem_do_duong"));
                hr.setNhipTim(optionalInt(rs, "nhip_tim"));
                hr.setHuyetApTamThu(optionalInt(rs, "huyet_ap_tam_thu"));
                hr.setHuyetApTamTruong(optionalInt(rs, "huyet_ap_tam_truong"));
                hr.setGhiChu(optionalString(rs, "ghi_chu"));
                list.add(hr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HealthRecord> getRecentDailyRecords(String patientId) {
        String sql = "SELECT DATE(thoi_gian_do) as record_date, AVG(duong_huyet_mgdl) as avg_glucose, SUM(lieu_luong_insulin_ui) as total_insulin " +
                "FROM health_records " +
                "WHERE patient_id = ? " +
                "GROUP BY DATE(thoi_gian_do) " +
                "ORDER BY record_date DESC LIMIT 7";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setDuongHuyetMgdl(optionalDouble(rs, "avg_glucose"));
                hr.setLieuLuongInsulinUi(optionalInt(rs, "total_insulin"));
                hr.setThoiGianDo(rs.getTimestamp("record_date"));
                list.add(hr);
            }
            java.util.Collections.reverse(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
