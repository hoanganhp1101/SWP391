package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/patient-list")
public class PatientListController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String keyword = request.getParameter("keyword");
        String glucose = request.getParameter("glucose");
        String hba1c = request.getParameter("hba1c");
        String bmi = request.getParameter("bmi");
        String action = request.getParameter("action");

        List<Patient> patients =
                patientDAO.searchPatients(keyword, glucose, hba1c, bmi, action, scopeDoctorId);

        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patients", patients);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientmanagement.jsp")
                .forward(request, response);
    }
}
