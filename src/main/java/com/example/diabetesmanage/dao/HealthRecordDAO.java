package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO {

    public List<HealthRecord> getHealthRecord() {

        List<HealthRecord> list = new ArrayList<>();

        String sql =
                "SELECT hr.*,p.patient_code " +

                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id "+
                        "ORDER BY thoi_gian_do DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(rs.getString("id"));
                hr.setHealthRecordId(rs.getString("health_record_code"));
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

                if (timestamp != null) {

                    LocalDate lastVisitDate =
                            timestamp.toLocalDateTime().toLocalDate();

                    int daysSinceLastVisit =
                            (int) ChronoUnit.DAYS.between(
                                    lastVisitDate,
                                    LocalDate.now()
                            );

                    hr.setDaysSinceLastVisit(daysSinceLastVisit);
                    Patient patient = new Patient();
                    patient.setPatientCode(
                            rs.getString("patient_code")
                    );
                    hr.setPatient(patient);
                }

                list.add(hr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public HealthRecord getHealthRecordRecordById(String recordId) {

        String sql =
        "SELECT hr.*, " +
                "p.id AS patient_id, " +
                "p.patient_code," +
                "u.ho_ten " +
                "FROM health_records hr " +
                "JOIN patients p ON hr.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE hr.id = ?";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, recordId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(rs.getString("id"));
                hr.setHealthRecordId(rs.getString("health_record_code"));

                hr.setDuongHuyetMgdl(
                        rs.getDouble("duong_huyet_mgdl")
                );

                hr.setThoiDiemDoDuong(
                        rs.getString("thoi_diem_do_duong")
                );

                hr.setHuyetApTamThu(
                        rs.getInt("huyet_ap_tam_thu")
                );

                hr.setHuyetApTamTruong(
                        rs.getInt("huyet_ap_tam_truong")
                );

                hr.setNhipTim(
                        rs.getInt("nhip_tim")
                );

                hr.setCanNangKg(
                        rs.getDouble("can_nang_kg")
                );

                hr.setBmi(
                        rs.getDouble("bmi")
                );

                hr.setHba1cPercent(
                        rs.getDouble("hba1c_percent")
                );

                hr.setCholesterolMmol(
                        rs.getDouble("cholesterol_mmol")
                );

                hr.setTriglycerideMmol(
                        rs.getDouble("triglyceride_mmol")
                );

                hr.setSoBuocChan(
                        rs.getInt("so_buoc_chan")
                );

                hr.setCarbsG(
                        rs.getDouble("carbs_g")
                );

                hr.setSoGioNgu(
                        rs.getDouble("so_gio_ngu")
                );

                hr.setLieuLuongInsulinUi(
                        rs.getInt("lieu_luong_insulin_ui")
                );

                hr.setLoaiInsulinTiem(
                        rs.getString("loai_insulin_tiem")
                );

                hr.setGhiChu(
                        rs.getString("ghi_chu")
                );

                Timestamp timestamp =
                        rs.getTimestamp("thoi_gian_do");

                if (timestamp != null) {
                    hr.setThoiGianDo(
                            timestamp.toLocalDateTime()
                    );
                }

                Patient patient = new Patient();
                patient.setPatientCode(
                        rs.getString("patient_code")
                );

                User user = new User();
                user.setHoTen(
                        rs.getString("ho_ten")
                );

                patient.setUser(user);

                hr.setPatient(patient);



                return hr;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<HealthRecord> searchHealthRecordRecords(
            String startDate,
            String endDate,
            String keyword) {

        List<HealthRecord> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT hr.*, p.patient_code " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "WHERE 1=1 "
        );

        // Filter theo ngày
        if (startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank()) {

            sql.append(
                    "AND DATE(hr.thoi_gian_do) BETWEEN ? AND ? "
            );
        }

        // Filter theo keyword
        if (keyword != null && !keyword.isBlank()) {

            sql.append(
                    "AND ( " +
                            "hr.health_record_code LIKE ? " +
                            "OR p.patient_code LIKE ? " +
                            ") "
            );
        }

        // ORDER BY luôn để cuối
        sql.append(
                "ORDER BY hr.thoi_gian_do DESC"
        );

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql.toString())
        ) {

            int index = 1;

            // Set ngày
            if (startDate != null && !startDate.isBlank()
                    && endDate != null && !endDate.isBlank()) {

                ps.setString(index++, startDate);
                ps.setString(index++, endDate);
            }

            // Set keyword
            if (keyword != null && !keyword.isBlank()) {

                String search = "%" + keyword + "%";

                ps.setString(index++, search);
                ps.setString(index++, search);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HealthRecord hr = new HealthRecord();

                hr.setId(
                        rs.getString("id")
                );

                hr.setHealthRecordId(
                        rs.getString("health_record_code")
                );

                hr.setDuongHuyetMgdl(
                        rs.getDouble("duong_huyet_mgdl")
                );

                hr.setHba1cPercent(
                        rs.getDouble("hba1c_percent")
                );

                hr.setBmi(
                        rs.getDouble("bmi")
                );

                hr.setCanNangKg(
                        rs.getDouble("can_nang_kg")
                );

                Timestamp timestamp =
                        rs.getTimestamp("thoi_gian_do");

                if (timestamp != null) {

                    hr.setThoiGianDo(
                            timestamp.toLocalDateTime()
                    );

                    LocalDate lastVisitDate =
                            timestamp.toLocalDateTime()
                                    .toLocalDate();

                    int daysSinceLastVisit =
                            (int) ChronoUnit.DAYS.between(
                                    lastVisitDate,
                                    LocalDate.now()
                            );

                    hr.setDaysSinceLastVisit(
                            daysSinceLastVisit
                    );
                }

                Patient patient = new Patient();

                patient.setPatientCode(
                        rs.getString("patient_code")
                );

                hr.setPatient(patient);

                list.add(hr);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }

    public java.util.Map<String, java.util.List<HealthRecord>> getRecentRecordsGroupedByPatient(
            String doctorEmail,
            int maxRecordsPerPatient) {

        java.util.Map<String, java.util.List<HealthRecord>> grouped = new java.util.LinkedHashMap<>();

        String sql =
                "SELECT hr.*, " +
                        "p.id AS patient_id, " +
                        "p.patient_code, " +
                        "p.loai_tieu_duong, " +
                        "u.ho_ten " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users u ON p.user_id = u.id " +
                        "JOIN users d ON p.bac_si_id = d.id " +
                        "WHERE d.email = ? " +
                        "ORDER BY p.id, hr.thoi_gian_do DESC";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, doctorEmail);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String patientId = rs.getString("patient_id");
                java.util.List<HealthRecord> records =
                        grouped.computeIfAbsent(patientId, k -> new ArrayList<>());

                if (records.size() >= maxRecordsPerPatient) {
                    continue;
                }

                HealthRecord hr = mapDetailedHealthRecord(rs);
                records.add(hr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return grouped;
    }

    private HealthRecord mapDetailedHealthRecord(ResultSet rs) throws SQLException {

        HealthRecord hr = new HealthRecord();

        hr.setId(rs.getString("id"));
        hr.setHealthRecordId(rs.getString("health_record_code"));

        double glucose = rs.getDouble("duong_huyet_mgdl");
        if (!rs.wasNull()) {
            hr.setDuongHuyetMgdl(glucose);
        }

        double hba1c = rs.getDouble("hba1c_percent");
        if (!rs.wasNull()) {
            hr.setHba1cPercent(hba1c);
        }

        double bmi = rs.getDouble("bmi");
        if (!rs.wasNull()) {
            hr.setBmi(bmi);
        }

        int systolic = rs.getInt("huyet_ap_tam_thu");
        if (!rs.wasNull()) {
            hr.setHuyetApTamThu(systolic);
        }

        int diastolic = rs.getInt("huyet_ap_tam_truong");
        if (!rs.wasNull()) {
            hr.setHuyetApTamTruong(diastolic);
        }

        int insulin = rs.getInt("lieu_luong_insulin_ui");
        if (!rs.wasNull()) {
            hr.setLieuLuongInsulinUi(insulin);
        }

        Timestamp timestamp = rs.getTimestamp("thoi_gian_do");
        if (timestamp != null) {
            hr.setThoiGianDo(timestamp.toLocalDateTime());

            LocalDate lastVisitDate = timestamp.toLocalDateTime().toLocalDate();
            hr.setDaysSinceLastVisit(
                    (int) ChronoUnit.DAYS.between(lastVisitDate, LocalDate.now())
            );
        }

        Patient patient = new Patient();
        patient.setId(rs.getString("patient_id"));
        patient.setPatientCode(rs.getString("patient_code"));
        patient.setLoaiTieuDuong(rs.getString("loai_tieu_duong"));

        User user = new User();
        user.setHoTen(rs.getString("ho_ten"));
        patient.setUser(user);

        hr.setPatient(patient);

        return hr;
    }
    public HealthRecord getLatestHealthRecordByPatientId(String patientId) {

        String sql =
                "SELECT hr.*, " +
                        "p.id AS patient_id, " +
                        "p.patient_code, " +
                        "p.loai_tieu_duong, " +
                        "u.ho_ten " +
                        "FROM health_records hr " +
                        "JOIN patients p ON hr.patient_id = p.id " +
                        "JOIN users u ON p.user_id = u.id " +
                        "WHERE p.id = ? " +
                        "ORDER BY hr.thoi_gian_do DESC " +
                        "LIMIT 1";

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, patientId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapDetailedHealthRecord(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}