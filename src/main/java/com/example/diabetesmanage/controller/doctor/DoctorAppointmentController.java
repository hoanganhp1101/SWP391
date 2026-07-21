package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.model.Appointment;
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

@WebServlet("/doctor/appointments")
public class DoctorAppointmentController extends HttpServlet {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String status = Appointment.normalizeStatusFilter(request.getParameter("status"));
        String keyword = request.getParameter("keyword");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");

        List<Appointment> appointments =
                appointmentDAO.findAll(scopeDoctorId, status, keyword, fromDate, toDate);

        DoctorLayoutHelper.prepare(request, user, "appointments");
        request.setAttribute("appointments", appointments);
        request.getRequestDispatcher("/WEB-INF/views/doctor/doctorappointmentmanagement.jsp")
                .forward(request, response);
    }
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

        String redirect = request.getContextPath() + "/doctor/appointments";
        String statusFilter = request.getParameter("filterStatus");
        if (statusFilter != null && !statusFilter.isBlank()) {
            redirect += "?status=" + statusFilter;
        }

        if (Appointment.STATUS_DA_KHAM.equals(newStatus)) {
            boolean updated = false;
            if (appointmentId != null && !appointmentId.isBlank()) {
                Appointment appt = appointmentDAO.findById(appointmentId, scopeDoctorId);
                if (appt != null && Appointment.STATUS_CHO_KHAM.equals(appt.getTrangThai())) {
                    updated = appointmentDAO.updateStatus(
                            appointmentId, Appointment.STATUS_DA_KHAM, scopeDoctorId);
                }
            }
            if (!updated) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Khong the danh dau da kham (chi ap dung cho lich cho kham)");
                return;
            }
            redirect += (redirect.contains("?") ? "&" : "?") + "updated=1";
        } else if (Appointment.STATUS_HUY.equals(newStatus)) {
            boolean updated = appointmentDAO.updateStatus(appointmentId, Appointment.STATUS_HUY, scopeDoctorId);
            if (!updated) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Khong the huy lich (chi ap dung cho lich cho kham)");
                return;
            }
            redirect += (redirect.contains("?") ? "&" : "?") + "updated=1";
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Trạng thái không hợp lệ");
            return;
        }

        response.sendRedirect(redirect);
    }
}
