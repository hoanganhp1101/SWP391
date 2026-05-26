package com.example.diabetesmanage.controller.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin-dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Mock System Data cho giao diện Admin
        request.setAttribute("totalUsers", 1250);
        request.setAttribute("aiPredictions", 342);
        request.setAttribute("pendingModeration", 15);
        request.setAttribute("systemErrors", 2);

        // Mock System Logs: [Thời gian, Mức độ, Sự kiện, Người thực hiện, IP]
        List<String[]> logs = new ArrayList<>();
        logs.add(new String[]{"13:45 23/05", "INFO", "Cập nhật ngưỡng AI: Glucose > 130", "admin_huy", "192.168.1.15"});
        logs.add(new String[]{"13:30 23/05", "WARNING", "Mô hình AI phản hồi chậm (>2s)", "SYSTEM", "Localhost"});
        logs.add(new String[]{"12:15 23/05", "ERROR", "Lỗi đồng bộ sao lưu cơ sở dữ liệu", "SYSTEM", "Server-02"});
        logs.add(new String[]{"11:00 23/05", "INFO", "Thêm mới 5 danh mục thuốc", "admin_tu", "192.168.1.22"});
        logs.add(new String[]{"10:20 23/05", "INFO", "Phân quyền: Chuyển User04 thành Doctor", "admin_huy", "192.168.1.15"});

        request.setAttribute("systemLogs", logs);

        // Chuyển hướng tới giao diện JSP
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}