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
