package com.example.diabetesmanage.model.medical;

public enum PdfExportType {
    FULL("full", "Toan bo ho so"),
    INTERNAL_MEDICINE("internal", "Benh an noi tiet"),
    BLOOD_COUNT("blood", "Xet nghiem mau tong quat"),
    BIOCHEMISTRY("biochemistry", "Sinh hoa mau");

    private final String param;
    private final String label;

    PdfExportType(String param, String label) {
        this.param = param;
        this.label = label;
    }

    public String getParam() {
        return param;
    }

    public String getLabel() {
        return label;
    }

    public static PdfExportType fromParam(String value) {
        if (value == null || value.isBlank()) {
            return FULL;
        }
        for (PdfExportType type : values()) {
            if (type.param.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return FULL;
    }
}
