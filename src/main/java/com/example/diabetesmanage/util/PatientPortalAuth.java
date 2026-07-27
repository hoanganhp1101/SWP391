package com.example.diabetesmanage.util;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Resolve logged-in patient id from session for patient-portal servlets.
 */
public final class PatientPortalAuth {

    private PatientPortalAuth() {
    }

    public static String requirePatientId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            Object value = session.getAttribute("user");
            if (value instanceof User) {
                user = (User) value;
            }
        }
        if (user == null || !"benh_nhan".equalsIgnoreCase(user.getVaiTro())) {
            // API (IoT / mobile): always return JSON — never redirect to HTML login
            if (isApiPath(request)) {
                writeJsonUnauthorized(response, "Vui lòng đăng nhập lại tài khoản bệnh nhân.");
                return null;
            }

            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return null;
        }
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getPatientIdByUserId(user.getId());
        if (patientId == null || patientId.isBlank()) {
            patientId = patientDAO.ensurePatientProfileForUser(user.getId());
        }
        if (patientId == null || patientId.isBlank()) {
            if (isApiPath(request)) {
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Không tìm thấy hồ sơ bệnh nhân.");
                return null;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Khong tim thay ho so benh nhan.");
            return null;
        }
        return patientId;
    }

    private static boolean isApiPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/api/");
    }

    private static void writeJsonUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String safe = message == null ? "Unauthorized" : message.replace("\"", "'");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + safe + "\"}");
    }
}
