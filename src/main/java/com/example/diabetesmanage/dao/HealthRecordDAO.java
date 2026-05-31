package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.HealthRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO {

    public List<HealthRecord> getByPatient(String patientId) {

        List<HealthRecord> list = new ArrayList<>();

        String sql =
                "SELECT * " +
                        "FROM health_records " +
                        "WHERE patient_id = ? " +
                        "ORDER BY thoi_gian_do DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, patientId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(rs.getString("id"));
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                hr.setHba1cPercent(rs.getDouble("hba1c_percent"));
                hr.setBmi(rs.getDouble("bmi"));
                hr.setCanNangKg(rs.getDouble("can_nang_kg"));

                Timestamp timestamp =
                        rs.getTimestamp("thoi_gian_do");

                if (timestamp != null) {
                    hr.setThoiGianDo(
                            timestamp.toLocalDateTime()
                    );
                }

                list.add(hr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public HealthRecord getLatestRecord(String patientId) {

        String sql =
                "SELECT * " +
                        "FROM health_records " +
                        "WHERE patient_id = ? " +
                        "ORDER BY thoi_gian_do DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, patientId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(rs.getString("id"));
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                hr.setHba1cPercent(rs.getDouble("hba1c_percent"));
                hr.setBmi(rs.getDouble("bmi"));
                hr.setCanNangKg(rs.getDouble("can_nang_kg"));

                Timestamp timestamp =
                        rs.getTimestamp("thoi_gian_do");

                if (timestamp != null) {
                    hr.setThoiGianDo(
                            timestamp.toLocalDateTime()
                    );
                }

                return hr;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public HealthRecord getRecordById(String recordId) {

        String sql =
                "SELECT * " +
                        "FROM health_records " +
                        "WHERE id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, recordId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(rs.getString("id"));
                hr.setDuongHuyetMgdl(rs.getDouble("duong_huyet_mgdl"));
                hr.setHba1cPercent(rs.getDouble("hba1c_percent"));
                hr.setBmi(rs.getDouble("bmi"));
                hr.setCanNangKg(rs.getDouble("can_nang_kg"));

                Timestamp timestamp =
                        rs.getTimestamp("thoi_gian_do");

                if (timestamp != null) {
                    hr.setThoiGianDo(
                            timestamp.toLocalDateTime()
                    );
                }

                return hr;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(HealthRecord hr) {

        String sql =
                "INSERT INTO health_records(" +
                        "patient_id," +
                        "duong_huyet_mgdl," +
                        "hba1c_percent," +
                        "bmi," +
                        "can_nang_kg," +
                        "thoi_gian_do" +
                        ") VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    hr.getPatient().getId()
            );

            ps.setDouble(
                    2,
                    hr.getDuongHuyetMgdl()
            );

            ps.setDouble(
                    3,
                    hr.getHba1cPercent()
            );

            ps.setDouble(
                    4,
                    hr.getBmi()
            );

            ps.setDouble(
                    5,
                    hr.getCanNangKg()
            );

            ps.setTimestamp(
                    6,
                    Timestamp.valueOf(
                            hr.getThoiGianDo()
                    )
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}