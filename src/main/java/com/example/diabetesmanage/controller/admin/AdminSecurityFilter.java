package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.model.User;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {
        "/dashboard",
        "/patient-manager",
        "/admin/users",
        "/admin-dashboard",
        "/admin/prescribe",
        "/admin/medications",
        "/admin/foods",
        "/admin/assign",
        "/RecordController",
        "/ai-report",
        "/api/patient/health-records",
        "/admin/*"
})
public class AdminSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getServletPath();

        // Legacy admin login path redirects via AdminLoginServlet; still allow through filter.
        if ("/admin/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedInAdmin = false;
        if (session != null) {
            Object admin = session.getAttribute("adminUser");
            Object user = session.getAttribute("user");
            if (admin instanceof User) {
                User adminUser = (User) admin;
                if ("quan_tri_vien".equalsIgnoreCase(adminUser.getVaiTro())
                        && adminUser.getKichHoat() == 1) {
                    isLoggedInAdmin = true;
                }
            } else if (user instanceof User) {
                User u = (User) user;
                if ("quan_tri_vien".equalsIgnoreCase(u.getVaiTro())
                        && u.getKichHoat() == 1) {
                    isLoggedInAdmin = true;
                    session.setAttribute("adminUser", u);
                    session.setAttribute("loginUser", u);
                }
            }
        }

        if (isLoggedInAdmin) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/");
        }
    }
}
