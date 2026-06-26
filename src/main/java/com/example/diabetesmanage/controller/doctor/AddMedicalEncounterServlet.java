package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.model.form.AddMedicalEncounterForm;
import com.example.diabetesmanage.service.medical.MedicalEncounterCreateService;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/medical-encounters/add")
public class AddMedicalEncounterServlet extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();
    private final MedicalEncounterCreateService createService = new MedicalEncounterCreateService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        prepareForm(request, doctor, new AddMedicalEncounterForm());
        forwardForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        AddMedicalEncounterForm form;
        try {
            form = AddMedicalEncounterForm.fromRequest(request);
        } catch (NumberFormatException ex) {
            form = new AddMedicalEncounterForm();
            form.setPatientId(request.getParameter("patientId"));
            List<String> errors = new ArrayList<>();
            errors.add("Du lieu so khong hop le. Vui long kiem tra lai cac truong so.");
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        if (form.getPatientId() == null || form.getPatientId().isBlank()) {
            List<String> errors = new ArrayList<>();
            errors.add("Vui long chon benh nhan.");
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        if (!AuthContext.ensurePatientAccess(doctor, patientDAO, form.getPatientId(), response)) {
            return;
        }

        List<String> errors = createService.validate(form);
        if (!errors.isEmpty()) {
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        try {
            MedicalEncounterCreateService.CreateResult result =
                    createService.create(form, doctor.getId().toString());
            response.sendRedirect(request.getContextPath()
                    + "/doctor/record-detail?id=" + result.getHealthRecordId() + "&success=1");
        } catch (SQLException ex) {
            ex.printStackTrace();
            errors = new ArrayList<>();
            errors.add("Khong the luu ho so benh an. Vui long thu lai sau.");
            forwardWithErrors(request, response, form, errors, doctor);
        }
    }

    private void forwardWithErrors(HttpServletRequest request, HttpServletResponse response,
                                   AddMedicalEncounterForm form, List<String> errors, User doctor)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        prepareForm(request, doctor, form);
        forwardForm(request, response);
    }

    private void prepareForm(HttpServletRequest request, User doctor, AddMedicalEncounterForm form) {
        String doctorId = doctor.getId().toString();
        request.setAttribute("doctor", doctor);
        request.setAttribute("patients", patientDAO.getPatients(doctorId));
        request.setAttribute("form", form);
        if (form.getNgayKham() == null || form.getNgayKham().isBlank()) {
            form.setNgayKham(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (form.getKhoaKham() == null || form.getKhoaKham().isBlank()) {
            form.setKhoaKham("Khoa Nội tiết");
        }

        if (form.getPatientId() != null && !form.getPatientId().isBlank()) {
            Patient patient = patientDAO.getPatientById(form.getPatientId(), doctorId);
            request.setAttribute("patient", patient);
            if (patient != null && patient.getChieuCaoCm() != null && form.getChieuCaoCm() == null) {
                form.setChieuCaoCm(patient.getChieuCaoCm());
            }
        }
    }

    private void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/doctor/add-medical-encounter.jsp")
                .forward(request, response);
    }
}
