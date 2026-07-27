package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.ThresholdSettingsDAO;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "DoctorThresholdSettingsServlet", urlPatterns = {"/doctor/threshold-settings"})
public class DoctorThresholdSettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }
        loadPage(request, doctor.getId(), null);
        DoctorLayoutHelper.prepare(request, doctor, "threshold-settings");
        request.getRequestDispatcher("/WEB-INF/views/doctor/threshold-settings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String doctorId = doctor.getId();

        if ("1".equals(request.getParameter("reset"))) {
            ThresholdSettingsDAO dao = new ThresholdSettingsDAO();
            if (dao.resetToDefaults(doctorId)) {
                response.sendRedirect(request.getContextPath() + "/doctor/threshold-settings?saved=1&reset=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/doctor/threshold-settings?error=1");
            }
            return;
        }

        ThresholdSettings settings;
        List<String> errors = new ArrayList<>();
        try {
            settings = parseSettings(request, doctorId);
            validate(settings, errors);
        } catch (Exception ex) {
            response.sendRedirect(request.getContextPath() + "/doctor/threshold-settings?error=1");
            return;
        }

        if (!errors.isEmpty()) {
            loadPage(request, doctorId, errors);
            request.setAttribute("form", settings);
            DoctorLayoutHelper.prepare(request, doctor, "threshold-settings");
            request.getRequestDispatcher("/WEB-INF/views/doctor/threshold-settings.jsp").forward(request, response);
            return;
        }

        if (new ThresholdSettingsDAO().save(settings)) {
            response.sendRedirect(request.getContextPath() + "/doctor/threshold-settings?saved=1");
        } else {
            response.sendRedirect(request.getContextPath() + "/doctor/threshold-settings?error=1");
        }
    }

    private void loadPage(HttpServletRequest request, String doctorId, List<String> errors) {
        ThresholdSettings settings = new ThresholdSettingsDAO().getForDoctor(doctorId);
        if (request.getAttribute("form") == null) {
            request.setAttribute("form", settings);
        }
        if (errors != null) {
            request.setAttribute("errors", errors);
        }
    }

    private ThresholdSettings parseSettings(HttpServletRequest request, String bacSiId) {
        ThresholdSettings s = new ThresholdSettings();
        s.setBacSiId(bacSiId);
        s.setGlucoseLow(parseInt(request.getParameter("glucoseLow"), 70));
        s.setGlucoseHigh(parseInt(request.getParameter("glucoseHigh"), 180));
        s.setGlucoseDanger(parseInt(request.getParameter("glucoseDanger"), 250));
        s.setHba1cTarget(parseDouble(request.getParameter("hba1cTarget"), 7.0));
        s.setHba1cPoor(parseDouble(request.getParameter("hba1cPoor"), 8.0));
        s.setDaysNoMeasure(parseInt(request.getParameter("daysNoMeasure"), 7));
        return s;
    }

    private void validate(ThresholdSettings s, List<String> errors) {
        if (s.getGlucoseLow() < 40 || s.getGlucoseLow() > 100) {
            errors.add("Ngưỡng hạ đường huyết nên trong khoảng 40–100 mg/dL.");
        }
        if (s.getGlucoseHigh() <= s.getGlucoseLow()) {
            errors.add("Ngưỡng cao phải lớn hơn ngưỡng hạ đường huyết.");
        }
        if (s.getGlucoseDanger() <= s.getGlucoseHigh()) {
            errors.add("Ngưỡng nguy hiểm phải lớn hơn ngưỡng cao.");
        }
        if (s.getGlucoseHigh() > 400 || s.getGlucoseDanger() > 600) {
            errors.add("Ngưỡng đường huyết quá cao, vui lòng kiểm tra lại.");
        }
        if (s.getHba1cTarget() < 4 || s.getHba1cTarget() > 10) {
            errors.add("HbA1c mục tiêu nên trong khoảng 4–10%.");
        }
        if (s.getHba1cPoor() <= s.getHba1cTarget()) {
            errors.add("Ngưỡng HbA1c kém phải lớn hơn mục tiêu.");
        }
        if (s.getDaysNoMeasure() < 1 || s.getDaysNoMeasure() > 90) {
            errors.add("Số ngày không đo nên trong khoảng 1–90.");
        }
    }

    private int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(raw.trim());
    }

    private double parseDouble(String raw, double defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(raw.trim());
    }
}
