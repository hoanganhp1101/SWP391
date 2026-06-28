package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.util.SqlDiagnostics;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository cho báº£ng {@code health_records} â€” snapshot sá»©c khá»e (má»™t báº£n ghi / bá»‡nh nhÃ¢n).
 *
 * <p><b>READ (UI):</b> {@link #findSnapshotByPatientId} â€” O(1), khÃ´ng Ä‘á»c medical_encounters.
 * <p><b>WRITE:</b> chá»‰ qua {@link #upsertSnapshotFromEncounter} (gá»i tá»« HealthRecordSnapshotService).
 */
public class HealthRecordDAO {

    private static final Logger LOG = Logger.getLogger(HealthRecordDAO.class.getName());

    /**
     * Latest snapshot per patient_id â€” compatible MySQL 5.7+ (no window functions required).
     */
    private static final String LATEST_SNAPSHOT_JOIN =
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
                    ") hr_latest ON hr.id = hr_latest.latest_id ";

    private static final String HEALTH_RECORD_READ_COLUMNS =
            "hr.id AS hr_id, " +
                    "hr.patient_id AS hr_patient_id, " +
                    "hr.encounter_id AS hr_encounter_id, " +
                    "hr.last_encounter_id AS hr_last_encounter_id, " +
                    "hr.health_record_code AS hr_health_record_code, " +
                    "hr.chieu_cao_cm AS hr_chieu_cao_cm, " +
                    "hr.duong_huyet_mgdl AS hr_duong_huyet_mgdl, " +
                    "hr.thoi_diem_do_duong AS hr_thoi_diem_do_duong, " +
                    "hr.huyet_ap_tam_thu AS hr_huyet_ap_tam_thu, " +
                    "hr.huyet_ap_tam_truong AS hr_huyet_ap_tam_truong, " +
                    "hr.nhip_tim AS hr_nhip_tim, " +
                    "hr.nhiet_do_c AS hr_nhiet_do_c, " +
                    "hr.nhip_tho AS hr_nhip_tho, " +
                    "hr.can_nang_kg AS hr_can_nang_kg, " +
                    "hr.bmi AS hr_bmi, " +
                    "hr.hba1c_percent AS hr_hba1c_percent, " +
                    "hr.cholesterol_mmol AS hr_cholesterol_mmol, " +
                    "hr.triglyceride_mmol AS hr_triglyceride_mmol, " +
                    "hr.hdl_mmol AS hr_hdl_mmol, " +
                    "hr.ldl_mmol AS hr_ldl_mmol, " +
                    "hr.wbc AS hr_wbc, " +
                    "hr.rbc AS hr_rbc, " +
                    "hr.hgb AS hr_hgb, " +
                    "hr.hct AS hr_hct, " +
                    "hr.plt AS hr_plt, " +
                    "hr.ast AS hr_ast, " +
                    "hr.alt AS hr_alt, " +
                    "hr.ure AS hr_ure, " +
                    "hr.creatinine AS hr_creatinine, " +
                    "hr.trieu_chung AS hr_trieu_chung, " +
                    "hr.tien_su_benh AS hr_tien_su_benh, " +
                    "hr.kham_lam_sang AS hr_kham_lam_sang, " +
                    "hr.chan_doan_chinh AS hr_chan_doan_chinh, " +
                    "hr.chan_doan_phu AS hr_chan_doan_phu, " +
                    "hr.phan_loai_tieu_duong AS hr_phan_loai_tieu_duong, " +
                    "hr.huong_xu_tri AS hr_huong_xu_tri, " +
                    "hr.khuyen_nghi_dieu_tri AS hr_khuyen_nghi_dieu_tri, " +
                    "hr.che_do_an AS hr_che_do_an, " +
                    "hr.luyen_tap AS hr_luyen_tap, " +
                    "hr.so_buoc_chan AS hr_so_buoc_chan, " +
                    "hr.carbs_g AS hr_carbs_g, " +
                    "hr.so_gio_ngu AS hr_so_gio_ngu, " +
                    "hr.lieu_luong_insulin_ui AS hr_lieu_luong_insulin_ui, " +
                    "hr.loai_insulin_tiem AS hr_loai_insulin_tiem, " +
                    "hr.ghi_chu AS hr_ghi_chu, " +
                    "hr.chest_pain AS hr_chest_pain, " +
                    "hr.dizziness AS hr_dizziness, " +
                    "hr.fatigue AS hr_fatigue, " +
                    "hr.thoi_gian_do AS hr_thoi_gian_do, " +
                    "hr.ngay_tao AS hr_ngay_tao, " +
                    "hr.ngay_cap_nhat AS hr_ngay_cap_nhat, " +
                    "hr.nhap_boi AS hr_nhap_boi ";

    private static final String SNAPSHOT_SELECT_COLUMNS =
            "SELECT " + HEALTH_RECORD_READ_COLUMNS + ", " +
                    "p.id AS p_patient_id, " +
                    "p.patient_code AS p_patient_code, " +
                    "p.loai_tieu_duong AS p_loai_tieu_duong, " +
                    "pu.ho_ten AS patient_ho_ten, " +
                    "nu.id AS nhap_boi_user_id, " +
                    "nu.ho_ten AS nhap_boi_ho_ten ";

    private static final String SNAPSHOT_FROM_JOINS =
            "FROM health_records hr " +
                    LATEST_SNAPSHOT_JOIN +
                    "JOIN patients p ON hr.patient_id = p.id " +
                    "LEFT JOIN users pu ON p.user_id = pu.id " +
                    "LEFT JOIN users doc ON p.bac_si_id = doc.id " +
                    "LEFT JOIN users nu ON hr.nhap_boi = nu.id ";

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
        return listLatestSnapshotPerPatient(scopeDoctorId);
    }

    /**
     * Má»—i bá»‡nh nhÃ¢n tá»‘i Ä‘a má»™t snapshot (báº£n ghi má»›i nháº¥t theo ngay_cap_nhat / thoi_gian_do).
     */
    public List<HealthRecord> listLatestSnapshotPerPatient(String scopeDoctorId) {
        String sql =
                "SELECT hr.*, " +
                        "p.patient_code, " +
                        "p.loai_tieu_duong, " +
                        "pu.ho_ten AS patient_ho_ten, " +
                        "nu.ho_ten AS nhap_boi_ho_ten " +
                        "FROM health_records hr " +
                        LATEST_SNAPSHOT_JOIN +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "LEFT JOIN users pu ON p.user_id = pu.id " +
                        "LEFT JOIN users doc ON p.bac_si_id = doc.id " +
                        "LEFT JOIN users nu ON hr.nhap_boi = nu.id " +
                        "WHERE (? IS NULL OR doc.id = ?) " +
                        "ORDER BY COALESCE(hr.thoi_gian_do, hr.ngay_tao) DESC";

        LOG.log(Level.FINE, "listLatestSnapshotPerPatient SQL: {0}", sql);
        LOG.log(Level.FINE, "listLatestSnapshotPerPatient scopeDoctorId: {0}", scopeDoctorId);

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
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    list.add(mapDetailedHealthRecord(rs));
                } catch (SQLException mapEx) {
                    LOG.log(Level.WARNING, "Failed to map health_record row " + rowCount, mapEx);
                }
            }
            LOG.log(Level.INFO, "listLatestSnapshotPerPatient returned {0} row(s), mapped {1} record(s)",
                    new Object[]{rowCount, list.size()});
            if (rowCount > 0 && list.isEmpty()) {
                LOG.warning("ResultSet had rows but all mappings failed â€” check column aliases / schema.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "listLatestSnapshotPerPatient failed", e);
            e.printStackTrace();
        }
        return list;
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

    public List<HealthRecord> findByPatientId(String patientId, String scopeDoctorId) {
        List<HealthRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT hr.*, " +
                        "p.id AS patient_id, p.patient_code, p.loai_tieu_duong, " +
                        "pu.ho_ten AS patient_ho_ten, " +
                        "nu.id AS nhap_boi_id, nu.ho_ten AS nhap_boi_ho_ten " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users pu ON p.user_id = pu.id " +
                        "LEFT JOIN users nu ON hr.nhap_boi = nu.id " +
                        "WHERE hr.patient_id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }
        sql.append("ORDER BY hr.thoi_gian_do DESC");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, patientId);
            if (scopeDoctorId != null) {
                ps.setString(2, scopeDoctorId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPatientHealthRecord(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Latest health_records row for patient detail UI.
     * Independent query â€” no INNER JOIN on patients/encounters/labs.
     */
    public HealthRecord findSnapshotByPatientId(String patientId, String scopeDoctorId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }

        String sql =
                "SELECT * FROM health_records " +
                        "WHERE patient_id = ? " +
                        "ORDER BY thoi_gian_do DESC " +
                        "LIMIT 1";

        LOG.log(Level.FINE, "findSnapshotByPatientId patientId={0}", patientId);

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, patientId.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HealthRecord record = mapHealthRecordFromBaseRow(rs, patientId.trim());
                enrichHealthRecordOptionalJoins(record);
                LOG.log(Level.INFO, "findSnapshotByPatientId found record id={0} for patientId={1}",
                        new Object[]{record.getId(), patientId});
                return record;
            }
            LOG.log(Level.INFO, "findSnapshotByPatientId no row for patientId={0}", patientId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "findSnapshotByPatientId failed for patientId=" + patientId, e);
        }
        return null;
    }

    private HealthRecord mapHealthRecordFromBaseRow(ResultSet rs, String patientId) throws SQLException {
        HealthRecord hr = new HealthRecord();
        hr.setId(optionalString(rs, "id"));
        hr.setEncounterId(optionalString(rs, "encounter_id"));
        hr.setLastEncounterId(optionalString(rs, "last_encounter_id"));
        hr.setHealthRecordId(resolveHealthRecordCodeSafe(rs));
        hr.setChieuCaoCm(optionalDouble(rs, "chieu_cao_cm"));
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
        hr.setHdlMmol(optionalDouble(rs, "hdl_mmol"));
        hr.setLdlMmol(optionalDouble(rs, "ldl_mmol"));
        hr.setWbc(optionalDouble(rs, "wbc"));
        hr.setRbc(optionalDouble(rs, "rbc"));
        hr.setHgb(optionalDouble(rs, "hgb"));
        hr.setHct(optionalDouble(rs, "hct"));
        hr.setPlt(optionalDouble(rs, "plt"));
        hr.setAst(optionalDouble(rs, "ast"));
        hr.setAlt(optionalDouble(rs, "alt"));
        hr.setUre(optionalDouble(rs, "ure"));
        hr.setCreatinine(optionalDouble(rs, "creatinine"));
        hr.setTrieuChung(optionalString(rs, "trieu_chung"));
        hr.setTienSuBenh(optionalString(rs, "tien_su_benh"));
        hr.setKhamLamSang(optionalString(rs, "kham_lam_sang"));
        hr.setChanDoanChinh(optionalString(rs, "chan_doan_chinh"));
        hr.setChanDoanPhu(optionalString(rs, "chan_doan_phu"));
        hr.setPhanLoaiTieuDuong(optionalString(rs, "phan_loai_tieu_duong"));
        hr.setHuongXuTri(optionalString(rs, "huong_xu_tri"));
        hr.setKhuyenNghiDieuTri(optionalString(rs, "khuyen_nghi_dieu_tri"));
        hr.setCheDoAn(optionalString(rs, "che_do_an"));
        hr.setLuyenTap(optionalString(rs, "luyen_tap"));
        hr.setKhuyenNghi(firstNonBlank(
                optionalString(rs, "khuyen_nghi_dieu_tri"),
                optionalString(rs, "huong_xu_tri")
        ));
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
            LOG.log(Level.WARNING, "enrichHealthRecordOptionalJoins failed for record id=" + record.getId(), e);
        }
    }

    private String resolveHealthRecordCodeSafe(ResultSet rs) {
        String code = optionalString(rs, "health_record_code");
        if (code != null && !code.isBlank()) {
            return code;
        }
        String id = optionalString(rs, "id");
        return id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "N/A";
    }

    /** Schema cá»‘ Ä‘á»‹nh theo diabcare_db â€” khÃ´ng cháº¡y DDL runtime. */
    public void prepareSnapshotSchema() {
        // no-op
    }

    /** @deprecated dÃ¹ng {@link #findSnapshotByPatientId} */
    @Deprecated
    public HealthRecord findLatestByPatientId(String patientId, String scopeDoctorId) {
        return findSnapshotByPatientId(patientId, scopeDoctorId);
    }

    public HealthRecord getLatestHealthRecordByPatientId(String patientId, String scopeDoctorId) {
        return findSnapshotByPatientId(patientId, scopeDoctorId);
    }

    /**
     * UPSERT snapshot theo patientId sau mỗi MedicalEncounter.
     * Giữ lịch sử health_records — không xóa bản ghi cũ.
     */
    public void upsertSnapshotFromEncounter(
            Connection con,
            EncounterCreateRequest form,
            String patientId,
            String doctorId,
            String encounterId
    ) throws SQLException {
        String existingId = findSnapshotIdByPatientId(con, patientId);
        if (existingId == null) {
            insertSnapshotFromEncounter(con, form, patientId, doctorId, encounterId);
            LOG.log(Level.INFO, "Inserted health_records snapshot for patientId={0} encounterId={1}",
                    new Object[]{patientId, encounterId});
        } else {
            mergeSnapshotFromEncounter(con, existingId, form, doctorId, encounterId);
            LOG.log(Level.INFO, "Updated health_records snapshot id={0} patientId={1} encounterId={2}",
                    new Object[]{existingId, patientId, encounterId});
        }
    }

    public void deleteSnapshotIfLastEncounter(Connection con, String patientId, String encounterId)
            throws SQLException {
        // HealthRecord khÃ´ng bá»‹ xÃ³a khi xÃ³a encounter â€” giá»¯ snapshot hiá»‡n táº¡i.
    }

    /**
     * @deprecated DÃ¹ng {@link #upsertSnapshotFromEncounter}
     */
    @Deprecated
    public String syncFromEncounter(
            Connection con,
            EncounterCreateRequest form,
            String patientId,
            String doctorId,
            String encounterId
    ) throws SQLException {
        upsertSnapshotFromEncounter(con, form, patientId, doctorId, encounterId);
        return findSnapshotIdByPatientId(con, patientId);
    }

    /**
     * @deprecated KhÃ´ng táº¡o health_record tá»« appointment. Chá»‰ encounter form má»›i cáº­p nháº­t snapshot.
     */
    @Deprecated
    public String syncMinimalFromAppointment(
            Connection con,
            String patientId,
            String doctorId,
            String encounterId,
            LocalDateTime visitTime,
            String lyDoKham
    ) throws SQLException {
        String existingId = findSnapshotIdByPatientId(con, patientId);
        return existingId;
    }

    /**
     * @deprecated HealthRecord khÃ´ng bá»‹ xÃ³a theo encounter_id.
     */
    @Deprecated
    public void deleteByEncounterId(Connection con, String encounterId) throws SQLException {
        // no-op
    }

    public String findIdByEncounterId(Connection con, String encounterId) {
        return null;
    }

    private String findSnapshotIdByPatientId(Connection con, String patientId) throws SQLException {
        String sql =
                "SELECT id FROM health_records WHERE patient_id = ? " +
                        "ORDER BY thoi_gian_do DESC, ngay_tao DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        }
        return null;
    }

    private void insertSnapshotFromEncounter(
            Connection con,
            EncounterCreateRequest form,
            String patientId,
            String doctorId,
            String encounterId
    ) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();
        LocalDateTime visitTime = form.resolveNgayKham();
        LocalDateTime now = LocalDateTime.now();

        String sql =
                "INSERT INTO health_records " +
                        "(id, patient_id, nhap_boi, thoi_gian_do, ngay_tao, " +
                        "duong_huyet_mgdl, thoi_diem_do_duong, huyet_ap_tam_thu, huyet_ap_tam_truong, " +
                        "nhip_tim, nhiet_do_c, nhip_tho, can_nang_kg, bmi, " +
                        "hba1c_percent, cholesterol_mmol, triglyceride_mmol, " +
                        "carbs_g, lieu_luong_insulin_ui, loai_insulin_tiem, ghi_chu, " +
                        "wbc, rbc, hgb, hct, plt) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, id);
            ps.setString(idx++, patientId);
            ps.setString(idx++, doctorId);
            ps.setTimestamp(idx++, Timestamp.valueOf(visitTime));
            ps.setTimestamp(idx++, Timestamp.valueOf(now));
            idx = bindSnapshotMetrics(ps, idx, form);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("INSERT health_records affected " + rows + " row(s)");
            }
        } catch (SQLException ex) {
            SqlDiagnostics.log(LOG, Level.SEVERE, "INSERT health_records snapshot", sql,
                    new Object[]{id, patientId, doctorId, visitTime}, ex);
            throw ex;
        }
    }

    private void mergeSnapshotFromEncounter(
            Connection con,
            String recordId,
            EncounterCreateRequest form,
            String doctorId,
            String encounterId
    ) throws SQLException {
        LocalDateTime visitTime = form.resolveNgayKham();
        String sql =
                "UPDATE health_records SET " +
                        "nhap_boi = ?, thoi_gian_do = ?, " +
                        "duong_huyet_mgdl = COALESCE(?, duong_huyet_mgdl), " +
                        "thoi_diem_do_duong = COALESCE(?, thoi_diem_do_duong), " +
                        "huyet_ap_tam_thu = COALESCE(?, huyet_ap_tam_thu), " +
                        "huyet_ap_tam_truong = COALESCE(?, huyet_ap_tam_truong), " +
                        "nhip_tim = COALESCE(?, nhip_tim), " +
                        "nhiet_do_c = COALESCE(?, nhiet_do_c), " +
                        "nhip_tho = COALESCE(?, nhip_tho), " +
                        "can_nang_kg = COALESCE(?, can_nang_kg), " +
                        "bmi = COALESCE(?, bmi), " +
                        "hba1c_percent = COALESCE(?, hba1c_percent), " +
                        "cholesterol_mmol = COALESCE(?, cholesterol_mmol), " +
                        "triglyceride_mmol = COALESCE(?, triglyceride_mmol), " +
                        "carbs_g = COALESCE(?, carbs_g), " +
                        "lieu_luong_insulin_ui = COALESCE(?, lieu_luong_insulin_ui), " +
                        "loai_insulin_tiem = COALESCE(?, loai_insulin_tiem), " +
                        "ghi_chu = COALESCE(?, ghi_chu), " +
                        "wbc = COALESCE(?, wbc), " +
                        "rbc = COALESCE(?, rbc), " +
                        "hgb = COALESCE(?, hgb), " +
                        "hct = COALESCE(?, hct), " +
                        "plt = COALESCE(?, plt) " +
                        "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, doctorId);
            ps.setTimestamp(idx++, Timestamp.valueOf(visitTime));
            idx = bindSnapshotMetrics(ps, idx, form);
            ps.setString(idx, recordId);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("UPDATE health_records affected " + rows + " row(s) id=" + recordId);
            }
        } catch (SQLException ex) {
            SqlDiagnostics.log(LOG, Level.SEVERE, "UPDATE health_records snapshot", sql,
                    new Object[]{recordId, doctorId, visitTime}, ex);
            throw ex;
        }
    }

    private int bindSnapshotMetrics(PreparedStatement ps, int startIndex, EncounterCreateRequest form)
            throws SQLException {
        EncounterType type = form.resolveEncounterType();
        int idx = startIndex;

        if (type.isTaiKhamNoiTiet()) {
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
            JdbcUtil.setString(ps, idx++, form.getGhiChuSucKhoe());
            return bindBloodCountMetrics(ps, idx, form);
        }

        JdbcUtil.setDouble(ps, idx++, form.getDuongHuyetMgdl());
        JdbcUtil.setString(ps, idx++, form.getThoiDiemDoDuong());
        JdbcUtil.setInteger(ps, idx++, form.getHuyetApTamThu());
        JdbcUtil.setInteger(ps, idx++, form.getHuyetApTamTruong());
        JdbcUtil.setInteger(ps, idx++, form.getNhipTim());
        JdbcUtil.setDouble(ps, idx++, form.getNhietDoC());
        JdbcUtil.setInteger(ps, idx++, form.getNhipTho());
        JdbcUtil.setDouble(ps, idx++, form.getCanNangKg());
        JdbcUtil.setDouble(ps, idx++, form.getBmi());

        Double hba1c = form.getHba1cPercent();
        Double cholesterol = form.getCholesterolMmol();
        Double triglyceride = form.getTriglycerideMmol();
        if (type.isSinhHoaMau()) {
            if (hba1c == null) {
                hba1c = form.getLabHba1c();
            }
            if (cholesterol == null) {
                cholesterol = form.getLabCholesterol();
            }
            if (triglyceride == null) {
                triglyceride = form.getLabTriglyceride();
            }
        }
        JdbcUtil.setDouble(ps, idx++, hba1c);
        JdbcUtil.setDouble(ps, idx++, cholesterol);
        JdbcUtil.setDouble(ps, idx++, triglyceride);

        JdbcUtil.setDouble(ps, idx++, form.getCarbsG());
        JdbcUtil.setInteger(ps, idx++, form.getLieuLuongInsulinUi());
        JdbcUtil.setString(ps, idx++, form.getLoaiInsulinTiem());
        JdbcUtil.setString(ps, idx++, resolveGhiChu(form, type));
        return bindBloodCountMetrics(ps, idx, form);
    }

    private int bindBloodCountMetrics(PreparedStatement ps, int startIndex, EncounterCreateRequest form)
            throws SQLException {
        int idx = startIndex;
        JdbcUtil.setNullableDouble(ps, idx++, form.getLabWbc());
        JdbcUtil.setNullableDouble(ps, idx++, form.getLabRbc());
        JdbcUtil.setNullableDouble(ps, idx++, form.getLabHgb());
        JdbcUtil.setNullableDouble(ps, idx++, form.getLabHct());
        JdbcUtil.setNullableDouble(ps, idx++, form.getLabPlt());
        return idx;
    }

    private String resolveGhiChu(EncounterCreateRequest form, EncounterType type) {
        if (type.isMauTongQuat() || type.isSinhHoaMau()) {
            return firstNonBlank(form.getLabGhiChu(), form.getGhiChuSucKhoe());
        }
        return form.getGhiChuSucKhoe();
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
        HealthRecord hr = mapHealthRecordFromBaseRow(rs, resolvePatientId(rs));

        Patient patient = hr.getPatient() != null ? hr.getPatient() : new Patient();
        patient.setId(firstNonBlank(patient.getId(), resolvePatientId(rs)));
        String patientCode = optionalString(rs, "patient_code");
        if (patientCode != null && !patientCode.isBlank()) {
            patient.setPatientCode(patientCode);
        } else {
            try {
                patient.setPatientCode(PatientDAO.resolveCode(rs, "patient_code"));
            } catch (SQLException ignored) {
                // optional enrichment only
            }
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

    private HealthRecord mapPatientHealthRecord(ResultSet rs) throws SQLException {
        HealthRecord hr = mapSnapshotHealthRecord(rs);

        User nhapBoi = new User();
        String nhapBoiId = firstNonBlank(
                optionalString(rs, "nhap_boi_user_id"),
                optionalString(rs, "hr_nhap_boi")
        );
        if (nhapBoiId != null && !nhapBoiId.isBlank()) {
            try {
                nhapBoi.setId(java.util.UUID.fromString(nhapBoiId.trim()));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid UUID
            }
        }
        nhapBoi.setHoTen(optionalString(rs, "nhap_boi_ho_ten"));
        hr.setNhapBoi(nhapBoi);

        User patientUser = new User();
        patientUser.setHoTen(firstNonBlank(optionalString(rs, "patient_ho_ten"), ""));
        if (hr.getPatient() != null) {
            hr.getPatient().setUser(patientUser);
        }
        return hr;
    }

    private HealthRecord mapSnapshotHealthRecord(ResultSet rs) throws SQLException {
        HealthRecord hr = new HealthRecord();
        hr.setId(optionalString(rs, "hr_id"));
        hr.setEncounterId(optionalString(rs, "hr_encounter_id"));
        hr.setLastEncounterId(optionalString(rs, "hr_last_encounter_id"));
        hr.setHealthRecordId(resolveHealthRecordCode(rs));
        hr.setChieuCaoCm(optionalDouble(rs, "hr_chieu_cao_cm"));
        hr.setDuongHuyetMgdl(optionalDouble(rs, "hr_duong_huyet_mgdl"));
        hr.setThoiDiemDoDuong(optionalString(rs, "hr_thoi_diem_do_duong"));
        hr.setHuyetApTamThu(optionalInt(rs, "hr_huyet_ap_tam_thu"));
        hr.setHuyetApTamTruong(optionalInt(rs, "hr_huyet_ap_tam_truong"));
        hr.setNhipTim(optionalInt(rs, "hr_nhip_tim"));
        hr.setNhietDoC(optionalDouble(rs, "hr_nhiet_do_c"));
        hr.setNhipTho(optionalInt(rs, "hr_nhip_tho"));
        hr.setCanNangKg(optionalDouble(rs, "hr_can_nang_kg"));
        hr.setBmi(optionalDouble(rs, "hr_bmi"));
        hr.setHba1cPercent(optionalDouble(rs, "hr_hba1c_percent"));
        hr.setCholesterolMmol(optionalDouble(rs, "hr_cholesterol_mmol"));
        hr.setTriglycerideMmol(optionalDouble(rs, "hr_triglyceride_mmol"));
        hr.setHdlMmol(optionalDouble(rs, "hr_hdl_mmol"));
        hr.setLdlMmol(optionalDouble(rs, "hr_ldl_mmol"));
        hr.setWbc(optionalDouble(rs, "hr_wbc"));
        hr.setRbc(optionalDouble(rs, "hr_rbc"));
        hr.setHgb(optionalDouble(rs, "hr_hgb"));
        hr.setHct(optionalDouble(rs, "hr_hct"));
        hr.setPlt(optionalDouble(rs, "hr_plt"));
        hr.setAst(optionalDouble(rs, "hr_ast"));
        hr.setAlt(optionalDouble(rs, "hr_alt"));
        hr.setUre(optionalDouble(rs, "hr_ure"));
        hr.setCreatinine(optionalDouble(rs, "hr_creatinine"));
        hr.setTrieuChung(optionalString(rs, "hr_trieu_chung"));
        hr.setTienSuBenh(optionalString(rs, "hr_tien_su_benh"));
        hr.setKhamLamSang(optionalString(rs, "hr_kham_lam_sang"));
        hr.setChanDoanChinh(optionalString(rs, "hr_chan_doan_chinh"));
        hr.setChanDoanPhu(optionalString(rs, "hr_chan_doan_phu"));
        hr.setPhanLoaiTieuDuong(optionalString(rs, "hr_phan_loai_tieu_duong"));
        hr.setHuongXuTri(optionalString(rs, "hr_huong_xu_tri"));
        hr.setKhuyenNghiDieuTri(optionalString(rs, "hr_khuyen_nghi_dieu_tri"));
        hr.setCheDoAn(optionalString(rs, "hr_che_do_an"));
        hr.setLuyenTap(optionalString(rs, "hr_luyen_tap"));
        hr.setKhuyenNghi(firstNonBlank(
                optionalString(rs, "hr_khuyen_nghi_dieu_tri"),
                optionalString(rs, "hr_huong_xu_tri")
        ));
        hr.setSoBuocChan(optionalInt(rs, "hr_so_buoc_chan"));
        hr.setCarbsG(optionalDouble(rs, "hr_carbs_g"));
        hr.setSoGioNgu(optionalDouble(rs, "hr_so_gio_ngu"));
        hr.setLieuLuongInsulinUi(optionalInt(rs, "hr_lieu_luong_insulin_ui"));
        hr.setLoaiInsulinTiem(optionalString(rs, "hr_loai_insulin_tiem"));
        hr.setGhiChu(optionalString(rs, "hr_ghi_chu"));
        hr.setChestPain(optionalBoolean(rs, "hr_chest_pain"));
        hr.setDizziness(optionalBoolean(rs, "hr_dizziness"));
        hr.setFatigue(optionalBoolean(rs, "hr_fatigue"));

        Timestamp measuredAt = optionalTimestamp(rs, "hr_thoi_gian_do");
        if (measuredAt != null) {
            hr.setThoiGianDo(measuredAt.toLocalDateTime());
            LocalDate lastVisitDate = measuredAt.toLocalDateTime().toLocalDate();
            hr.setDaysSinceLastVisit((int) ChronoUnit.DAYS.between(lastVisitDate, LocalDate.now()));
        }

        Timestamp createdAt = optionalTimestamp(rs, "hr_ngay_tao");
        if (createdAt != null) {
            hr.setNgayTao(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = optionalTimestamp(rs, "hr_ngay_cap_nhat");
        if (updatedAt != null) {
            hr.setNgayCapNhat(updatedAt.toLocalDateTime());
        }

        Patient patient = new Patient();
        patient.setId(resolvePatientId(rs));
        patient.setPatientCode(PatientDAO.resolveCode(rs, "p_patient_code"));
        patient.setLoaiTieuDuong(optionalString(rs, "p_loai_tieu_duong"));
        hr.setPatient(patient);
        return hr;
    }

    private String resolveHealthRecordCode(ResultSet rs) throws SQLException {
        String code = optionalString(rs, "hr_health_record_code");
        if (code != null && !code.isBlank()) {
            return code;
        }
        String id = optionalString(rs, "hr_id");
        return id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "N/A";
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
