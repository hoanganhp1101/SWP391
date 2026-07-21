package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.DietPlanDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.MasterFoodDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.DietPlan;
import com.example.diabetesmanage.model.DietPlanDetail;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.MasterFood;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.util.PatientPortalAuth;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet("/patient-diet")
public class PatientDietServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        DietPlanDAO dietPlanDAO = new DietPlanDAO();
        DietPlan todayPlan = dietPlanDAO.getTodayDietPlan(patientId);

        Patient patientInfo = patientDAO.getPatientById(patientId);
        request.setAttribute("patientInfo", patientInfo);
        request.setAttribute("todayPlan", todayPlan);

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object msg = session.getAttribute("dietFlash");
            Object err = session.getAttribute("dietError");
            if (msg != null) {
                request.setAttribute("dietFlash", msg);
                session.removeAttribute("dietFlash");
            }
            if (err != null) {
                request.setAttribute("dietError", err);
                session.removeAttribute("dietError");
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-diet.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");

        if (!"generate".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/patient-diet");
            return;
        }

        try {
            MasterFoodDAO foodDAO = new MasterFoodDAO();
            foodDAO.ensureDefaultFoods();

            DietPlanDAO dietPlanDAO = new DietPlanDAO();
            if (!dietPlanDAO.ensureDietTables()) {
                session.setAttribute("dietError",
                        "Không tạo được bảng thực đơn trong MySQL. Kiểm tra kết nối DB.");
                response.sendRedirect(request.getContextPath() + "/patient-diet");
                return;
            }

            List<MasterFood> foods = foodDAO.getAllFoods().stream()
                    .filter(MasterFood::isTrangThai)
                    .collect(Collectors.toList());

            if (foods.isEmpty()) {
                session.setAttribute("dietError",
                        "Chưa có danh mục thực phẩm trong hệ thống. Vui lòng chạy lại newdb.sql hoặc SetupDB.");
                response.sendRedirect(request.getContextPath() + "/patient-diet");
                return;
            }

            Set<String> validFoodIds = foods.stream()
                    .map(MasterFood::getId)
                    .collect(Collectors.toCollection(HashSet::new));

            PatientDAO patientDAO = new PatientDAO();
            Patient patient = patientDAO.getPatientById(patientId);
            HealthRecordDAO hrDAO = new HealthRecordDAO();
            HealthRecord record = hrDAO.getLatestComprehensiveRecord(patientId);

            GeminiService geminiService = new GeminiService();
            String jsonResponse = geminiService.generateDailyDietPlan(patient, record, foods);
            List<DietPlanDetail> details = parseDietDetails(jsonResponse);
            details = filterValidDetails(details, validFoodIds);

            boolean usedFallback = false;
            if (details.isEmpty()) {
                details = buildFallbackPlan(foods);
                usedFallback = true;
            }

            if (details.isEmpty()) {
                session.setAttribute("dietError", "Không tạo được thực đơn. Vui lòng thử lại sau.");
                response.sendRedirect(request.getContextPath() + "/patient-diet");
                return;
            }

            DietPlan plan = new DietPlan();
            plan.setId(UUID.randomUUID().toString());
            plan.setPatientId(patientId);
            plan.setDoctorId("AI_SYSTEM");
            plan.setGhiChu(usedFallback
                    ? "Thực đơn gợi ý mặc định (AI tạm thời không phản hồi). Bạn có thể tạo lại sau."
                    : "Thực đơn được tạo tự động bởi AI dựa trên chỉ số sức khỏe của bạn.");

            for (DietPlanDetail d : details) {
                d.setId(UUID.randomUUID().toString());
                d.setDietPlanId(plan.getId());
                if (d.getBuaAn() == null || d.getBuaAn().isBlank()) {
                    d.setBuaAn("Trưa");
                }
            }
            plan.setChiTietThucPham(details);

            deleteTodayAiPlans(patientId);

            boolean saved = dietPlanDAO.saveDietPlan(plan);
            if (saved) {
                session.setAttribute("dietFlash", usedFallback
                        ? "Đã tạo thực đơn dự phòng vì AI đang quá tải hoặc lỗi kết nối."
                        : "Đã tạo thực đơn AI thành công!");
            } else {
                session.setAttribute("dietError",
                        "Không lưu được thực đơn (kiểm tra bảng diet_plans / diet_plan_details trong MySQL).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("dietError", "Lỗi khi tạo thực đơn: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/patient-diet");
    }

    private void deleteTodayAiPlans(String patientId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM diet_plans WHERE patient_id = ? AND DATE(ngay_tao) = CURDATE() AND doctor_id = 'AI_SYSTEM'")) {
            ps.setString(1, patientId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[PatientDietServlet] deleteTodayAiPlans: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<DietPlanDetail> parseDietDetails(String jsonResponse) {
        List<DietPlanDetail> empty = new ArrayList<>();
        if (jsonResponse == null || jsonResponse.isBlank() || "[]".equals(jsonResponse.trim())) {
            return empty;
        }
        try {
            String cleaned = extractJsonArray(jsonResponse.trim());
            Gson gson = new Gson();
            Type listType = new TypeToken<List<DietPlanDetail>>() {}.getType();
            List<DietPlanDetail> details = gson.fromJson(cleaned, listType);
            return details != null ? details : empty;
        } catch (Exception e) {
            System.err.println("[PatientDietServlet] parseDietDetails failed: " + e.getMessage());
            e.printStackTrace();
            return empty;
        }
    }

    private String extractJsonArray(String raw) {
        String cleaned = raw;
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }

        // Một số model trả object bọc mảng
        try {
            JsonElement el = JsonParser.parseString(cleaned);
            if (el.isJsonObject()) {
                for (String key : new String[]{"meals", "items", "data", "plan"}) {
                    if (el.getAsJsonObject().has(key) && el.getAsJsonObject().get(key).isJsonArray()) {
                        return el.getAsJsonObject().getAsJsonArray(key).toString();
                    }
                }
            }
            if (el.isJsonArray()) {
                JsonArray arr = el.getAsJsonArray();
                return arr.toString();
            }
        } catch (Exception ignored) {
        }
        return cleaned;
    }

    private List<DietPlanDetail> filterValidDetails(List<DietPlanDetail> details, Set<String> validFoodIds) {
        List<DietPlanDetail> valid = new ArrayList<>();
        if (details == null) {
            return valid;
        }
        for (DietPlanDetail d : details) {
            if (d == null || d.getFoodId() == null) {
                continue;
            }
            String foodId = d.getFoodId().trim();
            if (!validFoodIds.contains(foodId)) {
                continue;
            }
            d.setFoodId(foodId);
            valid.add(d);
        }
        return valid;
    }

    /** Dự phòng khi Gemini lỗi / rate-limit: chọn món carbs thấp cho 3 bữa. */
    private List<DietPlanDetail> buildFallbackPlan(List<MasterFood> foods) {
        List<MasterFood> sorted = foods.stream()
                .sorted((a, b) -> Double.compare(
                        a.getChiSoGI() != null ? a.getChiSoGI() : 99,
                        b.getChiSoGI() != null ? b.getChiSoGI() : 99))
                .collect(Collectors.toList());

        String[] meals = {"Sáng", "Trưa", "Tối"};
        List<DietPlanDetail> plan = new ArrayList<>();
        for (int i = 0; i < meals.length && i < sorted.size(); i++) {
            MasterFood food = sorted.get(i % sorted.size());
            MasterFood side = sorted.get((i + 3) % sorted.size());

            DietPlanDetail main = new DietPlanDetail();
            main.setFoodId(food.getId());
            main.setBuaAn(meals[i]);
            main.setGhiChu("Món chính gợi ý (GI thấp)");
            plan.add(main);

            if (!side.getId().equals(food.getId())) {
                DietPlanDetail veg = new DietPlanDetail();
                veg.setFoodId(side.getId());
                veg.setBuaAn(meals[i]);
                veg.setGhiChu("Món kèm / rau");
                plan.add(veg);
            }
        }
        return plan;
    }
}
