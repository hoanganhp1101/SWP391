package com.example.diabetesmanage.util;

import com.example.diabetesmanage.dto.HighRiskPatientDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Đánh giá mức cảnh báo chỉ số sức khỏe cho UI (badge trên dashboard phân tích bệnh nhân).
 */
public final class HealthMetricAssessment {

    private HealthMetricAssessment() {
    }

    public enum Level {
        NORMAL("Bình thường", "metric-badge-normal", "🟢"),
        HIGH("Cao", "metric-badge-high", "🟡"),
        DANGER("Nguy hiểm", "metric-badge-danger", "🟠"),
        VERY_HIGH("Rất cao", "metric-badge-very-high", "🔴"),
        NO_DATA("Chưa có dữ liệu", "metric-badge-nodata", "");

        private final String label;
        private final String cssClass;
        private final String emoji;

        Level(String label, String cssClass, String emoji) {
            this.label = label;
            this.cssClass = cssClass;
            this.emoji = emoji;
        }

        public String getLabel() {
            return label;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    public static Level evaluateGlucose(Double mgdl) {
        if (mgdl == null) {
            return Level.NO_DATA;
        }
        if (mgdl < 70) {
            return Level.DANGER;
        }
        if (mgdl <= 140) {
            return Level.NORMAL;
        }
        if (mgdl <= 180) {
            return Level.HIGH;
        }
        if (mgdl <= 250) {
            return Level.DANGER;
        }
        return Level.VERY_HIGH;
    }

    public static Level evaluateBloodPressure(Integer systolic, Integer diastolic) {
        if (systolic == null || diastolic == null) {
            return Level.NO_DATA;
        }
        if (systolic >= 180 || diastolic >= 120) {
            return Level.VERY_HIGH;
        }
        if ((systolic >= 140 && systolic <= 179) || (diastolic >= 90 && diastolic <= 119)) {
            return Level.DANGER;
        }
        if ((systolic >= 120 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            return Level.HIGH;
        }
        if (systolic < 120 && diastolic < 80) {
            return Level.NORMAL;
        }
        return Level.HIGH;
    }

    public static Level evaluateBmi(Double bmi) {
        if (bmi == null) {
            return Level.NO_DATA;
        }
        if (bmi < 18.5) {
            return Level.HIGH;
        }
        if (bmi <= 24.9) {
            return Level.NORMAL;
        }
        if (bmi <= 29.9) {
            return Level.HIGH;
        }
        if (bmi <= 34.9) {
            return Level.DANGER;
        }
        return Level.VERY_HIGH;
    }

    public static Level evaluateHba1c(Double hba1cPercent) {
        if (hba1cPercent == null) {
            return Level.NO_DATA;
        }
        if (hba1cPercent < 5.7) {
            return Level.NORMAL;
        }
        if (hba1cPercent <= 6.4) {
            return Level.HIGH;
        }
        if (hba1cPercent <= 8.9) {
            return Level.DANGER;
        }
        return Level.VERY_HIGH;
    }

    /** Gắn badge mức chỉ số lên request cho trang phân tích bệnh nhân nguy hiểm. */
    public static void populateMetricLevels(
            HttpServletRequest request,
            HighRiskPatientDTO detail) {
        if (request == null || detail == null) {
            return;
        }
        request.setAttribute("glucoseLevel", evaluateGlucose(detail.getDuongHuyetGanNhat()));
        request.setAttribute("bpLevel", evaluateBloodPressure(
                detail.getHuyetApTamThu(), detail.getHuyetApTamTruong()));
        request.setAttribute("bmiLevel", evaluateBmi(detail.getBmiGanNhat()));
        request.setAttribute("hba1cLevel", evaluateHba1c(detail.getHba1cGanNhat()));
    }
}
