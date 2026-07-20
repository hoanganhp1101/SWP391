package com.example.diabetesmanage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GeminiServiceTest {

    private GeminiService geminiServiceSpy;

    @BeforeEach
    public void setUp() {
        // Sử dụng Spy để giữ nguyên logic thật, chỉ giả lập phương thức gọi API ra ngoài
        GeminiService realService = new GeminiService();
        geminiServiceSpy = spy(realService);
    }

    @Test
    public void testExtractMedicalDataFromText_ReturnsCleanedJson() {
        // Chuẩn bị dữ liệu giả lập (mock data) mà API trả về (có chứa markdown block ```json)
        String fakeApiResponse = "```json\n" +
                "{\n" +
                "  \"canNangKg\": 60.5,\n" +
                "  \"huyetApTamThu\": 120\n" +
                "}\n" +
                "```";

        // Yêu cầu Mockito chặn cuộc gọi callGeminiAPI và trả về fakeApiResponse thay vì gọi HTTP request thật
        doReturn(fakeApiResponse).when(geminiServiceSpy).callGeminiAPI(anyString(), anyBoolean());

        // Thực thi hàm cần test
        String result = geminiServiceSpy.extractMedicalDataFromText("Bệnh nhân nặng 60.5kg, huyết áp 120/80");

        // Kiểm tra kết quả (Assert): Xem hàm extractMedicalDataFromText có cắt bỏ đúng chữ ```json không
        assertNotNull(result);
        assertFalse(result.contains("```json"));
        assertFalse(result.contains("```"));
        assertTrue(result.contains("\"canNangKg\": 60.5"));
    }

    @Test
    public void testExtractMedicalDataFromText_HandlesError() {
        // Giả lập API trả về lỗi
        doReturn("ERROR: Rate limit exceeded").when(geminiServiceSpy).callGeminiAPI(anyString(), anyBoolean());

        // Thực thi
        String result = geminiServiceSpy.extractMedicalDataFromText("Bệnh án text");

        // Kiểm tra kết quả: Hàm phải xử lý được lỗi và trả về {}
        assertEquals("{}", result);
    }
}
