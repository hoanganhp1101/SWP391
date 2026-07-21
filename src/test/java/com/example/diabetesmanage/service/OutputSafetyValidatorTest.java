package com.example.diabetesmanage.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputSafetyValidatorTest {

    @Test
    void blocksPrescriptionLanguage() {
        String result = OutputSafetyValidator.validateAndSanitize("Bạn nên uống 500mg metformin mỗi ngày.");
        assertTrue(result.contains("không thể đưa ra chẩn đoán"));
    }

    @Test
    void blocksDiagnosticCertainty() {
        String result = OutputSafetyValidator.validateAndSanitize("Bạn chắc chắn bị tiểu đường type 2.");
        assertTrue(result.contains("không thể đưa ra chẩn đoán"));
    }

    @Test
    void doesNotAppendDisclaimer() {
        String result = OutputSafetyValidator.validateAndSanitize("Nên ăn nhiều rau xanh và hạn chế đường.");
        assertFalse(result.contains("⚠️"));
        assertFalse(result.contains("tham khảo ý kiến bác sĩ"));
    }

    @Test
    void stripsDisclaimerAndGreetingPrefix() {
        String raw = "Chào bạn, đường huyết 180 sau ăn hơi cao.\n\n"
                + "⚠️ Lưu ý: Thông tin chỉ mang tính tham khảo. Vui lòng tham khảo ý kiến bác sĩ.";
        String result = OutputSafetyValidator.validateAndSanitize(raw);
        assertFalse(result.toLowerCase().startsWith("chào bạn"));
        assertFalse(result.contains("⚠️"));
        assertTrue(result.toLowerCase().contains("đường huyết"));
    }
}
