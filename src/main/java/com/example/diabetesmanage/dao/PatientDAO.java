package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public List<Patient> getPatients() {

        List<Patient> list = new ArrayList<>();

        String sql =
                "SELECT vps.* " +
                        "FROM v_patient_summary vps " +
                        "JOIN patients p ON vps.patient_id = p.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, "bacsi@example.com");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Patient p = new Patient();

                p.setId(rs.getString("patient_id"));

                User user = new User();
                user.setHoTen(rs.getString("ho_ten"));
                user.setEmail(rs.getString("email"));

                p.setUser(user);

                p.setTuoi(rs.getInt("tuoi"));
                p.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));
                p.setGioiTinh(rs.getString("gioi_tinh"));
                p.setNgayCapNhat(rs.getTimestamp("lan_do_cuoi"));

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
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
            String patientId) {

        String sql =
                "SELECT " +
                        "p.*, " +
                        "TIMESTAMPDIFF(YEAR, p.ngay_sinh, CURDATE()) AS tuoi, " +
                        "u.id AS user_id, " +
                        "u.ho_ten, " +
                        "u.email, " +
                        "u.so_dien_thoai " +
                        "FROM patients p " +
                        "JOIN users u ON p.user_id = u.id " +
                        "WHERE p.id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, patientId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Patient p = new Patient();

                p.setId(rs.getString("id"));

                User user = new User();
                user.setId(rs.getString("user_id"));
                user.setHoTen(rs.getString("ho_ten"));
                user.setEmail(rs.getString("email"));
                user.setSoDienThoai(rs.getString("so_dien_thoai"));

                p.setUser(user);

                p.setTuoi(rs.getInt("tuoi"));
                p.setNgaySinh(rs.getDate("ngay_sinh").toLocalDate());
                p.setGioiTinh(rs.getString("gioi_tinh"));
                p.setDiaChi(rs.getString("dia_chi"));

                p.setBaoHiemYTe(
                        rs.getString("bao_hiem_y_te")
                );

                p.setTienSuBenh(
                        rs.getString("tien_su_benh")
                );

                p.setDiUng(
                        rs.getString("di_ung")
                );

                p.setNhomMau(
                        rs.getString("nhom_mau")
                );

                p.setLoaiTieuDuong(
                        rs.getString("loai_tieu_duong")
                );

                Date diagnosisDate =
                        rs.getDate("ngay_chan_doan_tieu_duong");

                if (diagnosisDate != null) {
                    p.setNgayChanDoanTieuDuong(
                            diagnosisDate.toLocalDate()
                    );
                }

                Timestamp updatedAt =
                        rs.getTimestamp("ngay_cap_nhat");

                p.setNgayCapNhat(updatedAt);

                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
