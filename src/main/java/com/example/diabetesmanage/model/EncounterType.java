package com.example.diabetesmanage.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum EncounterType {

    TAI_KHAM_NOI_TIET("tai_kham_noi_tiet", "Bệnh án tái khám Nội tiết"),
    MAU_TONG_QUAT("mau_tong_quat", "Kết quả xét nghiệm máu tổng quát"),
    SINH_HOA_MAU("sinh_hoa_mau", "Kết quả sinh hóa máu");

    private static final Map<String, EncounterType> BY_CODE = new HashMap<>();
    private static final Map<String, EncounterType> BY_ALIAS = new HashMap<>();
    private static final Map<String, EncounterType> BY_LABEL = new HashMap<>();

    static {
        for (EncounterType type : values()) {
            BY_CODE.put(normalize(type.code), type);
            BY_LABEL.put(normalize(type.label), type);
        }
        registerAliases(TAI_KHAM_NOI_TIET,
                "internal_examination", "noi_tiet", "kham_noi_tiet");
        registerAliases(SINH_HOA_MAU,
                "biochemistry_test", "biochemistry", "sinh_hoa");
        registerAliases(MAU_TONG_QUAT,
                "general_blood_test", "blood_test", "cbc");
    }

    private final String code;
    private final String label;

    EncounterType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTaiKhamNoiTiet() {
        return this == TAI_KHAM_NOI_TIET;
    }

    public boolean isMauTongQuat() {
        return this == MAU_TONG_QUAT;
    }

    public boolean isSinhHoaMau() {
        return this == SINH_HOA_MAU;
    }

    public static EncounterType fromCode(String code) {
        EncounterType resolved = fromCodeOrNull(code);
        return resolved != null ? resolved : TAI_KHAM_NOI_TIET;
    }

    /** Trả về null nếu không khớp mã — dùng khi suy luận từ dữ liệu DB. */
    public static EncounterType fromCodeOrNull(String code) {
        String normalized = normalize(code);
        if (normalized == null) {
            return null;
        }
        EncounterType direct = BY_CODE.get(normalized);
        if (direct != null) {
            return direct;
        }
        return BY_ALIAS.get(normalized);
    }

    /**
     * Suy luận mã loại hồ sơ từ chẩn đoán / lý do khám (bản ghi cũ không có JSON loai_encounter).
     */
    public static String inferCodeFromLabels(String... labels) {
        if (labels == null) {
            return null;
        }
        for (String label : labels) {
            String normalized = normalize(label);
            if (normalized == null) {
                continue;
            }
            EncounterType type = BY_LABEL.get(normalized);
            if (type != null) {
                return type.code;
            }
        }
        return null;
    }

    public static String resolveTypeCode(String columnCode, String jsonCode, String chanDoanChinh, String lyDoKham) {
        String resolved = canonicalCodeOrNull(columnCode);
        if (resolved != null) {
            return resolved;
        }
        resolved = canonicalCodeOrNull(jsonCode);
        if (resolved != null) {
            return resolved;
        }
        resolved = inferCodeFromLabels(chanDoanChinh, lyDoKham);
        if (resolved != null) {
            return resolved;
        }
        return TAI_KHAM_NOI_TIET.code;
    }

    private static void registerAliases(EncounterType type, String... aliases) {
        for (String alias : aliases) {
            BY_ALIAS.put(normalize(alias), type);
        }
    }

    private static String canonicalCodeOrNull(String code) {
        EncounterType type = fromCodeOrNull(code);
        return type != null ? type.code : null;
    }

    private static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
