package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.HighRiskPatient;
import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.service.ClinicalRiskService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HighRiskPatientDAO {

    public List<HighRiskPatient> getMonitoredPatients(String keyword, String riskLevel) {
        List<HighRiskPatient> patients = getPatientsWithLatestMetrics(keyword);
        patients.forEach(this::calculateRisk);

        return patients.stream()
                .filter(patient -> riskLevel == null || riskLevel.trim().isEmpty()
                        || riskLevel.equals(patient.getRiskLevel()))
                .sorted(Comparator.comparingInt(HighRiskPatient::getRiskScore).reversed()
                        .thenComparing(HighRiskPatient::getPatientName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private List<HighRiskPatient> getPatientsWithLatestMetrics(String keyword) {
        List<HighRiskPatient> patients = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id AS patient_id, u.ho_ten, u.email, u.so_dien_thoai, p.loai_tieu_duong, " +
                "doctor.ho_ten AS doctor_name, " +
                "(SELECT hr.duong_huyet_mgdl FROM health_records hr WHERE hr.patient_id = p.id AND hr.duong_huyet_mgdl IS NOT NULL ORDER BY hr.thoi_gian_do DESC LIMIT 1) AS latest_glucose, " +
                "(SELECT hr.hba1c_percent FROM health_records hr WHERE hr.patient_id = p.id AND hr.hba1c_percent IS NOT NULL ORDER BY hr.thoi_gian_do DESC LIMIT 1) AS latest_hba1c, " +
                "(SELECT hr.huyet_ap_tam_thu FROM health_records hr WHERE hr.patient_id = p.id AND hr.huyet_ap_tam_thu IS NOT NULL ORDER BY hr.thoi_gian_do DESC LIMIT 1) AS latest_systolic, " +
                "(SELECT hr.huyet_ap_tam_truong FROM health_records hr WHERE hr.patient_id = p.id AND hr.huyet_ap_tam_truong IS NOT NULL ORDER BY hr.thoi_gian_do DESC LIMIT 1) AS latest_diastolic, " +
                "(SELECT hr.bmi FROM health_records hr WHERE hr.patient_id = p.id AND hr.bmi IS NOT NULL ORDER BY hr.thoi_gian_do DESC LIMIT 1) AS latest_bmi, " +
                "(SELECT MAX(hr.thoi_gian_do) FROM health_records hr WHERE hr.patient_id = p.id) AS last_measurement_time, " +
                "(SELECT COUNT(*) FROM alerts a WHERE a.patient_id = p.id AND a.muc_do IN ('cao', 'nguy_hiem') AND a.thoi_gian_tao >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS recent_alert_count, " +
                "(SELECT COUNT(*) FROM alerts a WHERE a.patient_id = p.id AND a.da_doc_bs = 0 AND a.muc_do IN ('cao', 'nguy_hiem')) AS unread_doctor_alert_count " +
                "FROM patients p " +
                "JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN users doctor ON p.bac_si_id = doctor.id " +
                "WHERE u.kich_hoat = 1");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (u.ho_ten LIKE ? OR u.email LIKE ? OR u.so_dien_thoai LIKE ?)");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapPatient(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return patients;
    }

    private HighRiskPatient mapPatient(ResultSet rs) throws Exception {
        HighRiskPatient patient = new HighRiskPatient();
        patient.setPatientId(rs.getString("patient_id"));
        patient.setPatientName(rs.getString("ho_ten"));
        patient.setEmail(rs.getString("email"));
        patient.setPhone(rs.getString("so_dien_thoai"));
        patient.setDiabetesType(rs.getString("loai_tieu_duong"));
        patient.setDoctorName(rs.getString("doctor_name"));
        patient.setLatestGlucose(getNullableDouble(rs, "latest_glucose"));
        patient.setLatestHba1c(getNullableDouble(rs, "latest_hba1c"));
        patient.setSystolicBloodPressure(getNullableInteger(rs, "latest_systolic"));
        patient.setDiastolicBloodPressure(getNullableInteger(rs, "latest_diastolic"));
        patient.setBmi(getNullableDouble(rs, "latest_bmi"));
        patient.setLastMeasurementTime(rs.getTimestamp("last_measurement_time"));
        patient.setRecentAlertCount(rs.getInt("recent_alert_count"));
        patient.setUnreadDoctorAlertCount(rs.getInt("unread_doctor_alert_count"));
        return patient;
    }

    void calculateRisk(HighRiskPatient patient) {
        int score = 0;

        // Ngưỡng lấy từ ClinicalRiskService để đồng bộ với pipeline alerts
        // và bảng xếp hạng nguy hiểm phía bác sĩ.
        Double glucose = patient.getLatestGlucose();
        if (glucose == null) {
            score += 15;
            patient.getRiskReasons().add("Chưa có dữ liệu đường huyết");
        } else if (glucose >= ClinicalRiskService.GLUCOSE_CRITICAL_MGDL
                || glucose <= ClinicalRiskService.GLUCOSE_LOW_MGDL) {
            score += 50;
            patient.getRiskReasons().add("Đường huyết ở ngưỡng nguy hiểm: " + formatNumber(glucose) + " mg/dL");
        } else if (glucose >= ClinicalRiskService.GLUCOSE_HIGH_MGDL) {
            score += 20;
            patient.getRiskReasons().add("Đường huyết cao: " + formatNumber(glucose) + " mg/dL");
        }

        Double hba1c = patient.getLatestHba1c();
        if (hba1c != null) {
            if (hba1c >= ClinicalRiskService.HBA1C_CRITICAL) {
                score += 35;
                patient.getRiskReasons().add("HbA1c rất cao: " + formatNumber(hba1c) + "%");
            } else if (hba1c >= ClinicalRiskService.HBA1C_HIGH) {
                score += 15;
                patient.getRiskReasons().add("HbA1c cần theo dõi: " + formatNumber(hba1c) + "%");
            }
        }

        Integer systolic = patient.getSystolicBloodPressure();
        Integer diastolic = patient.getDiastolicBloodPressure();
        if (systolic != null && diastolic != null) {
            if (systolic >= ClinicalRiskService.BP_SYS_DANGER
                    || diastolic >= ClinicalRiskService.BP_DIA_DANGER) {
                score += 35;
                patient.getRiskReasons().add("Huyết áp nguy hiểm: " + systolic + "/" + diastolic + " mmHg");
            } else if (systolic >= ClinicalRiskService.BP_SYS_WATCH
                    || diastolic >= ClinicalRiskService.BP_DIA_WATCH) {
                score += 15;
                patient.getRiskReasons().add("Huyết áp cần theo dõi: " + systolic + "/" + diastolic + " mmHg");
            }
        }

        Double bmi = patient.getBmi();
        if (bmi != null) {
            if (bmi >= ClinicalRiskService.BMI_HIGH) {
                score += 15;
                patient.getRiskReasons().add("BMI cao: " + formatNumber(bmi));
            } else if (bmi >= ClinicalRiskService.BMI_OVERWEIGHT) {
                score += 8;
                patient.getRiskReasons().add("BMI vượt chuẩn: " + formatNumber(bmi));
            }
        }

        if (patient.getRecentAlertCount() > 0) {
            score += Math.min(25, patient.getRecentAlertCount() * 8);
            patient.getRiskReasons().add(patient.getRecentAlertCount() + " cảnh báo cao/nguy hiểm trong 7 ngày");
        }

        if (patient.getUnreadDoctorAlertCount() > 0) {
            score += Math.min(15, patient.getUnreadDoctorAlertCount() * 5);
            patient.getRiskReasons().add(patient.getUnreadDoctorAlertCount() + " cảnh báo bác sĩ chưa đọc");
        }

        if (isMeasurementStale(patient.getLastMeasurementTime())) {
            score += 12;
            patient.getRiskReasons().add("Chưa cập nhật chỉ số trong hơn 7 ngày");
        }

        patient.setRiskScore(Math.min(score, 100));
        patient.setRiskLevel(resolveRiskLevel(patient.getRiskScore()));
        if (patient.getRiskReasons().isEmpty()) {
            patient.getRiskReasons().add("Chưa phát hiện dấu hiệu bất thường từ dữ liệu hiện có");
        }
    }

    private boolean isMeasurementStale(Timestamp lastMeasurementTime) {
        if (lastMeasurementTime == null) {
            return true;
        }

        long days = Duration.between(lastMeasurementTime.toInstant(), Instant.now()).toDays();
        return days > ClinicalRiskService.MONITORING_GAP_DAYS;
    }

    private String resolveRiskLevel(int score) {
        // Thang chung critical/high/medium/low — khớp với phía bác sĩ
        return ClinicalRiskService.resolveRiskLevel(score);
    }

    private Double getNullableDouble(ResultSet rs, String column) throws Exception {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private Integer getNullableInteger(ResultSet rs, String column) throws Exception {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private String formatNumber(Double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
