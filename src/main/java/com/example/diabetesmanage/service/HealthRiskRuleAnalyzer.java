package com.example.diabetesmanage.service;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.PatientHealthSnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HealthRiskRuleAnalyzer {

    private static final double GLUCOSE_LOW = 70;
    private static final double GLUCOSE_HIGH = 180;
    private static final double GLUCOSE_CRITICAL = 250;
    private static final double HBA1C_HIGH = 7.0;
    private static final double HBA1C_CRITICAL = 9.0;
    private static final double BMI_HIGH = 30;
    private static final int BP_SYSTOLIC_HIGH = 140;
    private static final int BP_DIASTOLIC_HIGH = 90;
    private static final int MONITORING_GAP_DAYS = 7;

    public void analyze(PatientHealthSnapshot snapshot) {

        List<String> reasons = new ArrayList<>();
        int score = 0;
        boolean critical = false;

        List<HealthRecord> records = snapshot.getRecentRecords();
        if (records == null || records.isEmpty()) {
            snapshot.setRiskReasons(new ArrayList<>());
            snapshot.setRiskScore(0);
            snapshot.setCritical(false);
            return;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(
                HealthRecord::getThoiGianDo,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        HealthRecord latest = sorted.get(sorted.size() - 1);

        if (latest.getDaysSinceLastVisit() > MONITORING_GAP_DAYS) {
            reasons.add("Không theo dõi sức khỏe đều đặn ("
                    + latest.getDaysSinceLastVisit() + " ngày không cập nhật)");
            score += 35;
        }

        for (HealthRecord record : sorted) {
            Double glucose = record.getDuongHuyetMgdl();
            if (glucose != null && glucose < GLUCOSE_LOW) {
                reasons.add("Đường huyết quá thấp (" + formatNumber(glucose) + " mg/dL)");
                score += 90;
                critical = true;
                break;
            }
        }

        Double latestGlucose = latest.getDuongHuyetMgdl();
        if (latestGlucose != null) {
            if (latestGlucose >= GLUCOSE_CRITICAL) {
                reasons.add("Đường huyết quá cao (" + formatNumber(latestGlucose) + " mg/dL)");
                score += 100;
                critical = true;
            } else if (latestGlucose >= GLUCOSE_HIGH) {
                reasons.add("Đường huyết cao (" + formatNumber(latestGlucose) + " mg/dL)");
                score += 70;
            }
        }

        Double latestHba1c = findLatestHba1c(sorted);
        if (latestHba1c != null) {
            if (latestHba1c >= HBA1C_CRITICAL) {
                reasons.add("HbA1c rất cao (" + formatNumber(latestHba1c) + "%)");
                score += 85;
                critical = true;
            } else if (latestHba1c >= HBA1C_HIGH) {
                reasons.add("HbA1c cao (" + formatNumber(latestHba1c) + "%)");
                score += 55;
            }
        }

        Integer systolic = latest.getHuyetApTamThu();
        Integer diastolic = latest.getHuyetApTamTruong();
        if ((systolic != null && systolic >= BP_SYSTOLIC_HIGH)
                || (diastolic != null && diastolic >= BP_DIASTOLIC_HIGH)) {
            reasons.add("Huyết áp cao ("
                    + (systolic != null ? systolic : "?")
                    + "/"
                    + (diastolic != null ? diastolic : "?")
                    + ")");
            score += 60;
        }

        Double latestBmi = findLatestBmi(sorted);
        if (latestBmi != null && latestBmi >= BMI_HIGH) {
            reasons.add("BMI cao (" + formatNumber(latestBmi) + ")");
            score += 40;
        }

        if (hasRisingGlucoseTrend(sorted)) {
            reasons.add("Đường huyết tăng liên tục nhiều ngày");
            score += 65;
        }

        if (hasInsulinIneffective(sorted)) {
            reasons.add("Insulin tăng nhưng đường huyết không cải thiện");
            score += 75;
        }

        if (hasIrregularMonitoring(sorted)) {
            reasons.add("Khoảng cách giữa các lần đo quá dài, theo dõi không đều");
            score += 45;
        }

        snapshot.setRiskReasons(deduplicate(reasons));
        snapshot.setRiskScore(score);
        snapshot.setCritical(critical || score >= 85);
    }

    private boolean hasRisingGlucoseTrend(List<HealthRecord> sorted) {

        List<Double> dailyGlucose = new ArrayList<>();
        LocalDate lastDate = null;

        for (HealthRecord record : sorted) {
            if (record.getDuongHuyetMgdl() == null || record.getThoiGianDo() == null) {
                continue;
            }
            LocalDate date = record.getThoiGianDo().toLocalDate();
            if (lastDate == null || !lastDate.equals(date)) {
                dailyGlucose.add(record.getDuongHuyetMgdl());
                lastDate = date;
            }
        }

        if (dailyGlucose.size() < 3) {
            return false;
        }

        int risingDays = 0;
        for (int i = 1; i < dailyGlucose.size(); i++) {
            if (dailyGlucose.get(i) > dailyGlucose.get(i - 1)) {
                risingDays++;
            } else {
                risingDays = 0;
            }
            if (risingDays >= 2) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInsulinIneffective(List<HealthRecord> sorted) {

        List<HealthRecord> withInsulin = new ArrayList<>();
        for (HealthRecord record : sorted) {
            if (record.getLieuLuongInsulinUi() != null
                    && record.getLieuLuongInsulinUi() > 0
                    && record.getDuongHuyetMgdl() != null) {
                withInsulin.add(record);
            }
        }

        if (withInsulin.size() < 2) {
            return false;
        }

        HealthRecord older = withInsulin.get(0);
        HealthRecord newer = withInsulin.get(withInsulin.size() - 1);

        boolean insulinIncreased =
                newer.getLieuLuongInsulinUi() > older.getLieuLuongInsulinUi();

        boolean glucoseNotImproved =
                newer.getDuongHuyetMgdl() >= older.getDuongHuyetMgdl() - 10;

        return insulinIncreased && glucoseNotImproved
                && newer.getDuongHuyetMgdl() >= GLUCOSE_HIGH;
    }

    private boolean hasIrregularMonitoring(List<HealthRecord> sorted) {

        if (sorted.size() < 2) {
            return false;
        }

        LocalDateTime previous = null;
        int gapsOverLimit = 0;

        for (HealthRecord record : sorted) {
            if (record.getThoiGianDo() == null) {
                continue;
            }
            if (previous != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        record.getThoiGianDo().toLocalDate(),
                        previous.toLocalDate()
                );
                if (days > MONITORING_GAP_DAYS) {
                    gapsOverLimit++;
                }
            }
            previous = record.getThoiGianDo();
        }

        return gapsOverLimit >= 1;
    }

    private Double findLatestHba1c(List<HealthRecord> sorted) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i).getHba1cPercent() != null) {
                return sorted.get(i).getHba1cPercent();
            }
        }
        return null;
    }

    private Double findLatestBmi(List<HealthRecord> sorted) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i).getBmi() != null) {
                return sorted.get(i).getBmi();
            }
        }
        return null;
    }

    private List<String> deduplicate(List<String> reasons) {
        return new ArrayList<>(new java.util.LinkedHashSet<>(reasons));
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }
}
