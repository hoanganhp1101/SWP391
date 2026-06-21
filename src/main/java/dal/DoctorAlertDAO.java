package dal;

import config.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.Alert;

public class DoctorAlertDAO {

    private static final String FROM_JOIN = """
            FROM alerts a
            LEFT JOIN patients p ON a.patient_id = p.id
            LEFT JOIN users u ON p.user_id = u.id
            """;

    public List<Alert> getAllAlerts() {
        return getAlerts(null, null, null, null, 1, Integer.MAX_VALUE, null);
    }

    public int countAlerts(String severity, String status, String alertType, String keyword) {
        return countAlerts(severity, status, alertType, keyword, null);
    }

    public int countAlerts(String severity, String status, String alertType, String keyword, String doctorId) {
        FilterQuery filter = buildFilter(severity, status, alertType, keyword, doctorId);
        String sql = "SELECT COUNT(*) " + FROM_JOIN + filter.where;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, filter.params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Alert> getAlerts(String severity, String status, String alertType, String keyword, int page, int pageSize) {
        return getAlerts(severity, status, alertType, keyword, page, pageSize, null);
    }

    public List<Alert> getAlerts(String severity, String status, String alertType, String keyword, int page, int pageSize, String doctorId) {
        List<Alert> list = new ArrayList<>();
        FilterQuery filter = buildFilter(severity, status, alertType, keyword, doctorId);

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safePageSize;

        String sql = """
                SELECT a.*, u.ho_ten AS ho_ten_benh_nhan, u.so_dien_thoai AS so_dien_thoai_benh_nhan
                """ + FROM_JOIN + filter.where + """
                 ORDER BY a.thoi_gian_tao DESC
                 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        List<Object> params = new ArrayList<>(filter.params);
        params.add(offset);
        params.add(safePageSize);

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAlert(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private FilterQuery buildFilter(String severity, String status, String alertType, String keyword, String doctorId) {
        FilterQuery filter = new FilterQuery();
        filter.where.append("WHERE 1 = 1 ");
        appendDoctorFilter(filter.where, filter.params, doctorId);
        appendSeverityFilter(filter.where, filter.params, severity);
        appendStatusFilter(filter.where, status);
        appendAlertTypeFilter(filter.where, filter.params, alertType);
        appendKeywordFilter(filter.where, filter.params, keyword);
        return filter;
    }

    private void appendDoctorFilter(StringBuilder sql, List<String> params, String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            return;
        }
        sql.append("AND p.bac_si_id = ? ");
        params.add(doctorId.trim());
    }

    private void bindParams(PreparedStatement ps, List<?> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer intValue) {
                ps.setInt(i + 1, intValue);
            } else {
                ps.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private Alert mapAlert(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setId(getUuid(rs, "id"));
        alert.setPatientId(getUuid(rs, "patient_id"));
        alert.setAiAnalysisId(getUuid(rs, "ai_analysis_id"));
        alert.setLoaiCanhBao(rs.getString("loai_canh_bao"));
        alert.setMucDo(rs.getString("muc_do"));
        alert.setTieuDe(rs.getString("tieu_de"));
        alert.setNoiDung(rs.getString("noi_dung"));
        alert.setDaDocBn(rs.getBoolean("da_doc_bn"));
        alert.setDaDocBs(rs.getBoolean("da_doc_bs"));
        alert.setXuLyBoi(getUuid(rs, "xu_ly_boi"));
        alert.setGhiChuXuLy(rs.getString("ghi_chu_xu_ly"));
        alert.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
        alert.setThoiGianXuLy(rs.getTimestamp("thoi_gian_xu_ly"));
        alert.setHoTenBenhNhan(rs.getString("ho_ten_benh_nhan"));
        alert.setSoDienThoaiBenhNhan(rs.getString("so_dien_thoai_benh_nhan"));
        return alert;
    }

    public boolean quickAction(UUID alertId, UUID doctorId, String ghiChu, boolean markResolved) {
        if (alertId == null || doctorId == null || ghiChu == null || ghiChu.isBlank()) {
            return false;
        }

        // Luôn ghi lại thời điểm xử lý khi bác sĩ thao tác nhanh.
        // Nếu đánh dấu "đã giải quyết" thì chốt thời gian giải quyết, ngược lại
        // vẫn lưu thời điểm xử lý gần nhất để theo dõi tiến độ.
        String sql = """
                UPDATE alerts
                SET ghi_chu_xu_ly = CASE
                        WHEN ghi_chu_xu_ly IS NULL OR ghi_chu_xu_ly = '' THEN ?
                        ELSE ghi_chu_xu_ly + CHAR(10) + ?
                    END,
                    da_doc_bs = 1,
                    xu_ly_boi = ?,
                    thoi_gian_xu_ly = GETDATE()
                WHERE id = ?
                """;

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String note = ghiChu.trim();
            ps.setString(1, note);
            ps.setString(2, note);
            ps.setString(3, doctorId.toString());
            ps.setString(4, alertId.toString());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private UUID getUuid(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : UUID.fromString(value);
    }

    private void appendSeverityFilter(StringBuilder sql, List<String> params, String severity) {
        if (severity == null || severity.isBlank() || "all".equalsIgnoreCase(severity)) {
            return;
        }

        String[] patterns = switch (severity) {
            case "danger" -> new String[]{"%nguy_hiem%", "%nguy%", "%danger%", "%đỏ%"};
            case "high" -> new String[]{"%cao%", "%high%", "%vàng%", "%vang%"};
            case "medium" -> new String[]{"%trung_binh%", "%trung%", "%medium%", "%xanh%"};
            default -> new String[0];
        };

        if (patterns.length == 0) {
            return;
        }

        sql.append("AND (");
        for (int i = 0; i < patterns.length; i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("LOWER(COALESCE(a.muc_do, '')) LIKE ? ");
            params.add(patterns[i]);
        }
        sql.append(") ");
    }

    private void appendStatusFilter(StringBuilder sql, String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return;
        }

        switch (status) {
            case "unread" -> sql.append("AND a.da_doc_bs = 0 ");
            case "processing" -> sql.append("AND a.da_doc_bs = 1 AND a.thoi_gian_xu_ly IS NULL ");
            case "resolved" -> sql.append("AND a.thoi_gian_xu_ly IS NOT NULL ");
            default -> {
            }
        }
    }

    private void appendAlertTypeFilter(StringBuilder sql, List<String> params, String alertType) {
        if (alertType == null || alertType.isBlank() || "all".equalsIgnoreCase(alertType)) {
            return;
        }

        // Chỉ so khớp trên cột mã loại (loai_canh_bao) để các loại không bị trùng kết quả.
        // Không quét tieu_de/noi_dung vì hầu hết cảnh báo đều chứa các từ khóa chung.
        String[] patterns = switch (alertType) {
            case "glucose" -> new String[]{"%duong_huyet%", "%glucose%"};
            case "missed_measurement" -> new String[]{"%khong_do%", "%quen_do%", "%do_lien_tuc%", "%measurement%"};
            case "missed_medication" -> new String[]{"%bo_thuoc%", "%thuoc%", "%medication%"};
            case "abnormal_trend" -> new String[]{"%xu_huong%", "%trend%"};
            default -> new String[0];
        };

        if (patterns.length == 0) {
            return;
        }

        sql.append("AND (");
        for (int i = 0; i < patterns.length; i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("LOWER(COALESCE(a.loai_canh_bao, '')) LIKE ? ");
            params.add(patterns[i]);
        }
        sql.append(") ");
    }

    private void appendKeywordFilter(StringBuilder sql, List<String> params, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String searchValue = "%" + keyword.trim() + "%";
        sql.append("AND (");
        sql.append("u.ho_ten LIKE ? ");
        sql.append("OR CONVERT(varchar(36), p.id) LIKE ? ");
        sql.append("OR CONVERT(varchar(36), a.patient_id) LIKE ? ");
        sql.append(") ");
        params.add(searchValue);
        params.add(searchValue);
        params.add(searchValue);
    }

    private static final class FilterQuery {
        private final StringBuilder where = new StringBuilder();
        private final List<String> params = new ArrayList<>();
    }
}
