package dal;

import config.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

public class UserDAO extends DBContext {

    public static UserDAO instance;

    public UserDAO() {
        super();
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
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

    /**
     * @deprecated Dùng getUserByEmail(email) thay thế.
     */
    @Deprecated
    public User findUserByUsername(String email) {
        return getUserByEmail(email);
    }

    public User checkLogin(String emailOrUsername, String hashedPassword) {
        if (emailOrUsername == null || hashedPassword == null) {
            return null;
        }

        User user = getUserByEmail(emailOrUsername);
        if (user == null) {
            user = getUserByUsername(emailOrUsername);
        }
        if (user != null && hashedPassword.equals(user.getMatKhauHash())) {
            return user;
        }
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

    // ── Google OAuth ─────────────────────────────────────────────────────────
    /**
     * Tìm user theo GoogleId.
     */
    public User getUserByGoogleId(String googleId) {
        if (googleId == null || googleId.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT Id, Username, PasswordHash, FullName, email, Role, CreatedAt, UpdatedAt FROM User WHERE GoogleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, googleId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "getUserByGoogleId error", e);
        }
        return null;
    }

    public User createOrUpdateGoogleUser(String email, String fullName, String googleId) {
        // Kiểm tra xem email đã có chưa
        User existing = getUserByEmail(email);
        if (existing != null) {
            // Gắn GoogleId vào account hiện có
            String sql = "UPDATE User SET GoogleId = ? WHERE Id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, googleId);
                ps.setString(2, existing.getId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Link GoogleId error", e);
            }
            
            return existing;
        }

        // Tạo user mới từ Google
//        String username = email.split("@")[0]; // lấy phần trước @ làm username
//        String sql = """
//                INSERT INTO User (Username, PasswordHash, FullName, Email, Role, GoogleId)
//                VALUES (?, '', ?, ?, 'Employee', ?)
//                """;
//        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setString(1, username);
//            ps.setString(2, fullName);
//            ps.setString(3, email);
//            ps.setString(4, googleId);
//            ps.executeUpdate();
//            try (ResultSet keys = ps.getGeneratedKeys()) {
//                if (keys.next()) {
//                    return getUserByEmail(email); // reload fresh record
//                }
//            }
//        } catch (SQLException e) {
//            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "createOrUpdateGoogleUser error", e);
//        }
        return null;
    }

    // ================= CHECK DUPLICATE =================
    

    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM User WHERE email=?";
        return checkExists(sql, email);
    }

    public boolean isemailExistsForUpdate(String email, UUID id) {
        String sql = "SELECT 1 FROM users WHERE email=? AND id<>?";
        try (
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, id.toString());

            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkExists(String sql, String value) {
        try (
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, value);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

}
