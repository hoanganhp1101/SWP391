package com.example.diabetesmanage.service;

import java.util.regex.Pattern;

/**
 * Hậu kiểm câu trả lời AI trước khi hiển thị cho bệnh nhân.
 */
public final class OutputSafetyValidator {

    private static final Pattern UNSAFE_PRESCRIPTION = Pattern.compile(
            "kê\\s*(cho|đơn)|nên\\s*(uống|dùng|tăng|giảm)\\s*\\d|"
                    + "liều\\s*(nên|khuyên|đề\\s*nghị)|tự\\s*(ý|tay)\\s*(ngưng|bỏ|dừng)|"
                    + "bạn\\s*(nên|hãy|phải)\\s*(uống|dùng|tăng|giảm)\\s*(liều|thuốc)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern UNSAFE_DIAGNOSIS = Pattern.compile(
            "bạn\\s*(chắc\\s*chắn|đang)\\s*bị|chẩn\\s*đoán\\s*(là|bạn)|"
                    + "100%\\s*(là|bị)|chắc\\s*chắn\\s*bạn\\s*bị",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern UNSAFE_SENSITIVE = Pattern.compile(
            "bạn\\s*nên\\s*(tự\\s*tử|chết)|cách\\s*(làm\\s*bom|lừa\\s*đảo)|"
                    + "hướng\\s*dẫn\\s*(tự\\s*tử|mua\\s*ma\\s*túy)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Gỡ disclaimer mẫu nếu model vẫn tự chèn. */
    private static final Pattern DISCLAIMER_PATTERN = Pattern.compile(
            "(?m)\\s*⚠️?\\s*Lưu ý:\\s*Thông tin chỉ mang tính tham khảo\\.?\\s*"
                    + "(Vui lòng tham khảo ý kiến bác sĩ\\.?)?\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern DISCLAIMER_ALT = Pattern.compile(
            "(?m)\\s*⚠️\\s*Thông tin chỉ mang tính tham khảo\\.?\\s*"
                    + "(Không thay thế ý kiến bác sĩ[^\\n]*)?\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Gỡ lời chào lặp đầu câu: "Chào bạn,", "Xin chào!", ... */
    private static final Pattern GREETING_PREFIX = Pattern.compile(
            "^(chào\\s*bạn|xin\\s*chào|hello|hi)\\s*[,!.:\\-–—]?\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private OutputSafetyValidator() {
    }

    public static String validateAndSanitize(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return fallbackMessage();
        }

        String cleaned = stripNoise(rawResponse.trim());

        if (UNSAFE_PRESCRIPTION.matcher(cleaned).find()
                || UNSAFE_DIAGNOSIS.matcher(cleaned).find()
                || UNSAFE_SENSITIVE.matcher(cleaned).find()) {
            return "Tôi không thể đưa ra chẩn đoán, hướng dẫn đổi thuốc, hoặc trả lời nội dung nguy hiểm.\n\n"
                    + "Bạn nên trao đổi với bác sĩ về điều trị. Ngoài ra bạn có thể hỏi tôi bất cứ điều gì khác.";
        }

        return cleaned.isBlank() ? fallbackMessage() : cleaned;
    }

    static String stripNoise(String text) {
        String cleaned = DISCLAIMER_PATTERN.matcher(text).replaceAll("").trim();
        cleaned = DISCLAIMER_ALT.matcher(cleaned).replaceAll("").trim();
        cleaned = GREETING_PREFIX.matcher(cleaned).replaceFirst("").trim();
        // Viết hoa lại chữ cái đầu sau khi gỡ lời chào
        if (!cleaned.isEmpty()) {
            cleaned = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        }
        return cleaned;
    }

    public static String fallbackMessage() {
        return "Xin lỗi, tôi đang gặp sự cố kết nối. Vui lòng thử lại sau.";
    }

    public static String rateLimitMessage() {
        return "Hiện tại API Gemini đã hết hạn mức tạm thời (quota).\n\n"
                + "Vui lòng đợi vài phút rồi hỏi lại, hoặc tạo API key mới tại "
                + "https://aistudio.google.com/apikey rồi cập nhật file gemini.properties.";
    }

    public static String messageForApiError(String rawError) {
        if (rawError == null) {
            return fallbackMessage();
        }
        String err = rawError.toUpperCase();
        if (err.contains("RATE_LIMIT") || err.contains("429") || err.contains("RESOURCE_EXHAUSTED")) {
            return rateLimitMessage();
        }
        if (err.contains("API_KEY") || err.contains("HTTP_400") || err.contains("HTTP_403")) {
            return "API key Gemini không hợp lệ hoặc bị từ chối. "
                    + "Hãy tạo key mới tại https://aistudio.google.com/apikey "
                    + "và cập nhật file gemini.properties.";
        }
        if (err.contains("UNAVAILABLE") || err.contains("503")) {
            return "Máy chủ Gemini đang quá tải tạm thời. Vui lòng thử lại sau vài giây.";
        }
        return fallbackMessage();
    }
}
