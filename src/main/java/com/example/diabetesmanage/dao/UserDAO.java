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
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static UserDAO instance;

    private static final String ACCOUNT_COLUMNS =
            "id, ho_ten, email, so_dien_thoai, mat_khau_hash, anh_dai_dien, kich_hoat, ngay_tao, ngay_cap_nhat, lan_dang_nhap_cuoi";

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
        String sql =
                "SELECT ho_ten FROM doctors WHERE id = ? " +
                "UNION ALL SELECT ho_ten FROM admins WHERE id = ? " +
                "UNION ALL SELECT ho_ten FROM patients WHERE id = ? LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            ps.setString(3, userId);
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
        String table = tableForRole(role.trim());
        List<User> userList = new ArrayList<>();
        String sql = "SELECT " + ACCOUNT_COLUMNS + ", ? AS vai_tro FROM " + table;
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
        String sql =
                "SELECT " + ACCOUNT_COLUMNS + ", 'bac_si' AS vai_tro FROM doctors WHERE email = ? " +
                "UNION ALL SELECT " + ACCOUNT_COLUMNS + ", 'quan_tri_vien' AS vai_tro FROM admins WHERE email = ? " +
                "UNION ALL SELECT " + ACCOUNT_COLUMNS + ", 'benh_nhan' AS vai_tro FROM patients WHERE email = ? " +
                "LIMIT 1";
        try {
            Connection conn = DBContext.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Không kết nối được MySQL. Kiểm tra DBContext và dịch vụ MySQL.");
            }
            try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
                String trimmed = email.trim();
                ps.setString(1, trimmed);
                ps.setString(2, trimmed);
                ps.setString(3, trimmed);
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

    public User checkLogin(String email, String hashedPassword) {
        if (email == null || hashedPassword == null) {
            return null;
        }
        User user = getUserByEmail(email);
        if (user == null) {
            return null;
        }
        if (hashedPassword.trim().equalsIgnoreCase(user.getMatKhauHash().trim())) {
            return user;
        }
        return null;
    }

    public boolean updatePassword(String userId, String hashedPassword) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return updateInRoleTables(userId, "mat_khau_hash = ?", hashedPassword);
    }

    public boolean isEmailExists(String email) {
        return getUserByEmail(email) != null;
    }

    public boolean registerUser(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(java.util.UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO patients (id, ho_ten, email, so_dien_thoai, mat_khau_hash, kich_hoat, ngay_sinh, loai_tieu_duong, ngay_tao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, user.getId());
            st.setString(2, user.getHoTen());
            st.setString(3, user.getEmail());
            st.setString(4, user.getSoDienThoai());
            st.setString(5, user.getMatKhauHash());
            st.setBoolean(6, user.isKichHoat());
            st.setDate(7, java.sql.Date.valueOf("2000-01-01"));
            st.setString(8, "Type 2");
            st.setTimestamp(9, user.getNgayTao() != null ? user.getNgayTao() : new Timestamp(System.currentTimeMillis()));
            return st.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User authenticateAdmin(String email, String rawPassword) {
        String sql = "SELECT " + ACCOUNT_COLUMNS + ", 'quan_tri_vien' AS vai_tro FROM admins "
                + "WHERE email = ? AND mat_khau_hash = ? AND kich_hoat = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hashSHA256(rawPassword));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác thực đăng nhập Admin: " + e.getMessage());
        }
        return null;
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
        if (u == null || u.getId() == null || u.getVaiTro() == null) {
            return false;
        }
        String table = tableForRole(u.getVaiTro());
        String sql = "UPDATE " + table + " SET ho_ten = ?, email = ?, so_dien_thoai = ?, ngay_cap_nhat = NOW() WHERE id = ?";
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (");
        sql.append(buildUnionSelect(false));
        sql.append(") AS all_accounts WHERE 1=1");
        List<Object> params = buildFilterParams(sql, role, status, keyword, false);
        return queryCount(sql.toString(), params);
    }

    public List<User> getFilteredUsers(String role, String status, String keyword, int offset, int limit) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM (");
        sql.append(buildUnionSelect(true));
        sql.append(") AS all_accounts WHERE 1=1");
        List<Object> params = buildFilterParams(sql, role, status, keyword, false);
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

    public boolean addUser(User u) {
        if (u == null || u.getVaiTro() == null) {
            return false;
        }
        String table = tableForRole(u.getVaiTro());
        String sql = "INSERT INTO " + table
                + " (id, ho_ten, email, so_dien_thoai, mat_khau_hash, kich_hoat, ngay_tao) "
                + "VALUES (?, ?, ?, ?, ?, 1, NOW())";
        if ("patients".equals(table)) {
            sql = "INSERT INTO patients (id, ho_ten, email, so_dien_thoai, mat_khau_hash, kich_hoat, ngay_sinh, loai_tieu_duong, ngay_tao) "
                    + "VALUES (?, ?, ?, ?, ?, 1, '2000-01-01', 'Type 2', NOW())";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.util.UUID.randomUUID().toString());
            ps.setString(2, u.getHoTen());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getSoDienThoai());
            ps.setString(5, hashSHA256(u.getMatKhauHash()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm user mới: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserStatus(String userId, int status) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        if (updateStatusInTable("doctors", userId, status)) {
            return true;
        }
        if (updateStatusInTable("admins", userId, status)) {
            return true;
        }
        return updateStatusInTable("patients", userId, status);
    }

    private boolean updateStatusInTable(String table, String userId, int status) {
        String sql = "UPDATE " + table + " SET kich_hoat = ?, ngay_cap_nhat = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái " + table + ": " + e.getMessage());
        }
        return false;
    }

    private boolean updateInRoleTables(String userId, String setClause, String value) {
        for (String table : List.of("doctors", "admins", "patients")) {
            String sql = "UPDATE " + table + " SET " + setClause + ", ngay_cap_nhat = NOW() WHERE id = ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, value);
                ps.setString(2, userId);
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            } catch (SQLException ignored) {
                // try next table
            }
        }
        return false;
    }

    private String buildUnionSelect(boolean includeRole) {
        return "SELECT " + ACCOUNT_COLUMNS + ", 'bac_si' AS vai_tro FROM doctors " +
                "UNION ALL SELECT " + ACCOUNT_COLUMNS + ", 'quan_tri_vien' AS vai_tro FROM admins " +
                "UNION ALL SELECT " + ACCOUNT_COLUMNS + ", 'benh_nhan' AS vai_tro FROM patients";
    }

    private List<Object> buildFilterParams(StringBuilder sql, String role, String status, String keyword, boolean forCount) {
        List<Object> params = new ArrayList<>();
        if (isValidRole(role)) {
            sql.append(" AND vai_tro = ?");
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

    private String tableForRole(String role) {
        if ("bac_si".equals(role)) {
            return "doctors";
        }
        if ("quan_tri_vien".equals(role)) {
            return "admins";
        }
        return "patients";
    }

    private boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        String normalizedRole = role.trim();
        return "quan_tri_vien".equals(normalizedRole)
                || "bac_si".equals(normalizedRole)
                || "y_ta".equals(normalizedRole)
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
