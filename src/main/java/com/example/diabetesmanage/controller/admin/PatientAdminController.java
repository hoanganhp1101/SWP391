package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "PatientController", urlPatterns = {"/patient-manager"})
public class PatientAdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        PatientDAO patientDAO = new PatientDAO();

        if (action != null && action.equals("view")) {
            try {
                String id = request.getParameter("id");

                Patient patient = patientDAO.getPatientByIdAdmin("id");

                request.setAttribute("patient", patient);

                request.getRequestDispatcher("/WEB-INF/views/admin/patient-detail.jsp").forward(request, response);
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/patient-manager");
            }
        }
        else {
            List<Patient> patientList = patientDAO.getAllPatients();

            request.setAttribute("patientList", patientList);

            request.getRequestDispatcher("/WEB-INF/views/admin/patient-manager.jsp").forward(request, response);
        }
    }
}