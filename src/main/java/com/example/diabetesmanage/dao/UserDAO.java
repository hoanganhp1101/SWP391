package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auth + quản lý tài khoản trên bảng {@code users} (cột {@code role}).
 * Hồ sơ bác sĩ/bệnh nhân: {@code doctors}/{@code patients} cùng UUID.
 */
public class UserDAO {

    private static UserDAO instance;

    private static final String USER_COLUMNS =
            "id, ho_ten, email, so_dien_thoai, mat_khau_hash, anh_dai_dien, "
                    + "role AS vai_tro, kich_hoat, ngay_tao, ngay_cap_nhat, lan_dang_nhap_cuoi";

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    public String getNameById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String sql = "SELECT ho_ten FROM users WHERE id = ? LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("ho_ten") : null;
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "getNameById error", e);
            return null;
        }
    }

    public List<User> getUsersByRole(String role) {
        if (!isValidRole(role)) {
            return List.of();
        }
        List<User> userList = new ArrayList<>();
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE role = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapResultSetToUser(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE email = ? LIMIT 1";
        try {
            Connection conn = DBContext.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Không kết nối được MySQL. Kiểm tra DBContext và dịch vụ MySQL.");
            }
            try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "getUserByEmail error", e);
            throw new IllegalStateException("Lỗi truy vấn người dùng khi đăng nhập.", e);
        }
        return null;
    }

    public User getUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE id = ? LIMIT 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "getUserById error", e);
            return null;
        }
    }

    /**
     * Đăng nhập theo email + mật khẩu. Vai trò lấy từ cột {@code users.role}.
     */
    public User checkLogin(String email, String hashedPassword) {
        if (email == null || hashedPassword == null) {
            return null;
        }
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE email = ? AND mat_khau_hash = ? LIMIT 1";
        try {
            Connection conn = DBContext.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Không kết nối được MySQL. Kiểm tra DBContext và dịch vụ MySQL.");
            }
            try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                ps.setString(2, hashedPassword.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "checkLogin error", e);
            throw new IllegalStateException("Lỗi truy vấn người dùng khi đăng nhập.", e);
        }
        return null;
    }

    public boolean updatePassword(String userId, String hashedPassword) {
        if (userId == null || userId.isBlank() || hashedPassword == null) {
            return false;
        }
        String sql = "UPDATE users SET mat_khau_hash = ?, ngay_cap_nhat = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, userId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEmailExists(String email) {
        return getUserByEmail(email) != null;
    }

    /**
     * Đăng ký bệnh nhân: insert {@code users} + {@code patients} cùng id.
     */
    public boolean registerUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setVaiTro("benh_nhan");

        String insertUser = "INSERT INTO users (id, ho_ten, email, so_dien_thoai, mat_khau_hash, kich_hoat, role, ngay_tao) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'benh_nhan', ?)";
        String insertPatient = "INSERT INTO patients (id, patient_code, ngay_sinh, loai_tieu_duong, ngay_tao) "
                + "VALUES (?, ?, '2000-01-01', 'Type 2', NOW())";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement(insertUser)) {
                st.setString(1, user.getId());
                st.setString(2, user.getHoTen());
                st.setString(3, user.getEmail());
                st.setString(4, user.getSoDienThoai());
                st.setString(5, user.getMatKhauHash());
                st.setBoolean(6, user.isKichHoat());
                st.setTimestamp(7, user.getNgayTao() != null
                        ? user.getNgayTao() : new Timestamp(System.currentTimeMillis()));
                if (st.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement st = conn.prepareStatement(insertPatient)) {
                st.setString(1, user.getId());
                st.setString(2, nextPatientCode(conn));
                if (st.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public String hashSHA256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest((input == null ? "" : input).getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateUser(User u) {
        if (u == null || u.getId() == null) {
            return false;
        }
        String sql = "UPDATE users SET ho_ten = ?, email = ?, so_dien_thoai = ?, ngay_cap_nhat = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getHoTen());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSoDienThoai());
            ps.setString(4, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật user: " + e.getMessage());
        }
        return false;
    }

    public int getTotalUsersCount(String role, String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> params = buildFilterParams(sql, role, status, keyword);
        return queryCount(sql.toString(), params);
    }

    public List<User> getFilteredUsers(String role, String status, String keyword, int offset, int limit) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + USER_COLUMNS + " FROM users WHERE 1=1");
        List<Object> params = buildFilterParams(sql, role, status, keyword);
        sql.append(" ORDER BY ngay_tao DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách user phân trang: " + e.getMessage());
        }
        return list;
    }

    /**
     * Admin tạo bác sĩ / quản trị viên. Bệnh nhân chỉ qua {@link #registerUser(User)}.
     */
    public boolean addUser(User u) {
        if (u == null || u.getVaiTro() == null) {
            return false;
        }
        String role = u.getVaiTro().trim();
        if (!"quan_tri_vien".equals(role) && !"bac_si".equals(role)) {
            return false;
        }
        String id = UUID.randomUUID().toString();
        String hashed = hashSHA256(u.getMatKhauHash());

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String insertUser = "INSERT INTO users (id, ho_ten, email, so_dien_thoai, mat_khau_hash, kich_hoat, role, ngay_tao) "
                    + "VALUES (?, ?, ?, ?, ?, 1, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, id);
                ps.setString(2, u.getHoTen());
                ps.setString(3, u.getEmail());
                ps.setString(4, u.getSoDienThoai());
                ps.setString(5, hashed);
                ps.setString(6, role);
                if (ps.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            if ("bac_si".equals(role)) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO doctors (id, chuyen_khoa) VALUES (?, NULL)")) {
                    ps.setString(1, id);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm user mới: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public boolean updateUserStatus(String userId, int status) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        String sql = "UPDATE users SET kich_hoat = ?, ngay_cap_nhat = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái user: " + e.getMessage());
        }
        return false;
    }

    /** Đảm bảo có dòng doctors cho user bác sĩ. */
    public boolean ensureDoctorProfile(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        String sql = "INSERT IGNORE INTO doctors (id, chuyen_khoa) VALUES (?, NULL)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String nextPatientCode(Connection conn) throws SQLException {
        String sql = "SELECT LPAD(COALESCE(MAX(CAST(SUBSTRING(patient_code, 3) AS UNSIGNED)), 0) + 1, 4, '0') AS next_num "
                + "FROM patients WHERE patient_code REGEXP '^BN[0-9]+$'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString("next_num") != null) {
                return "BN" + rs.getString("next_num");
            }
        }
        return "BN" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private List<Object> buildFilterParams(StringBuilder sql, String role, String status, String keyword) {
        List<Object> params = new ArrayList<>();
        if (isValidRole(role)) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        if (isValidStatus(status)) {
            sql.append(" AND kich_hoat = ?");
            params.add(Integer.parseInt(status.trim()));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (ho_ten LIKE ? OR email LIKE ? OR so_dien_thoai LIKE ?)");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        return params;
    }

    private int queryCount(String sql, List<Object> params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm tổng user: " + e.getMessage());
        }
        return 0;
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        String normalizedRole = role.trim();
        return "quan_tri_vien".equals(normalizedRole)
                || "bac_si".equals(normalizedRole)
                || "benh_nhan".equals(normalizedRole);
    }

    private boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalizedStatus = status.trim();
        return "0".equals(normalizedStatus) || "1".equals(normalizedStatus);
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setHoTen(rs.getString("ho_ten"));
        u.setEmail(rs.getString("email"));
        u.setSoDienThoai(rs.getString("so_dien_thoai"));
        u.setVaiTro(rs.getString("vai_tro"));
        u.setMatKhauHash(rs.getString("mat_khau_hash"));
        u.setAnhDaiDien(rs.getString("anh_dai_dien"));
        u.setKichHoat(rs.getInt("kich_hoat"));
        u.setNgayTao(rs.getTimestamp("ngay_tao"));
        u.setNgayCapNhat(rs.getTimestamp("ngay_cap_nhat"));
        u.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));
        return u;
    }
}
