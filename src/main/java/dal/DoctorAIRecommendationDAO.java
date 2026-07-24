package dal;

import config.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.AIAnalysis;

public class DoctorAIRecommendationDAO {

    private static final String FROM_JOIN = """
            FROM ai_analysis a
            JOIN patients p ON a.patient_id = p.id
            LEFT JOIN users u ON p.user_id = u.id
            """;

    private static volatile boolean statusColumnsReady = false;

    /** Tự thêm cột trang_thai / ghi_chu_bs / xu_ly_boi nếu DB chưa có. */
    public synchronized String ensureStatusColumns() {
        if (statusColumnsReady) {
            return null;
        }
        String[] statements = {
            """
            IF COL_LENGTH('dbo.ai_analysis', 'trang_thai') IS NULL
                ALTER TABLE dbo.ai_analysis ADD trang_thai NVARCHAR(30) NOT NULL
                    CONSTRAINT DF_ai_analysis_trang_thai DEFAULT N'chua_xem'
            """,
            """
            IF COL_LENGTH('dbo.ai_analysis', 'ghi_chu_bs') IS NULL
                ALTER TABLE dbo.ai_analysis ADD ghi_chu_bs NVARCHAR(MAX) NULL
            """,
            """
            IF COL_LENGTH('dbo.ai_analysis', 'xu_ly_boi') IS NULL
                ALTER TABLE dbo.ai_analysis ADD xu_ly_boi UNIQUEIDENTIFIER NULL
            """
        };
        try (Connection conn = new DBContext().getConnection();
             java.sql.Statement st = conn.createStatement()) {
            for (String sql : statements) {
                st.execute(sql);
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT COL_LENGTH('dbo.ai_analysis', 'trang_thai')")) {
                if (rs.next() && rs.getObject(1) != null) {
                    statusColumnsReady = true;
                    return null;
                }
            }
            return "Không tạo được cột trang_thai trên bảng ai_analysis.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage() == null ? "Không thể ALTER bảng ai_analysis." : e.getMessage();
        }
    }

    public int count(String doctorId, String level, String status, String keyword) {
        return loadFiltered(doctorId, level, status, keyword).size();
    }

    public List<AIAnalysis> list(String doctorId, String level, String status, String keyword, int page, int pageSize) {
        List<AIAnalysis> filtered = loadFiltered(doctorId, level, status, keyword);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int from = (safePage - 1) * safeSize;
        if (from >= filtered.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(from + safeSize, filtered.size());
        return new ArrayList<>(filtered.subList(from, to));
    }

    /**
     * Lọc mức / trạng thái ở Java để không phụ thuộc cột trang_thai trong SQL
     * (cột có thể chưa migrate → WHERE a.trang_thai làm cả query fail → list rỗng).
     */
    private List<AIAnalysis> loadFiltered(String doctorId, String level, String status, String keyword) {
        Filter f = buildSqlFilter(doctorId, keyword);
        String sql = """
                SELECT a.*, u.ho_ten AS ho_ten_benh_nhan
                """ + FROM_JOIN + f.where + """
                 ORDER BY a.thoi_gian_phan_tich DESC
                """;

        List<AIAnalysis> list = new ArrayList<>();
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, f.params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }

        // Mỗi bệnh nhân chỉ giữ bản khuyến nghị mới nhất (tránh trùng tên trên list)
        java.util.LinkedHashMap<String, AIAnalysis> latestByPatient = new java.util.LinkedHashMap<>();
        for (AIAnalysis a : list) {
            String key;
            if (a.getPatientId() != null) {
                key = a.getPatientId().toString();
            } else if (a.getId() != null) {
                key = a.getId().toString();
            } else {
                continue;
            }
            latestByPatient.putIfAbsent(key, a);
        }

        List<AIAnalysis> out = new ArrayList<>();
        for (AIAnalysis a : latestByPatient.values()) {
            if (matchLevel(a, level) && matchStatus(a, status)) {
                out.add(a);
            }
        }
        return out;
    }

