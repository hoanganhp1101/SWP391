package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminLoginServletTest {
    @Test
    void missingCredentialsAreRejectedBeforeDaoCall() throws Exception {
        UserDAO dao = mock(UserDAO.class);
        AdminLoginServlet servlet = new AdminLoginServlet(dao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getParameter("email")).thenReturn(" ");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verifyNoInteractions(dao);
        verify(dispatcher).forward(request, response);
        verify(request).setAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void successfulLoginRotatesSessionAndNeverStoresPasswordCookie() throws Exception {
        UserDAO dao = mock(UserDAO.class);
        AdminLoginServlet servlet = new AdminLoginServlet(dao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        User admin = new User();
        admin.setId("admin-1");
        when(request.getParameter("email")).thenReturn(" admin@example.com ");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getParameter("rememberMe")).thenReturn("on");
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);
        when(request.getContextPath()).thenReturn("/app");
        when(dao.authenticateAdmin("admin@example.com", "secret")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(oldSession).invalidate();
        verify(newSession).setAttribute("adminUser", admin);
        verify(response).sendRedirect("/app/dashboard");
        ArgumentCaptor<Cookie> cookies = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookies.capture());
        Cookie passwordCookie = cookies.getAllValues().stream()
                .filter(cookie -> "adminPass".equals(cookie.getName())).findFirst().orElseThrow();
        assertEquals("", passwordCookie.getValue());
        assertEquals(0, passwordCookie.getMaxAge());
        assertTrue(passwordCookie.isHttpOnly());
    }
}
