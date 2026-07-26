package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.service.GeminiService;
import com.example.diabetesmanage.util.PatientPortalAuth;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatientPrescriptionServletTest {

    private PatientPrescriptionServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MockedStatic<PatientPortalAuth> mockedAuth;

    @BeforeEach
    void setUp() {
        servlet = new PatientPrescriptionServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        mockedAuth = mockStatic(PatientPortalAuth.class);
    }

    @AfterEach
    void tearDown() {
        mockedAuth.close();
    }

    @Test
    void testDoGet_DefaultChecklistView() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions");
        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-prescriptions.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<PrescriptionDAO> presDAOMock = mockConstruction(PrescriptionDAO.class, (mock, context) -> {
                when(mock.getLatestPrescription(patientId)).thenReturn(new Prescription());
             });
             MockedConstruction<MedicationLogDAO> logDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getChecklistByDate(eq(patientId), any())).thenReturn(new ArrayList<>());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("viewMode"), eq("checklist"));
            verify(request).setAttribute(eq("todayChecklist"), anyList());
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_AiReminder() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions/ai-reminder");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                Patient patient = new Patient();
                patient.setHoTen("Test Patient");
                when(mock.getPatientById(patientId)).thenReturn(patient);
             });
             MockedConstruction<MedicationLogDAO> logDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getChecklistByDate(eq(patientId), any())).thenReturn(new ArrayList<>());
             });
             MockedConstruction<GeminiService> geminiMock = mockConstruction(GeminiService.class, (mock, context) -> {
                when(mock.generateMedicationReminder(anyString(), anyList())).thenReturn("Hãy nhớ uống thuốc đúng giờ!");
             })) {

            servlet.doGet(request, response);
            writer.flush();

            String responseContent = stringWriter.toString();
            assertTrue(responseContent.contains("Hãy nhớ uống thuốc đúng giờ!"));
        }
    }

    @Test
    void testDoPost_ToggleMedication() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions/toggle");
        when(request.getParameter("medicationId")).thenReturn("med-1");
        when(request.getParameter("date")).thenReturn("2026-07-26");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        try (MockedConstruction<MedicationLogDAO> logDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.toggleMedicationStatus(eq(patientId), eq("med-1"), any())).thenReturn(true);
             })) {

            servlet.doPost(request, response);
            writer.flush();

            String responseContent = stringWriter.toString();
            assertTrue(responseContent.contains("\"status\":\"success\""));
        }
    }

    @Test
    void testDoGet_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        when(request.getServletPath()).thenReturn("/patient-prescriptions");
        
        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class)) {
            servlet.doGet(request, response);
            verify(request, never()).getRequestDispatcher(anyString());
        }
    }

    @Test
    void testDoGet_ProgressView() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions");
        when(request.getParameter("view")).thenReturn("progress");
        when(request.getParameter("range")).thenReturn("30");
        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-prescriptions.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<PrescriptionDAO> presDAOMock = mockConstruction(PrescriptionDAO.class, (mock, context) -> {
                when(mock.getLatestPrescription(patientId)).thenReturn(new Prescription());
             });
             MockedConstruction<MedicationLogDAO> logDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getAdherenceRate(patientId, 30)).thenReturn(85);
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute("viewMode", "progress");
            verify(request).setAttribute("range", 30);
            verify(request).setAttribute("adherenceRate", 85);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_AiReminder_MissingPatient() throws Exception {
        // AI reminder API when patient ID is null (handled internally by servlet returning a JSON error)
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        
        when(request.getServletPath()).thenReturn("/patient-prescriptions/ai-reminder");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class)) {
            servlet.doGet(request, response);
            writer.flush();

            String responseContent = stringWriter.toString();
            assertTrue(responseContent.isEmpty());
        }
    }

    @Test
    void testDoPost_Toggle_MissingMedicationId() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions/toggle");
        when(request.getParameter("medicationId")).thenReturn(""); // Missing or empty ID
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);
        writer.flush();

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("\"status\":\"error\""));
        assertTrue(responseContent.contains("\"message\":\"Thiếu thông tin\""));
    }

    @Test
    void testDoPost_Toggle_Failure() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getServletPath()).thenReturn("/patient-prescriptions/toggle");
        when(request.getParameter("medicationId")).thenReturn("med-1");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        try (MockedConstruction<MedicationLogDAO> logDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.toggleMedicationStatus(eq(patientId), eq("med-1"), any())).thenReturn(false);
             })) {

            servlet.doPost(request, response);
            writer.flush();

            String responseContent = stringWriter.toString();
            assertTrue(responseContent.contains("\"status\":\"error\""));
            assertTrue(responseContent.contains("Lỗi cập nhật CSDL"));
        }
    }
}
