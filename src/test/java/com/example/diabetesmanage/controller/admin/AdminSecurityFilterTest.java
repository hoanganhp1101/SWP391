package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AdminSecurityFilterTest {
    private final AdminSecurityFilter filter = new AdminSecurityFilter();

    @Test
    void loginPageIsAlwaysPublic() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getServletPath()).thenReturn("/admin/login");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void activeAdministratorCanContinue() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        User admin = new User();
        admin.setVaiTro("quan_tri_vien");
        admin.setKichHoat(1);
        when(request.getServletPath()).thenReturn("/admin/users");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("adminUser")).thenReturn(admin);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void nonAdminSessionIsRedirectedToLogin() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        User doctor = new User();
        doctor.setVaiTro("bac_si");
        doctor.setKichHoat(1);
        when(request.getServletPath()).thenReturn("/admin/users");
        when(request.getContextPath()).thenReturn("/diabcare");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("adminUser")).thenReturn(doctor);

        filter.doFilter(request, response, chain);

        // Login đã hợp nhất tại "/" — filter đưa phiên không phải admin về trang chủ
        verify(response).sendRedirect("/diabcare/");
        verifyNoInteractions(chain);
    }
}
