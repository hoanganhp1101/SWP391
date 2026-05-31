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

    public List<User> getAllUsers() {

        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY ngay_tao DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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

                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thực hiện getAllUsers(): " + e.getMessage());
        }
        return list;
    }
}
