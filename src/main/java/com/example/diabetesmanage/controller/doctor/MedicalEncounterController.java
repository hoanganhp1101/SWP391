package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.dto.EncounterCreateDTO;
import com.example.diabetesmanage.dto.MedicalEncounterDTO;
import com.example.diabetesmanage.service.*;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/doctor/patient-records")
public class MedicalEncounterController extends HttpServlet {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final EncounterAiAnalysis aiService = new EncounterAiAnalysis();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type");
        String status = request.getParameter("status");
        String patientId = request.getParameter("patientId");

        List<MedicalEncounter> records = encounterDAO.searchEncounters(
                scopeDoctorId, startDate, endDate, keyword, type, status, patientId);

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("records", records);
        request.setAttribute("patientId", patientId);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecordmanagement.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        switch (action) {
            case "add":
                add(request, response);
                break;
            case "detail":
                viewDetail(request, response);
                break;
            case "analyze":
                analyze(request, response);
                break;
            case "form":
                form(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ");
                break;
        }
    }

    private void add(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        EncounterCreateDTO form = new EncounterCreateDTO();
        form.setNgayKham(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setKhoaKham("Khoa Nội tiết");
        prepareForm(request, doctor, form);
        request.getRequestDispatcher("/WEB-INF/views/doctor/add-medical-encounter.jsp")
                .forward(request, response);
    }
    private void viewDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String encounterId = request.getParameter("id");
        if (encounterId == null || encounterId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        if (!AuthContext.ensureEncounterAccess(user, patientDAO, encounterDAO, encounterId, response)) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            encounter = encounterDAO.getEncounterById(encounterId, null);
        }
        if (encounter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy lần khám");
            return;
        }

        MedicalEncounterDTO detailView = medicalRecordService.loadMedicalRecordDetail(encounterId, scopeDoctorId);
        if (detailView == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy lần khám");
            return;
        }

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("encounter", encounter);
        request.setAttribute("detailView", detailView);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }

    private void analyze(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        User doctor = AuthContext.getUser(request);

        EncounterCreateDTO form;
        try {
            form = EncounterCreateDTO.fromRequest(request);
            form.prepareForSave();
        } catch (NumberFormatException ex) {
            writeJson(response, HttpServletResponse.SC_OK,
                    error(numericInputErrors(request)));
            return;
        }

        List<String> errors = medicalRecordService.validateStep1(form);
        if (!errors.isEmpty()) {
            writeJson(response, HttpServletResponse.SC_OK, error(errors));
            return;
        }

        Patient patient = null;
        String doctorId = doctor.getId() != null ? doctor.getId().toString() : null;
        if (doctorId != null) {
            patient = patientDAO.getPatientById(form.getPatientId(), doctorId);
        }
        if (patient == null) {
            writeJson(response, HttpServletResponse.SC_OK,
                    error(List.of("Không tìm thấy bệnh nhân hoặc bạn không có quyền truy cập.")));
            return;
        }

        EncounterAiAnalysis analysis = aiService.analyze(form, patient);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("used", analysis.isUsed());
        body.put("configured", analysis.isConfigured());
        body.put("error", analysis.getError());
        body.put("summaryText", analysis.buildSummaryText());

        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("riskLevel", analysis.getRiskLevel());
        ai.put("riskScore", analysis.getRiskScore());
        ai.put("possibleDisease", analysis.getPossibleDisease());
        ai.put("riskFactors", analysis.getRiskFactors());
        ai.put("recommendedTests", analysis.getRecommendedTests());
        ai.put("recommendations", analysis.getRecommendations());
        ai.put("shortExplanation", analysis.getShortExplanation());
        body.put("ai", ai);

        writeJson(response, HttpServletResponse.SC_OK, body);
    }

    private Map<String, Object> error(List<String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("errors", new ArrayList<>(errors));
        return body;
    }

