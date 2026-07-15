package com.example.diabetesmanage.util;

import com.example.diabetesmanage.model.EncounterType;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;

/**
 * Đọc/ghi chỉ số lâm sàng trong cột {@code medical_encounters.kham_lam_sang} (JSON).
 */
public final class EncounterClinicalJson {

    private EncounterClinicalJson() {
    }

    public static String buildFromForm(EncounterCreateRequest form) {
        EncounterType type = form.resolveEncounterType();
        StringBuilder json = new StringBuilder("{\"loai_encounter\":\"");
        json.append(type.getCode()).append('"');
        boolean first = false;
        first = appendField(json, first, "khoa_kham", form.resolveKhoaKham());
        first = appendField(json, first, "noi_dung",
                firstNonBlank(form.getKhamLamSang(), form.getTrieuChung()));
        first = appendField(json, first, "trieu_chung", form.getTrieuChung());
        first = appendNumber(json, first, "chieu_cao_cm", form.getChieuCaoCm());
        first = appendNumber(json, first, "can_nang_kg", form.getCanNangKg());
        first = appendNumber(json, first, "bmi", form.getBmi());
        first = appendNumber(json, first, "huyet_ap_tam_thu", form.getHuyetApTamThu());
        first = appendNumber(json, first, "huyet_ap_tam_truong", form.getHuyetApTamTruong());
        first = appendNumber(json, first, "nhip_tim", form.getNhipTim());
        first = appendNumber(json, first, "nhiet_do_c", form.getNhietDoC());
        first = appendNumber(json, first, "nhip_tho", form.getNhipTho());
        first = appendNumber(json, first, "duong_huyet_mgdl", form.getDuongHuyetMgdl());
        first = appendField(json, first, "thoi_diem_do_duong", form.getThoiDiemDoDuong());
        first = appendNumber(json, first, "carbs_g", form.getCarbsG());
        first = appendField(json, first, "loai_insulin_tiem", form.getLoaiInsulinTiem());
        first = appendNumber(json, first, "lieu_luong_insulin_ui", form.getLieuLuongInsulinUi());
        json.append('}');
        return json.toString();
    }

    public static String parseString(String json, String key) {
        if (json == null || json.isBlank() || key == null) {
            return null;
        }
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return json.substring(start);
        }
        return json.substring(start, end)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static Double parseDouble(String json, String key) {
        if (json == null || json.isBlank() || key == null) {
            return null;
        }
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && "0123456789.-".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        if (end == start) {
            return null;
        }
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Integer parseInteger(String json, String key) {
        Double value = parseDouble(json, key);
        return value != null ? value.intValue() : null;
    }

    private static boolean appendField(StringBuilder json, boolean first, String key, String value) {
        if (value == null || value.isBlank()) {
            return first;
        }
        if (!first) {
            json.append(',');
        }
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        json.append('"').append(key).append("\":\"").append(escaped).append('"');
        return false;
    }

    private static boolean appendNumber(StringBuilder json, boolean first, String key, Number value) {
        if (value == null) {
            return first;
        }
        if (!first) {
            json.append(',');
        }
        json.append('"').append(key).append("\":").append(value);
        return false;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
