package dal;

import com.example.diabetesmanage.context.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Tổng hợp số liệu cho màn Analytics của bác sĩ.
 *
 * Khi khởi tạo với doctorId, mọi truy vấn chỉ tính trên bệnh nhân do bác sĩ đó
 * phụ trách (patients.bac_si_id = doctorId). Nếu doctorId null/blank thì tính
 * trên toàn bộ bệnh nhân.
 *
 * Mỗi truy vấn được bọc try/catch riêng và trả về giá trị mặc định (0 / rỗng)
 * để trang vẫn hiển thị kể cả khi một bảng/cột chưa khớp.
 */
public class DoctorAnalyticsDAO {

    public static final int GLUCOSE_LOW = 70;
    public static final int GLUCOSE_HIGH = 180;

    private final String doctorId;

    public DoctorAnalyticsDAO() {
        this(null);
    }

    public DoctorAnalyticsDAO(String doctorId) {
        this.doctorId = (doctorId == null || doctorId.isBlank()) ? null : doctorId.trim();
    }

    private boolean scoped() {
        return doctorId != null;
    }

    // ===== KPI =====

    public int countPatients() {
        if (scoped()) {
            return queryInt("SELECT COUNT(*) FROM patients WHERE bac_si_id = ?", doctorId);
        }
        return queryInt("SELECT COUNT(*) FROM patients");
    }

    public int unresolvedAlerts() {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM alerts a "
                + "JOIN patients p ON a.patient_id = p.id "
                + "WHERE a.thoi_gian_xu_ly IS NULL" + doctorClause("p", p);
        return queryInt(sql, p.toArray());
    }

    public double averageGlucose(int days) {
        List<Object> p = new ArrayList<>();
        p.add(days);
        String sql = "SELECT AVG(hr.duong_huyet_mgdl) FROM health_records hr "
                + "JOIN patients p ON hr.patient_id = p.id "
                + "WHERE hr.duong_huyet_mgdl IS NOT NULL "
                + "AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)" + doctorClause("p", p);
        return queryDouble(sql, p.toArray());
    }

    public double timeInRange(int days) {
        List<Object> p = new ArrayList<>();
        p.add(GLUCOSE_LOW);
        p.add(GLUCOSE_HIGH);
        p.add(days);
        String sql = "SELECT CAST(SUM(CASE WHEN hr.duong_huyet_mgdl BETWEEN ? AND ? THEN 1 ELSE 0 END) AS FLOAT) "
                + "* 100.0 / NULLIF(COUNT(*), 0) "
                + "FROM health_records hr JOIN patients p ON hr.patient_id = p.id "
                + "WHERE hr.duong_huyet_mgdl IS NOT NULL "
                + "AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)" + doctorClause("p", p);
        return queryDouble(sql, p.toArray());
    }

    public double pctHba1cAtTarget() {
        List<Object> p = new ArrayList<>();
        String sql = "WITH latest AS ("
                + "  SELECT hr.patient_id, hr.hba1c_percent, "
                + "         ROW_NUMBER() OVER (PARTITION BY hr.patient_id ORDER BY hr.thoi_gian_do DESC) AS rn "
                + "  FROM health_records hr JOIN patients p ON hr.patient_id = p.id "
                + "  WHERE hr.hba1c_percent IS NOT NULL" + doctorClause("p", p)
                + ") "
                + "SELECT CAST(SUM(CASE WHEN hba1c_percent < 7 THEN 1 ELSE 0 END) AS FLOAT) "
                + "* 100.0 / NULLIF(COUNT(*), 0) FROM latest WHERE rn = 1";
        return queryDouble(sql, p.toArray());
    }

    // ===== ACTION LIST (lâm sàng) =====

    public int patientsWithHypo(int days) {
        List<Object> p = new ArrayList<>();
        p.add(GLUCOSE_LOW);
        p.add(days);
        String sql = "SELECT COUNT(DISTINCT hr.patient_id) FROM health_records hr "
                + "JOIN patients p ON hr.patient_id = p.id "
                + "WHERE hr.duong_huyet_mgdl < ? AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)"
                + doctorClause("p", p);
        return queryInt(sql, p.toArray());
    }

    public int patientsHighHba1c() {
        List<Object> p = new ArrayList<>();
        String sql = "WITH latest AS ("
                + "  SELECT hr.patient_id, hr.hba1c_percent, "
                + "         ROW_NUMBER() OVER (PARTITION BY hr.patient_id ORDER BY hr.thoi_gian_do DESC) AS rn "
                + "  FROM health_records hr JOIN patients p ON hr.patient_id = p.id "
                + "  WHERE hr.hba1c_percent IS NOT NULL" + doctorClause("p", p)
                + ") "
                + "SELECT COUNT(*) FROM latest WHERE rn = 1 AND hba1c_percent >= 8";
        return queryInt(sql, p.toArray());
    }

