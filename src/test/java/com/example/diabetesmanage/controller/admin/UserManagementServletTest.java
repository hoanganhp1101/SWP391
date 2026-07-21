package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.UserDAO;
import com.example.diabetesmanage.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserManagementServletTest {
    @Test
    void invalidCreateIsRejectedWithoutDaoWrite() throws Exception {
        UserDAO dao = mock(UserDAO.class);
        UserManagementServlet servlet = new UserManagementServlet(dao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("hoTen")).thenReturn("Admin");
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("vaiTro")).thenReturn("quan_tri_vien");

        servlet.doPost(request, response);

        verify(dao, never()).addUser(any());
        verify(session).setAttribute(eq("flashMessage"), anyString());
        verify(response).sendRedirect("/admin/users");
    }

    @Test
    void currentAdministratorCannotLockOwnAccount() throws Exception {
        UserDAO dao = mock(UserDAO.class);
        UserManagementServlet servlet = new UserManagementServlet(dao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User admin = new User();
        admin.setId("admin-1");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("adminUser")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("toggleStatus");
        when(request.getParameter("id")).thenReturn("admin-1");
        when(request.getParameter("status")).thenReturn("0");

        servlet.doPost(request, response);

        verify(dao, never()).updateUserStatus(anyString(), anyInt());
        verify(session).setAttribute("flashType", "warning");
    }

    @Test
    void validCreatePassesUserToDao() throws Exception {
        UserDAO dao = mock(UserDAO.class);
        UserManagementServlet servlet = new UserManagementServlet(dao);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("hoTen")).thenReturn("Bác sĩ A");
        when(request.getParameter("email")).thenReturn("doctor@example.com");
        when(request.getParameter("vaiTro")).thenReturn("bac_si");
        when(request.getParameter("matKhau")).thenReturn("secret");
        when(dao.addUser(any())).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).addUser(argThat(user -> "Bác sĩ A".equals(user.getHoTen())
                && "bac_si".equals(user.getVaiTro()) && "secret".equals(user.getMatKhauHash())));
    }
}
