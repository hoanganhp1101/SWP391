package com.example.diabetesmanage.controller.admin;

import java.io.IOException;
import java.util.List;

import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "UserManagementServlet", urlPatterns = {"/admin/users"})
public class UserManagementServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        int page = 1;
        int recordsPerPage = 10;

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * recordsPerPage;

        int totalRecords = userDAO.getTotalUsersCount(role, status, keyword);
        int totalPages = (int) Math.ceil(totalRecords * 1.0 / recordsPerPage);

        List<User> list = userDAO.getFilteredUsers(role, status, keyword, offset, recordsPerPage);

        request.setAttribute("userList", list);

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("recordsPerPage", recordsPerPage);

        request.setAttribute("selectedRole", role);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("searchKeyword", keyword);

        request.getRequestDispatcher("/WEB-INF/views/admin/user-management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}