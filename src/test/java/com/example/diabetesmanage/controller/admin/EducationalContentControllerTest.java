package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.EducationalContentDAO;
import com.example.diabetesmanage.model.EducationalContent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EducationalContentControllerTest {
    @Test
    void validAddBuildsNormalizedContent() throws Exception {
        EducationalContentDAO dao = mock(EducationalContentDAO.class);
        EducationalContentController controller = new EducationalContentController(dao);
        HttpServletRequest request = requestWithSession();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("action")).thenReturn("add");
        when(request.getParameter("title")).thenReturn("Ăn uống lành mạnh");
        when(request.getParameter("category")).thenReturn("dinh_duong");
        when(request.getParameter("content")).thenReturn("Nội dung");
        when(request.getParameter("targetAudience")).thenReturn("benh_nhan");
        when(request.getParameter("displayOrder")).thenReturn("-5");
        when(request.getParameter("active")).thenReturn("on");
        when(dao.addContent(any())).thenReturn(true);

        controller.doPost(request, response);

        ArgumentCaptor<EducationalContent> captor = ArgumentCaptor.forClass(EducationalContent.class);
        verify(dao).addContent(captor.capture());
        assertEquals(0, captor.getValue().getDisplayOrder());
        assertTrue(captor.getValue().isActive());
        verify(response).sendRedirect("/app/admin/educational-content");
    }

    @Test
    void invalidOrUnknownActionNeverWritesToDatabase() throws Exception {
        EducationalContentDAO dao = mock(EducationalContentDAO.class);
        EducationalContentController controller = new EducationalContentController(dao);
        HttpServletRequest request = requestWithSession();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("action")).thenReturn("publish-everything");
        when(request.getContextPath()).thenReturn("");

        controller.doPost(request, response);

        verify(dao, never()).addContent(any());
        verify(dao, never()).updateContent(any());
        verify(dao, never()).deleteContent(any());
        verify(response).sendRedirect("/admin/educational-content");
    }

    @Test
    void updateRequiresAnIdAndValidFields() throws Exception {
        EducationalContentDAO dao = mock(EducationalContentDAO.class);
        EducationalContentController controller = new EducationalContentController(dao);
        HttpServletRequest request = requestWithSession();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("title")).thenReturn("Title");
        when(request.getParameter("category")).thenReturn("dinh_duong");
        when(request.getParameter("content")).thenReturn("Body");
        when(request.getParameter("targetAudience")).thenReturn("benh_nhan");
        when(request.getParameter("id")).thenReturn(" ");

        controller.doPost(request, response);

        verify(dao, never()).updateContent(any());
    }

    private HttpServletRequest requestWithSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        return request;
    }
}
