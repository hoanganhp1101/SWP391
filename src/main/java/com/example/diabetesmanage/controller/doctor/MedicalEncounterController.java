package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalDocumentDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.MedicalDocument;
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
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/doctor/patient-records")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class MedicalEncounterController extends HttpServlet {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final EncounterAiAnalysis aiService = new EncounterAiAnalysis();
    private final MedicalDocumentDAO medicalDocumentDAO = new MedicalDocumentDAO();
    private final MedicalRecordPdfService pdfService = new MedicalRecordPdfService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        if ("detail".equals(request.getParameter("action"))) {
            viewDetail(request, response);
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

        List<Patient> assignedPatients = patientDAO.searchPatients(
                null, null, null, null, null, null, null, null, null, scopeDoctorId);

        DoctorLayoutHelper.prepare(request, user, "records");
        request.setAttribute("records", records);
        request.setAttribute("patientId", patientId);
        request.setAttribute("assignedPatients", assignedPatients);
        consumeFlash(request);
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
            case "share":
                shareToPatient(request, response);
                break;
            case "import":
                importDocument(request, response);
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
        consumeFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/doctor/medicalrecorddetail.jsp")
                .forward(request, response);
    }

    /**
     * Xuất PDF hồ sơ khám → lưu medical_documents để bệnh nhân xem ở Lịch sử khám bệnh.
     */
    private void shareToPatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String encounterId = request.getParameter("id");
        if (encounterId == null || encounterId.isBlank()) {
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
        MedicalEncounterDTO detailView = medicalRecordService.loadMedicalRecordDetail(encounterId, scopeDoctorId);
        if (encounter == null || detailView == null) {
            setFlash(request, "error", "Không tìm thấy hồ sơ để chia sẻ.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        try {
            byte[] pdfBytes = pdfService.generateMedicalRecordPdf(
                    detailView, MedicalRecordPdfService.PdfExportType.FULL);
            String relativeUrl = storePdfBytes(pdfBytes,
                    "hs_" + safeFilePart(detailView.getRecordCode()) + "_" + System.currentTimeMillis() + ".pdf");

            MedicalDocument doc = new MedicalDocument();
            doc.setPatientId(encounter.getPatientId());
            doc.setBacSiId(encounter.getBacSiId() != null ? encounter.getBacSiId() : user.getId());
            doc.setLoaiTaiLieu("Hồ sơ khám bệnh"
                    + (detailView.getRecordCode() != null ? " - " + detailView.getRecordCode() : ""));
            doc.setTrangThai("hoan_thanh");
            doc.setFileUrl(relativeUrl);
            if (encounter.getNgayKham() != null) {
                doc.setNgayThucHien(Date.valueOf(encounter.getNgayKham().toLocalDate()));
            } else {
                doc.setNgayThucHien(new Date(System.currentTimeMillis()));
            }

            if (!medicalDocumentDAO.addDocument(doc)) {
                setFlash(request, "error", "Không lưu được tài liệu cho bệnh nhân.");
            } else {
                setFlash(request, "success",
                        "Đã gửi hồ sơ cho bệnh nhân. Bệnh nhân xem tại mục Lịch sử khám bệnh.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setFlash(request, "error", "Không tạo được PDF để chia sẻ: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath()
                + "/doctor/patient-records?action=detail&id=" + encounterId);
    }

    /**
     * Bác sĩ import PDF bên ngoài → bệnh nhân xem được.
     */
    private void importDocument(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User user = AuthContext.requireDoctor(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("patientId");
        String loaiTaiLieu = request.getParameter("loaiTaiLieu");
        if (patientId == null || patientId.isBlank()) {
            setFlash(request, "error", "Vui lòng chọn bệnh nhân.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }
        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }

        Part filePart = request.getPart("pdfFile");
        if (filePart == null || filePart.getSize() <= 0) {
            setFlash(request, "error", "Vui lòng chọn tệp PDF.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }
        if (filePart.getSize() > 10 * 1024 * 1024) {
            setFlash(request, "error", "Tệp không được vượt quá 10MB.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }
        String contentType = filePart.getContentType();
        String submitted = filePart.getSubmittedFileName();
        boolean isPdf = (contentType != null && contentType.equalsIgnoreCase("application/pdf"))
                || (submitted != null && submitted.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            setFlash(request, "error", "Chỉ hỗ trợ tệp PDF.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
            return;
        }

        try {
            String fileName = "import_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            String relativeUrl = storeUploadedPdf(filePart, fileName);

            MedicalDocument doc = new MedicalDocument();
            doc.setPatientId(patientId.trim());
            doc.setBacSiId(user.getId());
            doc.setLoaiTaiLieu(loaiTaiLieu == null || loaiTaiLieu.isBlank()
                    ? "Tài liệu khám bệnh" : loaiTaiLieu.trim());
            doc.setTrangThai("hoan_thanh");
            doc.setFileUrl(relativeUrl);
            doc.setNgayThucHien(new Date(System.currentTimeMillis()));

            if (!medicalDocumentDAO.addDocument(doc)) {
                setFlash(request, "error", "Import thất bại khi lưu database.");
            } else {
                setFlash(request, "success", "Đã import PDF. Bệnh nhân có thể xem trong Lịch sử khám bệnh.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setFlash(request, "error", "Import thất bại: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/doctor/patient-records");
    }

    private String storePdfBytes(byte[] pdfBytes, String fileName) throws IOException {
        Path dir = Path.of(getServletContext().getRealPath("/uploads/documents"));
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        Files.write(target, pdfBytes);
        return "uploads/documents/" + fileName;
    }

    private String storeUploadedPdf(Part filePart, String fileName) throws IOException {
        Path dir = Path.of(getServletContext().getRealPath("/uploads/documents"));
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        filePart.write(target.toString());
        return "uploads/documents/" + fileName;
    }

    private static String safeFilePart(String value) {
        if (value == null || value.isBlank()) {
            return "record";
        }
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    private void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object type = session.getAttribute("flashType");
        Object message = session.getAttribute("flashMessage");
        session.removeAttribute("flashType");
        session.removeAttribute("flashMessage");
        if (message == null) {
            return;
        }
        if ("success".equals(type)) {
            request.setAttribute("flashSuccess", message);
        } else {
            request.setAttribute("flashError", message);
        }
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
            prefillFromCurrentHealthData(form, patient);
        }
    }

    /**
     * Nạp sẵn chỉ số hiện có của hồ sơ sức khỏe vào các ô còn trống khi mở form,
     * để bác sĩ thấy giá trị hiện tại và chỉ sửa trường cần thay đổi.
     */
    private void prefillFromCurrentHealthData(EncounterCreateDTO form, Patient patient) {
        HealthRecord latest = healthRecordDAO.getLatestByPatientId(form.getPatientId());
        if (latest != null) {
            if (form.getChieuCaoCm() == null) {
                form.setChieuCaoCm(latest.getChieuCaoCm());
            }
            if (form.getCanNangKg() == null) {
                form.setCanNangKg(latest.getCanNangKg());
            }
            if (form.getBmi() == null) {
                form.setBmi(latest.getBmi());
            }
            if (form.getDuongHuyetMgdl() == null) {
                form.setDuongHuyetMgdl(latest.getDuongHuyetMgdl());
                if (form.getThoiDiemDoDuong() == null || form.getThoiDiemDoDuong().isBlank()) {
                    form.setThoiDiemDoDuong(latest.getThoiDiemDoDuong());
                }
            }
            if (form.getHba1cPercent() == null) {
                form.setHba1cPercent(latest.getHba1cPercent());
            }
            if (form.getHuyetApTamThu() == null) {
                form.setHuyetApTamThu(latest.getHuyetApTamThu());
            }
            if (form.getHuyetApTamTruong() == null) {
                form.setHuyetApTamTruong(latest.getHuyetApTamTruong());
            }
            if (form.getNhipTim() == null) {
                form.setNhipTim(latest.getNhipTim());
            }
            if (form.getNhietDoC() == null) {
                form.setNhietDoC(latest.getNhietDoC());
            }
            if (form.getNhipTho() == null) {
                form.setNhipTho(latest.getNhipTho());
            }
        }
        if (form.getTienSuBenh() == null || form.getTienSuBenh().isBlank()) {
            String tienSu = latest != null ? latest.getTienSuBenh() : null;
            if ((tienSu == null || tienSu.isBlank()) && patient != null) {
                tienSu = patient.getTienSuBenh();
            }
            if (tienSu != null && !tienSu.isBlank()) {
                form.setTienSuBenh(tienSu);
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
