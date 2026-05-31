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

        User doctor =
                (User) request.getSession()
                        .getAttribute("user");

        List<Patient> patients =
                patientDAO.getPatientsByDoctor(
                        doctor.getId()
                );

        request.setAttribute(
                "patients",
                patients
        );

        request.getRequestDispatcher(
                "/doctor/patient-list.jsp"
        ).forward(request, response);
    }
}
