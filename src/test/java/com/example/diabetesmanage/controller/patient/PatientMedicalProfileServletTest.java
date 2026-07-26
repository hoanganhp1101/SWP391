package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.*;
import com.example.diabetesmanage.model.*;
import com.example.diabetesmanage.util.PatientPortalAuth;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatientMedicalProfileServletTest {

    private PatientMedicalProfileServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MockedStatic<PatientPortalAuth> mockedAuth;

    @BeforeEach
    void setUp() {
        servlet = new PatientMedicalProfileServlet();
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
    void testDoGet_Authenticated() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        when(request.getRequestDispatcher("/WEB-INF/views/patient/patient-medical-profile.jsp")).thenReturn(dispatcher);

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(new Patient());
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class, (mock, context) -> {
                when(mock.getLatestComprehensiveRecord(patientId)).thenReturn(new HealthRecord());
             });
             MockedConstruction<PrescriptionDAO> presDAOMock = mockConstruction(PrescriptionDAO.class, (mock, context) -> {
                when(mock.getLatestPrescription(patientId)).thenReturn(new Prescription());
             });
             MockedConstruction<AlertDAO> alertDAOMock = mockConstruction(AlertDAO.class, (mock, context) -> {
                when(mock.getRecentAlerts(patientId)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<MedicalDocumentDAO> docDAOMock = mockConstruction(MedicalDocumentDAO.class, (mock, context) -> {
                when(mock.getRecentDocuments(patientId)).thenReturn(new ArrayList<>());
             })) {

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("patientInfo"), any(Patient.class));
            verify(request).setAttribute(eq("latestRecord"), any(HealthRecord.class));
            verify(request).setAttribute(eq("latestPrescription"), any(Prescription.class));
            verify(request).setAttribute(eq("alerts"), anyList());
            verify(request).setAttribute(eq("medicalDocuments"), anyList());

            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_UpdateProfile_Success() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setChieuCaoCm(170.0);

        when(request.getParameter("gioiTinh")).thenReturn("Nam");
        when(request.getParameter("chieuCaoCm")).thenReturn("170");
        when(request.getParameter("diaChi")).thenReturn("Hanoi");
        when(request.getParameter("canNangKg")).thenReturn("65");
        when(request.getParameter("huyetApTamThu")).thenReturn("120");
        when(request.getParameter("huyetApTamTruong")).thenReturn("80");
        when(request.getPart("pdfFile")).thenReturn(null); // No file upload for this test
        when(request.getContextPath()).thenReturn("");

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(patient);
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class)) {

            servlet.doPost(request, response);

            // Verify patient profile is updated
            verify(patientDAOMock.constructed().get(0)).updatePatientMedicalProfile(patient);
            
            // Verify vital signs inserted
            // Double weight, Double bmi, Integer systole, Integer diastole, Integer heartRate, Double glucose, Double hba1c, Double cholesterol, Double triglyceride
            verify(hrDAOMock.constructed().get(0)).insertExtractedHealthRecord(
                eq(patientId), eq(65.0), any(Double.class), eq(120), eq(80), 
                isNull(), isNull(), isNull(), isNull(), isNull()
            );

            // Verify redirect to the same page
            verify(response).sendRedirect("/patient-medical-profile?success=true");
        }
    }

    @Test
    void testDoPost_Unauthenticated() throws Exception {
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(null);
        servlet.doPost(request, response);
        verify(request, never()).getParameter(anyString());
    }

    @Test
    void testDoPost_UpdateVitals_SafeParsing() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        Patient patient = new Patient();
        patient.setId(patientId);
        when(request.getParameter("canNangKg")).thenReturn("abc"); // Invalid number
        when(request.getParameter("huyetApTamThu")).thenReturn(" 120 "); // With spaces
        when(request.getContextPath()).thenReturn("");

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(patient);
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class)) {

            servlet.doPost(request, response);

            // canNangKg (weight) should be parsed safely as null because "abc" is invalid.
            // huyetApTamThu (systole) should be parsed safely as 120.
            verify(hrDAOMock.constructed().get(0)).insertExtractedHealthRecord(
                eq(patientId), isNull(), isNull(), eq(120), isNull(), 
                isNull(), isNull(), isNull(), isNull(), isNull()
            );
        }
    }

    @Test
    void testDoPost_UpdateVitals_OutOfBounds() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        Patient patient = new Patient();
        patient.setId(patientId);
        when(request.getParameter("canNangKg")).thenReturn("15"); // Less than min 20
        when(request.getParameter("nhipTim")).thenReturn("300"); // Greater than max 250
        when(request.getContextPath()).thenReturn("");

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(patient);
             });
             MockedConstruction<HealthRecordDAO> hrDAOMock = mockConstruction(HealthRecordDAO.class)) {

            servlet.doPost(request, response);

            // Weight and Heart Rate should be filtered out to null
            verify(hrDAOMock.constructed().get(0)).insertExtractedHealthRecord(
                eq(patientId), isNull(), isNull(), isNull(), isNull(), 
                isNull(), isNull(), isNull(), isNull(), isNull()
            );
        }
    }

    @Test
    void testDoPost_PdfUpload_InvalidFormat() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        Patient patient = new Patient();
        patient.setId(patientId);
        when(request.getContextPath()).thenReturn("");

        Part filePart = mock(Part.class);
        when(request.getPart("pdfFile")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(1024L);
        when(filePart.getContentType()).thenReturn("image/png"); // Not PDF

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(patient);
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Chỉ hỗ trợ tải lên tệp PDF.", StandardCharsets.UTF_8)));
        }
    }

    @Test
    void testDoPost_PdfUpload_TooLarge() throws Exception {
        String patientId = "patient-123";
        mockedAuth.when(() -> PatientPortalAuth.requirePatientId(request, response)).thenReturn(patientId);

        Patient patient = new Patient();
        patient.setId(patientId);
        when(request.getContextPath()).thenReturn("");

        Part filePart = mock(Part.class);
        when(request.getPart("pdfFile")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(15L * 1024 * 1024); // 15MB > 10MB limit

        try (MockedConstruction<PatientDAO> patientDAOMock = mockConstruction(PatientDAO.class, (mock, context) -> {
                when(mock.getPatientById(patientId)).thenReturn(patient);
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect(contains("error=" + URLEncoder.encode("Tệp đính kèm không được vượt quá 10MB.", StandardCharsets.UTF_8)));
        }
    }
}
