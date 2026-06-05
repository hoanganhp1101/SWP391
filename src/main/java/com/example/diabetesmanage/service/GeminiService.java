package com.example.diabetesmanage.service;

import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

/**
 * Service gọi Google Gemini API để phân tích sức khỏe, chatbot, và tạo báo cáo.
 * Sử dụng prompt engineering chuyên biệt cho bệnh tiểu đường.
 */
public class GeminiService {

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private String apiKey;
    private String model;
    private int maxTokens;
    private final Gson gson = new Gson();

    public GeminiService() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
                this.apiKey = props.getProperty("gemini.api.key", "");
                this.model = props.getProperty("gemini.model", "gemini-2.0-flash");
                this.maxTokens = Integer.parseInt(props.getProperty("gemini.max.tokens", "2048"));
            } else {
                System.err.println("[GeminiService] config.properties not found in classpath!");
                this.apiKey = "";
                this.model = "gemini-2.0-flash";
                this.maxTokens = 2048;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== CHỨC NĂNG 1: PHÂN TÍCH SỨC KHỎE ====================

    /**
     * Phân tích chỉ số sức khỏe của bệnh nhân và trả về đánh giá nguy cơ.
     * Gọi sau khi bệnh nhân nhập dữ liệu sức khỏe mới.
     */
    public AIAnalysis analyzeHealthData(HealthRecord record, Patient patient) {
        String prompt = buildHealthAnalysisPrompt(record, patient);
        String response = callGeminiAPI(prompt);

        if (response == null || response.isEmpty()) {
            return createFallbackAnalysis(record, patient);
        }

        return parseHealthAnalysisResponse(response, record, patient);
    }

    private String buildHealthAnalysisPrompt(HealthRecord record, Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là hệ thống AI hỗ trợ y tế chuyên về bệnh tiểu đường. ");
        sb.append("Phân tích chỉ số sức khỏe sau đây và trả lời CHÍNH XÁC theo format JSON bên dưới.\n\n");

        sb.append("=== THÔNG TIN BỆNH NHÂN ===\n");
        if (patient != null) {
            sb.append("- Loại tiểu đường: ").append(patient.getLoaiTieuDuong() != null ? patient.getLoaiTieuDuong() : "Type 2").append("\n");
            sb.append("- Giới tính: ").append(patient.getGioiTinh() != null ? patient.getGioiTinh() : "Không rõ").append("\n");
            if (patient.getTienSuBenh() != null) {
                sb.append("- Tiền sử bệnh: ").append(patient.getTienSuBenh()).append("\n");
            }
        }

        sb.append("\n=== CHỈ SỐ VỪA ĐO ===\n");
        if (record.getDuongHuyetMgdl() != null) {
            sb.append("- Đường huyết: ").append(record.getDuongHuyetMgdl()).append(" mg/dL");
            sb.append(" (Thời điểm: ").append(record.getThoiDiemDoDuong() != null ? record.getThoiDiemDoDuong() : "không rõ").append(")\n");
        }
        if (record.getNhipTim() != null) {
            sb.append("- Nhịp tim: ").append(record.getNhipTim()).append(" BPM\n");
        }
        if (record.getHuyetApTamThu() != null && record.getHuyetApTamTruong() != null) {
            sb.append("- Huyết áp: ").append(record.getHuyetApTamThu()).append("/").append(record.getHuyetApTamTruong()).append(" mmHg\n");
        }

        sb.append("\n=== NGƯỠNG THAM KHẢO CHO BỆNH NHÂN TIỂU ĐƯỜNG ===\n");
        sb.append("- Đường huyết lúc đói: 80-130 mg/dL (mục tiêu), >180 (cao), >250 (nguy hiểm)\n");
        sb.append("- Đường huyết sau ăn 2h: <180 mg/dL (mục tiêu)\n");
        sb.append("- Huyết áp: <130/80 mmHg (mục tiêu cho ĐTĐ)\n");
        sb.append("- Nhịp tim: 60-100 BPM (bình thường)\n");

        sb.append("\n=== YÊU CẦU: Trả lời CHÍNH XÁC theo JSON format sau (KHÔNG thêm markdown, KHÔNG thêm ```json) ===\n");
        sb.append("{\n");
        sb.append("  \"diem_nguy_co\": <số từ 0-100, 0=an toàn nhất, 100=nguy hiểm nhất>,\n");
        sb.append("  \"muc_canh_bao\": \"<an_toan|trung_binh|cao|nguy_hiem>\",\n");
        sb.append("  \"do_tin_cay\": <số từ 0.0 đến 1.0>,\n");
        sb.append("  \"phan_tich\": \"<Phân tích chi tiết bằng tiếng Việt, 2-3 câu>\",\n");
        sb.append("  \"yeu_to_nguy_co\": [\"yếu tố 1\", \"yếu tố 2\"],\n");
        sb.append("  \"khuyen_nghi\": [\"khuyến nghị 1\", \"khuyến nghị 2\", \"khuyến nghị 3\"]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private AIAnalysis parseHealthAnalysisResponse(String response, HealthRecord record, Patient patient) {
        AIAnalysis analysis = new AIAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setPatientId(record.getPatientId());
        analysis.setModelVersion(model);

        try {
            // Dọn dẹp response - loại bỏ markdown code fences nếu có
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            } else if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonObject json = JsonParser.parseString(cleanResponse).getAsJsonObject();

            analysis.setDiemNguyCo(json.has("diem_nguy_co") ? json.get("diem_nguy_co").getAsDouble() : 50.0);
            analysis.setMucCanhBao(json.has("muc_canh_bao") ? json.get("muc_canh_bao").getAsString() : "trung_binh");
            analysis.setDoTinCay(json.has("do_tin_cay") ? json.get("do_tin_cay").getAsDouble() : 0.7);
            analysis.setPhanTichChiTiet(json.has("phan_tich") ? json.get("phan_tich").getAsString() : "Đã phân tích.");

            if (json.has("yeu_to_nguy_co")) {
                analysis.setYeuToNguyCo(json.get("yeu_to_nguy_co").toString());
            }
            if (json.has("khuyen_nghi")) {
                analysis.setKhuyenNghi(json.get("khuyen_nghi").toString());
            }

            // Lưu dữ liệu đầu vào
            JsonObject inputData = new JsonObject();
            if (record.getDuongHuyetMgdl() != null) inputData.addProperty("duong_huyet_mgdl", record.getDuongHuyetMgdl());
            if (record.getNhipTim() != null) inputData.addProperty("nhip_tim", record.getNhipTim());
            if (record.getHuyetApTamThu() != null) inputData.addProperty("huyet_ap_tam_thu", record.getHuyetApTamThu());
            if (record.getHuyetApTamTruong() != null) inputData.addProperty("huyet_ap_tam_truong", record.getHuyetApTamTruong());
            analysis.setDuLieuDauVao(inputData.toString());

        } catch (Exception e) {
            System.err.println("[GeminiService] Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
            return createFallbackAnalysis(record, patient);
        }

        return analysis;
    }

    /**
     * Phân tích dự phòng khi API không khả dụng — dùng rule-based thuần túy.
     */
    private AIAnalysis createFallbackAnalysis(HealthRecord record, Patient patient) {
        AIAnalysis analysis = new AIAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setPatientId(record.getPatientId());
        analysis.setModelVersion("rule-based-fallback");
        analysis.setDoTinCay(0.6);

        double score = 0;
        StringBuilder details = new StringBuilder();
        StringBuilder riskFactors = new StringBuilder("[");
        StringBuilder recommendations = new StringBuilder("[");

        // Rule-based: Đường huyết
        if (record.getDuongHuyetMgdl() != null) {
            double glucose = record.getDuongHuyetMgdl();
            if (glucose > 250) {
                score += 40;
                details.append("Đường huyết ở mức nguy hiểm (").append(glucose).append(" mg/dL). ");
                riskFactors.append("\"Đường huyết rất cao\",");
                recommendations.append("\"Liên hệ bác sĩ ngay lập tức\",");
            } else if (glucose > 180) {
                score += 25;
                details.append("Đường huyết cao hơn mức mục tiêu (").append(glucose).append(" mg/dL). ");
                riskFactors.append("\"Đường huyết cao\",");
                recommendations.append("\"Theo dõi sát và kiểm tra lại sau 2 giờ\",");
            } else if (glucose >= 70 && glucose <= 130) {
                details.append("Đường huyết trong khoảng mục tiêu (").append(glucose).append(" mg/dL). ");
            } else if (glucose < 70) {
                score += 35;
                details.append("Đường huyết quá thấp - nguy cơ hạ đường huyết (").append(glucose).append(" mg/dL). ");
                riskFactors.append("\"Hạ đường huyết\",");
                recommendations.append("\"Ăn/uống ngay 15g carbohydrate nhanh\",");
            }
        }

        // Rule-based: Huyết áp
        if (record.getHuyetApTamThu() != null) {
            int sys = record.getHuyetApTamThu();
            if (sys >= 180) {
                score += 30;
                riskFactors.append("\"Huyết áp rất cao\",");
                recommendations.append("\"Cần kiểm soát huyết áp khẩn cấp\",");
            } else if (sys >= 140) {
                score += 15;
                riskFactors.append("\"Huyết áp tăng\",");
            }
        }

        // Rule-based: Nhịp tim
        if (record.getNhipTim() != null) {
            int hr = record.getNhipTim();
            if (hr > 100 || hr < 60) {
                score += 10;
                riskFactors.append("\"Nhịp tim bất thường\",");
            }
        }

        // Xác định mức cảnh báo
        String mucCanhBao;
        if (score >= 60) mucCanhBao = "nguy_hiem";
        else if (score >= 35) mucCanhBao = "cao";
        else if (score >= 15) mucCanhBao = "trung_binh";
        else mucCanhBao = "an_toan";

        if (details.length() == 0) {
            details.append("Các chỉ số trong giới hạn bình thường.");
        }
        recommendations.append("\"Tiếp tục theo dõi định kỳ và tuân thủ phác đồ điều trị\"]");
        riskFactors.append("]");

        // Clean up JSON arrays
        String riskFactorsStr = riskFactors.toString().replace(",]", "]").replace("[,", "[");
        String recommendationsStr = recommendations.toString().replace(",]", "]").replace("[,", "[");

        analysis.setDiemNguyCo(Math.min(score, 100));
        analysis.setMucCanhBao(mucCanhBao);
        analysis.setPhanTichChiTiet(details.toString().trim());
        analysis.setYeuToNguyCo(riskFactorsStr);
        analysis.setKhuyenNghi(recommendationsStr);

        // Lưu dữ liệu đầu vào
        JsonObject inputData = new JsonObject();
        if (record.getDuongHuyetMgdl() != null) inputData.addProperty("duong_huyet_mgdl", record.getDuongHuyetMgdl());
        if (record.getNhipTim() != null) inputData.addProperty("nhip_tim", record.getNhipTim());
        if (record.getHuyetApTamThu() != null) inputData.addProperty("huyet_ap_tam_thu", record.getHuyetApTamThu());
        analysis.setDuLieuDauVao(inputData.toString());

        return analysis;
    }

    // ==================== CHỨC NĂNG 2: CHATBOT ====================

    /**
     * Chatbot AI hỗ trợ bệnh nhân hỏi đáp về tiểu đường.
     * Luôn kèm disclaimer rằng đây chỉ là tham khảo.
     */
    public String chat(String userMessage, String patientContext) {
        String prompt = buildChatPrompt(userMessage, patientContext);
        String response = callGeminiAPI(prompt);

        if (response == null || response.isEmpty()) {
            return "Xin lỗi, tôi đang gặp sự cố kết nối. Vui lòng thử lại sau hoặc liên hệ bác sĩ nếu có vấn đề khẩn cấp.";
        }

        return response;
    }

    private String buildChatPrompt(String userMessage, String patientContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là DiabCare AI — trợ lý sức khỏe chuyên về bệnh tiểu đường. ");
        sb.append("Hãy trả lời bằng tiếng Việt, ngắn gọn và dễ hiểu.\n\n");

        sb.append("=== QUY TẮC BẮT BUỘC ===\n");
        sb.append("1. KHÔNG BAO GIỜ tự kê đơn thuốc hoặc thay đổi liều lượng thuốc.\n");
        sb.append("2. KHÔNG BAO GIỜ chẩn đoán bệnh. Chỉ đưa thông tin tham khảo.\n");
        sb.append("3. LUÔN LUÔN khuyên bệnh nhân hỏi ý kiến bác sĩ cho mọi quyết định y tế.\n");
        sb.append("4. Nếu phát hiện tình huống khẩn cấp (đường huyết <54 hoặc >400, mất ý thức...), ");
        sb.append("khuyên gọi cấp cứu NGAY LẬP TỨC.\n");
        sb.append("5. Cuối mỗi câu trả lời, thêm dòng: \"⚠️ Lưu ý: Thông tin chỉ mang tính tham khảo. Vui lòng tham khảo ý kiến bác sĩ.\"\n\n");

        if (patientContext != null && !patientContext.isEmpty()) {
            sb.append("=== BỐI CẢNH BỆNH NHÂN ===\n");
            sb.append(patientContext).append("\n\n");
        }

        sb.append("=== CÂU HỎI CỦA BỆNH NHÂN ===\n");
        sb.append(userMessage);

        return sb.toString();
    }

    // ==================== CHỨC NĂNG 3: BÁO CÁO CHO BÁC SĨ ====================

    /**
     * Tạo báo cáo tổng hợp cho bác sĩ dựa trên dữ liệu bệnh nhân.
     */
    public String generateDoctorReport(Patient patient, String healthSummary, String prescriptionSummary) {
        String prompt = buildDoctorReportPrompt(patient, healthSummary, prescriptionSummary);
        String response = callGeminiAPI(prompt);

        if (response == null || response.isEmpty()) {
            return "Không thể tạo báo cáo AI lúc này. Vui lòng thử lại sau.";
        }

        return response;
    }

    private String buildDoctorReportPrompt(Patient patient, String healthSummary, String prescriptionSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là AI hỗ trợ bác sĩ nội tiết phân tích hồ sơ bệnh nhân tiểu đường. ");
        sb.append("Hãy tạo BÁO CÁO TỔNG HỢP chuyên nghiệp bằng tiếng Việt.\n\n");

        sb.append("=== THÔNG TIN BỆNH NHÂN ===\n");
        if (patient != null) {
            sb.append("- Họ tên: ").append(patient.getHoTen() != null ? patient.getHoTen() : "N/A").append("\n");
            sb.append("- Loại tiểu đường: ").append(patient.getLoaiTieuDuong() != null ? patient.getLoaiTieuDuong() : "Type 2").append("\n");
            sb.append("- Giới tính: ").append(patient.getGioiTinh() != null ? patient.getGioiTinh() : "N/A").append("\n");
            if (patient.getTienSuBenh() != null) sb.append("- Tiền sử bệnh: ").append(patient.getTienSuBenh()).append("\n");
            if (patient.getTienSuGiaDinh() != null) sb.append("- Tiền sử gia đình: ").append(patient.getTienSuGiaDinh()).append("\n");
            if (patient.getDiUng() != null) sb.append("- Dị ứng: ").append(patient.getDiUng()).append("\n");
        }

        sb.append("\n=== DỮ LIỆU SỨC KHỎE GẦN ĐÂY ===\n");
        sb.append(healthSummary != null ? healthSummary : "Không có dữ liệu.").append("\n");

        sb.append("\n=== ĐƠN THUỐC HIỆN TẠI ===\n");
        sb.append(prescriptionSummary != null ? prescriptionSummary : "Không có dữ liệu.").append("\n");

        sb.append("\n=== YÊU CẦU BÁO CÁO ===\n");
        sb.append("Vui lòng phân tích và trả lời theo các mục sau:\n");
        sb.append("1. **ĐÁNH GIÁ TỔNG QUAN**: Tình trạng kiểm soát đường huyết của bệnh nhân.\n");
        sb.append("2. **XU HƯỚNG**: Các chỉ số đang cải thiện hay xấu đi?\n");
        sb.append("3. **YẾU TỐ NGUY CƠ**: Nguy cơ biến chứng (thận, mắt, tim mạch, thần kinh).\n");
        sb.append("4. **ĐÁNH GIÁ TUÂN THỦ ĐIỀU TRỊ**: Dựa trên dữ liệu đo, bệnh nhân có tuân thủ tốt không?\n");
        sb.append("5. **KHUYẾN NGHỊ CHO BÁC SĨ**: Gợi ý điều chỉnh phác đồ (nếu cần).\n\n");
        sb.append("LƯU Ý: Đây là báo cáo hỗ trợ tham khảo. Quyết định lâm sàng cuối cùng thuộc về bác sĩ điều trị.\n");

        return sb.toString();
    }

    // ==================== CORE API CALL ====================

    /**
     * Gọi Google Gemini API và trả về text response.
     */
    private String callGeminiAPI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[GeminiService] API key is not configured!");
            return null;
        }

        HttpURLConnection conn = null;
        try {
            String urlStr = API_BASE_URL + model + ":generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // 30s
            conn.setReadTimeout(60000);    // 60s

            // Build request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Generation config
            JsonObject genConfig = new JsonObject();
            genConfig.addProperty("maxOutputTokens", maxTokens);
            genConfig.addProperty("temperature", 0.3); // Thấp hơn = chính xác hơn cho y tế
            requestBody.add("generationConfig", genConfig);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
                // Parse Gemini response to extract text
                JsonObject responseJson = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObj = firstCandidate.getAsJsonObject("content");
                    JsonArray partsArr = contentObj.getAsJsonArray("parts");
                    if (partsArr != null && partsArr.size() > 0) {
                        return partsArr.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            } else {
                System.err.println("[GeminiService] API Error (" + responseCode + "): " + responseBuilder.toString());
            }

        } catch (Exception e) {
            System.err.println("[GeminiService] Exception calling Gemini API: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return null;
    }
}
