package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.MasterMedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MasterMedication;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PrescriptionController", urlPatterns = {"/admin/prescribe"})
public class PrescriptionController extends HttpServlet {

    private MasterMedicationDAO medicationDAO;
    private PrescriptionDAO prescriptionDAO;
    private PatientDAO patientDAO;

    public PrescriptionController() {
    }

    PrescriptionController(MasterMedicationDAO medicationDAO, PrescriptionDAO prescriptionDAO, PatientDAO patientDAO) {
        this.medicationDAO = medicationDAO;
        this.prescriptionDAO = prescriptionDAO;
        this.patientDAO = patientDAO;
    }

    @Override
    public void init() throws ServletException {
        if (medicationDAO == null) {
            medicationDAO = new MasterMedicationDAO();
        }
        if (prescriptionDAO == null) {
            prescriptionDAO = new PrescriptionDAO();
        }
        if (patientDAO == null) {
            patientDAO = new PatientDAO();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String patientId = request.getParameter("patientId");
        Patient patient = patientDAO.getPatientByIdAdmin(patientId);

        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient-manager");
            return;
        }

        List<MasterMedication> medList = medicationDAO.getAllMedications();
        request.setAttribute("patientId", patientId);
        request.setAttribute("patient", patient);
        request.setAttribute("medList", medList);

        request.getRequestDispatcher("/WEB-INF/views/admin/prescription-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("adminUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login?required=1");
            return;
        }

        String patientId = request.getParameter("patientId");
        Patient patient = patientDAO.getPatientByIdAdmin(patientId);
        if (patient == null) {
            response.sendRedirect(request.getContextPath() + "/patient-manager");
            return;
        }

        String[] medicationIds = request.getParameterValues("medicationId[]");
        String[] lieuLuongs = request.getParameterValues("lieuLuong[]");
        String[] tanSuats = request.getParameterValues("tanSuat[]");

        List<PrescriptionDetail> details = buildPrescriptionDetails(medicationIds, lieuLuongs, tanSuats);
        if (!details.isEmpty()) {
            Prescription prescription = new Prescription();
            prescription.setPatientId(patientId);
            prescription.setBacSiId(loginUser.getId());
            prescription.setGhiChu(request.getParameter("ghiChu"));
            boolean success = prescriptionDAO.createPrescription(prescription, details);
            setFlash(request, success ? "success" : "danger",
                    success ? "Đã lưu đơn thuốc." : "Không thể lưu đơn thuốc. Vui lòng kiểm tra lại dữ liệu.");
        } else {
            setFlash(request, "danger", "Đơn thuốc phải có ít nhất một thuốc với đủ liều lượng và tần suất.");
        }

        response.sendRedirect(request.getContextPath() + "/patient-manager?action=view&id=" + encode(patientId));
    }

    List<PrescriptionDetail> buildPrescriptionDetails(String[] medicationIds, String[] lieuLuongs, String[] tanSuats) {
        List<PrescriptionDetail> details = new ArrayList<>();
        if (medicationIds == null || lieuLuongs == null || tanSuats == null) {
            return details;
        }

        int count = Math.min(medicationIds.length, Math.min(lieuLuongs.length, tanSuats.length));
        for (int i = 0; i < count; i++) {
            if (isBlank(medicationIds[i]) || isBlank(lieuLuongs[i]) || isBlank(tanSuats[i])) {
                continue;
            }

            PrescriptionDetail detail = new PrescriptionDetail();
            detail.setMedicationId(medicationIds[i].trim());
            detail.setLieuLuong(lieuLuongs[i].trim());
            detail.setTanSuat(tanSuats[i].trim());
            details.add(detail);
        }

        return details;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
