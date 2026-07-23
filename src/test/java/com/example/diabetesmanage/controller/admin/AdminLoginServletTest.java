package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminLoginServlet chỉ còn legacy redirect về login thống nhất (/).
 */
class AdminLoginServletTest {

    @Test
    void doGetRedirectsLoggedInAdminToDashboard() throws Exception {
        AdminLoginServlet servlet = new AdminLoginServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("adminUser")).thenReturn(new User());
        when(request.getContextPath()).thenReturn("/app");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/admin-dashboard");
    }

    @Test
    void doGetRedirectsAnonymousUserToUnifiedLogin() throws Exception {
        AdminLoginServlet servlet = new AdminLoginServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/");
    }

    @Test
    void doPostAlwaysRedirectsToUnifiedLogin() throws Exception {
        AdminLoginServlet servlet = new AdminLoginServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/");
    }
}
