package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.DietPlanDAO;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.MasterFoodDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.DietPlan;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.Patient;
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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatientDietServletTest {

    private PatientDietServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MockedStatic<PatientPortalAuth> mockedAuth;

    @BeforeEach
    void setUp() {
        servlet = new PatientDietServlet();
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
    void testDoGet_Authenticated_Success() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-diet.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<DietPlanDAO> dietPlanDAOMock = mockConstruction(DietPlanDAO.class, (mock, context) -> {
                when(mock.getTodayDietPlan(patientId)).thenReturn(new DietPlan());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("patientInfo"), any(Patient.class));
            verify(request).setAttribute(eq("todayPlan"), any(DietPlan.class));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GenerateDietPlan() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("generate");
        when(request.getContextPath()).thenReturn("");

        String jsonResponse = "[{\"foodId\": \"f1\", \"buaAn\": \"Sáng\", \"ghiChu\": \"Test\"}]";

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class, (mock, context) -> {
                when(mock.getLatestComprehensiveRecord(patientId)).thenReturn(new HealthRecord());
             });
             MockedConstruction<MasterFoodDAO> foodDAOMock = mockConstruction(MasterFoodDAO.class, (mock, context) -> {
                when(mock.getAllFoods()).thenReturn(new ArrayList<>());
             });
             MockedConstruction<GeminiService> geminiMock = mockConstruction(GeminiService.class, (mock, context) -> {
                when(mock.generateDailyDietPlan(any(), any(), anyList())).thenReturn(jsonResponse);
             });
             MockedConstruction<DietPlanDAO> dietPlanDAOMock = mockConstruction(DietPlanDAO.class)) {

            servlet.doPost(request, response);

            verify(dietPlanDAOMock.constructed().get(0)).saveDietPlan(any(DietPlan.class));
            verify(response).sendRedirect("/patient-diet");
        }
    }

    @Test
    void testDoGet_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        servlet.doGet(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testDoGet_NoPlan() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-diet.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<DietPlanDAO> dietPlanDAOMock = mockConstruction(DietPlanDAO.class, (mock, context) -> {
                when(mock.getTodayDietPlan(patientId)).thenReturn(null); // No plan yet
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("patientInfo"), any(Patient.class));
            verify(request).setAttribute(eq("todayPlan"), isNull()); // Expecting null
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        when(request.getParameter("action")).thenReturn("generate");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);
        verify(response).sendRedirect("/patient-diet");
    }

    @Test
    void testDoPost_GenerateDietPlan_AiFailure() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("generate");
        when(request.getContextPath()).thenReturn("");

        String jsonResponse = null; // AI failure

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class, (mock, context) -> {
                when(mock.getLatestComprehensiveRecord(patientId)).thenReturn(new HealthRecord());
             });
             MockedConstruction<MasterFoodDAO> foodDAOMock = mockConstruction(MasterFoodDAO.class, (mock, context) -> {
                when(mock.getAllFoods()).thenReturn(new ArrayList<>());
             });
             MockedConstruction<GeminiService> geminiMock = mockConstruction(GeminiService.class, (mock, context) -> {
                when(mock.generateDailyDietPlan(any(), any(), anyList())).thenReturn(jsonResponse);
             });
             MockedConstruction<DietPlanDAO> dietPlanDAOMock = mockConstruction(DietPlanDAO.class)) {

            servlet.doPost(request, response);

            // Should not save to DB if AI fails (DietPlanDAO is not constructed)
            assertTrue(dietPlanDAOMock.constructed().isEmpty());
            // Still redirects back to the diet page (perhaps to show an error or just refresh)
            verify(response).sendRedirect("/patient-diet");
        }
    }

    @Test
    void testDoPost_UnsupportedAction() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("unknown_action");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        // Does nothing and just redirects
        verify(response).sendRedirect("/patient-diet");
    }
}
