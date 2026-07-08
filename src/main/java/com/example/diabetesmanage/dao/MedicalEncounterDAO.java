package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.util.EncounterClinicalJson;
import com.example.diabetesmanage.util.SqlDiagnostics;

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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MedicalEncounterDAO {

    private static final Logger LOG = Logger.getLogger(MedicalEncounterDAO.class.getName());

    public List<MedicalEncounter> findByPatientId(String patientId) {
        return findByPatientIdScoped(patientId, null);
    }

    public List<MedicalEncounter> findByPatientIdScoped(String patientId, String scopeDoctorId) {
        List<MedicalEncounter> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM + "WHERE me.patient_id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }
        sql.append("ORDER BY me.ngay_kham DESC");

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
                list.add(mapWithPatient(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, List<MedicalEncounter>> getRecentEncountersGroupedByPatient(
            String scopeDoctorId,
            int maxRecordsPerPatient
    ) {
        Map<String, List<MedicalEncounter>> grouped = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");
        sql.append("ORDER BY p.id, me.ngay_kham DESC");

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
                List<MedicalEncounter> encounters = grouped.computeIfAbsent(patientId, k -> new ArrayList<>());
                if (encounters.size() >= maxRecordsPerPatient) {
                    continue;
                }
                encounters.add(mapWithPatient(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grouped;
    }

    private static final String ENCOUNTER_SELECT =
            "SELECT me.*, p.patient_code, u.ho_ten AS patient_name, bs.ho_ten AS doctor_name ";

    private static final String ENCOUNTER_FROM =
            "FROM medical_encounters me " +
                    "JOIN patients p ON me.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "LEFT JOIN users bs ON me.bac_si_id = bs.id ";

    /**
     * Bộ lọc hồ sơ khám bệnh dùng chung. Điều kiện SQL chỉ được thêm khi tham số có giá trị
     * (khoảng ngày, từ khóa, bệnh nhân). Loại hồ sơ và trạng thái không phải cột thật trong
     * medical_encounters (loại suy ra từ kham_lam_sang, trạng thái luôn "da_kham") nên được lọc
     * theo đúng giá trị đã resolve của từng bản ghi.
     */
    public List<MedicalEncounter> searchEncounters(
            String scopeDoctorId, String startDate, String endDate,
            String keyword, String encounterType, String status, String patientId
    ) {
        boolean hasDate = startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasPatient = patientId != null && !patientId.isBlank();

        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM);
        sql.append(scopeDoctorId == null ? "WHERE 1=1 " : "WHERE p.bac_si_id = ? ");
        if (hasPatient) {
            sql.append("AND me.patient_id = ? ");
        }
        if (hasDate) {
            sql.append("AND DATE(me.ngay_kham) BETWEEN ? AND ? ");
        }
        if (hasKeyword) {
            sql.append("AND (me.encounter_code LIKE ? OR p.patient_code LIKE ? OR u.ho_ten LIKE ? " +
                    "OR me.ly_do_kham LIKE ? OR me.chan_doan_chinh LIKE ?) ");
        }
        sql.append("ORDER BY me.ngay_kham DESC");

        EncounterType typeFilter = EncounterType.fromCodeOrNull(encounterType);
        boolean hasStatus = status != null && !status.isBlank();

        List<MedicalEncounter> list = new ArrayList<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            int idx = 1;
            if (scopeDoctorId != null) {
                ps.setString(idx++, scopeDoctorId);
            }
            if (hasPatient) {
                ps.setString(idx++, patientId.trim());
            }
            if (hasDate) {
                ps.setString(idx++, startDate);
                ps.setString(idx++, endDate);
            }
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                for (int i = 0; i < 5; i++) {
                    ps.setString(idx++, like);
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MedicalEncounter enc = mapWithPatient(rs);
                if (typeFilter != null && enc.getEncounterType() != typeFilter) {
                    continue;
                }
                if (hasStatus && !status.equalsIgnoreCase(enc.getTrangThai())) {
                    continue;
                }
                list.add(enc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public MedicalEncounter getEncounterById(String encounterId, String scopeDoctorId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM + "WHERE me.id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND (p.bac_si_id = ? OR me.bac_si_id = ?) ");
        }
        sql.append("LIMIT 1");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, encounterId);
            if (scopeDoctorId != null) {
                ps.setString(2, scopeDoctorId);
                ps.setString(3, scopeDoctorId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapWithPatient(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean encounterExists(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM medical_encounters WHERE id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPatientIdByEncounterId(String encounterId) {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }
        String sql = "SELECT patient_id FROM medical_encounters WHERE id = ?";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("patient_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String resolveEncounterCode(ResultSet rs) throws SQLException {
        String code = rs.getString("encounter_code");
        if (code != null && !code.isBlank()) {
            return code;
        }
        String id = rs.getString("id");
        if (id == null || id.isBlank()) {
            return "N/A";
        }
        String compact = id.replace("-", "").toUpperCase();
        if (compact.length() > 8) {
            compact = compact.substring(0, 8);
        }
        return "EC-" + compact;
    }

    public List<Map<String, String>> getMedicationDetailsByEncounterId(String encounterId) {
        List<Map<String, String>> list = new ArrayList<>();
        if (encounterId == null || encounterId.isBlank()) {
            return list;
        }

        String sql =
                "SELECT m.ten_thuoc, m.hoat_chat, m.lieu_luong, m.don_vi, m.tan_suat, m.duong_dung, " +
                        "m.thoi_diem_uong, m.thoi_gian_dung_ngay, m.ghi_chu " +
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
                Map<String, String> med = new LinkedHashMap<>();
                med.put("name", firstNonBlank(rs.getString("ten_thuoc"), "—"));
                med.put("ingredient", firstNonBlank(rs.getString("hoat_chat"), "—"));
                String dose = rs.getString("lieu_luong");
                String unit = rs.getString("don_vi");
                med.put("dose", dose != null && !dose.isBlank() ? dose : "—");
                med.put("unit", unit != null && !unit.isBlank() ? unit : "—");
                med.put("frequency", firstNonBlank(rs.getString("tan_suat"), "—"));
                med.put("route", firstNonBlank(rs.getString("duong_dung"), "—"));
                Integer days = rs.getObject("thoi_gian_dung_ngay") != null
                        ? rs.getInt("thoi_gian_dung_ngay") : null;
                med.put("days", days != null ? String.valueOf(days) : "—");
                med.put("usage", firstNonBlank(rs.getString("thoi_diem_uong"), "—"));
                med.put("note", firstNonBlank(rs.getString("ghi_chu"), "—"));
                list.add(med);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /** Schema cố định theo diabcare_db — không chạy DDL runtime. */
    public void prepareSchema() {
        // no-op
    }

    /**
     * Validates required INSERT fields before SQL execution.
     */
    public void validateInsertFields(EncounterCreateRequest form, String doctorId) throws SQLException {
        String patientId = requireUuidValue("patient_id", form.getPatientId());
        String bacSiId = requireUuidValue("bac_si_id", doctorId);
        rejectDisplayCode("patient_id", patientId);
        rejectDisplayCode("bac_si_id", bacSiId);
        if (!patientExists(patientId)) {
            throw new SQLException("patient_id does not exist in patients table: " + patientId);
        }
        if (!userExists(bacSiId)) {
            throw new SQLException("bac_si_id does not exist in users table: " + bacSiId);
        }
        String chanDoanChinh = resolveChanDoanChinh(form);
        if (chanDoanChinh == null || chanDoanChinh.isBlank()) {
            throw new SQLException("chan_doan_chinh must not be null");
        }
        LOG.log(Level.INFO, "validateInsertFields OK patient_id={0} bac_si_id={1}",
                new Object[]{patientId, bacSiId});
    }

    public boolean existsById(String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return false;
        }
        try (Connection con = DBContext.getConnection()) {
            if (con == null) {
                throw new SQLException("Không thể kết nối database");
            }
            return existsById(con, encounterId);
        }
    }

    public boolean existsById(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM medical_encounters WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Insert encounter trong transaction. bac_si_id lấy từ session (doctorId).
     */
    public String insert(Connection con, EncounterCreateRequest form, String doctorId) throws SQLException {
        if (con == null) {
            throw new SQLException("Connection is required for medical_encounters INSERT");
        }

        validateInsertFields(form, doctorId);

        String id = UUID.randomUUID().toString();
        String patientId = requireUuidValue("patient_id", form.getPatientId());
        String bacSiId = requireUuidValue("bac_si_id", doctorId);
        String khamLamSangJson = EncounterClinicalJson.buildFromForm(form);
        String lyDoKham = resolveLyDoKham(form);
        String quaTrinhBenhLy = form.getQuaTrinhBenhLy();
        String chanDoanChinh = resolveChanDoanChinh(form);
        String chanDoanPhu = form.getChanDoanPhu();
        String huongXuTri = form.getHuongXuTri();
        Timestamp ngayKham = Timestamp.valueOf(form.resolveNgayKham());

        logInsertParameters(
                id, patientId, bacSiId, ngayKham, lyDoKham, quaTrinhBenhLy, khamLamSangJson,
                chanDoanChinh, chanDoanPhu, huongXuTri);

        String sql =
                "INSERT INTO medical_encounters " +
                        "(id, patient_id, bac_si_id, ngay_kham, ly_do_kham, qua_trinh_benh_ly, kham_lam_sang, " +
                        "chan_doan_chinh, chan_doan_phu, huong_xu_tri, ngay_tao) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, bacSiId);
            ps.setTimestamp(4, ngayKham);
            JdbcUtil.setString(ps, 5, lyDoKham);
            JdbcUtil.setString(ps, 6, quaTrinhBenhLy);
            JdbcUtil.setString(ps, 7, khamLamSangJson);
            JdbcUtil.setString(ps, 8, chanDoanChinh);
            JdbcUtil.setString(ps, 9, chanDoanPhu);
            JdbcUtil.setString(ps, 10, huongXuTri);
            ps.setTimestamp(11, ngayKham);

            int rows = ps.executeUpdate();
            LOG.log(Level.INFO,
                    "INSERT medical_encounters executeUpdate patient_id={0} bac_si_id={1} rows={2}",
                    new Object[]{patientId, bacSiId, rows});
            if (rows <= 0) {
                throw new SQLException("INSERT medical_encounters affected " + rows + " row(s), expected 1");
            }
            if (!existsById(con, id)) {
                throw new SQLException("INSERT medical_encounters verification failed for id=" + id);
            }
        } catch (SQLException ex) {
            SqlDiagnostics.log(LOG, Level.SEVERE, "INSERT medical_encounters", sql,
                    new Object[]{id, patientId, bacSiId, ngayKham, lyDoKham, chanDoanChinh}, ex);
            throw ex;
        }
        LOG.log(Level.INFO,
                "INSERT medical_encounters succeeded id={0} patient_id={1} bac_si_id={2}",
                new Object[]{id, patientId, bacSiId});
        return id;
    }

    private void logInsertParameters(
            String id,
            String patientId,
            String bacSiId,
            Timestamp ngayKham,
            String lyDoKham,
            String quaTrinhBenhLy,
            String khamLamSangJson,
            String chanDoanChinh,
            String chanDoanPhu,
            String huongXuTri
    ) {
        LOG.log(Level.INFO,
                "INSERT medical_encounters parameters: id={0}, patient_id={1}, bac_si_id={2}, ngay_kham={3}, "
                        + "ly_do_kham={4}, qua_trinh_benh_ly={5}, kham_lam_sang={6}, chan_doan_chinh={7}, "
                        + "chan_doan_phu={8}, huong_xu_tri={9}, ngay_tao={10}",
                new Object[] {
                        id, patientId, bacSiId, ngayKham, lyDoKham, quaTrinhBenhLy, khamLamSangJson,
                        chanDoanChinh, chanDoanPhu, huongXuTri, ngayKham
                });
    }

    private void requireUuid(String fieldName, String value) throws SQLException {
        requireUuidValue(fieldName, value);
    }

    private String requireUuidValue(String fieldName, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            throw new SQLException(fieldName + " is required");
        }
        String trimmed = value.trim();
        rejectDisplayCode(fieldName, trimmed);
        try {
            UUID.fromString(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new SQLException(fieldName + " must be a valid UUID: " + trimmed, ex);
        }
        return trimmed;
    }

    private void rejectDisplayCode(String fieldName, String value) throws SQLException {
        if (value != null && value.matches("(?i)(PAT|HR|ENC|LAB)-\\d+")) {
            throw new SQLException(fieldName + " must be UUID, not display code: " + value);
        }
    }

    private boolean patientExists(String patientId) throws SQLException {
        String sql = "SELECT 1 FROM patients WHERE id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            if (con == null) {
                throw new SQLException("Không thể kết nối database");
            }
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean userExists(String userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id = ? LIMIT 1";
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            if (con == null) {
                throw new SQLException("Không thể kết nối database");
            }
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tạo encounter khi hoàn thành lịch hẹn (schema diabcare_db).
     */
    public String insertFromAppointment(
            Connection con,
            String appointmentId,
            String patientId,
            String doctorId,
            LocalDateTime visitDate,
            String lyDoKham
    ) throws SQLException {
        String id = java.util.UUID.randomUUID().toString();
        String chanDoan = (lyDoKham != null && !lyDoKham.isBlank()) ? lyDoKham : "Đang cập nhật";
        String khamLamSangJson =
                "{\"loai_encounter\":\"" + EncounterType.TAI_KHAM_NOI_TIET.getCode() + "\"}";
        String sql =
                "INSERT INTO medical_encounters " +
                        "(id, patient_id, bac_si_id, ngay_kham, ly_do_kham, kham_lam_sang, " +
                        "chan_doan_chinh, ngay_tao) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId);
            ps.setString(3, doctorId);
            ps.setTimestamp(4, Timestamp.valueOf(visitDate));
            JdbcUtil.setString(ps, 5, lyDoKham);
            ps.setString(6, khamLamSangJson);
            ps.setString(7, chanDoan);
            ps.setTimestamp(8, Timestamp.valueOf(visitDate));
            ps.executeUpdate();
        }
        return id;
    }

    /** Bảng medical_encounters không có cột appointment_id trong schema hiện tại. */
    public boolean existsByAppointmentId(String appointmentId) {
        return false;
    }

    private String resolveChanDoanChinh(EncounterCreateRequest form) {
        if (form.getChanDoanChinh() != null && !form.getChanDoanChinh().isBlank()) {
            return form.getChanDoanChinh();
        }
        if (!form.resolveEncounterType().isTaiKhamNoiTiet()) {
            return form.resolveEncounterType().getLabel();
        }
        return null;
    }

    private String resolveLyDoKham(EncounterCreateRequest form) {
        if (form.getLyDoKham() != null && !form.getLyDoKham().isBlank()) {
            return form.getLyDoKham();
        }
        if (form.getTrieuChung() != null && !form.getTrieuChung().isBlank()) {
            return form.getTrieuChung();
        }
        return form.resolveEncounterType().getLabel();
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
        Map<String, String> advice = getPrescriptionAdviceByEncounterId(encounterId);
        addIfPresent(list, advice.get("huong_dieu_tri"));
        addIfPresent(list, advice.get("che_do_an"));
        addIfPresent(list, advice.get("luyen_tap"));
        addIfPresent(list, advice.get("ghi_chu"));
        return list;
    }

    public Map<String, String> getPrescriptionAdviceByEncounterId(String encounterId) {
        Map<String, String> advice = new LinkedHashMap<>();
        if (encounterId == null || encounterId.isBlank()) {
            return advice;
        }

        String sql =
                "SELECT huong_dieu_tri, che_do_an, luyen_tap, ghi_chu " +
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
                putIfPresent(advice, "huong_dieu_tri", rs.getString("huong_dieu_tri"));
                putIfPresent(advice, "che_do_an", rs.getString("che_do_an"));
                putIfPresent(advice, "luyen_tap", rs.getString("luyen_tap"));
                putIfPresent(advice, "ghi_chu", rs.getString("ghi_chu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return advice;
    }

    private void putIfPresent(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }

    /**
     * Bước 2 (Treatment Plan): cập nhật chẩn đoán và hướng xử trí cho encounter đã tồn tại.
     * KHÔNG tạo lại encounter. chan_doan_chinh là NOT NULL nên chỉ ghi khi có giá trị.
     */
    public void updateTreatmentPlan(
            Connection con,
            String encounterId,
            String chanDoanChinh,
            String chanDoanPhu,
            String huongXuTri
    ) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            throw new SQLException("encounter_id is required for updateTreatmentPlan");
        }
        String diagnosis = (chanDoanChinh != null && !chanDoanChinh.isBlank())
                ? chanDoanChinh.trim() : "Đang cập nhật";
        String sql =
                "UPDATE medical_encounters " +
                        "SET chan_doan_chinh = ?, chan_doan_phu = ?, huong_xu_tri = ? " +
                        "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, diagnosis);
            JdbcUtil.setString(ps, 2, chanDoanPhu);
            JdbcUtil.setString(ps, 3, huongXuTri);
            ps.setString(4, encounterId.trim());
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("UPDATE medical_encounters affected 0 rows for id=" + encounterId);
            }
        }
    }

    public void deleteById(Connection con, String encounterId) throws SQLException {
        String sql = "DELETE FROM medical_encounters WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ps.executeUpdate();
        }
    }

    public MedicalEncounter getLatestByPatientId(String patientId) {
        return getLatestByPatientId(patientId, null);
    }

    public MedicalEncounter getLatestByPatientId(String patientId, String scopeDoctorId) {
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM + "WHERE me.patient_id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND p.bac_si_id = ? ");
        }
        sql.append("ORDER BY me.ngay_kham DESC LIMIT 1");

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
                return mapWithPatient(rs);
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
        enc.setDisplayCode(resolveEncounterCode(rs));
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
        String khamLamSang = rs.getString("kham_lam_sang");
        enc.setKhamLamSang(khamLamSang);

        Timestamp created = optionalTimestamp(rs, "ngay_tao");
        if (created == null) {
            created = optionalTimestamp(rs, "created_at");
        }
        if (created != null) {
            enc.setNgayTao(created.toLocalDateTime());
        }

        String loaiFromJson = EncounterClinicalJson.parseString(khamLamSang, "loai_encounter");
        String loaiFromColumn = optionalString(rs, "loai_encounter");
        enc.setLoaiEncounter(EncounterType.resolveTypeCode(
                loaiFromColumn, loaiFromJson, enc.getChanDoanChinh(), enc.getLyDoKham()));
        enc.setTrangThai("da_kham");
        return enc;
    }

    private MedicalEncounter mapWithPatient(ResultSet rs) throws SQLException {
        MedicalEncounter enc = map(rs);
        enc.setPatientCode(rs.getString("patient_code"));
        enc.setPatientName(rs.getString("patient_name"));
        enc.setDoctorName(rs.getString("doctor_name"));
        return enc;
    }

    private String optionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private Timestamp optionalTimestamp(ResultSet rs, String column) {
        try {
            return rs.getTimestamp(column);
        } catch (SQLException ex) {
            return null;
        }
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
