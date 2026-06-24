package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.medical.MedicalFieldItem;

public class MedicalLabAnalyzer {

    public MedicalFieldItem glucose(Double value, String timing) {
        MedicalFieldItem item = field("Glucose", format(value), "mg/dL", "70-99 (lúc đói)");
        if (value == null) {
            return item;
        }
        item.setHighlightLevel(MedicalFieldItem.HighlightLevel.CORE);
        if (value < 70) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.CRITICAL);
        } else if (value >= 126) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.CRITICAL);
        } else if (value >= 100) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        if (timing != null && !timing.isBlank()) {
            item.setLabel("Glucose (" + timing + ")");
        }
        return item;
    }

    public MedicalFieldItem hba1c(Double value) {
        MedicalFieldItem item = field("HbA1c", format(value), "%", "< 5.7");
        item.setHighlightLevel(MedicalFieldItem.HighlightLevel.CORE);
        if (value == null) {
            return item;
        }
        if (value >= 6.5) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.CRITICAL);
        } else if (value >= 5.7) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        return item;
    }

    public MedicalFieldItem bmi(Double value) {
        MedicalFieldItem item = field("BMI", format(value), "kg/m²", "18.5-24.9");
        if (value != null && value >= 30) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        return item;
    }

    public MedicalFieldItem bloodPressure(Integer systolic, Integer diastolic) {
        String value = "—";
        if (systolic != null && diastolic != null) {
            value = systolic + "/" + diastolic;
        }
        MedicalFieldItem item = field("Huyết áp", value, "mmHg", "< 120/80");
        if (systolic != null && systolic >= 140) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        if (diastolic != null && diastolic >= 90) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        return item;
    }

    public MedicalFieldItem lab(String label, Double value, String unit, String range,
                                double low, double high) {
        MedicalFieldItem item = field(label, format(value), unit, range);
        if (value != null && (value < low || value > high)) {
            item.setAbnormal(true);
            item.setHighlightLevel(MedicalFieldItem.HighlightLevel.WARNING);
        }
        return item;
    }

    public MedicalFieldItem textField(String label, String value) {
        return field(label, value == null || value.isBlank() ? "—" : value, null, null);
    }

    public String diabetesAlert(Double glucoseMgdl, Double hba1c) {
        if (hba1c != null && hba1c >= 6.5) {
            return "HbA1c ≥ 6.5% — tiêu chí chẩn đoán Đái tháo đường";
        }
        if (glucoseMgdl != null && glucoseMgdl >= 126) {
            return "Glucose ≥ 126 mg/dL — đường huyết bất thường";
        }
        if (hba1c != null && hba1c >= 5.7) {
            return "HbA1c 5.7-6.4% — tiền đái tháo đường";
        }
        return null;
    }

    public String diabetesAlert(HealthRecord record) {
        return diabetesAlert(record.getDuongHuyetMgdl(), record.getHba1cPercent());
    }

    private MedicalFieldItem field(String label, String value, String unit, String range) {
        MedicalFieldItem item = new MedicalFieldItem(label, value, unit);
        item.setReferenceRange(range);
        return item;
    }

    private String format(Double value) {
        if (value == null) {
            return "—";
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value.doubleValue());
        }
        return String.format("%.1f", value);
    }
}
