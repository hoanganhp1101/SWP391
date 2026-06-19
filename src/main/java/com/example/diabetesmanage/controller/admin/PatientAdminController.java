package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
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
                Patient patient = patientDAO.getPatientByIdAdmin(id);
                request.setAttribute("patient", patient);
                request.getRequestDispatcher("/WEB-INF/views/admin/patient-detail.jsp").forward(request, response);
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/patient-manager");
            }
        } else {
            List<Patient> patientList = patientDAO.getAllPatients();
            request.setAttribute("patientList", patientList);
            request.getRequestDispatcher("/WEB-INF/views/admin/patient-manager.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        PatientDAO patientDAO = new PatientDAO();

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/patient-manager");
            return;
        }

        try {
            if (action.equals("delete")) {
                String id = request.getParameter("id");
                patientDAO.deletePatient(id);
            }
            else if (action.equals("add") || action.equals("update")) {
                Patient p = new Patient();
                p.setTenBenhNhan(request.getParameter("hoTen"));
                p.setEmail(request.getParameter("email"));
                p.setSoDienThoai(request.getParameter("soDienThoai"));
                p.setLoaiTieuDuong(request.getParameter("loaiTieuDuong"));

                String ngaySinhStr = request.getParameter("ngaySinh");
                if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                    p.setNgaySinh(Date.valueOf(ngaySinhStr));
                }

                if (action.equals("add")) {
                    patientDAO.addPatient(p);
                } else {
                    p.setId(request.getParameter("id"));
                    patientDAO.updatePatient(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/patient-manager");
    }
}