    private void writeJson(HttpServletResponse response, int status, Map<String, Object> body)
            throws IOException {
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.write(gson.toJson(body));
        }
    }

    private void form(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        EncounterCreateDTO form;
        try {
            form = EncounterCreateDTO.fromRequest(request);
            form.prepareForSave();
        } catch (NumberFormatException ex) {
            form = new EncounterCreateDTO();
            form.setPatientId(request.getParameter("patientId"));
            List<String> errors = numericInputErrors(request);
            request.setAttribute("fieldErrors", mapErrorsToFields(errors));
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        if (form.getPatientId() == null || form.getPatientId().isBlank()) {
            List<String> errors = new ArrayList<>();
            errors.add("Vui lòng chọn bệnh nhân.");
            request.setAttribute("fieldErrors", mapErrorsToFields(errors));
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        if (doctor.getId() == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Không xác định được bác sĩ đang đăng nhập.");
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        String doctorUuid = doctor.getId().toString();

        if (!AuthContext.ensurePatientAccess(doctor, patientDAO, form.getPatientId(), response)) {
            return;
        }

        List<String> errors = medicalRecordService.validateStep1(form);
        if (!errors.isEmpty()) {
            request.setAttribute("fieldErrors", mapErrorsToFields(errors));
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        try {
            // chan_doan_chinh là NOT NULL nhưng chẩn đoán được nhập ở Bước 2 nên cần giá trị tạm.

            MedicalRecordService.CreateResult result =
                    medicalRecordService.create(form, doctorUuid);
            if (result.getEncounterId() == null || result.getEncounterId().isBlank()) {
                throw new SQLException("Không nhận được mã lần khám sau khi lưu");
            }
            if (!encounterDAO.existsById(result.getEncounterId())) {
                throw new SQLException("Không tìm thấy lần khám trong cơ sở dữ liệu sau khi lưu, mã="
                        + result.getEncounterId());
            }
            MedicalEncounter persisted = encounterDAO.getEncounterById(
                    result.getEncounterId(), doctorUuid);
            if (persisted == null) {
                throw new SQLException("Không thể tải lại lần khám từ cơ sở dữ liệu, mã="
                        + result.getEncounterId());
            }
            String encounterId = result.getEncounterId();

            // Chỉ bệnh án tái khám Nội tiết mới đi tiếp sang Bước 2 (kê đơn).
            // Hồ sơ xét nghiệm chỉ lưu lần khám và kết quả xét nghiệm rồi quay về danh sách.
            if (form.isTaiKhamNoiTiet()) {
                // Giữ tạm tóm tắt AI trong phiên để Bước 2 hiển thị chỉ đọc.
                String aiSummary = request.getParameter("aiSummary");
                if (aiSummary != null && !aiSummary.isBlank()) {
                    request.getSession(true).setAttribute("aiSummary:" + encounterId, aiSummary.trim());
                }
                // PRG: chuyển sang Bước 2 để tải lại trang không tạo lần khám trùng.
                response.sendRedirect(request.getContextPath()
                        + "/doctor/treatment-plan?id=" + encounterId);
            } else {
                // PRG: lưu xong quay về danh sách hồ sơ.
                response.sendRedirect(request.getContextPath()
                        + "/doctor/patient-records?success=1&patientId=" + form.getPatientId());
            }
        } catch (SQLException ex) {
            errors = new ArrayList<>();
            String detail = ex.getMessage();
            if (detail != null && !detail.isBlank()) {
                errors.add("Không thể lưu hồ sơ bệnh án: " + detail);
            } else {
                errors.add("Không thể lưu hồ sơ bệnh án. Vui lòng thử lại sau.");
            }
            forwardWithErrors(request, response, form, errors, doctor);
        }
    }

    private void forwardWithErrors(HttpServletRequest request, HttpServletResponse response,
                                   EncounterCreateDTO form, List<String> errors, User doctor)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        prepareForm(request, doctor, form);
        request.getRequestDispatcher("/WEB-INF/views/doctor/add-medical-encounter.jsp")
                .forward(request, response);
    }

    private void prepareForm(HttpServletRequest request, User doctor, EncounterCreateDTO form) {
        String doctorId = doctor.getId().toString();
        DoctorLayoutHelper.prepare(request, doctor, "records");
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

    private List<String> numericInputErrors(HttpServletRequest request) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("duongHuyetMgdl", "Đường huyết");
        labels.put("hba1cPercent", "HbA1c");
        labels.put("chieuCaoCm", "Chiều cao");
        labels.put("canNangKg", "Cân nặng");
        labels.put("bmi", "BMI");
        labels.put("huyetApTamThu", "Huyết áp tâm thu");
        labels.put("huyetApTamTruong", "Huyết áp tâm trương");
        labels.put("nhipTim", "Nhịp tim");
        labels.put("nhietDoC", "Nhiệt độ");
        labels.put("nhipTho", "Nhịp thở");
        labels.put("carbsG", "Carbohydrate");
        labels.put("lieuLuongInsulinUi", "Liều insulin");
        labels.put("labGlucoseMau", "Đường huyết");
        labels.put("labHba1c", "HbA1c");
        labels.put("labCholesterol", "Cholesterol");
        labels.put("labTriglyceride", "Triglyceride");
        labels.put("labHdl", "HDL");
        labels.put("labLdl", "LDL");
        labels.put("labAst", "AST");
        labels.put("labAlt", "ALT");
        labels.put("labUre", "Ure");
        labels.put("labCreatinine", "Creatinine");
        labels.put("labWbc", "WBC");
        labels.put("labRbc", "RBC");
        labels.put("labHgb", "HGB");
        labels.put("labHct", "HCT");
        labels.put("labPlt", "PLT");

        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, String> field : labels.entrySet()) {
            String value = request.getParameter(field.getKey());
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalized = value.trim().replace(',', '.');
            boolean integerField = "huyetApTamThu".equals(field.getKey())
                    || "huyetApTamTruong".equals(field.getKey())
                    || "nhipTim".equals(field.getKey())
                    || "nhipTho".equals(field.getKey())
                    || "lieuLuongInsulinUi".equals(field.getKey());
            boolean valid = integerField
                    ? normalized.matches("^\\d+$")
                    : normalized.matches("^\\d+(\\.\\d+)?$");
            if (!valid) {
                errors.add(field.getValue() + " chỉ được nhập số.");
                continue;
            }
            try {
                if (integerField) {
                    Integer.parseInt(normalized);
                } else {
                    Double.parseDouble(normalized);
                }
            } catch (NumberFormatException ex) {
                errors.add(field.getValue() + " chỉ được nhập số.");
            }
        }
        if (errors.isEmpty()) {
            errors.add("Giá trị phải là số hợp lệ.");
        }
        return errors;
    }

    private Map<String, String> mapErrorsToFields(List<String> errors) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (errors == null) {
            return fieldErrors;
        }
        for (String error : errors) {
            if (error == null) {
                continue;
            }
            String field = null;
            if (error.contains("bệnh nhân") || error.contains("Bệnh nhân")) field = "patientId";
            else if (error.contains("Ngày khám")) field = "ngayKham";
            else if (error.contains("Loại hồ sơ")) field = "encounterType";
            else if (error.contains("Lý do khám") || error.contains("Triệu chứng")) field = "trieuChung";
            else if (error.contains("Đường huyết")) {
                fieldErrors.putIfAbsent("duongHuyetMgdl", error);
                fieldErrors.putIfAbsent("labGlucoseMau", error);
            }
            else if (error.contains("Chiều cao")) field = "chieuCaoCm";
            else if (error.contains("Cân nặng")) field = "canNangKg";
            else if (error.contains("Huyết áp tâm thu")) field = "huyetApTamThu";
            else if (error.contains("Huyết áp tâm trương")) field = "huyetApTamTruong";
            else if (error.contains("Nhịp tim")) field = "nhipTim";
            else if (error.contains("Nhiệt độ")) field = "nhietDoC";
            else if (error.contains("Nhịp thở")) field = "nhipTho";
            else if (error.contains("Creatinine")) field = "labCreatinine";
            else if (error.contains("Cholesterol")) field = "labCholesterol";
            else if (error.contains("Triglyceride")) field = "labTriglyceride";
            else if (error.contains("HDL")) field = "labHdl";
            else if (error.contains("LDL")) field = "labLdl";
            else if (error.contains("AST")) field = "labAst";
            else if (error.contains("ALT")) field = "labAlt";
            else if (error.contains("Ure")) field = "labUre";
            else if (error.contains("WBC")) field = "labWbc";
            else if (error.contains("RBC")) field = "labRbc";
            else if (error.contains("HGB")) field = "labHgb";
            else if (error.contains("HCT")) field = "labHct";
            else if (error.contains("PLT")) field = "labPlt";
            else if (error.contains("HbA1c")) {
                fieldErrors.putIfAbsent("hba1cPercent", error);
                fieldErrors.putIfAbsent("labHba1c", error);
            }
            if (field != null) {
                fieldErrors.putIfAbsent(field, error);
            }
        }
        return fieldErrors;
    }
}
