package com.example.diabetesmanage.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Kiểm soát an toàn trước khi gọi AI.
 * Chatbot trả lời mọi chủ đề và tự dẫn dắt về sức khỏe/bệnh án,
 * nhưng chặn: cấp cứu (xử lý riêng), kê đơn/chẩn đoán/prompt-injection,
 * và ngôn từ xúc phạm/khiếm nhã từ người dùng.
 */
public final class HealthChatGuard {

    public enum Status {
        ALLOWED,
        OUT_OF_SCOPE,
        EMERGENCY,
        BLOCKED
    }

    public static final class Result {
        private final Status status;
        private final String reply;

        public Result(Status status, String reply) {
            this.status = status;
            this.reply = reply;
        }

        public Status getStatus() {
            return status;
        }

        public String getReply() {
            return reply;
        }
    }

    private static final Pattern EMERGENCY_PATTERN = Pattern.compile(
            "mất\\s*ý\\s*thức|ngất\\s*xỉu|ngất|khó\\s*thở|thở\\s*không\\s*nổi|đau\\s*ngực|co\\s*giật|"
                    + "choáng\\s*váng|tím\\s*tái|không\\s*thở|đau\\s*tim|cấp\\s*cứu|gọi\\s*115|"
                    + "tự\\s*tử|muốn\\s*chết|tự\\s*làm\\s*hại|"
                    + "đường\\s*huyết\\s*(dưới|thấp|hạ)\\s*(3[0-9]|[1-4][0-9])|"
                    + "đường\\s*huyết\\s*(trên|cao|hơn)?\\s*(4[0-9]{2}|[5-9][0-9]{2})|"
                    + "hạ\\s*đường\\s*huyết\\s*nặng|tăng\\s*đường\\s*huyết\\s*nghiêm\\s*trọng|"
                    + "mê\\s*sảng|nôn\\s*liên\\s*tục.*(mất\\s*sức|yếu|choáng)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Yêu cầu y tế không an toàn và prompt-injection. */
    private static final Pattern BLOCKED_PATTERN = Pattern.compile(
            "kê\\s*đơn|kê\\s*thuốc|đổi\\s*liều|tăng\\s*liều|giảm\\s*liều|tự\\s*(ý|tay)\\s*(ngưng|bỏ|dừng)\\s*thuốc|"
                    + "chẩn\\s*đoán\\s*(cho|tôi|mình|bệnh)|bỏ\\s*qua\\s*quy\\s*tắc|"
                    + "ignore\\s*(all|previous|prior)\\s*instructions|system\\s*prompt|"
                    + "bạn\\s*phải\\s*(kê|cho|tăng|giảm)|thay\\s*thuốc\\s*khác",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Từ ngữ xúc phạm / khiếm nhã (có dấu). Kiểm tra trên chuỗi đã lowercase + NFC.
     */
    private static final Pattern OFFENSIVE_PATTERN = Pattern.compile(
            "địt|đụ\\s*mẹ|đụ\\s*má|đéo|đĩ|đồ\\s*ngu|thằng\\s*ngu|con\\s*ngu|đần\\s*độn|"
                    + "óc\\s*chó|súc\\s*vật|đồ\\s*chó|chó\\s*đẻ|khốn\\s*nạn|đồ\\s*khốn|"
                    + "mẹ\\s*mày|bố\\s*mày|cái\\s*lồn|lồn|cặc|buồi|dái|"
                    + "vãi\\s*lồn|vcl|vl\\b|đm\\b|đmm|đcm|"
                    + "mày\\s*ngu|ngu\\s*vl|ngu\\s*vãi|"
                    + "\\bfuck\\b|\\bfucking\\b|\\bshit\\b|\\bbitch\\b|\\basshole\\b|"
                    + "\\bdamn\\b|\\bcunt\\b|\\bdickhead\\b|\\bmotherfucker\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Biến thể gõ không dấu / viết tắt thường gặp.
     * Chỉ dùng cụm rõ ràng để tránh nhầm với từ thường (đi, buổi...).
     */
    private static final Pattern OFFENSIVE_ASCII_PATTERN = Pattern.compile(
            "\\b(dit\\s*me|dit\\s*mee|du\\s*me|du\\s*ma|deo\\b|dm|dmm|dcm|vcl|vlo[n]?|"
                    + "cai\\s*lon|oc\\s*cho|suc\\s*vat|do\\s*cho|khon\\s*nan|"
                    + "me\\s*may|bo\\s*may|thang\\s*ngu|con\\s*ngu|do\\s*ngu|ngu\\s*vl|"
                    + "fuck|fucking|shit|bitch|asshole|cunt|dickhead|motherfucker)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Câu ngắn xác nhận — trả lời ngắn, không gọi API. */
    private static final Pattern ACK_PATTERN = Pattern.compile(
            "^(vâng|vang|ừ|uh|ok|okay|oke|được|duoc|rõ|ro|"
                    + "cảm\\s*ơn|cam\\s*on|thanks|thank\\s*you|"
                    + "tiếp\\s*đi|tiep\\s*di|tiếp\\s*tục)\\s*[!.?]*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private HealthChatGuard() {
    }

    public static Result evaluate(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return new Result(Status.BLOCKED, "Vui lòng nhập câu hỏi.");
        }

        String message = normalize(rawMessage.trim());

        // Cấp cứu luôn được xử lý trước.
        if (EMERGENCY_PATTERN.matcher(message).find()) {
            return new Result(Status.EMERGENCY, emergencyReply());
        }

        // Chặn từ ngữ xúc phạm / khiếm nhã.
        if (isOffensive(message)) {
            return new Result(Status.BLOCKED, offensiveReply());
        }

        // An toàn thuốc / chống prompt-injection.
        if (BLOCKED_PATTERN.matcher(message).find()) {
            return new Result(Status.BLOCKED, blockedReply());
        }

        // Xác nhận ngắn: trả lời ngắn gọn, tránh spam API khi user chỉ gõ "ok".
        if (ACK_PATTERN.matcher(message).matches()) {
            return new Result(Status.ALLOWED, ackReply(message));
        }

        // Mọi câu hỏi khác (kể cả ngoài chủ đề) → gửi Gemini để trả lời rồi
        // dẫn dắt người dùng quay lại chủ đề sức khỏe/bệnh án.
        return new Result(Status.ALLOWED, null);
    }

    static boolean isOffensive(String normalizedMessage) {
        if (OFFENSIVE_PATTERN.matcher(normalizedMessage).find()) {
            return true;
        }
        String ascii = stripDiacritics(normalizedMessage);
        return OFFENSIVE_ASCII_PATTERN.matcher(ascii).find();
    }

    static String normalize(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFC);
    }

    static String stripDiacritics(String input) {
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Giữ chữ đ/Đ (không phải tổ hợp dấu thông thường trong NFD cho mọi JVM)
        return nfd.replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'd');
    }

    private static String ackReply(String message) {
        if (message.matches(".*(cảm\\s*ơn|cam\\s*on|thanks).*")) {
            return "Không có gì ạ. Hôm nay sức khỏe và đường huyết của bạn thế nào?";
        }
        if (message.matches(".*(tiếp|tiep).*")) {
            return "Được. Bạn muốn nói tiếp về chỉ số, triệu chứng hay chế độ sinh hoạt?";
        }
        return "Mình đang lắng nghe. Bạn muốn trao đổi thêm điều gì về sức khỏe hoặc bệnh án?";
    }

    private static String emergencyReply() {
        return "Tình huống bạn mô tả có thể cần xử trí khẩn cấp.\n\n"
                + "Hãy gọi **115** hoặc đến cơ sở y tế gần nhất ngay lập tức.\n"
                + "Nếu hạ đường huyết nặng và còn tỉnh, có thể uống 15g carbohydrate nhanh (nước ngọt, mật ong) "
                + "theo hướng dẫn bác sĩ — nhưng vẫn cần được hỗ trợ y tế sớm.";
    }

    private static String blockedReply() {
        return "Tôi không thể chẩn đoán bệnh, kê đơn hoặc thay đổi liều thuốc.\n\n"
                + "Mọi quyết định điều trị cần được bác sĩ tư vấn trực tiếp. "
                + "Bạn có thể hỏi về chỉ số, triệu chứng, chế độ ăn, vận động hoặc thông tin trong bệnh án.";
    }

    private static String offensiveReply() {
        return "Xin vui lòng dùng ngôn từ lịch sự khi trao đổi với DiabCare AI.\n\n"
                + "Tôi sẵn sàng hỗ trợ bạn về sức khỏe, tiểu đường và bệnh án. "
                + "Bạn muốn hỏi gì liên quan đến chỉ số hoặc tình trạng hiện tại?";
    }
}
