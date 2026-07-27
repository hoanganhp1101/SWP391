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
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "UserManagementServlet", urlPatterns = {"/admin/users"})
public class UserManagementServlet extends HttpServlet {

    private final UserDAO userDAO;

    public UserManagementServlet() {
        this(new UserDAO());
    }

    UserManagementServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        int page = 1;
        int recordsPerPage = 8;

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        if (page < 1) {
            page = 1;
        }

        int offset = (page - 1) * recordsPerPage;

        int totalRecords = userDAO.getTotalUsersCount(role, status, keyword);
        int totalPages = (int) Math.ceil(totalRecords * 1.0 / recordsPerPage);

        List<User> list = userDAO.getFilteredUsers(role, status, keyword, offset, recordsPerPage);
        if (page > 1 && list.isEmpty() && totalRecords > 0) {
            page = totalPages;
            offset = (page - 1) * recordsPerPage;
            list = userDAO.getFilteredUsers(role, status, keyword, offset, recordsPerPage);
        }

        request.setAttribute("userList", list);

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("recordsPerPage", recordsPerPage);

        request.setAttribute("selectedRole", role);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("searchKeyword", keyword);
        consumeFlash(request);

        request.getRequestDispatcher("/WEB-INF/views/admin/user-management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            if (!hasRequiredUserFields(request, true)) {
                setFlash(request, "danger", "Vui lòng nhập đầy đủ họ tên, email, vai trò và mật khẩu.");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }
            User u = new User();
            u.setHoTen(request.getParameter("hoTen"));
            u.setEmail(request.getParameter("email"));
            u.setSoDienThoai(request.getParameter("soDienThoai"));
            u.setVaiTro(request.getParameter("vaiTro"));
            u.setMatKhauHash(request.getParameter("matKhau"));

            boolean success = userDAO.addUser(u);
            setFlash(request, success ? "success" : "danger",
                    success ? "Da tao nguoi dung moi." : "Khong the tao nguoi dung. Vui long kiem tra email hoac du lieu nhap.");
            response.sendRedirect(request.getContextPath() + "/admin/users");

        } else if ("update".equals(action)) {
            if (isBlank(request.getParameter("id")) || !hasRequiredUserFields(request, false)) {
                setFlash(request, "danger", "Dữ liệu cập nhật người dùng không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }
            User u = new User();
            u.setId(request.getParameter("id"));
            u.setHoTen(request.getParameter("hoTen"));
            u.setEmail(request.getParameter("email"));
            u.setSoDienThoai(request.getParameter("soDienThoai"));
            u.setVaiTro(request.getParameter("vaiTro"));

            boolean success = userDAO.updateUser(u);
            setFlash(request, success ? "success" : "danger",
                    success ? "Da cap nhat nguoi dung." : "Khong the cap nhat nguoi dung.");
            response.sendRedirect(request.getContextPath() + "/admin/users");

        } else if ("toggleStatus".equals(action)) {
            String id = request.getParameter("id");
            String statusStr = request.getParameter("status");

            if (id != null && isValidStatus(statusStr)) {
                HttpSession session = request.getSession(false);
                User currentAdmin = session == null ? null : (User) session.getAttribute("adminUser");
                int newStatus = Integer.parseInt(statusStr);

                if (currentAdmin != null && currentAdmin.getId().equals(id) && newStatus == 0) {
                    setFlash(request, "warning", "Khong the khoa tai khoan dang dang nhap.");
                } else {
                    boolean success = userDAO.updateUserStatus(id, newStatus);
                    setFlash(request, success ? "success" : "danger",
                            success ? "Da cap nhat trang thai nguoi dung." : "Khong the cap nhat trang thai nguoi dung.");
                }
            } else {
                setFlash(request, "danger", "Du lieu trang thai khong hop le.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/users");

        } else {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private boolean isValidStatus(String status) {
        return "0".equals(status) || "1".equals(status);
    }

    private boolean hasRequiredUserFields(HttpServletRequest request, boolean requirePassword) {
        return !isBlank(request.getParameter("hoTen"))
                && !isBlank(request.getParameter("email"))
                && isValidRole(request.getParameter("vaiTro"))
                && (!requirePassword || !isBlank(request.getParameter("matKhau")));
    }

    /** Admin chỉ được tạo/sửa role nhân sự; bệnh nhân tự đăng ký. */
    private boolean isValidRole(String role) {
        return "quan_tri_vien".equals(role) || "bac_si".equals(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    private void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        request.setAttribute("flashType", session.getAttribute("flashType"));
        request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
        session.removeAttribute("flashType");
        session.removeAttribute("flashMessage");
    }
}
