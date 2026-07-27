package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.AlertScanDAO;
import com.example.diabetesmanage.dao.DoctorAlertDAO;
import com.example.diabetesmanage.dao.ThresholdSettingsDAO;
import com.example.diabetesmanage.model.DoctorAlert;
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
import java.util.List;

@WebServlet(
        name = "DoctorAlertsController",
        urlPatterns = {"/DoctorAlertsController", "/doctor/alerts", "/DocterAlertsController"}
)
public class DoctorAlertsController extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requireDoctor(request, response);
        if (user == null) {
            return;
        }
        String doctorId = user.getId();

        // Quét bổ sung cảnh báo trước khi đọc danh sách; lỗi quét không chặn trang.
        try {
            ThresholdSettings thresholds = new ThresholdSettingsDAO().getForDoctor(doctorId);
            new AlertScanDAO().scanAndCreateAlerts(doctorId, thresholds);
        } catch (Exception e) {
            System.err.println("Không thể quét cảnh báo khi mở trang: " + e.getMessage());
        }

        String severity = getFilterValue(request, "severity");
        String status = getFilterValue(request, "status");
        String type = getFilterValue(request, "type");
        String keyword = getKeyword(request);
        int page = parsePage(request.getParameter("page"));

        DoctorAlertDAO dao = new DoctorAlertDAO();
        int totalAlerts = dao.countAlerts(severity, status, type, keyword, doctorId);
        int totalPages = totalAlerts == 0 ? 0 : (int) Math.ceil((double) totalAlerts / PAGE_SIZE);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        List<DoctorAlert> alerts = dao.getAlerts(severity, status, type, keyword, page, PAGE_SIZE, doctorId);

        int fromIndex = totalAlerts == 0 ? 0 : ((page - 1) * PAGE_SIZE) + 1;
        int toIndex = Math.min(page * PAGE_SIZE, totalAlerts);

        DoctorLayoutHelper.prepare(request, user, "alerts");
        request.setAttribute("listAlerts", alerts);
        request.setAttribute("totalAlerts", totalAlerts);
        request.setAttribute("severityFilter", severity);
        request.setAttribute("statusFilter", status);
        request.setAttribute("typeFilter", type);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("fromIndex", fromIndex);
        request.setAttribute("toIndex", toIndex);

        request.getRequestDispatcher("/WEB-INF/views/doctor/alert.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Doctor Alerts Controller";
    }

    private String getFilterValue(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return "all";
        }
        return value.trim();
    }

    private String getKeyword(HttpServletRequest request) {
        String value = request.getParameter("keyword");
        return value == null ? "" : value.trim();
    }

    private int parsePage(String rawPage) {
        if (rawPage == null || rawPage.isBlank()) {
            return 1;
        }
        try {
            return Math.max(Integer.parseInt(rawPage.trim()), 1);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
