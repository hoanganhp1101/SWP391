package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.AppointmentStatusService;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/appointments/status")
public class UpdateAppointmentStatusServlet extends HttpServlet {

    private final AppointmentStatusService statusService = new AppointmentStatusService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requireDoctor(request, response);
        if (user == null) {
            return;
        }

        String appointmentId = request.getParameter("id");
        String newStatus = Appointment.normalizeStatusFilter(request.getParameter("status"));
        String scopeDoctorId = AuthContext.scopeDoctorId(user);

        String redirect = request.getContextPath() + "/doctor/medical-encounters";
        String statusFilter = request.getParameter("filterStatus");
        if (statusFilter != null && !statusFilter.isBlank()) {
            redirect += "?status=" + statusFilter;
        }

        if (Appointment.STATUS_DA_KHAM.equals(newStatus)) {
            boolean updated = statusService.markCompleted(appointmentId, scopeDoctorId);
            if (!updated) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Khong the danh dau da kham (chi ap dung cho lich cho kham)");
                return;
            }
            redirect += (redirect.contains("?") ? "&" : "?") + "updated=1";
        } else if (Appointment.STATUS_HUY.equals(newStatus)) {
            boolean updated = statusService.cancel(appointmentId, scopeDoctorId);
            if (!updated) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Khong the huy lich (chi ap dung cho lich cho kham)");
                return;
            }
            redirect += (redirect.contains("?") ? "&" : "?") + "updated=1";
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Trang thai khong hop le");
            return;
        }

        response.sendRedirect(redirect);
    }
}
