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
        "/admin-dashboard",
        "/patient-manager",
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

        if ("/admin/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("adminUser");
        boolean isLoggedInAdmin = sessionUser instanceof User
                && "quan_tri_vien".equals(((User) sessionUser).getVaiTro())
                && ((User) sessionUser).getKichHoat() == 1;

        if (isLoggedInAdmin) {
            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/login?required=1");
    }
}
