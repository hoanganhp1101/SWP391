package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.PatientAssignmentDAO;
// Giả định bạn đã có PatientDAO và UserDAO
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PatientAssignmentController", urlPatterns = {"/admin/patient-assignments"})
public class PatientAssignmentController extends HttpServlet {

    private PatientAssignmentDAO assignmentDAO;
    private PatientDAO patientDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        assignmentDAO = new PatientAssignmentDAO();
        patientDAO = new PatientDAO(); // DAO lấy danh sách bệnh nhân
        userDAO = new UserDAO();       // DAO lấy danh sách user
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy danh sách bệnh nhân
        request.setAttribute("patientList", patientDAO.getAllPatients());

        // Lấy danh sách các user có vai trò là "Bác sĩ"
        List<User> doctorList = userDAO.getUsersByRole("bac_si");
        request.setAttribute("doctorList", doctorList);

        // Lấy danh sách các phân công hiện tại (Map <ID Bệnh nhân, Tên Bác sĩ>)
        Map<String, String> activeAssignments = assignmentDAO.getActiveAssignments();
        request.setAttribute("activeAssignments", activeAssignments);

        request.getRequestDispatcher("/WEB-INF/views/admin/patient-assignment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String patientId = request.getParameter("patientId");
        String doctorId = request.getParameter("doctorId");

        if (patientId != null && doctorId != null && !doctorId.isEmpty()) {
            assignmentDAO.assignDoctor(patientId, doctorId);
        }

        // Sau khi phân công xong, quay lại trang danh sách
        response.sendRedirect(request.getContextPath() + "/admin/patient-assignments");
    }
}