package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.medical.EncounterCreateRequest;
import com.example.diabetesmanage.service.medical.MedicalEncounterCreateService;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/medical-encounters/add")
public class AddMedicalEncounterServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AddMedicalEncounterServlet.class.getName());

    private final PatientDAO patientDAO = new PatientDAO();
    private final MedicalEncounterCreateService createService = new MedicalEncounterCreateService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User doctor = AuthContext.requireDoctor(request, response);
        if (doctor == null) {
            return;
        }

        prepareForm(request, doctor, new EncounterCreateRequest());
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

        EncounterCreateRequest form;
        try {
            form = EncounterCreateRequest.fromRequest(request);
            createService.normalizeEndocrinePayload(request, form);
            createService.logEndocrinePayload("http-request", form);
        } catch (NumberFormatException ex) {
            form = new EncounterCreateRequest();
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

        if (doctor.getId() == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Khong xac dinh duoc bac si dang nhap.");
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        String doctorUuid = doctor.getId().toString();
        LOG.log(Level.INFO, "POST medical-encounters/add (continue) patient_id={0} bac_si_id={1}",
                new Object[]{form.getPatientId(), doctorUuid});

        if (!AuthContext.ensurePatientAccess(doctor, patientDAO, form.getPatientId(), response)) {
            return;
        }

        // Bước "Tiếp tục kê đơn": validate nhẹ Bước 1 (chẩn đoán/đơn thuốc để sang Bước 2).
        List<String> errors = createService.validateStep1(form);
        if (!errors.isEmpty()) {
            forwardWithErrors(request, response, form, errors, doctor);
            return;
        }

        try {
            // chan_doan_chinh là NOT NULL nhưng chẩn đoán nhập ở Bước 2 → đặt placeholder.
            createService.ensureDiagnosisPlaceholder(form);

            MedicalEncounterCreateService.CreateResult result =
                    createService.create(form, doctorUuid);
            if (result.getEncounterId() == null || result.getEncounterId().isBlank()) {
                throw new SQLException("Encounter id is empty after create");
            }
            if (!createService.isEncounterPersisted(result.getEncounterId())) {
                throw new SQLException("Encounter not found in database after commit id="
                        + result.getEncounterId());
            }
            MedicalEncounter persisted = createService.loadPersistedEncounter(
                    result.getEncounterId(), doctorUuid);
            if (persisted == null) {
                throw new SQLException("Cannot reload encounter from database id="
                        + result.getEncounterId());
            }
            String encounterId = result.getEncounterId();
            LOG.log(Level.INFO,
                    "Created medical_encounter (step1) id={0} patient_id={1} bac_si_id={2} type={3}",
                    new Object[]{
                            persisted.getId(),
                            persisted.getPatientId(),
                            doctorUuid,
                            persisted.getEncounterTypeLabel()
                    });

            // Chỉ Bệnh án tái khám Nội tiết mới đi tiếp sang Bước 2 (kê đơn).
            // Hồ sơ xét nghiệm (máu tổng quát / sinh hóa) chỉ lưu Encounter + Lab Result rồi về danh sách.
            if (form.resolveEncounterType().isTaiKhamNoiTiet()) {
                // Giữ tạm AI Summary trong session để Bước 2 hiển thị readonly (không có bảng ai_analysis).
                String aiSummary = request.getParameter("aiSummary");
                if (aiSummary != null && !aiSummary.isBlank()) {
                    request.getSession(true).setAttribute("aiSummary:" + encounterId, aiSummary.trim());
                }
                // PRG: redirect sang Bước 2 → F5 an toàn, không tạo encounter trùng.
                response.sendRedirect(request.getContextPath()
                        + "/doctor/treatment-plan?id=" + encounterId);
            } else {
                // PRG: lưu xong về danh sách hồ sơ.
                response.sendRedirect(request.getContextPath()
                        + "/doctor/patient-records?success=1&patientId=" + form.getPatientId());
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE,
                    "Failed to save medical encounter patient_id=" + form.getPatientId()
                            + " bac_si_id=" + doctorUuid, ex);
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
                                   EncounterCreateRequest form, List<String> errors, User doctor)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        prepareForm(request, doctor, form);
        forwardForm(request, response);
    }

    private void prepareForm(HttpServletRequest request, User doctor, EncounterCreateRequest form) {
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

    private void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/doctor/add-medical-encounter.jsp")
                .forward(request, response);
    }
}
