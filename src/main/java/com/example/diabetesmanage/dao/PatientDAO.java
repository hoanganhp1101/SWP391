package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public List<Patient> getPatientsByDoctor(String doctorId) {

        List<Patient> list = new ArrayList<>();

        String sql =
                "SELECT p.*, u.ho_ten " +
                        "FROM patients p " +
                        "JOIN users u ON p.user_id = u.id " +
                        "WHERE p.bac_si_id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, doctorId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Patient p = new Patient();

                p.setId(rs.getString("id"));

                User user = new User();
                user.setHoTen(rs.getString("ho_ten"));

                p.setUser(user);

                p.setLoaiTieuDuong(
                        rs.getString("loai_tieu_duong")
                );

                p.setGioiTinh(
                        rs.getString("gioi_tinh")
                );

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Patient getPatientByIdAndDoctor(
            String patientId,
            String doctorId) {

        String sql =
                "SELECT p.*, u.ho_ten " +
                        "FROM patients p " +
                        "JOIN users u ON p.user_id = u.id " +
                        "WHERE p.id = ? " +
                        "AND p.bac_si_id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, patientId);
            ps.setString(2, doctorId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Patient p = new Patient();

                p.setId(rs.getString("id"));

                User user = new User();
                user.setHoTen(rs.getString("ho_ten"));

                p.setUser(user);

                p.setLoaiTieuDuong(
                        rs.getString("loai_tieu_duong")
                );

                p.setDiaChi(
                        rs.getString("dia_chi")
                );

                p.setTienSuBenh(
                        rs.getString("tien_su_benh")
                );

                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
