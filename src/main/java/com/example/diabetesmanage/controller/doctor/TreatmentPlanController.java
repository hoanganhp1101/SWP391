package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/doctor/treatment-plan")
public class TreatmentPlanController extends HttpServlet {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();

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

        EncounterCreateDTO form;
        try {
            form = EncounterCreateDTO.fromRequest(request);
        } catch (NumberFormatException ex) {
            String message = "Dữ liệu số không hợp lệ. Vui lòng kiểm tra lại trường vừa nhập.";
            String[] values = request.getParameterValues("medThoiGianDungNgay");
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    try {
                        Integer.parseInt(value.trim());
                    } catch (NumberFormatException medicationDaysEx) {
                        message = "Số ngày dùng của thuốc dòng " + (i + 1)
                                + " phải là số nguyên không âm.";
                        break;
                    }
                }
            }
            request.setAttribute("fieldErrors", Map.of("medThoiGianDungNgay", message));
            renderFormWithError(request, response, doctor, encounter,
                    List.of(message));
            return;
        }

        List<String> errors = new ArrayList<>();
        if (form.getChanDoanChinh() == null || form.getChanDoanChinh().isBlank()) {
            errors.add("Vui lòng nhập Chẩn đoán chính.");
            request.setAttribute("fieldErrors",
                    Map.of("chanDoanChinh", "Vui lòng nhập Chẩn đoán chính."));
        }
        for (int i = 0; i < form.getMedications().size(); i++) {
            EncounterCreateDTO.MedicationLineItem medication = form.getMedications().get(i);
            int row = i + 1;
            if (medication.getLieuLuong() == null || medication.getLieuLuong().isBlank()) {
                errors.add("Vui lòng nhập Liều lượng cho thuốc dòng " + row + ".");
            }
            if (medication.getTanSuat() == null || medication.getTanSuat().isBlank()) {
                errors.add("Vui lòng nhập Tần suất cho thuốc dòng " + row + ".");
            }
            if (medication.getThoiGianDungNgay() != null && medication.getThoiGianDungNgay() < 0) {
                errors.add("Số ngày dùng của thuốc dòng " + row + " phải là số nguyên không âm.");
            }
        }
        if (!errors.isEmpty()) {
            renderFormWithError(request, response, doctor, encounter, errors);
            return;
        }

        String patientId = encounter.getPatientId();
        String doctorUuid = doctor.getId() != null ? doctor.getId().toString() : encounter.getBacSiId();

        try {
            if (encounterId == null || encounterId.isBlank() || !encounterDAO.existsById(encounterId)) {
                throw new SQLException("Lần khám không tồn tại: " + encounterId);
            }

            try (Connection con = DBContext.getConnection()) {
                if (con == null) {
                    throw new SQLException("Không thể kết nối cơ sở dữ liệu");
                }

                boolean autoCommit = con.getAutoCommit();
                con.setAutoCommit(false);

                try {
                    encounterDAO.updateTreatmentPlan(
                            con, encounterId,
                            form.getChanDoanChinh(),
                            form.getChanDoanPhu(),
                            form.getHuongXuTri());

                    if (form.getPhanLoaiTieuDuong() != null && !form.getPhanLoaiTieuDuong().isBlank()) {
                        patientDAO.updateLoaiTieuDuong(con, patientId, form.getPhanLoaiTieuDuong());
                    }

                    String existingPrescriptionId =
                            prescriptionDAO.getIdByEncounterId(con, encounterId);
                    medicationDAO.deleteByPrescriptionId(con, existingPrescriptionId);
                    prescriptionDAO.deleteByEncounterId(con, encounterId);

                    if (form.hasPrescriptionData()) {
                        String prescriptionId = prescriptionDAO.insert(
                                con, form, patientId, doctorUuid, encounterId);

                        if (form.hasMedications()) {
                            medicationDAO.insertAll(con, prescriptionId, form.getMedications());
                        }
                    }

                    con.commit();
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                } finally {
                    con.setAutoCommit(autoCommit);
                }
            }
            request.getSession(true).removeAttribute("aiSummary:" + encounterId);
            response.sendRedirect(request.getContextPath()
                    + "/doctor/patient-records?success=1&patientId=" + patientId);
        } catch (SQLException ex) {
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
        Map<String, String> advice = prescriptionDAO.getAdviceByEncounterId(encounter.getId());
        request.setAttribute("advice", advice);
        String prescriptionId = prescriptionDAO.getIdByEncounterId(encounter.getId());
        List<Map<String, String>> meds = medicationDAO.getDetailsByPrescriptionId(prescriptionId);
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
