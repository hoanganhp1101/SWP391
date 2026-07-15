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

@WebServlet("/doctor/medical-encounters")
public class MedicalEncounterListController extends HttpServlet {

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

        List<Appointment> appointments = appointmentDAO.findAll(scopeDoctorId, status, keyword);

        DoctorLayoutHelper.prepare(request, user, "appointments");
        request.setAttribute("appointments", appointments);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalencountermanagement.jsp")
                .forward(request, response);
    }
}
