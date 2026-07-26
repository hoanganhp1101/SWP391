package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.util.PatientPortalAuth;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import jakarta.servlet.http.Part;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PatientDashboardServletTest {

    private PatientDashboardServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MockedStatic<PatientPortalAuth> mockedAuth;

    @BeforeEach
    void setUp() {
        servlet = new PatientDashboardServlet();
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
    void testDoGet_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);

        servlet.doGet(request, response);

        // requirePatientId will handle redirect if null is returned, so we just verify it was called
        mockedAuth.verify(() -> PatientPortalAuth.requirePatientId(request, response), times(1));
        // Verify no forwarding happened
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testDoGet_Authenticated_Success() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);
        
        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-dashboard.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                Patient patient = new Patient();
                patient.setId(patientId);
                patient.setHoTen("Test Patient");
                when(mock.getPatientById(patientId)).thenReturn(patient);
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class, (mock, context) -> {
                HealthRecord glucose = new HealthRecord();
                glucose.setDuongHuyetMgdl(100.0);
                when(mock.getLatestHealthRecord(patientId)).thenReturn(glucose);

                HealthRecord heartRate = new HealthRecord();
                heartRate.setNhipTim(75);
                when(mock.getLatestHeartRateRecord(patientId)).thenReturn(heartRate);

                HealthRecord bp = new HealthRecord();
                bp.setHuyetApTamThu(120);
                bp.setHuyetApTamTruong(80);
                when(mock.getLatestBloodPressureRecord(patientId)).thenReturn(bp);

                List<HealthRecord> allRecords = new ArrayList<>();
                allRecords.add(glucose);
                when(mock.getAllRecordsForChart(patientId)).thenReturn(allRecords);
             });
             MockedConstruction<AppointmentDAO> appDAOMock = mockConstruction(AppointmentDAO.class, (mock, context) -> {
                when(mock.getUpcomingAppointments(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<MedicalDocumentDAO> docDAOMock = mockConstruction(MedicalDocumentDAO.class, (mock, context) -> {
                when(mock.getRecentDocuments(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AlertDAO> alertDAOMock = mockConstruction(AlertDAO.class, (mock, context) -> {
                when(mock.getRecentAlerts(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AIAnalysisDAO> aiDAOMock = mockConstruction(AIAnalysisDAO.class, (mock, context) -> {
                when(mock.getLatestAnalysis(patientId)).thenReturn(new AIAnalysis());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute("latestGlucose", 100.0);
            verify(request).setAttribute("latestHeartRate", 75);
            verify(request).setAttribute("latestSystolic", 120);
            verify(request).setAttribute("latestDiastolic", 80);
            verify(request).setAttribute(eq("patientInfo"), any(Patient.class));
            verify(request).setAttribute(eq("appointments"), anyList());
            verify(request).setAttribute(eq("medicalDocuments"), anyList());
            verify(request).setAttribute(eq("alerts"), anyList());
            verify(request).setAttribute(eq("aiAnalysis"), any(AIAnalysis.class));
            
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_UpdateProfile_Success() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("hoTen")).thenReturn("Test Name");
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("ngaySinh")).thenReturn("1990-01-01");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");

        // mock avatar upload returning null
        when(request.getPart("avatarFile")).thenReturn(null);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
            when(mock.updatePatientProfile(any(Patient.class))).thenReturn(true);
        })) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("patient-dashboard?profileUpdated=1");
        }
    }

    @Test
    void testDoPost_UpdateProfile_MissingFields() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");
        // missing hoTen, email, etc.

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Vui lòng điền đầy đủ họ tên, email, số điện thoại, ngày sinh.", StandardCharsets.UTF_8)));
    }

    @Test
    void testDoPost_UpdateProfile_InvalidEmail() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");
        when(request.getParameter("hoTen")).thenReturn("Test Name");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("ngaySinh")).thenReturn("1990-01-01");
        when(request.getParameter("email")).thenReturn("invalid-email");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Email không hợp lệ.", StandardCharsets.UTF_8)));
    }

    @Test
    void testDoPost_UpdateProfile_InvalidPhone() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");
        when(request.getParameter("hoTen")).thenReturn("Test Name");
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("ngaySinh")).thenReturn("1990-01-01");
        when(request.getParameter("soDienThoai")).thenReturn("123456789"); // Doesn't start with 0 or length 10

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Số điện thoại không hợp lệ", StandardCharsets.UTF_8)));
    }

    @Test
    void testDoPost_UpdateProfile_FutureDate() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");
        when(request.getParameter("hoTen")).thenReturn("Test Name");
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        // Date in the future
        String futureDate = LocalDate.now().plusDays(1).toString();
        when(request.getParameter("ngaySinh")).thenReturn(futureDate);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Ngày sinh không được lớn hơn ngày hiện tại.", StandardCharsets.UTF_8)));
    }

    @Test
    void testDoPost_UpdateProfile_AvatarTooLarge() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getParameter("action")).thenReturn("updateProfile");
        when(request.getParameter("hoTen")).thenReturn("Test Name");
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("ngaySinh")).thenReturn("1990-01-01");
        when(request.getParameter("returnUrl")).thenReturn("patient-dashboard");

        Part avatarPart = mock(Part.class);
        when(request.getPart("avatarFile")).thenReturn(avatarPart);
        when(avatarPart.getSize()).thenReturn(6L * 1024 * 1024); // 6MB, limit is 5MB

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Ảnh đại diện không được vượt quá 5MB.", StandardCharsets.UTF_8)));
    }
}