    public int patientsNotMeasured(int days) {
        List<Object> p = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM patients p WHERE 1 = 1");
        sql.append(doctorClause("p", p));
        sql.append(" AND NOT EXISTS (SELECT 1 FROM health_records hr "
                + "WHERE hr.patient_id = p.id AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY))");
        p.add(days);
        return queryInt(sql.toString(), p.toArray());
    }

    public int overdueFollowups() {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COUNT(DISTINCT pr.patient_id) FROM prescriptions pr "
                + "JOIN patients p ON pr.patient_id = p.id "
                + "WHERE pr.ngay_tai_kham IS NOT NULL AND pr.ngay_tai_kham < NOW()"
                + doctorClause("p", p);
        return queryInt(sql, p.toArray());
    }

    // ===== CHARTS =====

    public List<Object[]> glucoseByDay(int days) {
        List<Object> p = new ArrayList<>();
        p.add(days);
        String sql = "SELECT DATE_FORMAT(hr.thoi_gian_do, '%Y-%m-%d') AS d, AVG(hr.duong_huyet_mgdl) AS g "
                + "FROM health_records hr JOIN patients p ON hr.patient_id = p.id "
                + "WHERE hr.duong_huyet_mgdl IS NOT NULL "
                + "AND hr.thoi_gian_do >= DATE_SUB(NOW(), INTERVAL ? DAY)" + doctorClause("p", p) + " "
                + "GROUP BY DATE_FORMAT(hr.thoi_gian_do, '%Y-%m-%d') ORDER BY d";
        return queryList(sql, p.toArray());
    }

    public List<Object[]> alertsByType() {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COALESCE(a.loai_canh_bao, 'Khác') AS k, COUNT(*) AS sl "
                + "FROM alerts a JOIN patients p ON a.patient_id = p.id "
                + "WHERE 1 = 1" + doctorClause("p", p)
                + " GROUP BY a.loai_canh_bao ORDER BY sl DESC";
        return queryList(sql, p.toArray());
    }

    public List<Object[]> alertsBySeverity() {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COALESCE(a.muc_do, 'Khác') AS k, COUNT(*) AS sl "
                + "FROM alerts a JOIN patients p ON a.patient_id = p.id "
                + "WHERE 1 = 1" + doctorClause("p", p)
                + " GROUP BY a.muc_do ORDER BY sl DESC";
        return queryList(sql, p.toArray());
    }

    public List<Object[]> hba1cDistribution() {
        List<Object> p = new ArrayList<>();
        String sql = "WITH latest AS ("
                + "  SELECT hr.patient_id, hr.hba1c_percent, "
                + "         ROW_NUMBER() OVER (PARTITION BY hr.patient_id ORDER BY hr.thoi_gian_do DESC) AS rn "
                + "  FROM health_records hr JOIN patients p ON hr.patient_id = p.id "
                + "  WHERE hr.hba1c_percent IS NOT NULL" + doctorClause("p", p)
                + ") "
                + "SELECT nhom, COUNT(*) FROM ("
                + "  SELECT CASE WHEN hba1c_percent < 7 THEN 'Tốt (<7%)' "
                + "              WHEN hba1c_percent < 8 THEN 'Khá (7-8%)' "
                + "              ELSE 'Kém (>=8%)' END AS nhom "
                + "  FROM latest WHERE rn = 1"
                + ") t GROUP BY nhom";
        return queryList(sql, p.toArray());
    }

    // ===== Helpers =====

    /** Trả về mệnh đề lọc bác sĩ và thêm tham số vào danh sách (nếu có scope). */
    private String doctorClause(String alias, List<Object> params) {
        if (!scoped()) {
            return "";
        }
        params.add(doctorId);
        return " AND " + alias + ".bac_si_id = ?";
    }

    private int queryInt(String sql, Object... params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
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

    private double queryDouble(String sql, Object... params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? 0d : v;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0d;
    }

    private List<Object[]> queryList(String sql, Object... params) {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getString(1), rs.getDouble(2)});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void bind(PreparedStatement ps, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            Object v = params[i];
            if (v instanceof Integer intValue) {
                ps.setInt(i + 1, intValue);
            } else if (v instanceof Double doubleValue) {
                ps.setDouble(i + 1, doubleValue);
            } else {
                ps.setString(i + 1, String.valueOf(v));
            }
        }
    }
}
