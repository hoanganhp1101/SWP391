package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.DashboardDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/dashboard", "/admin-dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private DashboardDAO dashboardDAO;
    private PatientDAO patientDAO;

    @Override
    public void init() throws ServletException {
        // Khởi tạo các DAO để truy vấn Database
        dashboardDAO = new DashboardDAO();
        patientDAO = new PatientDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 1. Lấy 3 chỉ số thống kê trên cùng từ Database
            int totalPatients = dashboardDAO.getTotalPatients();
            int activeStaff = dashboardDAO.getActiveStaffCount();
            int criticalAlerts = dashboardDAO.getCriticalAlertsCount();

            // 2. Lấy danh sách bệnh nhân (Có thể hiển thị 5-10 người mới nhất)
            // Tạm thời dùng hàm getAllPatients(), nếu bạn có hàm getRecentPatients(5) thì càng tốt
            List<Patient> patientList = patientDAO.getAllPatients();

            // 3. Gắn dữ liệu vào Request để đẩy sang JSP
            request.setAttribute("totalPatients", totalPatients);
            request.setAttribute("activeStaff", activeStaff);
            request.setAttribute("criticalAlerts", criticalAlerts);
            request.setAttribute("patientList", patientList);

            // Chuyển hướng tới giao diện JSP
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, trả về trang dashboard trống hoặc trang báo lỗi
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
        }
    }
}