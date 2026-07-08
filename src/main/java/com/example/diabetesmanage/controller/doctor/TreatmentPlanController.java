package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.service.medical.TreatmentPlanService;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bước 2 - Treatment Plan. Load theo encounterId (encounter đã được tạo ở Bước "Tiếp tục kê đơn").
 *
 * <p>GET: hiển thị thông tin bệnh nhân, AI Summary (readonly), form chẩn đoán/đơn thuốc/hướng xử trí.
 * POST: lưu chẩn đoán + đơn thuốc + recommendation (KHÔNG tạo lại Medical Encounter).
 */
@WebServlet("/doctor/treatment-plan")
public class TreatmentPlanController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TreatmentPlanController.class.getName());

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final TreatmentPlanService treatmentPlanService = new TreatmentPlanService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String encounterId = request.getParameter("id");
        String scope = AuthContext.scopeDoctorId(doctor);
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scope);
        if (encounter == null) {
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-records?error=" + encode("Không tìm thấy lần khám."));
            return;
        }

        Patient patient = patientDAO.getPatientById(encounter.getPatientId(), scope);
        renderForm(request, response, doctor, encounter, patient);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        String encounterId = request.getParameter("encounterId");
        if (!AuthContext.ensureEncounterAccess(doctor, patientDAO, encounterDAO, encounterId, response)) {
            return;
        }

        String scope = AuthContext.scopeDoctorId(doctor);
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scope);
        if (encounter == null) {
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-records?error=" + encode("Không tìm thấy lần khám."));
            return;
        }

        EncounterCreateRequest form;
        try {
            form = EncounterCreateRequest.fromRequest(request);
        } catch (NumberFormatException ex) {
            renderFormWithError(request, response, doctor, encounter,
                    List.of("Dữ liệu số không hợp lệ. Vui lòng kiểm tra lại."));
            return;
        }

        List<String> errors = new ArrayList<>();
        if (form.getChanDoanChinh() == null || form.getChanDoanChinh().isBlank()) {
            errors.add("Chẩn đoán chính là bắt buộc.");
        }
        if (!errors.isEmpty()) {
            renderFormWithError(request, response, doctor, encounter, errors);
            return;
        }

        String patientId = encounter.getPatientId();
        String doctorUuid = doctor.getId() != null ? doctor.getId().toString() : encounter.getBacSiId();

        try {
            treatmentPlanService.save(encounterId, patientId, doctorUuid, form);
            request.getSession(true).removeAttribute("aiSummary:" + encounterId);
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-records?success=1&patientId=" + patientId);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Failed to save treatment plan encounterId=" + encounterId, ex);
            String detail = ex.getMessage();
            renderFormWithError(request, response, doctor, encounter,
                    List.of("Không thể lưu hồ sơ: " + (detail != null ? detail : "Vui lòng thử lại.")));
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response,
                            User doctor, MedicalEncounter encounter, Patient patient)
            throws ServletException, IOException {

        DoctorLayoutHelper.prepare(request, doctor, "records");
        request.setAttribute("encounter", encounter);
        request.setAttribute("patient", patient);

        Object aiSummary = request.getSession(true).getAttribute("aiSummary:" + encounter.getId());
        request.setAttribute("aiSummary", aiSummary);

        // Dữ liệu đơn thuốc/khuyến nghị cũ (nếu bác sĩ quay lại chỉnh sửa).
        Map<String, String> advice = encounterDAO.getPrescriptionAdviceByEncounterId(encounter.getId());
        request.setAttribute("advice", advice);
        List<Map<String, String>> meds = encounterDAO.getMedicationDetailsByEncounterId(encounter.getId());
        request.setAttribute("meds", meds);

        String currentDiagnosis = encounter.getChanDoanChinh();
        if ("Đang cập nhật".equals(currentDiagnosis)) {
            currentDiagnosis = "";
        }
        request.setAttribute("currentDiagnosis", currentDiagnosis);

        request.getRequestDispatcher("/WEB-INF/views/doctor/treatment-plan.jsp")
                .forward(request, response);
    }

    private void renderFormWithError(HttpServletRequest request, HttpServletResponse response,
                                     User doctor, MedicalEncounter encounter, List<String> errors)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        Patient patient = patientDAO.getPatientById(
                encounter.getPatientId(), AuthContext.scopeDoctorId(doctor));
        renderForm(request, response, doctor, encounter, patient);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
