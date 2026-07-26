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
            // Fix for mobile app: allow mock access without login since mobile app has no login screen yet
            if (request.getRequestURI().contains("/api/mobile")) {
                try (java.sql.Connection conn = com.example.diabetesmanage.context.DBContext.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id FROM patients LIMIT 1");
                     java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("id");
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized\"}");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Khong tim thay ho so benh nhan.");
            return null;
        }
        return patientId;
    }
}
