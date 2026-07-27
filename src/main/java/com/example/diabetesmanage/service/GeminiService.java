package com.example.diabetesmanage.service;

import com.example.diabetesmanage.config.GeminiConfig;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.MedicationLog;
import com.example.diabetesmanage.model.MasterFood;
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
import java.util.List;
import java.time.LocalDate;
import java.time.Period;

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

    // Simple in-memory cache for medication reminders to avoid hitting Gemini API rate limits
    private static final java.util.Map<String, String> medicationReminderCache = new java.util.concurrent.ConcurrentHashMap<>();

    public GeminiService() {
        loadConfig();
    }

    private void loadConfig() {
        GeminiConfig config = GeminiConfig.load();
        if (config.isConfigured()) {
            this.apiKey = config.getApiKey();
            this.model = config.getModel();
            this.maxTokens = 2048;
            System.out.println("[GeminiService] Loaded API key from " + config.getConfigSource()
                    + ", model=" + this.model);
            return;
        }

        // Fallback: legacy config.properties
        Properties props = new Properties();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            if (is == null) {
                is = GeminiService.class.getResourceAsStream("/config.properties");
            }
            if (is != null) {
                props.load(is);
                this.apiKey = props.getProperty("gemini.api.key", "");
                this.model = props.getProperty("gemini.model", "gemini-flash-lite-latest");
                this.maxTokens = Integer.parseInt(props.getProperty("gemini.max.tokens", "2048"));
                is.close();
                System.out.println("[GeminiService] Loaded API key from config.properties, model=" + this.model);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("[GeminiService] No Gemini API key found. Set GEMINI_API_KEY or gemini.properties.");
        this.apiKey = "";
        this.model = "gemini-flash-lite-latest";
        this.maxTokens = 2048;
    }

    // ==================== CHỨC NĂNG 1: PHÂN TÍCH SỨC KHỎE ====================

    /**
     * Phân tích chỉ số sức khỏe của bệnh nhân và trả về đánh giá nguy cơ.
     * Gọi sau khi bệnh nhân nhập dữ liệu sức khỏe mới.
     */
    public AIAnalysis analyzeHealthData(HealthRecord record, Patient patient) {
        String prompt = buildHealthAnalysisPrompt(record, patient);
        String response = callGeminiAPI(prompt, true);

        if (response == null || response.isEmpty() || response.startsWith("ERROR:")) {
            return createFallbackAnalysis(record, patient, response != null ? response : "Unknown error");
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
            String cleanResponse = response.trim();
            int startIndex = cleanResponse.indexOf('{');
            int endIndex = cleanResponse.lastIndexOf('}');
            if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                cleanResponse = cleanResponse.substring(startIndex, endIndex + 1);
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
            System.err.println("[GeminiService] Raw Response was: " + response);
            e.printStackTrace();
            return createFallbackAnalysis(record, patient, "ERROR: Parse Failed - " + e.getMessage());
        }

        return analysis;
    }

    /**
     * Phân tích dự phòng khi API không khả dụng — dùng rule-based thuần túy.
     */
    private AIAnalysis createFallbackAnalysis(HealthRecord record, Patient patient) {
        return createFallbackAnalysis(record, patient, "rule-based-fallback");
    }

    private AIAnalysis createFallbackAnalysis(HealthRecord record, Patient patient, String errorMsg) {
        AIAnalysis analysis = new AIAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setPatientId(record.getPatientId());
        
        String modelStr = errorMsg != null ? errorMsg : "rule-based-fallback";
        if (modelStr.length() > 50) {
            modelStr = modelStr.substring(0, 47) + "...";
        }
        analysis.setModelVersion(modelStr);
        
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
     * Chatbot AI hỗ trợ bệnh nhân — ủy quyền cho {@link HealthChatService}.
     */
    public String chat(String userMessage, String patientContext) {
        PatientHealthContext ctx = new PatientHealthContext();
        if (patientContext != null && !patientContext.isBlank()) {
            ctx.setLoaiTieuDuong(extractLineValue(patientContext, "Loại tiểu đường:"));
            ctx.setTienSuBenhTomTat(extractLineValue(patientContext, "Tiền sử bệnh:"));
            ctx.setDuongHuyetMgdl(parseDouble(extractLineValue(patientContext, "Đường huyết gần nhất:")));
            String bp = extractLineValue(patientContext, "Huyết áp gần nhất:");
            if (bp != null && bp.contains("/")) {
                String[] parts = bp.replace(" mmHg", "").split("/");
                if (parts.length == 2) {
                    ctx.setHuyetApTamThu(parseInt(parts[0].trim()));
                    ctx.setHuyetApTamTruong(parseInt(parts[1].trim()));
                }
            }
        }
        HealthChatResponse response = new HealthChatService(this).process(userMessage, ctx);
        return response.getReply();
    }

    private static String extractLineValue(String block, String prefix) {
        for (String line : block.split("\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private static Double parseDouble(String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value.replace(" mg/dL", "").replace("%", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== CHỨC NĂNG 3: BÁO CÁO CHO BÁC SĨ ====================

    /**
     * Tạo báo cáo tổng hợp cho bác sĩ dựa trên dữ liệu bệnh nhân.
     */
    public String generateDoctorReport(Patient patient, String healthSummary, String prescriptionSummary) {
        String prompt = buildDoctorReportPrompt(patient, healthSummary, prescriptionSummary);
        String response = callGeminiAPI(prompt, false);

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

    // ==================== CHỨC NĂNG 4: NHẮC NHỞ UỐNG THUỐC AI ====================

    /**
     * Tạo lời nhắc nhở uống thuốc cá nhân hóa bằng AI.
     */
    public String generateMedicationReminder(String patientName, List<MedicationLog> checklist) {
        if (checklist == null || checklist.isEmpty()) {
            return "Hôm nay bạn không có lịch uống thuốc nào. Hãy tiếp tục duy trì thói quen sống khỏe nhé!";
        }

        int chuaUong = 0;
        int daUong = 0;
        for (MedicationLog log : checklist) {
            if ("da_uong".equals(log.getTrangThai())) {
                daUong++;
            } else {
                chuaUong++;
            }
        }

        // Cache check: patient + date + daUong + total
        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String cacheKey = (patientName != null ? patientName : "default") + "_" + todayStr + "_" + daUong + "_" + (daUong + chuaUong);
        if (medicationReminderCache.containsKey(cacheKey)) {
            System.out.println("[GeminiService] Cache HIT for medication reminder: " + cacheKey);
            return medicationReminderCache.get(cacheKey);
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một trợ lý y tế ảo thân thiện của ứng dụng DiabCare, chuyên chăm sóc bệnh nhân tiểu đường. ");
        prompt.append("Dưới đây là danh sách thuốc hôm nay của bệnh nhân tên là ").append(patientName != null ? patientName : "Bệnh nhân").append(":\n\n");

        for (MedicationLog log : checklist) {
            prompt.append("- Thuốc: ").append(log.getTenThuoc())
                  .append(" (").append(log.getLieuLuong()).append(" ").append(log.getDonVi()).append(") - ");
            if ("da_uong".equals(log.getTrangThai())) {
                prompt.append("ĐÃ UỐNG\n");
            } else {
                prompt.append("CHƯA UỐNG\n");
            }
        }

        prompt.append("\n=== YÊU CẦU ===\n");
        prompt.append("Hãy viết MỘT lời nhắn siêu ngắn gọn (tối đa 2-3 câu), thân thiện và dùng emoji để:\n");
        if (chuaUong == 0) {
            prompt.append("- Khen ngợi bệnh nhân vì đã uống đủ tất cả các loại thuốc hôm nay. Động viên họ tiếp tục phát huy.\n");
        } else if (daUong == 0) {
            prompt.append("- Nhắc nhở nhẹ nhàng bệnh nhân nhớ uống thuốc đúng giờ vì hôm nay họ chưa uống viên nào.\n");
        } else {
            prompt.append("- Khen ngợi phần đã uống, và nhắc nhở họ đừng quên ").append(chuaUong).append(" loại thuốc còn lại trong ngày.\n");
        }
        prompt.append("LƯU Ý: Trả lời trực tiếp đoạn hội thoại, không cần mào đầu, không cần định dạng Markdown phức tạp.");

        String response = callGeminiAPI(prompt.toString(), false);

        if (response == null || response.isEmpty() || response.startsWith("ERROR:")) {
            if (chuaUong == 0) return "Tuyệt vời! Bạn đã uống đủ thuốc hôm nay. Hãy tiếp tục phát huy nhé! 🌟";
            return "Đừng quên uống các loại thuốc còn lại trong ngày hôm nay nhé! Sức khỏe là quan trọng nhất! 💊";
        }

        String cleanedResponse = response.trim();
        medicationReminderCache.put(cacheKey, cleanedResponse);
        return cleanedResponse;
    }

    // ==================== CORE API CALL ====================

    /**
     * Gọi Google Gemini API và trả về text response.
     */
    protected String callGeminiAPI(String prompt, boolean forceJson) {
        return callGeminiAPI(prompt, forceJson, 0.3, maxTokens);
    }

    /**
     * Gọi Gemini với cấu hình riêng cho chat sức khỏe (temperature thấp, token giới hạn).
     */
    public String callHealthChatAPI(String prompt) {
        return callHealthChatAPI(null, prompt);
    }

    /**
     * Gọi Gemini cho chat sức khỏe với systemInstruction (luật y tế) tách riêng khỏi câu hỏi.
     * Cách này giúp model trả lời TỰ NHIÊN mà vẫn tuân thủ luật.
     */
    public String callHealthChatAPI(String systemInstruction, String userPrompt) {
        // Temperature cao hơn để hội thoại tự nhiên, vẫn an toàn nhờ systemInstruction + safetySettings
        return callGeminiAPI(systemInstruction, userPrompt, false, 0.6, Math.min(maxTokens, 1024));
    }

    /**
     * Gọi Google Gemini API với temperature và maxTokens tùy chỉnh.
     */
    protected String callGeminiAPI(String prompt, boolean forceJson, double temperature, int tokenLimit) {
        return callGeminiAPI(null, prompt, forceJson, temperature, tokenLimit);
    }

    /**
     * Gọi Google Gemini API — hỗ trợ systemInstruction tùy chọn.
     */
    protected String callGeminiAPI(String systemInstruction, String prompt, boolean forceJson,
                                   double temperature, int tokenLimit) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[GeminiService] API key is not configured!");
            return "ERROR:API_KEY_MISSING";
        }

        String[] modelsToTry = buildModelCandidates();
        String lastError = "ERROR:Unknown";

        for (String candidateModel : modelsToTry) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                String result = invokeGeminiOnce(candidateModel, systemInstruction, prompt,
                        forceJson, temperature, tokenLimit);
                if (result != null && !result.startsWith("ERROR:")) {
                    if (!candidateModel.equals(this.model)) {
                        System.out.println("[GeminiService] Fallback model succeeded: " + candidateModel);
                    }
                    return result;
                }
                lastError = result != null ? result : "ERROR:Unknown";
                if (lastError.contains("429") || lastError.contains("RATE_LIMIT")) {
                    try {
                        Thread.sleep(1200L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "ERROR:RATE_LIMIT";
                    }
                    continue;
                }
                // Non-rate-limit errors: try next model
                break;
            }
        }
        return lastError;
    }

    private String[] buildModelCandidates() {
        java.util.LinkedHashSet<String> models = new java.util.LinkedHashSet<>();
        if (model != null && !model.isBlank()) {
            models.add(model.trim());
        }
        // Ưu tiên model còn free-tier quota (2.0-flash thường hết hạn mức sớm)
        models.add("gemini-flash-lite-latest");
        models.add("gemini-3.1-flash-lite");
        models.add("gemini-3.5-flash-lite");
        models.add("gemini-flash-latest");
        models.add("gemini-2.0-flash");
        return models.toArray(new String[0]);
    }

    private String invokeGeminiOnce(String modelName, String systemInstruction, String prompt,
                                    boolean forceJson, double temperature, int tokenLimit) {
        HttpURLConnection conn = null;
        try {
            String urlStr = API_BASE_URL + modelName + ":generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

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

            // System instruction: đặt luật y tế cho model (áp dụng xuyên suốt, tự nhiên hơn)
            if (systemInstruction != null && !systemInstruction.isBlank()) {
                JsonObject sysContent = new JsonObject();
                JsonArray sysParts = new JsonArray();
                JsonObject sysPart = new JsonObject();
                sysPart.addProperty("text", systemInstruction);
                sysParts.add(sysPart);
                sysContent.add("parts", sysParts);
                requestBody.add("system_instruction", sysContent);
            }

            // Safety settings: chặn nội dung nguy hiểm ở tầng model
            JsonArray safetySettings = new JsonArray();
            String[] categories = {
                    "HARM_CATEGORY_HARASSMENT",
                    "HARM_CATEGORY_HATE_SPEECH",
                    "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "HARM_CATEGORY_DANGEROUS_CONTENT"
            };
            for (String category : categories) {
                JsonObject setting = new JsonObject();
                setting.addProperty("category", category);
                setting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                safetySettings.add(setting);
            }
            requestBody.add("safetySettings", safetySettings);

            JsonObject genConfig = new JsonObject();
            genConfig.addProperty("maxOutputTokens", tokenLimit);
            genConfig.addProperty("temperature", temperature);
            if (forceJson) {
                genConfig.addProperty("responseMimeType", "application/json");
            }
            requestBody.add("generationConfig", genConfig);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder responseBuilder = new StringBuilder();
            if (inputStream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
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
                return "ERROR:EmptyCandidates";
            }

            System.err.println("[GeminiService] API Error model=" + modelName
                    + " (" + responseCode + "): " + truncate(responseBuilder.toString(), 400));
            if (responseCode == 429) {
                return "ERROR:RATE_LIMIT:429";
            }
            if (responseCode == 503) {
                return "ERROR:UNAVAILABLE:503";
            }
            if (responseCode == 404) {
                return "ERROR:MODEL_NOT_FOUND:" + modelName;
            }
            return "ERROR:HTTP_" + responseCode;

        } catch (Exception e) {
            System.err.println("[GeminiService] Exception calling Gemini API: " + e.getMessage());
            return "ERROR:" + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    public String extractMedicalDataFromText(String pdfText) {
        String prompt = "Bạn là một AI chuyên ngành y tế. Hãy trích xuất các chỉ số sức khỏe từ văn bản bệnh án sau đây và trả về dưới dạng JSON hợp lệ (chỉ trả về JSON, không chứa định dạng markdown ```json).\n"
                + "Nếu không tìm thấy chỉ số nào, hãy trả về giá trị null cho trường đó.\n\n"
                + "Các trường bắt buộc trong JSON:\n"
                + "{\n"
                + "  \"canNangKg\": (kiểu số float),\n"
                + "  \"chieuCaoCm\": (kiểu số float),\n"
                + "  \"nhomMau\": (kiểu chuỗi),\n"
                + "  \"huyetApTamThu\": (kiểu số nguyên),\n"
                + "  \"huyetApTamTruong\": (kiểu số nguyên),\n"
                + "  \"nhipTim\": (kiểu số nguyên, tìm các từ như 'Nhịp tim', 'Mạch'),\n"
                + "  \"duongHuyetMgdl\": (kiểu số float, tìm các từ như 'Glucose', 'Đường máu'. Nếu đơn vị là mmol/L, BẮT BUỘC nhân 18 để ra mg/dL),\n"
                + "  \"hba1c\": (kiểu số float, tìm 'HbA1c'),\n"
                + "  \"cholesterol\": (kiểu số float, tìm các từ như 'Cholesterol', 'Cholesterol máu', 'Cholesterol toàn phần'),\n"
                + "  \"triglyceride\": (kiểu số float, tìm các từ như 'Triglyceride', 'Triglycerid', 'Triglycerid máu'),\n"
                + "  \"ngayChanDoanTieuDuong\": (kiểu chuỗi định dạng YYYY-MM-DD),\n"
                + "  \"ghiChu\": (kiểu chuỗi)\n"
                + "}\n\n"
                + "Hướng dẫn chi tiết:\n"
                + "1. Huyết áp: Ví dụ '120/80 mmHg' -> huyetApTamThu = 120, huyetApTamTruong = 80.\n"
                + "2. Đường huyết: NẾU có đơn vị là mmol/L, hãy nhân giá trị đó với 18 để ra mg/dL (VD: 6.2 mmol/L -> 111.6).\n"
                + "3. Chỉ lấy phần số, bỏ %, bỏ mmol/L, U/L, g/L, v.v.\n\n"
                + "Nội dung bệnh án:\n" + pdfText;

        String rawResponse = callGeminiAPI(prompt, true);
        if (rawResponse != null && rawResponse.startsWith("ERROR:")) {
            return "{}";
        }

        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    /**
     * Tính BMR theo công thức Mifflin-St Jeor, sau đó nhân hệ số vận động để ra TDEE.
     * Với bệnh nhân tiểu đường, thường lấy TDEE và có thể điều chỉnh giảm nhẹ (~ -10% đến -15%)
     * nếu mục tiêu là kiểm soát cân nặng/đường huyết, theo hướng dẫn ADA (không cắt giảm quá mạnh).
     */
    /** Tính tuổi hiện tại từ ngày sinh. */
    private int tinhTuoiTuNgaySinh(LocalDate ngaySinh) {
        return Period.between(ngaySinh, LocalDate.now()).getYears();
    }

    private double tinhCaloMucTieu(double canNangKg, double chieuCaoCm, int tuoi, String gioiTinh, double heSoVanDong) {
        double bmr;
        if ("Nam".equalsIgnoreCase(gioiTinh)) {
            bmr = 10 * canNangKg + 6.25 * chieuCaoCm - 5 * tuoi + 5;
        } else {
            bmr = 10 * canNangKg + 6.25 * chieuCaoCm - 5 * tuoi - 161;
        }
        double tdee = bmr * heSoVanDong; // vd: 1.2 ít vận động, 1.375 vận động nhẹ, 1.55 vừa
        return tdee;
    }

    public String generateDailyDietPlan(Patient patient, HealthRecord record, List<MasterFood> foods) {
        if (foods == null || foods.isEmpty()) {
            return "[]";
        }

        // Mặc định "ít vận động" vì app hiện chưa thu thập mức độ vận động của bệnh nhân.
        // Đây là lựa chọn thận trọng cho tính toán y tế (thà ước tính calo thấp hơn
        // thực tế một chút còn hơn đưa ra mục tiêu quá cao). Nếu sau này có field
        // mức độ vận động, thay HE_SO_VAN_DONG_MAC_DINH bằng giá trị lấy từ hồ sơ.
        final double HE_SO_VAN_DONG_MAC_DINH = 1.2;

        // --- Tính calo mục tiêu trước bằng code, không để AI tự suy luận ---
        Double caloMucTieu = null;
        Double carbMucTieuMoiBua = null;
        if (patient != null && record != null
                && record.getCanNangKg() != null
                && patient.getChieuCaoCm() != null
                && patient.getNgaySinhLocalDate() != null) {
            int tuoi = tinhTuoiTuNgaySinh(patient.getNgaySinhLocalDate());
            caloMucTieu = tinhCaloMucTieu(record.getCanNangKg(), patient.getChieuCaoCm(),
                    tuoi, patient.getGioiTinh(), HE_SO_VAN_DONG_MAC_DINH);
            // Khuyến nghị chung cho tiểu đường: ~45% năng lượng từ carbs, chia 3 bữa
            carbMucTieuMoiBua = (caloMucTieu * 0.45 / 4) / 3; // 4 kcal/g carb, chia 3 bữa
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là chuyên gia dinh dưỡng cho bệnh nhân tiểu đường. ");
        prompt.append("Hãy gợi ý thực đơn 1 ngày (Sáng, Trưa, Tối) chỉ từ danh sách món có sẵn.\n");
        prompt.append("Thông tin bệnh nhân:\n");
        if (patient != null) {
            prompt.append("- Chiều cao: ").append(patient.getChieuCaoCm()).append(" cm\n");
        }
        if (record != null) {
            prompt.append("- Cân nặng: ").append(record.getCanNangKg() != null ? record.getCanNangKg() : "Không rõ").append(" kg\n");
            prompt.append("- Đường huyết gần nhất: ").append(record.getDuongHuyetMgdl() != null ? record.getDuongHuyetMgdl() : "Không rõ").append(" mg/dL\n");
            prompt.append("- HbA1c: ").append(record.getHba1cPercent() != null ? record.getHba1cPercent() : "Không rõ").append(" %\n");
            prompt.append("- BMI: ").append(record.getBmi() != null ? record.getBmi() : "Không rõ").append("\n");
        }

        if (caloMucTieu != null) {
            prompt.append(String.format("\nMỤC TIÊU NĂNG LƯỢNG: khoảng %.0f kcal/ngày (tổng 3 bữa nên nằm trong khoảng %.0f–%.0f kcal).\n",
                    caloMucTieu, caloMucTieu * 0.9, caloMucTieu * 1.1));
            prompt.append(String.format("MỤC TIÊU CARBS: khoảng %.0fg carbs mỗi bữa (Sáng/Trưa/Tối), không chênh lệch quá 20%%.\n",
                    carbMucTieuMoiBua));
        } else {
            prompt.append("\n(Không đủ dữ liệu tuổi/giới tính để tính calo mục tiêu chính xác — hãy chọn khẩu phần trung bình, ưu tiên GI thấp.)\n");
        }

        prompt.append("\nDanh sách thực phẩm (CHỈ dùng đúng id bên dưới):\n");
        for (MasterFood f : foods) {
            prompt.append("- id=\"").append(f.getId()).append("\": ").append(f.getTenThucPham())
                    .append(" | Loại=").append(f.getLoaiMon() != null ? f.getLoaiMon() : "N/A")
                    .append(" | Carbs=").append(f.getCarbsG()).append("g")
                    .append(" | Calo=").append(f.getCaloKcal())
                    .append(" | GI=").append(f.getChiSoGI()).append("\n");
        }

        prompt.append("\nQUY TẮC:\n");
        prompt.append("1. Chỉ chọn foodId nằm trong danh sách trên. Không bịa id mới.\n");
        prompt.append("2. CẤU TRÚC BỮA ĂN: Mỗi bữa (Sáng/Trưa/Tối) chọn CHÍNH XÁC 1 món có Loại='mon_chinh', và có thể kèm TỐI ĐA 2 món phụ (Loại='rau_cu', 'trai_cay' hoặc 'mon_phu'). Không chọn 2 món chính trong cùng một bữa.\n");
        prompt.append("3. PHÂN BỔ NĂNG LƯỢNG: Bữa Tối nên có tổng calo và carbs THẤP HƠN bữa Sáng và bữa Trưa.\n");
        prompt.append("4. Tổng calo/carbs cả ngày phải bám sát mục tiêu năng lượng đã nêu ở trên (nếu có), ưu tiên GI thấp.\n");
        prompt.append("5. Trả về DUY NHẤT một mảng JSON (không markdown, không giải thích):\n");
        prompt.append("[{\"foodId\":\"").append(foods.get(0).getId())
                .append("\",\"buaAn\":\"Sáng\",\"ghiChu\":\"Gợi ý\"}]\n");

        String rawResponse = callGeminiAPI(prompt.toString(), true);
        System.out.println("[GeminiService] generateDailyDietPlan raw="
                + (rawResponse == null ? "null" : rawResponse.substring(0, Math.min(200, rawResponse.length()))));

        if (rawResponse == null || rawResponse.startsWith("ERROR")) {
            return "[]";
        }

        String cleaned = trichXuatJsonArray(rawResponse);
        if (cleaned == null) {
            System.out.println("[GeminiService] Không tìm thấy JSON array hợp lệ trong response.");
            return "[]";
        }

        // Kiểm tra tất cả foodId trả về đều nằm trong danh sách gốc — bắt buộc,
        // để tránh AI hallucinate id không tồn tại gây lỗi ở tầng dưới.
        String hopLe = locFoodIdHopLe(cleaned, foods);
        return hopLe;
    }

    /**
     * Trích nội dung từ dấu '[' đầu tiên đến ']' cuối cùng, thay vì chỉ cắt tiền tố ```json.
     * Chịu được trường hợp model trả về có khoảng trắng/giải thích thừa quanh JSON.
     */
    private String trichXuatJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            return null;
        }
        return raw.substring(start, end + 1).trim();
    }

    /**
     * Lọc bỏ các phần tử có foodId không nằm trong danh sách food gốc.
     * Đây là bước validation bắt buộc cho một ứng dụng y tế — không tin tưởng
     * hoàn toàn output của LLM dù đã yêu cầu trong prompt.
     */
    private String locFoodIdHopLe(String jsonArrayStr, List<MasterFood> foods) {
        try {
            com.google.gson.JsonArray arr = com.google.gson.JsonParser.parseString(jsonArrayStr).getAsJsonArray();
            com.google.gson.JsonArray ketQua = new com.google.gson.JsonArray();
            java.util.Set<String> idHopLe = new java.util.HashSet<>();
            for (MasterFood f : foods) idHopLe.add(f.getId());

            for (com.google.gson.JsonElement el : arr) {
                com.google.gson.JsonObject obj = el.getAsJsonObject();
                if (obj.has("foodId") && idHopLe.contains(obj.get("foodId").getAsString())) {
                    ketQua.add(obj);
                } else {
                    System.out.println("[GeminiService] Bỏ qua foodId không hợp lệ: " + obj);
                }
            }
            return ketQua.toString();
        } catch (Exception e) {
            System.out.println("[GeminiService] Lỗi parse JSON, trả về mảng rỗng: " + e.getMessage());
            return "[]";
        }
    }
}
