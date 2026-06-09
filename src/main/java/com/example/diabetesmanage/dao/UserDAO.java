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

        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND vai_tro = ?");
            params.add(role);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND kich_hoat = ?");
            params.add(Integer.parseInt(status));
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

        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND vai_tro = ?");
            params.add(role);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND kich_hoat = ?");
            params.add(Integer.parseInt(status));
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
            ps.setString(6, u.getMatKhauHash());

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