package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.example.diabetesmanage.model.User;

public class UserDAO extends DBContext {

    public static UserDAO instance;
    public Connection connection = DBContext.getConnection();

    public UserDAO() {
        super();
    }

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
        String sql = "SELECT ho_ten FROM users WHERE id = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("ho_ten") : null;
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "getNameById error", e);
            return null;
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();

        u.setId(UUID.fromString(rs.getString("id")));
        u.setHoTen(rs.getString("ho_ten"));
        u.setEmail(rs.getString("email"));
        u.setSoDienThoai(rs.getString("so_dien_thoai"));
        u.setVaiTro(rs.getString("vai_tro"));
        u.setMatKhauHash(rs.getString("mat_khau_hash"));
        u.setAnhDaiDien(rs.getString("anh_dai_dien"));
        u.setKichHoat(rs.getBoolean("kich_hoat"));
        u.setNgayTao(rs.getTimestamp("ngay_tao"));
        u.setNgayCapNhat(rs.getTimestamp("ngay_cap_nhat"));
        u.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));

        return u;
    }

    // ── Find by Email ─────────────────────────────────────────────────────────
    /**
     * Tìm user theo Email.
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "getUserByemail error", e);
        }
        return null;
    }

    public User checkLogin(String email, String hashedPassword) {
        if (email == null || hashedPassword == null) {

            return null;
        }

        // 1. Tìm user trong DB theo Email
        User user = getUserByEmail(email);

        if (user == null) {

            return null;
        }

        // In ra để bạn nhìn tận mắt hai chuỗi có khớp nhau không ở tab Output
        // 2. Sửa từ .equals() sang .trim().equalsIgnoreCase() để chống lỗi khoảng trắng và chữ hoa/thường
        if (hashedPassword.trim().equalsIgnoreCase(user.getMatKhauHash().trim())) {

            return user;
        }

        System.out.println("DEBUG: Tìm thấy tài khoản nhưng MẬT KHẨU KHÔNG KHỚP!");
        return null;
    }

    public boolean updatePassword(UUID userId, String hashedPassword) {
        String sql = "UPDATE users SET mat_khau_hash = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, userId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "updatePassword error", e);
        }
        return false;
    }

    // ================= CHECK DUPLICATE =================
    public boolean isEmailExists(String email) {
        // Kiểm tra xem chữ 'email' và 'users' có viết hoa/thường chuẩn khớp với DB không
        String sql = "SELECT email FROM users WHERE email = ?";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return true; // Nếu tìm thấy bản ghi nghĩa là ĐÃ TỒN TẠI
                }
            }
        } catch (Exception e) {
            System.out.println("dal.UserDAO.isEmailExists error");
            e.printStackTrace(); // <-- Lỗi thật sự sẽ in ra ở đây trong tab Console
        }
        return false;
    }

    /**
     * Thêm tài khoản người dùng mới vào database.
     */
    public boolean registerUser(User user) {
        // Thêm cột so_dien_thoai vào danh sách cột và thêm một dấu hỏi (?) tương ứng
        String sql = "INSERT INTO users (ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash, kich_hoat, ngay_tao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            // Lưu ý sắp xếp chuẩn xác thứ tự các dấu ? dựa trên câu SQL trên
            st.setString(1, user.getHoTen());
            st.setString(2, user.getEmail());
            st.setString(3, user.getSoDienThoai()); // Cột số điện thoại mới thêm
            st.setString(4, user.getVaiTro());
            st.setString(5, user.getMatKhauHash());
            st.setBoolean(6, user.isKichHoat());
            st.setTimestamp(7, user.getNgayTao());

            int row = st.executeUpdate();
            return row > 0;
        } catch (Exception e) {
            System.out.println("dal.UserDAO.registerUser registerUser error");
            e.printStackTrace();
        }
        return false;
    }
}