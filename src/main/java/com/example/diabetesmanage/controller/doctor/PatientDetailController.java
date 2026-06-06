package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;

import com.example.diabetesmanage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/doctor/patient-detail")
public class PatientDetailController extends HttpServlet {

    private PatientDAO patientDAO;

    @Override
    public void init() {
        patientDAO = new PatientDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("id");

        if (patientId == null || patientId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }

//        User doctor =
//                (User) request.getSession()
//                        .getAttribute("user");

        Patient patient =
                patientDAO.getPatientByIdAndDoctor(
                        patientId);

        if (patient == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Patient not found");
            return;
        }

        request.setAttribute("patient", patient);

        request.getRequestDispatcher(
                "/WEB-INF/views/doctor/patientdetail.jsp"
        ).forward(request, response);
    }
}