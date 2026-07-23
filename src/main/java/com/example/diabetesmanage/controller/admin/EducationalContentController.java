package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.EducationalContentDAO;
import com.example.diabetesmanage.model.EducationalContent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "EducationalContentController", urlPatterns = {"/admin/educational-content"})
public class EducationalContentController extends HttpServlet {

    private EducationalContentDAO contentDAO;

    public EducationalContentController() {
    }

    EducationalContentController(EducationalContentDAO contentDAO) {
        this.contentDAO = contentDAO;
    }

    @Override
    public void init() throws ServletException {
        if (contentDAO == null) {
            contentDAO = new EducationalContentDAO();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String category = request.getParameter("category");
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        request.setAttribute("contentList", contentDAO.getAllContents(category, status, keyword));
        request.setAttribute("selectedCategory", category);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("totalContents", contentDAO.countAll());
        request.setAttribute("activeContents", contentDAO.countActive());
        request.setAttribute("nutritionContents", contentDAO.countByCategory("dinh_duong"));
        request.setAttribute("medicationContents", contentDAO.countByCategory("thuoc_insulin"));
        consumeFlash(request);

        request.getRequestDispatcher("/WEB-INF/views/admin/educational-content.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            String id = request.getParameter("id");
            boolean success = !isBlank(id) && contentDAO.deleteContent(id);
            setFlash(request, success ? "success" : "danger",
                    success ? "Đã xóa nội dung giáo dục." : "Không thể xóa nội dung giáo dục.");
            response.sendRedirect(request.getContextPath() + "/admin/educational-content");
            return;
        }

        if (!"add".equals(action) && !"update".equals(action)) {
            setFlash(request, "danger", "Thao tác không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin/educational-content");
            return;
        }

        EducationalContent content = buildContent(request);
        if (!isValid(content) || ("update".equals(action) && isBlank(request.getParameter("id")))) {
            setFlash(request, "danger", "Vui lòng nhập đầy đủ tiêu đề, chủ đề và nội dung hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin/educational-content");
            return;
        }
        boolean success;
        if ("update".equals(action)) {
            content.setId(request.getParameter("id"));
            success = contentDAO.updateContent(content);
            setFlash(request, success ? "success" : "danger",
                    success ? "Đã cập nhật nội dung giáo dục." : "Không thể cập nhật nội dung giáo dục.");
        } else {
            success = contentDAO.addContent(content);
            setFlash(request, success ? "success" : "danger",
                    success ? "Đã thêm nội dung giáo dục." : "Không thể thêm nội dung giáo dục.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/educational-content");
    }

    private EducationalContent buildContent(HttpServletRequest request) {
        EducationalContent content = new EducationalContent();
        content.setTitle(request.getParameter("title"));
        content.setCategory(request.getParameter("category"));
        content.setSummary(request.getParameter("summary"));
        content.setContent(request.getParameter("content"));
        content.setTargetAudience(request.getParameter("targetAudience"));
        content.setDisplayOrder(Math.max(0, parseInt(request.getParameter("displayOrder"))));
        content.setActive(request.getParameter("active") != null);
        return content;
    }

    private int parseInt(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isValid(EducationalContent content) {
        return !isBlank(content.getTitle())
                && !isBlank(content.getContent())
                && isValidCategory(content.getCategory())
                && isValidAudience(content.getTargetAudience());
    }

    private boolean isValidCategory(String category) {
        return "dinh_duong".equals(category) || "thuoc_insulin".equals(category)
                || "van_dong".equals(category) || "tu_cham_soc".equals(category);
    }

    private boolean isValidAudience(String audience) {
        return "benh_nhan".equals(audience) || "nguoi_cham_soc".equals(audience)
                || "tat_ca".equals(audience);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("eduFlashType", type);
        session.setAttribute("eduFlashMessage", message);
    }

    private void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        request.setAttribute("flashType", session.getAttribute("eduFlashType"));
        request.setAttribute("flashMessage", session.getAttribute("eduFlashMessage"));
        session.removeAttribute("eduFlashType");
        session.removeAttribute("eduFlashMessage");
    }
}
