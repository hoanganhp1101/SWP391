package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.AIRecommendationScanDAO;
import com.example.diabetesmanage.dao.AIRecommendationScanDAO.ScanResult;
import com.example.diabetesmanage.dao.DoctorAIRecommendationDAO;
import com.example.diabetesmanage.dao.ThresholdSettingsDAO;
import com.example.diabetesmanage.model.AIAnalysis;
import com.example.diabetesmanage.model.ThresholdSettings;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(
        name = "DoctorAIRecommendationServlet",
        urlPatterns = {"/doctor/ai-recommendations"}
)
public class DoctorAIRecommendationServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }
        String doctorId = doctor.getId();
        DoctorLayoutHelper.prepare(request, doctor, "ai-recommendations");

        String id = request.getParameter("id");
        DoctorAIRecommendationDAO dao = new DoctorAIRecommendationDAO();
        dao.ensureStatusColumns();

        // Không tự gọi Gemini khi mở trang (tránh treo request).
        // Bác sĩ bấm "Đồng bộ Gemini" (POST sync=1) mới chạy scan.
        boolean viewingDetail = id != null && !id.isBlank();

        if (viewingDetail) {
            AIAnalysis detail = dao.findById(id, doctorId);
            if (detail == null) {
                response.sendRedirect(
                        request.getContextPath() + "/doctor/ai-recommendations?error=1");
                return;
            }
            request.setAttribute("detail", detail);
            request.getRequestDispatcher(
                    "/WEB-INF/views/doctor/ai-recommendation-detail.jsp")
                    .forward(request, response);
            return;
        }

        String level = blankToAll(request.getParameter("level"));
        String status = blankToAll(request.getParameter("status"));
        String keyword = request.getParameter("keyword") == null
                ? "" : request.getParameter("keyword").trim();
        int page = parsePage(request.getParameter("page"));

        int total = dao.count(doctorId, level, status, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
        List<AIAnalysis> recommendations =
                dao.list(doctorId, level, status, keyword, page, PAGE_SIZE);

        request.setAttribute("list", recommendations);
        request.setAttribute("total", total);
        request.setAttribute("levelFilter", level);
        request.setAttribute("statusFilter", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("fromIndex", total == 0 ? 0 : (page - 1) * PAGE_SIZE + 1);
        request.setAttribute("toIndex", Math.min(page * PAGE_SIZE, total));
        request.getRequestDispatcher("/WEB-INF/views/doctor/ai-recommendations.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }
        String doctorId = doctor.getId();

        if ("1".equals(request.getParameter("sync"))) {
            ThresholdSettings thresholds = new ThresholdSettingsDAO().getForDoctor(doctorId);
            ScanResult scan = new AIRecommendationScanDAO().scan(doctorId, thresholds, true);
            redirectAfterScan(request, response, scan);
            return;
        }

        String id = request.getParameter("id");
        if (id == null || id.isBlank()) {
            response.sendRedirect(request.getContextPath()
                    + "/doctor/ai-recommendations?error=1&errmsg="
                    + encode("Thiếu mã khuyến nghị."));
            return;
        }

        String status = request.getParameter("status");
        String updateError = new DoctorAIRecommendationDAO().updateStatus(
                id.trim(),
                doctorId,
                status,
                request.getParameter("ghiChu"),
                doctorId
        );
        if (updateError == null) {
            response.sendRedirect(request.getContextPath()
                    + "/doctor/ai-recommendations?id=" + encode(id.trim()) + "&saved=1");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/doctor/ai-recommendations?id=" + encode(id.trim())
                + "&error=1&errmsg=" + encode(shorten(updateError, 400)));
    }

    private void redirectAfterScan(
            HttpServletRequest request, HttpServletResponse response, ScanResult scan)
            throws IOException {
        String base = request.getContextPath() + "/doctor/ai-recommendations";
        if (scan.isError() && scan.getCreated() == 0 && scan.getRefreshed() == 0) {
            response.sendRedirect(base
                    + "?error=1&failed=" + scan.getInsertFailed()
                    + "&scanned=" + scan.getPatientsScanned()
                    + "&errmsg=" + encode(shorten(scan.getLastError(), 300)));
            return;
        }

        String geminiError = shorten(scan.getLastGeminiError(), 400);
        response.sendRedirect(base
                + "?synced=1"
                + "&created=" + scan.getCreated()
                + "&refreshed=" + scan.getRefreshed()
                + "&gemini=" + scan.getGeminiUsed()
                + "&geminiOn=" + (scan.isGeminiEnabled() ? "1" : "0")
                + "&skipped=" + scan.getSkipped()
                + "&scanned=" + scan.getPatientsScanned()
                + "&norisk=" + scan.getPatientsNoRisk()
                + (geminiError.isBlank() ? "" : "&geminiErr=" + encode(geminiError)));
    }

    private String blankToAll(String value) {
        return value == null || value.isBlank() ? "all" : value.trim();
    }

    private int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            return Math.max(Integer.parseInt(value.trim()), 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
