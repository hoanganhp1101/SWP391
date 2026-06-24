package com.example.diabetesmanage.service;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.PatientHealthSnapshot;
import com.example.diabetesmanage.model.PatientMetricTag;
import com.example.diabetesmanage.model.UrgentPatientAlert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PatientAlertBuilder {

    private static final double GLUCOSE_HIGH = 180;
    private static final double GLUCOSE_CRITICAL = 250;
    private static final double HBA1C_HIGH = 7.0;

    public static String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase();
        return first + last;
    }

    public static String resolveRiskLevel(PatientHealthSnapshot snapshot) {
        if (snapshot.isCritical() || snapshot.getRiskScore() >= 85) {
            return "critical";
        }
        if (snapshot.getRiskScore() >= 55) {
            return "high";
        }
        return "medium";
    }

    public static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Chưa cập nhật";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "Vừa xong";
        }
        if (minutes < 60) {
            return minutes + " phút trước";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }

        long days = duration.toDays();
        return days + " ngày trước";
    }

    public static List<PatientMetricTag> buildMetricTags(
            List<HealthRecord> records,
            List<String> riskReasons) {

        List<PatientMetricTag> tags = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return tags;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        HealthRecord latest = sorted.get(0);
        boolean glucoseRising = isGlucoseRising(sorted);

        if (latest.getDuongHuyetMgdl() != null) {
            double glucose = latest.getDuongHuyetMgdl();
            String label = glucose >= GLUCOSE_CRITICAL ? "Đường huyết rất cao"
                    : glucose >= GLUCOSE_HIGH ? "Đường huyết cao"
                    : glucose < 70 ? "Đường huyết thấp"
                    : "Đường huyết";
            tags.add(new PatientMetricTag(
                    label,
                    String.format("%.0f mg/dL", glucose),
                    "glucose",
                    glucoseRising
            ));
        }

        Double hba1c = findLatestHba1c(sorted);
        if (hba1c != null) {
            tags.add(new PatientMetricTag(
                    hba1c >= HBA1C_HIGH ? "HbA1c cao" : "HbA1c",
                    String.format("%.1f%%", hba1c),
                    "hba1c",
                    false
            ));
        }

        if (latest.getHuyetApTamThu() != null && latest.getHuyetApTamTruong() != null) {
            tags.add(new PatientMetricTag(
                    "Huyết áp",
                    latest.getHuyetApTamThu() + "/" + latest.getHuyetApTamTruong(),
                    "bp",
                    false
            ));
        }

        if (latest.getBmi() != null && latest.getBmi() >= 30) {
            tags.add(new PatientMetricTag(
                    "BMI cao",
                    String.format("%.1f", latest.getBmi()),
                    "bmi",
                    false
            ));
        }

        if (latest.getLieuLuongInsulinUi() != null && latest.getLieuLuongInsulinUi() > 0) {
            tags.add(new PatientMetricTag(
                    "Insulin",
                    latest.getLieuLuongInsulinUi() + " UI",
                    "insulin",
                    false
            ));
        }

        for (String reason : riskReasons) {
            if (reason.contains("Insulin tăng")) {
                tags.add(new PatientMetricTag(
                        "Can thiệp insulin",
                        "Không cải thiện",
                        "warning",
                        true
                ));
                break;
            }
            if (reason.contains("tăng liên tục")) {
                tags.add(new PatientMetricTag(
                        "Xu hướng",
                        "Đường huyết tăng",
                        "trend",
                        true
                ));
                break;
            }
        }

        return tags;
    }

    public static void populateAlertMetrics(
            UrgentPatientAlert alert,
            PatientHealthSnapshot snapshot) {

        List<HealthRecord> records = snapshot.getRecentRecords();
        alert.setInitials(buildInitials(snapshot.getPatientName()));
        alert.setRiskLevel(resolveRiskLevel(snapshot));
        alert.setNeedsUrgentReview(snapshot.isCritical());
        alert.setMetricTags(buildMetricTags(records, snapshot.getRiskReasons()));

        if (records.isEmpty()) {
            return;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        HealthRecord latest = sorted.get(0);
        alert.setDuongHuyetGanNhat(latest.getDuongHuyetMgdl());
        alert.setHba1cGanNhat(findLatestHba1c(sorted));
        alert.setHuyetApTamThu(latest.getHuyetApTamThu());
        alert.setHuyetApTamTruong(latest.getHuyetApTamTruong());
        alert.setBmiGanNhat(latest.getBmi());
        alert.setInsulinGanNhat(latest.getLieuLuongInsulinUi());
        alert.setTimeAgo(formatTimeAgo(latest.getThoiGianDo()));
        alert.setDetectedAgo("Phát hiện " + formatTimeAgo(latest.getThoiGianDo()));
        alert.setVitalDisplay(buildVitalDisplay(alert));
    }

    private static boolean isGlucoseRising(List<HealthRecord> sortedNewestFirst) {
        List<Double> values = new ArrayList<>();
        for (HealthRecord record : sortedNewestFirst) {
            if (record.getDuongHuyetMgdl() != null) {
                values.add(record.getDuongHuyetMgdl());
                if (values.size() >= 3) {
                    break;
                }
            }
        }

        if (values.size() < 2) {
            return false;
        }

        return values.get(0) > values.get(1);
    }

    private static Double findLatestHba1c(List<HealthRecord> records) {
        for (HealthRecord record : records) {
            if (record.getHba1cPercent() != null) {
                return record.getHba1cPercent();
            }
        }
        return null;
    }

    private static String buildVitalDisplay(UrgentPatientAlert alert) {
        if (alert.getDuongHuyetGanNhat() != null) {
            return String.format("%.0f mg/dL", alert.getDuongHuyetGanNhat());
        }
        if (alert.getHba1cGanNhat() != null) {
            return String.format("HbA1c %.1f%%", alert.getHba1cGanNhat());
        }
        return "—";
    }
}
