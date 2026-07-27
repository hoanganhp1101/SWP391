package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicalEncounterDAO {
    private static final String ENCOUNTER_SELECT =
            "SELECT me.id AS encounter_id, me.patient_id, me.bac_si_id, me.ngay_kham, me.ly_do_kham, "
                    + "me.qua_trinh_benh_ly, me.kham_lam_sang, me.chan_doan_chinh, me.chan_doan_phu, me.huong_xu_tri, "
                    + "me.ngay_tao, me.encounter_code, "
                    + "p.patient_code, pu.ho_ten AS patient_name, du.ho_ten AS doctor_name ";

    private static final String ENCOUNTER_FROM =
            "FROM medical_encounters me " +
                    "JOIN patients p ON me.patient_id = p.id " +
                    "JOIN users pu ON p.id = pu.id " +
                    "LEFT JOIN doctors bs ON me.bac_si_id = bs.id " +
                    "LEFT JOIN users du ON bs.id = du.id ";

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
            sql.append("AND (me.encounter_code LIKE ? OR p.patient_code LIKE ? OR pu.ho_ten LIKE ? " +
                    "OR me.ly_do_kham LIKE ? OR me.chan_doan_chinh LIKE ?) ");
        }
        sql.append("ORDER BY me.ngay_kham DESC, me.ngay_tao DESC, me.id DESC");

        String typeFilter = canonicalTypeCodeOrNull(encounterType);
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
                if (typeFilter != null && !typeFilter.equalsIgnoreCase(enc.getLoaiEncounter())) {
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

    /** Encounter mới nhất của bệnh nhân, sort ngay_kham rồi ngay_tao để không lấy nhầm bản cũ. */
    public MedicalEncounter getLatestEncounterByPatient(String patientId, String scopeDoctorId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT + ENCOUNTER_FROM
                + "WHERE me.patient_id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND (p.bac_si_id = ? OR me.bac_si_id = ?) ");
        }
        sql.append("ORDER BY me.ngay_kham DESC, me.ngay_tao DESC, me.id DESC LIMIT 1");

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setString(1, patientId.trim());
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

    /**
     * Lịch sử khám của bệnh nhân (mới → cũ), kèm đường huyết/HbA1c theo từng lần khám.
     */
    public List<MedicalEncounter> getHistoryByPatientId(String patientId, String scopeDoctorId) {
        return queryPatientHistory(patientId, scopeDoctorId, null, null);
    }

    public List<MedicalEncounter> getHistoryByPatientAndDateRange(
            String patientId, String scopeDoctorId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return getHistoryByPatientId(patientId, scopeDoctorId);
        }
        return queryPatientHistory(patientId, scopeDoctorId, from, to);
    }

    private List<MedicalEncounter> queryPatientHistory(
            String patientId, String scopeDoctorId, LocalDate from, LocalDate to) {
        if (patientId == null || patientId.isBlank()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder(ENCOUNTER_SELECT)
                .append(", hr.duong_huyet_mgdl AS hr_duong_huyet, hr.hba1c_percent AS hr_hba1c ")
                .append(", lr.glucose_mau AS lab_glucose_mau, lr.hba1c AS lab_hba1c ")
                .append(ENCOUNTER_FROM)
                .append("LEFT JOIN health_records hr ON hr.encounter_id = me.id ")
                .append("LEFT JOIN lab_results lr ON lr.encounter_id = me.id ")
                .append("WHERE me.patient_id = ? ");
        if (scopeDoctorId != null) {
            sql.append("AND (p.bac_si_id = ? OR me.bac_si_id = ?) ");
        }
        if (from != null && to != null) {
            sql.append("AND DATE(me.ngay_kham) BETWEEN ? AND ? ");
        }
        sql.append("ORDER BY me.ngay_kham DESC, me.ngay_tao DESC, me.id DESC");

        List<MedicalEncounter> list = new ArrayList<>();
        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            int idx = 1;
            ps.setString(idx++, patientId.trim());
            if (scopeDoctorId != null) {
                ps.setString(idx++, scopeDoctorId);
                ps.setString(idx++, scopeDoctorId);
            }
            if (from != null && to != null) {
                ps.setDate(idx++, java.sql.Date.valueOf(from));
                ps.setDate(idx, java.sql.Date.valueOf(to));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MedicalEncounter enc = mapWithPatient(rs);
                enc.setDuongHuyetMgdl(resolveHistoryGlucose(rs));
                enc.setHba1cPercent(resolveHistoryHba1c(rs));
                list.add(enc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Double resolveHistoryGlucose(ResultSet rs) throws SQLException {
        Double fromHealth = optionalDouble(rs, "hr_duong_huyet");
        if (fromHealth != null) {
            return fromHealth;
        }
        Double labMmol = optionalDouble(rs, "lab_glucose_mau");
        if (labMmol == null) {
            return null;
        }
        return Math.round(labMmol * 18.0182 * 10.0) / 10.0;
    }

    private static Double resolveHistoryHba1c(ResultSet rs) throws SQLException {
        Double fromHealth = optionalDouble(rs, "hr_hba1c");
        if (fromHealth != null) {
            return fromHealth;
        }
        return optionalDouble(rs, "lab_hba1c");
    }

    private static Double optionalDouble(ResultSet rs, String column) throws SQLException {
        try {
            Object value = rs.getObject(column);
            if (value == null) {
                return null;
            }
            return rs.getDouble(column);
        } catch (SQLException ex) {
            return null;
        }
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
        String id = rs.getString("encounter_id");
        if (id == null || id.isBlank()) {
            id = rs.getString("id");
        }
        if (id == null || id.isBlank()) {
            return "N/A";
        }
        String compact = id.replace("-", "").toUpperCase();
        if (compact.length() > 8) {
            compact = compact.substring(0, 8);
        }
        return "EC-" + compact;
    }

    /**
     * Validates required INSERT fields before SQL execution.
     */
    public void validateInsertFields(EncounterCreateDTO form, String doctorId) throws SQLException {
        String patientId = requireUuidValue("patient_id", form.getPatientId());
        String bacSiId = requireUuidValue("bac_si_id", doctorId);
        rejectDisplayCode("patient_id", patientId);
        rejectDisplayCode("bac_si_id", bacSiId);
        if (!patientExists(patientId)) {
            throw new SQLException("patient_id does not exist in patients table: " + patientId);
        }
        if (!userExists(bacSiId)) {
            throw new SQLException("bac_si_id does not exist in doctors table: " + bacSiId);
        }
        String chanDoanChinh = resolveChanDoanChinh(form);
        if (chanDoanChinh == null || chanDoanChinh.isBlank()) {
            throw new SQLException("chan_doan_chinh must not be null");
        }
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
    public String insert(Connection con, EncounterCreateDTO form, String doctorId) throws SQLException {
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
            if (rows <= 0) {
                throw new SQLException("INSERT medical_encounters affected " + rows + " row(s), expected 1");
            }
            if (!existsById(con, id)) {
                throw new SQLException("INSERT medical_encounters verification failed for id=" + id);
            }
        } catch (SQLException ex) {
            throw ex;
        }
        return id;
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
        String sql = "SELECT 1 FROM doctors WHERE id = ? LIMIT 1";
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

    private String resolveChanDoanChinh(EncounterCreateDTO form) {
        if (form.getChanDoanChinh() != null && !form.getChanDoanChinh().isBlank()) {
            return form.getChanDoanChinh();
        }
        if (!form.isTaiKhamNoiTiet()) {
            return encounterTypeLabel(form.resolveEncounterType());
        }
        return null;
    }

    private String resolveLyDoKham(EncounterCreateDTO form) {
        if (form.getLyDoKham() != null && !form.getLyDoKham().isBlank()) {
            return form.getLyDoKham();
        }
        if (form.getTrieuChung() != null && !form.getTrieuChung().isBlank()) {
            return form.getTrieuChung();
        }
        return encounterTypeLabel(form.resolveEncounterType());
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

    private MedicalEncounter map(ResultSet rs) throws SQLException {
        MedicalEncounter enc = new MedicalEncounter();
        String encounterId = rs.getString("encounter_id");
        if (encounterId == null || encounterId.isBlank()) {
            encounterId = rs.getString("id");
        }
        enc.setId(encounterId);
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
        enc.setLoaiEncounter(resolveTypeCode(
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
    public MedicalEncounter getEncounterById(Connection con, String encounterId) throws SQLException {
        if (encounterId == null || encounterId.isBlank()) {
            return null;
        }

        String sql = ENCOUNTER_SELECT + ENCOUNTER_FROM + "WHERE me.id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encounterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapWithPatient(rs);
            }
        }
        return null;
    }

    /** Chuẩn hóa mã loại hồ sơ (kể cả alias cũ); trả về null nếu không khớp. */
    private static String canonicalTypeCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalized = code.trim().replace('-', '_');
        if ("tai_kham_noi_tiet".equalsIgnoreCase(normalized)
                || "internal_examination".equalsIgnoreCase(normalized)
                || "noi_tiet".equalsIgnoreCase(normalized)
                || "kham_noi_tiet".equalsIgnoreCase(normalized)) {
            return "tai_kham_noi_tiet";
        }
        if ("mau_tong_quat".equalsIgnoreCase(normalized)
                || "general_blood_test".equalsIgnoreCase(normalized)
                || "blood_test".equalsIgnoreCase(normalized)
                || "cbc".equalsIgnoreCase(normalized)) {
            return "mau_tong_quat";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(normalized)
                || "biochemistry_test".equalsIgnoreCase(normalized)
                || "biochemistry".equalsIgnoreCase(normalized)
                || "sinh_hoa".equalsIgnoreCase(normalized)) {
            return "sinh_hoa_mau";
        }
        return null;
    }

    /** Suy luận mã loại hồ sơ từ chẩn đoán / lý do khám (bản ghi cũ không có JSON loai_encounter). */
    private static String inferTypeCodeFromLabels(String... labels) {
        if (labels == null) {
            return null;
        }
        for (String label : labels) {
            if (label == null || label.isBlank()) {
                continue;
            }
            String trimmed = label.trim();
            if ("Bệnh án tái khám Nội tiết".equalsIgnoreCase(trimmed)) {
                return "tai_kham_noi_tiet";
            }
            if ("Kết quả xét nghiệm máu tổng quát".equalsIgnoreCase(trimmed)) {
                return "mau_tong_quat";
            }
            if ("Kết quả sinh hóa máu".equalsIgnoreCase(trimmed)) {
                return "sinh_hoa_mau";
            }
        }
        return null;
    }

    private static String resolveTypeCode(String columnCode, String jsonCode, String chanDoanChinh, String lyDoKham) {
        String resolved = canonicalTypeCodeOrNull(columnCode);
        if (resolved != null) {
            return resolved;
        }
        resolved = canonicalTypeCodeOrNull(jsonCode);
        if (resolved != null) {
            return resolved;
        }
        resolved = inferTypeCodeFromLabels(chanDoanChinh, lyDoKham);
        if (resolved != null) {
            return resolved;
        }
        return "tai_kham_noi_tiet";
    }

    private static String encounterTypeLabel(String typeCode) {
        if ("mau_tong_quat".equalsIgnoreCase(typeCode)) {
            return "Kết quả xét nghiệm máu tổng quát";
        }
        if ("sinh_hoa_mau".equalsIgnoreCase(typeCode)) {
            return "Kết quả sinh hóa máu";
        }
        return "Bệnh án tái khám Nội tiết";
    }

}
