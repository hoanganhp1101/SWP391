package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.Patient;
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
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatientNotificationsServletTest {

    private PatientNotificationsServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MockedStatic<PatientPortalAuth> mockedAuth;

    @BeforeEach
    void setUp() {
        servlet = new PatientNotificationsServlet();
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

        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-notifications.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<AlertDAO> alertDAOMock = mockConstruction(AlertDAO.class, (mock, context) -> {
                when(mock.getAllAlerts(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<MedicationLogDAO> medLogDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getChecklistByDate(eq(patientId), any())).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AppointmentDAO> apptDAOMock = mockConstruction(AppointmentDAO.class, (mock, context) -> {
                when(mock.getUpcomingAppointments(patientId)).thenReturn(new ArrayList<>());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("patientInfo"), any(Patient.class));
            verify(request).setAttribute(eq("allNotifs"), anyList());

            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        servlet.doGet(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testDoGet_WithSystemAlerts() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);
        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-notifications.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<AlertDAO> alertDAOMock = mockConstruction(AlertDAO.class, (mock, context) -> {
                ArrayList<Alert> alerts = new ArrayList<>();
                Alert alert = new Alert();
                alert.setLoaiCanhBao("High Glucose");
                alert.setNoiDung("Đường huyết cao");
                alert.setThoiGianTao(new java.sql.Timestamp(System.currentTimeMillis()));
                alerts.add(alert);
                when(mock.getAllAlerts(patientId)).thenReturn(alerts);
             });
             MockedConstruction<MedicationLogDAO> medLogDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getChecklistByDate(eq(patientId), any())).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AppointmentDAO> apptDAOMock = mockConstruction(AppointmentDAO.class, (mock, context) -> {
                when(mock.getUpcomingAppointments(patientId)).thenReturn(new ArrayList<>());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("allNotifs"), argThat(list -> {
                return ((java.util.List<?>) list).size() == 1; // 1 System Alert
            }));
        }
    }

    @Test
    void testDoGet_WithAppointmentAlerts() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);
        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-notifications.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<AlertDAO> alertDAOMock = mockConstruction(AlertDAO.class, (mock, context) -> {
                when(mock.getAllAlerts(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<MedicationLogDAO> medLogDAOMock = mockConstruction(MedicationLogDAO.class, (mock, context) -> {
                when(mock.getChecklistByDate(eq(patientId), any())).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AppointmentDAO> apptDAOMock = mockConstruction(AppointmentDAO.class, (mock, context) -> {
                ArrayList<Appointment> appts = new ArrayList<>();
                Appointment appt = new Appointment();
                appt.setTrangThai("Sắp tới");
                appts.add(appt);
                when(mock.getUpcomingAppointments(patientId)).thenReturn(appts);
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("allNotifs"), argThat(list -> {
                return ((java.util.List<?>) list).size() == 1; // 1 Appointment Alert
            }));
        }
    }
}
