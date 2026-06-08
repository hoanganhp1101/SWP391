package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/patient-list")
public class PatientListController extends HttpServlet {

    private final PatientDAO patientDAO =
            new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
//        User doctor =
//                (User) request.getSession()
//                        .getAttribute("user");

//        List<Patient> patients =
//                patientDAO.getPatientsByDoctor(
//                        doctor.getId()
//                );
        String risk = request.getParameter("risk");

        List<Patient> patients;

        if (risk == null || risk.isBlank()) {

            patients = patientDAO.getPatients();

        } else {

            patients = patientDAO.getPatientsByRiskLevel(risk);
        }

        request.setAttribute("patients", patients);

        request.getRequestDispatcher(
                "/WEB-INF/views/doctor/patientmanagement.jsp"
        ).forward(request, response);
    }
}
