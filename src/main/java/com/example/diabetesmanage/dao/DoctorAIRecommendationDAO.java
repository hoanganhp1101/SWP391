package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.AIAnalysis;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class DoctorAIRecommendationDAO {

    private static final String FROM_JOIN = """
            FROM ai_analysis a
            JOIN patients p ON a.patient_id = p.id
            LEFT JOIN users u ON p.user_id = u.id
            """;

    /**
     * Các cột trạng thái đã được khai báo trong newdb.sql, không chạy DDL lúc ứng dụng hoạt động.
     */
    public String ensureStatusColumns() {
        return null;
    }

    public int count(String doctorId, String level, String status, String keyword) {
        return loadFiltered(doctorId, level, status, keyword).size();
    }

    public List<AIAnalysis> list(
            String doctorId, String level, String status, String keyword, int page, int pageSize) {
        List<AIAnalysis> filtered = loadFiltered(doctorId, level, status, keyword);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int from = (safePage - 1) * safeSize;
        if (from >= filtered.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(filtered.subList(from, Math.min(from + safeSize, filtered.size())));
    }

    private List<AIAnalysis> loadFiltered(
            String doctorId, String level, String status, String keyword) {
        Filter filter = buildSqlFilter(doctorId, keyword);
        String sql = """
                SELECT a.*, u.ho_ten AS ho_ten_benh_nhan
                """ + FROM_JOIN + filter.where + """
                 ORDER BY a.thoi_gian_phan_tich DESC
                """;

        List<AIAnalysis> rows = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, filter.params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapRow(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return rows;
        }

        // Mỗi bệnh nhân chỉ xuất hiện một lần với khuyến nghị mới nhất.
        LinkedHashMap<String, AIAnalysis> latestByPatient = new LinkedHashMap<>();
        for (AIAnalysis item : rows) {
            String key = item.getPatientId() != null ? item.getPatientId() : item.getId();
            if (key != null) {
                latestByPatient.putIfAbsent(key, item);
            }
        }

        List<AIAnalysis> result = new ArrayList<>();
        for (AIAnalysis item : latestByPatient.values()) {
            if (matchesLevel(item, level) && matchesStatus(item, status)) {
                result.add(item);
            }
        }
        return result;
    }

    private boolean matchesLevel(AIAnalysis item, String level) {
        if (level == null || level.isBlank() || "all".equalsIgnoreCase(level)) {
            return true;
        }
        String actual = item.getMucCanhBao() == null
                ? "" : item.getMucCanhBao().trim().toLowerCase();
        String expected = level.trim().toLowerCase();
        return switch (expected) {
            case "nguy_hiem" -> actual.equals("nguy_hiem") || actual.contains("nguy");
            case "cao" -> actual.equals("cao") || (actual.contains("cao") && !actual.contains("nguy"));
            case "high" -> actual.equals("nguy_hiem") || actual.equals("cao")
                    || actual.contains("nguy") || actual.contains("high");
            case "trung_binh", "medium" -> actual.equals("trung_binh")
                    || actual.contains("trung") || actual.equals("medium");
            case "low", "thap" -> actual.equals("thap") || actual.equals("low");
            default -> actual.equals(expected) || actual.contains(expected);
        };
    }

    private boolean matchesStatus(AIAnalysis item, String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return true;
        }
        String actual = item.getTrangThai();
        if (actual == null || actual.isBlank()) {
            actual = "chua_xem";
        }
        return status.trim().equalsIgnoreCase(actual.trim());
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
                WHERE a.id = ? AND p.bac_si_id = ?
                """;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.trim());
            statement.setString(2, doctorId == null ? "" : doctorId.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return {@code null} khi cập nhật thành công, ngược lại là thông báo lỗi.
     */
    public String updateStatus(
            String id, String doctorId, String status, String note, String doctorUserId) {
        if (id == null || id.isBlank() || status == null || status.isBlank()) {
            return "Thiếu mã khuyến nghị hoặc trạng thái.";
        }
        String normalized = switch (status.trim()) {
            case "da_xem", "da_ap_dung", "bo_qua", "chua_xem" -> status.trim();
            default -> null;
        };
        if (normalized == null) {
            return "Trạng thái không hợp lệ: " + status;
        }
        if (doctorId == null || doctorId.isBlank()) {
            return "Thiếu thông tin bác sĩ đăng nhập.";
        }

        String sql = """
                UPDATE ai_analysis a
                JOIN patients p ON a.patient_id = p.id
                SET a.trang_thai = ?,
                    a.xu_ly_boi = ?,
                    a.ghi_chu_bs = CASE
                        WHEN ? = '' THEN a.ghi_chu_bs
                        WHEN a.ghi_chu_bs IS NULL OR a.ghi_chu_bs = '' THEN ?
                        ELSE CONCAT(a.ghi_chu_bs, CHAR(10), ?)
                    END
                WHERE a.id = ? AND p.bac_si_id = ?
                """;

        String trimmedNote = note == null ? "" : note.trim();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, doctorUserId);
            statement.setString(3, trimmedNote);
            statement.setString(4, trimmedNote);
            statement.setString(5, trimmedNote);
            statement.setString(6, id.trim());
            statement.setString(7, doctorId.trim());
            if (statement.executeUpdate() > 0) {
                return null;
            }
            return "Không tìm thấy khuyến nghị hoặc khuyến nghị không thuộc bệnh nhân của bác sĩ.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    /**
     * @return {@code null} khi thêm thành công, ngược lại là thông báo lỗi.
     */
    public String insert(AIAnalysis item) {
        if (item == null || item.getPatientId() == null) {
            return "Thiếu dữ liệu khuyến nghị.";
        }
        String sql = """
                INSERT INTO ai_analysis (
                    id, patient_id, health_record_id, diem_nguy_co, muc_canh_bao, do_tin_cay,
                    phan_tich_chi_tiet, yeu_to_nguy_co, khuyen_nghi, du_lieu_dau_vao,
                    model_version, thoi_gian_phan_tich, tokens_su_dung, trang_thai
                ) VALUES (?, ?, ?, ?, ?, ?, ?, JSON_QUOTE(?), JSON_QUOTE(?), JSON_QUOTE(?),
                          ?, NOW(), ?, ?)
                """;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, item.getId() == null
                    ? UUID.randomUUID().toString() : item.getId());
            statement.setString(index++, item.getPatientId());
            statement.setString(index++, item.getHealthRecordId());
            statement.setDouble(index++, item.getDiemNguyCo());
            statement.setString(index++, item.getMucCanhBao());
            if (item.getDoTinCay() == null) {
                statement.setObject(index++, null);
            } else {
                statement.setDouble(index++, item.getDoTinCay());
            }
            statement.setString(index++, item.getPhanTichChiTiet());
            statement.setString(index++, valueOrEmpty(item.getYeuToNguyCo()));
            statement.setString(index++, valueOrEmpty(item.getKhuyenNghi()));
            statement.setString(index++, valueOrEmpty(item.getDuLieuDauVao()));
            statement.setString(index++, item.getModelVersion());
            if (item.getTokensSuDung() == null) {
                statement.setObject(index++, null);
            } else {
                statement.setInt(index++, item.getTokensSuDung());
            }
            statement.setString(index, item.getTrangThai() == null
                    ? "chua_xem" : item.getTrangThai());
            return statement.executeUpdate() > 0 ? null : "INSERT trả về 0 dòng.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    public boolean hasOpenRecommendationToday(String patientId, String syncKey) {
        return findTodayId(patientId, syncKey) != null;
    }

    public String findTodayId(String patientId, String syncKey) {
        TodayRow row = findTodayRow(patientId, syncKey);
        return row == null ? null : row.id;
    }

    public TodayRow findTodayRow(String patientId, String syncKey) {
        String sql = """
                SELECT id, model_version
                FROM ai_analysis
                WHERE patient_id = ?
                  AND DATE(thoi_gian_phan_tich) = CURDATE()
                  AND JSON_UNQUOTE(du_lieu_dau_vao) LIKE ?
                ORDER BY thoi_gian_phan_tich DESC
                LIMIT 1
                """;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientId);
            statement.setString(2, "%" + syncKey + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new TodayRow(resultSet.getString("id"), resultSet.getString("model_version"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật nội dung khi bác sĩ yêu cầu Gemini tạo lại khuyến nghị trong ngày.
     */
    public String updateNarrative(String id, String doctorId, AIAnalysis item) {
        if (id == null || id.isBlank() || doctorId == null || doctorId.isBlank() || item == null) {
            return "Thiếu dữ liệu cập nhật.";
        }
        String sql = """
                UPDATE ai_analysis a
                JOIN patients p ON a.patient_id = p.id
                SET a.phan_tich_chi_tiet = ?,
                    a.yeu_to_nguy_co = JSON_QUOTE(?),
                    a.khuyen_nghi = JSON_QUOTE(?),
                    a.diem_nguy_co = ?,
                    a.muc_canh_bao = ?,
                    a.do_tin_cay = ?,
                    a.du_lieu_dau_vao = JSON_QUOTE(?),
                    a.model_version = ?,
                    a.tokens_su_dung = ?
                WHERE a.id = ? AND p.bac_si_id = ?
                """;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, item.getPhanTichChiTiet());
            statement.setString(index++, valueOrEmpty(item.getYeuToNguyCo()));
            statement.setString(index++, valueOrEmpty(item.getKhuyenNghi()));
            statement.setDouble(index++, item.getDiemNguyCo());
            statement.setString(index++, item.getMucCanhBao());
            if (item.getDoTinCay() == null) {
                statement.setObject(index++, null);
            } else {
                statement.setDouble(index++, item.getDoTinCay());
            }
            statement.setString(index++, valueOrEmpty(item.getDuLieuDauVao()));
            statement.setString(index++, item.getModelVersion());
            if (item.getTokensSuDung() == null) {
                statement.setObject(index++, null);
            } else {
                statement.setInt(index++, item.getTokensSuDung());
            }
            statement.setString(index++, id.trim());
            statement.setString(index, doctorId.trim());
            return statement.executeUpdate() > 0 ? null : "Không có dòng nào được cập nhật.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    private Filter buildSqlFilter(String doctorId, String keyword) {
        Filter filter = new Filter();
        filter.where.append("WHERE p.bac_si_id = ? ");
        filter.params.add(doctorId == null ? "" : doctorId.trim());

        if (keyword != null && !keyword.trim().isEmpty()) {
            String value = "%" + keyword.trim() + "%";
            filter.where.append("""
                    AND (u.ho_ten LIKE ?
                         OR JSON_UNQUOTE(a.khuyen_nghi) LIKE ?
                         OR JSON_UNQUOTE(a.yeu_to_nguy_co) LIKE ?)
                    """);
            filter.params.add(value);
            filter.params.add(value);
            filter.params.add(value);
        }
        return filter;
    }

    private AIAnalysis mapRow(ResultSet resultSet) throws SQLException {
        AIAnalysis item = new AIAnalysis();
        item.setId(resultSet.getString("id"));
        item.setPatientId(resultSet.getString("patient_id"));
        item.setHealthRecordId(resultSet.getString("health_record_id"));
        item.setDiemNguyCo(resultSet.getDouble("diem_nguy_co"));
        item.setMucCanhBao(resultSet.getString("muc_canh_bao"));
        double confidence = resultSet.getDouble("do_tin_cay");
        item.setDoTinCay(resultSet.wasNull() ? null : confidence);
        item.setPhanTichChiTiet(resultSet.getString("phan_tich_chi_tiet"));
        item.setYeuToNguyCo(readJsonText(resultSet.getString("yeu_to_nguy_co")));
        item.setKhuyenNghi(readJsonText(resultSet.getString("khuyen_nghi")));
        item.setDuLieuDauVao(readJsonText(resultSet.getString("du_lieu_dau_vao")));
        item.setModelVersion(resultSet.getString("model_version"));
        item.setThoiGianPhanTich(resultSet.getTimestamp("thoi_gian_phan_tich"));
        int tokens = resultSet.getInt("tokens_su_dung");
        item.setTokensSuDung(resultSet.wasNull() ? null : tokens);
        String state = resultSet.getString("trang_thai");
        item.setTrangThai(state == null || state.isBlank() ? "chua_xem" : state.trim());
        item.setGhiChuBs(resultSet.getString("ghi_chu_bs"));
        item.setXuLyBoi(resultSet.getString("xu_ly_boi"));
        try {
            item.setHoTenBenhNhan(resultSet.getString("ho_ten_benh_nhan"));
        } catch (SQLException ignored) {
            // Một số truy vấn nội bộ không lấy tên bệnh nhân.
        }
        return item;
    }

    private String readJsonText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        try {
            JsonElement element = JsonParser.parseString(value);
            return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                    ? element.getAsString() : value;
        } catch (Exception ignored) {
            return value;
        }
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setString(i + 1, String.valueOf(params.get(i)));
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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

        public boolean isGeminiRaw() {
            return modelVersion != null && modelVersion.contains("+raw");
        }
    }

    private static final class Filter {
        private final StringBuilder where = new StringBuilder();
        private final List<Object> params = new ArrayList<>();
    }
}
