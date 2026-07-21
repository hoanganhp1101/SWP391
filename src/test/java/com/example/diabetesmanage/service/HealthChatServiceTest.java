package com.example.diabetesmanage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class HealthChatServiceTest {

    private GeminiService geminiSpy;
    private HealthChatService healthChatService;

    @BeforeEach
    void setUp() {
        geminiSpy = spy(new GeminiService());
        healthChatService = new HealthChatService(geminiSpy);
    }

    @Test
    void processReturnsBlockedWithoutCallingGemini() {
        HealthChatResponse response = healthChatService.process("Kê đơn thuốc cho tôi", null);
        assertEquals("blocked", response.getStatusCode());
        assertTrue(response.getReply().contains("không thể chẩn đoán"));
    }

    @Test
    void processReturnsEmergencyWithoutCallingGemini() {
        HealthChatResponse response = healthChatService.process("Tôi ngất xỉu và đau ngực", null);
        assertEquals("emergency", response.getStatusCode());
        assertTrue(response.getReply().contains("115"));
    }

    @Test
    void processSendsOffTopicToGeminiForRedirect() {
        doReturn("Hà Nội hôm nay nắng nhẹ. Trời nóng dễ mất nước, bạn nhớ uống đủ; "
                + "đường huyết gần nhất của bạn bao nhiêu?")
                .when(geminiSpy).callHealthChatAPI(anyString(), anyString());

        HealthChatResponse response = healthChatService.process("Hôm nay thời tiết thế nào?", null);

        assertEquals("answered", response.getStatusCode());
        verify(geminiSpy).callHealthChatAPI(anyString(), anyString());
    }

    @Test
    void processSanitizesUnsafeModelOutput() {
        doReturn("Bạn nên uống 1000mg metformin mỗi ngày.")
                .when(geminiSpy).callHealthChatAPI(anyString(), anyString());

        HealthChatResponse response = healthChatService.process("Metformin là gì?", null);

        assertEquals("answered", response.getStatusCode());
        assertTrue(response.getReply().contains("không thể đưa ra chẩn đoán"));
    }

    @Test
    void processKeepsSafeOutputWithoutDisclaimer() {
        doReturn("Chế độ ăn nên hạn chế tinh bột tinh chế.")
                .when(geminiSpy).callHealthChatAPI(anyString(), anyString());

        HealthChatResponse response = healthChatService.process("Chế độ ăn tiểu đường thế nào?", null);

        assertEquals("answered", response.getStatusCode());
        assertFalse(response.getReply().contains("⚠️"));
        assertTrue(response.getReply().contains("Chế độ ăn"));
    }

    @Test
    void processHandlesApiError() {
        doReturn("ERROR:RATE_LIMIT:429")
                .when(geminiSpy).callHealthChatAPI(anyString(), anyString());

        HealthChatResponse response = healthChatService.process("Đường huyết bao nhiêu là bình thường?", null);

        assertFalse(response.isSuccess());
        assertTrue(response.getReply().contains("quá tải") || response.getReply().contains("hạn mức"));
    }
}
