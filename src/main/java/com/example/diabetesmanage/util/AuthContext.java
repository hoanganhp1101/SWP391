package com.example.diabetesmanage.util;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class AuthContext {

    public static final String SESSION_USER = "user";

    private AuthContext() {
    }

    public static User getUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object value = request.getSession(false).getAttribute(SESSION_USER);
        return value instanceof User ? (User) value : null;
    }

    public static boolean isDoctor(User user) {
        return user != null && "bac_si".equalsIgnoreCase(user.getVaiTro());
    }

    public static boolean isAdmin(User user) {
        return user != null && "quan_tri_vien".equalsIgnoreCase(user.getVaiTro());
    }

    /**
     * {@code null} = admin xem toàn bộ; non-null = UUID bác sĩ từ session.
     */
    public static String scopeDoctorId(User user) {
        if (isAdmin(user)) {
            return null;
        }
        if (isDoctor(user) && user.getId() != null) {
            return user.getId().toString();
        }
        return null;
    }

    public static User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return null;
        }
        return user;
    }

    public static User requireDoctor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return null;
        }
        if (!isDoctor(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chi bac si moi duoc phep truy cap");
            return null;
        }
        return user;
    }

    public static User requirePatientDataAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return null;
        }
        if (!isDoctor(user) && !isAdmin(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Khong co quyen truy cap du lieu benh nhan");
            return null;
        }
        return user;
    }

    public static boolean ensurePatientAccess(
            User user,
            PatientDAO patientDAO,
            String patientId,
            HttpServletResponse response
    ) throws IOException {
        if (patientId == null || patientId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu ma benh nhan");
            return false;
        }
        if (!patientDAO.exists(patientId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Khong tim thay benh nhan");
            return false;
        }
        if (isDoctor(user) && user.getId() != null
                && !patientDAO.isAssignedToDoctor(patientId, user.getId().toString())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ban khong co quyen truy cap benh nhan nay");
            return false;
        }
        return true;
    }

    public static boolean ensureEncounterAccess(
            User user,
            PatientDAO patientDAO,
            com.example.diabetesmanage.dao.MedicalEncounterDAO encounterDAO,
            String encounterId,
            HttpServletResponse response
    ) throws IOException {
        if (encounterId == null || encounterId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu ma lan kham");
            return false;
        }
        if (!encounterDAO.encounterExists(encounterId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Khong tim thay lan kham");
            return false;
        }
        String patientId = encounterDAO.getPatientIdByEncounterId(encounterId);
        return ensurePatientAccess(user, patientDAO, patientId, response);
    }
}
