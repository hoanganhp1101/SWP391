package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> getUsersByRole(String role) {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE vai_tro = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setHoTen(rs.getString("ho_ten"));
                    user.setEmail(rs.getString("email"));
                    // Đừng quên ánh xạ các trường khác nếu Model User của bạn có
                    // Ví dụ: user.setSoDienThoai(rs.getString("so_dien_thoai"));
                    user.setVaiTro(rs.getString("vai_tro"));

                    userList.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public User authenticateAdmin(String email, String rawPassword) {
        String sql = "SELECT * FROM users WHERE email = ? AND mat_khau_hash = ? AND vai_tro = 'quan_tri_vien' AND kich_hoat = 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            // TỰ ĐỘNG BĂM MẬT KHẨU THÔ THÀNH SHA-256 TRƯỚC KHI SO SÁNH
            String hashedPassword = hashSHA256(rawPassword);
            ps.setString(2, hashedPassword);

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
            return hexString.toString(); // Trả về chuỗi hash viết thường giống hệt trong DB
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateUser(User u) {
        String sql = "UPDATE users SET ho_ten = ?, email = ?, so_dien_thoai = ?, vai_tro = ?, ngay_cap_nhat = NOW() WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getHoTen());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSoDienThoai());
            ps.setString(4, u.getVaiTro());
            ps.setString(5, u.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật user: " + e.getMessage());
        }
        return false;
    }

    public int getTotalUsersCount(String role, String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
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

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public List<User> getFilteredUsers(String role, String status, String keyword, int offset, int limit) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
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

        sql.append(" ORDER BY ngay_tao DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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
        String sql = "INSERT INTO users" +
                " (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash, kich_hoat, ngay_tao)" +
                " VALUES (?, ?, ?, ?, ?, ?, 1, NOW())";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, java.util.UUID.randomUUID().toString());
            ps.setString(2, u.getHoTen());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getSoDienThoai());
            ps.setString(5, u.getVaiTro());
            ps.setString(6, hashSHA256(u.getMatKhauHash()));

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm user mới: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserStatus(String userId, int status) {
        String sql = "UPDATE users SET kich_hoat = ?, ngay_cap_nhat = NOW() WHERE id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setString(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái user: " + e.getMessage());
        }
        return false;
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