    private boolean matchLevel(AIAnalysis a, String level) {
        if (level == null || level.isBlank() || "all".equalsIgnoreCase(level)) {
            return true;
        }
        String muc = a.getMucCanhBao() == null ? "" : a.getMucCanhBao().trim().toLowerCase();
        String want = level.trim().toLowerCase();
        return switch (want) {
            case "nguy_hiem" -> muc.equals("nguy_hiem") || muc.contains("nguy");
            case "cao" -> muc.equals("cao") || (muc.contains("cao") && !muc.contains("nguy"));
            case "high" -> muc.equals("nguy_hiem") || muc.equals("cao")
                    || muc.contains("nguy") || muc.contains("cao") || muc.contains("high");
            case "trung_binh", "medium" -> muc.equals("trung_binh") || muc.contains("trung")
                    || muc.equals("medium");
            case "low", "thap" -> muc.equals("thap") || muc.contains("thap") || muc.contains("thấp")
                    || muc.equals("low");
            default -> muc.equals(want) || muc.contains(want);
        };
    }

    private boolean matchStatus(AIAnalysis a, String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return true;
        }
        String st = a.getTrangThai();
        if (st == null || st.isBlank()) {
            st = "chua_xem";
        }
        return status.trim().equalsIgnoreCase(st.trim());
    }

    public AIAnalysis findById(String id, String doctorId) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String sql = """
                SELECT a.*, u.ho_ten AS ho_ten_benh_nhan
                FROM ai_analysis a
                JOIN patients p ON a.patient_id = p.id
                LEFT JOIN users u ON p.user_id = u.id
                WHERE a.id = ? AND CONVERT(nvarchar(36), p.bac_si_id) = ?
                """;
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            ps.setString(2, doctorId == null ? "" : doctorId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return null nếu OK; ngược lại thông báo lỗi để hiện trên UI
     */
    public String updateStatus(String id, String doctorId, String status, String note, UUID doctorUserId) {
        String migrateErr = ensureStatusColumns();
        if (migrateErr != null) {
            return "DB thiếu cột trang_thai và không tự thêm được: " + shorten(migrateErr)
                    + " — chạy scripts/ensure-ai-analysis.sql bằng tài khoản có quyền ALTER.";
        }

        if (id == null || id.isBlank() || status == null || status.isBlank()) {
            return "Thiếu id hoặc trạng thái.";
        }
        String normalized = switch (status.trim()) {
            case "da_xem", "da_ap_dung", "bo_qua", "chua_xem" -> status.trim();
            default -> null;
        };
        if (normalized == null) {
            return "Trạng thái không hợp lệ: " + status;
        }
        if (doctorId == null || doctorId.isBlank()) {
            return "Thiếu bác sĩ đăng nhập.";
        }

        String n = note == null ? "" : note.trim();
        String idTrim = id.trim();
        String doctorTrim = doctorId.trim();
        String xuLy = doctorUserId == null ? null : doctorUserId.toString();

        // Cùng cách bind string như findById (đã đọc được chi tiết)
        String sqlFull = """
                UPDATE a
                SET a.trang_thai = ?,
                    a.xu_ly_boi = ?,
                    a.ghi_chu_bs = CASE
                        WHEN ? = '' THEN a.ghi_chu_bs
                        WHEN a.ghi_chu_bs IS NULL OR a.ghi_chu_bs = '' THEN ?
                        ELSE CONCAT(a.ghi_chu_bs, CHAR(10), ?)
                    END
                FROM ai_analysis a
                INNER JOIN patients p ON a.patient_id = p.id
                WHERE CONVERT(nvarchar(36), a.id) = ?
                  AND CONVERT(nvarchar(36), p.bac_si_id) = ?
                """;

        String sqlStatusNote = """
                UPDATE a
                SET a.trang_thai = ?,
                    a.ghi_chu_bs = CASE
                        WHEN ? = '' THEN a.ghi_chu_bs
                        WHEN a.ghi_chu_bs IS NULL OR a.ghi_chu_bs = '' THEN ?
                        ELSE CONCAT(a.ghi_chu_bs, CHAR(10), ?)
                    END
                FROM ai_analysis a
                INNER JOIN patients p ON a.patient_id = p.id
                WHERE CONVERT(nvarchar(36), a.id) = ?
                  AND CONVERT(nvarchar(36), p.bac_si_id) = ?
                """;

        String sqlStatusOnly = """
                UPDATE a
                SET a.trang_thai = ?
                FROM ai_analysis a
                INNER JOIN patients p ON a.patient_id = p.id
                WHERE CONVERT(nvarchar(36), a.id) = ?
                  AND CONVERT(nvarchar(36), p.bac_si_id) = ?
                """;

        String err1 = tryUpdate(sqlFull, ps -> {
            ps.setString(1, normalized);
            ps.setString(2, xuLy);
            ps.setString(3, n);
            ps.setString(4, n);
            ps.setString(5, n);
            ps.setString(6, idTrim);
            ps.setString(7, doctorTrim);
        });
        if (err1 == null) {
            return null;
        }

        String err2 = tryUpdate(sqlStatusNote, ps -> {
            ps.setString(1, normalized);
            ps.setString(2, n);
            ps.setString(3, n);
            ps.setString(4, n);
            ps.setString(5, idTrim);
            ps.setString(6, doctorTrim);
        });
        if (err2 == null) {
            return null;
        }

        String err3 = tryUpdate(sqlStatusOnly, ps -> {
            ps.setString(1, normalized);
            ps.setString(2, idTrim);
            ps.setString(3, doctorTrim);
        });
        if (err3 == null) {
            return null;
        }

        // Kiểm tra bản ghi có thuộc bác sĩ không
        if (findById(idTrim, doctorTrim) == null) {
            return "Không tìm thấy khuyến nghị hoặc không thuộc bệnh nhân của bạn.";
        }

        return "SQL lỗi (có thể thiếu cột trang_thai). Chạy scripts/ensure-ai-analysis.sql. Chi tiết: "
                + shorten(err1) + " | " + shorten(err3);
    }

    @FunctionalInterface
    private interface PsBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /** @return null = update OK (≥1 dòng); message = lỗi hoặc 0 dòng */
    private String tryUpdate(String sql, PsBinder binder) {
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "0 rows";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    private static String shorten(String s) {
        if (s == null) {
            return "";
        }
        return s.length() <= 180 ? s : s.substring(0, 180) + "...";
    }

    /** @return null nếu OK, ngược lại message lỗi SQL */
    public String insert(AIAnalysis item) {
        String err = tryInsert(item, true);
        if (err == null) {
            return null;
        }
        // Fallback: bảng cũ có thể thiếu trang_thai / tokens_su_dung
        String err2 = tryInsert(item, false);
        return err2 == null ? null : err + " | fallback: " + err2;
    }

    private String tryInsert(AIAnalysis item, boolean withStatus) {
        String sql = withStatus
                ? """
                INSERT INTO ai_analysis (
                    id, patient_id, health_record_id, diem_nguy_co, muc_canh_bao, do_tin_cay,
                    phan_tich_chi_tiet, yeu_to_nguy_co, khuyen_nghi, du_lieu_dau_vao,
                    model_version, thoi_gian_phan_tich, tokens_su_dung, trang_thai
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, ?)
                """
                : """
                INSERT INTO ai_analysis (
                    id, patient_id, health_record_id, diem_nguy_co, muc_canh_bao, do_tin_cay,
                    phan_tich_chi_tiet, yeu_to_nguy_co, khuyen_nghi, du_lieu_dau_vao,
                    model_version, thoi_gian_phan_tich
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
                """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setObject(i++, item.getId() == null ? UUID.randomUUID() : item.getId());
            ps.setObject(i++, item.getPatientId());
            ps.setObject(i++, item.getHealthRecordId());
            if (item.getDiemNguyCo() == null) {
                ps.setObject(i++, null);
            } else {
                ps.setDouble(i++, item.getDiemNguyCo());
            }
            ps.setString(i++, item.getMucCanhBao());
            if (item.getDoTinCay() == null) {
                ps.setObject(i++, null);
            } else {
                ps.setDouble(i++, item.getDoTinCay());
            }
            ps.setString(i++, item.getPhanTichChiTiet());
            ps.setString(i++, item.getYeuToNguyCo());
            ps.setString(i++, item.getKhuyenNghi());
            ps.setString(i++, item.getDuLieuDauVao());
            ps.setString(i++, item.getModelVersion());
            if (withStatus) {
                if (item.getTokensSuDung() == null) {
                    ps.setObject(i++, null);
                } else {
                    ps.setInt(i++, item.getTokensSuDung());
                }
                ps.setString(i, item.getTrangThai() == null ? "chua_xem" : item.getTrangThai());
            }
            return ps.executeUpdate() > 0 ? null : "INSERT trả về 0 dòng";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean hasOpenRecommendationToday(String patientId, String syncKey) {
        return findTodayId(patientId, syncKey) != null;
    }

    /** @return id khuyến nghị hôm nay của BN, hoặc null */
    public String findTodayId(String patientId, String syncKey) {
        TodayRow row = findTodayRow(patientId, syncKey);
        return row == null ? null : row.id;
    }

    public TodayRow findTodayRow(String patientId, String syncKey) {
        String sql = """
                SELECT TOP 1 CONVERT(nvarchar(36), id) AS id, model_version
                FROM ai_analysis
                WHERE patient_id = ?
                  AND CONVERT(date, thoi_gian_phan_tich) = CONVERT(date, GETDATE())
                  AND du_lieu_dau_vao LIKE ?
                ORDER BY thoi_gian_phan_tich DESC
                """;
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(patientId));
            ps.setString(2, "%" + syncKey + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TodayRow(rs.getString("id"), rs.getString("model_version"));
                }
            }
        } catch (Exception e) {
            try (Connection conn = new DBContext().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, patientId);
                ps.setString(2, "%" + syncKey + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new TodayRow(rs.getString("id"), rs.getString("model_version"));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static final class TodayRow {
        public final String id;
        public final String modelVersion;

        public TodayRow(String id, String modelVersion) {
            this.id = id;
            this.modelVersion = modelVersion;
        }

        public boolean isGemini() {
            return modelVersion != null && modelVersion.toLowerCase().startsWith("gemini");
        }

        /** Đã viết bằng Gemini mode raw — không cần force lại cùng ngày. */
        public boolean isGeminiRaw() {
            return modelVersion != null && modelVersion.contains("+raw");
        }
    }

    /** Cập nhật lại nội dung phân tích/khuyến nghị (dùng khi force Gemini refresh). */
    public String updateNarrative(String id, String doctorId, AIAnalysis item) {
        if (id == null || item == null) {
            return "Thiếu dữ liệu cập nhật.";
        }
        String sql = """
                UPDATE a
                SET a.phan_tich_chi_tiet = ?,
                    a.yeu_to_nguy_co = ?,
                    a.khuyen_nghi = ?,
                    a.diem_nguy_co = ?,
                    a.muc_canh_bao = ?,
                    a.do_tin_cay = ?,
                    a.du_lieu_dau_vao = ?,
                    a.model_version = ?,
                    a.tokens_su_dung = ?
                FROM ai_analysis a
                INNER JOIN patients p ON a.patient_id = p.id
                WHERE CONVERT(nvarchar(36), a.id) = ?
                  AND CONVERT(nvarchar(36), p.bac_si_id) = ?
                """;
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, item.getPhanTichChiTiet());
            ps.setString(i++, item.getYeuToNguyCo());
            ps.setString(i++, item.getKhuyenNghi());
            if (item.getDiemNguyCo() == null) {
                ps.setObject(i++, null);
            } else {
                ps.setDouble(i++, item.getDiemNguyCo());
            }
            ps.setString(i++, item.getMucCanhBao());
            if (item.getDoTinCay() == null) {
                ps.setObject(i++, null);
            } else {
                ps.setDouble(i++, item.getDoTinCay());
            }
            ps.setString(i++, item.getDuLieuDauVao());
            ps.setString(i++, item.getModelVersion());
            if (item.getTokensSuDung() == null) {
                ps.setObject(i++, null);
            } else {
                ps.setInt(i++, item.getTokensSuDung());
            }
            ps.setString(i++, id.trim());
            ps.setString(i, doctorId.trim());
            return ps.executeUpdate() > 0 ? null : "0 rows updated";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private Filter buildSqlFilter(String doctorId, String keyword) {
        Filter f = new Filter();
        // So khớp UUID ổn định (tránh lúc thấy list, lúc trống)
        f.where.append("WHERE CONVERT(nvarchar(36), p.bac_si_id) = ? ");
        f.params.add(doctorId == null ? "" : doctorId.trim());

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim() + "%";
            f.where.append("AND (u.ho_ten LIKE ? OR a.khuyen_nghi LIKE ? OR a.yeu_to_nguy_co LIKE ?) ");
            f.params.add(kw);
            f.params.add(kw);
            f.params.add(kw);
        }
        return f;
    }

    private AIAnalysis mapRow(ResultSet rs) throws SQLException {
        AIAnalysis a = new AIAnalysis();
        a.setId(uuid(rs, "id"));
        a.setPatientId(uuid(rs, "patient_id"));
        a.setHealthRecordId(uuid(rs, "health_record_id"));
        double diem = rs.getDouble("diem_nguy_co");
        a.setDiemNguyCo(rs.wasNull() ? null : diem);
        a.setMucCanhBao(rs.getString("muc_canh_bao"));
        double tinCay = rs.getDouble("do_tin_cay");
        a.setDoTinCay(rs.wasNull() ? null : tinCay);
        a.setPhanTichChiTiet(rs.getString("phan_tich_chi_tiet"));
        a.setYeuToNguyCo(rs.getString("yeu_to_nguy_co"));
        a.setKhuyenNghi(rs.getString("khuyen_nghi"));
        a.setDuLieuDauVao(rs.getString("du_lieu_dau_vao"));
        a.setModelVersion(rs.getString("model_version"));
        a.setThoiGianPhanTich(rs.getTimestamp("thoi_gian_phan_tich"));
        int tokens = rs.getInt("tokens_su_dung");
        a.setTokensSuDung(rs.wasNull() ? null : tokens);
        try {
            String tt = rs.getString("trang_thai");
            if (tt == null || tt.isBlank()) {
                a.setTrangThai("chua_xem");
            } else {
                a.setTrangThai(tt.trim());
            }
            a.setGhiChuBs(rs.getString("ghi_chu_bs"));
            a.setXuLyBoi(uuid(rs, "xu_ly_boi"));
        } catch (SQLException ignored) {
            a.setTrangThai("chua_xem");
        }
        try {
            a.setHoTenBenhNhan(rs.getString("ho_ten_benh_nhan"));
        } catch (SQLException ignored) {
        }
        return a;
    }

    private UUID uuid(ResultSet rs, String col) throws SQLException {
        String v = rs.getString(col);
        return v == null ? null : UUID.fromString(v);
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Integer intVal) {
                ps.setInt(i + 1, intVal);
            } else if (v instanceof Double doubleVal) {
                ps.setDouble(i + 1, doubleVal);
            } else {
                ps.setString(i + 1, String.valueOf(v));
            }
        }
    }

    private static final class Filter {
        private final StringBuilder where = new StringBuilder();
        private final List<Object> params = new ArrayList<>();
    }
}
