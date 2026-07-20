package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.service.PatientDetailService;
import com.example.diabetesmanage.service.PatientDetailService.DetailBundle;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/doctor/patient-list")
public class PatientListController extends HttpServlet {

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PatientDAO patientDAO = new PatientDAO();
    private final PatientDetailService patientDetailService = new PatientDetailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId != null && !patientId.isBlank()) {
            forwardPatientDetail(request, response, user);
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String keyword = request.getParameter("keyword");
        String glucose = request.getParameter("glucose");
        String hba1c = request.getParameter("hba1c");
        String bmi = request.getParameter("bmi");
        String bloodPressure = request.getParameter("bloodPressure");
        String age = request.getParameter("age");
        String gender = request.getParameter("gender");
        String diabetesType = request.getParameter("diabetesType");
        String action = request.getParameter("action");

        List<Patient> patients =
                patientDAO.searchPatients(keyword, glucose, hba1c, bmi,
                        bloodPressure, age, gender, diabetesType, action, scopeDoctorId);

        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patients", patients);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientmanagement.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }

        forwardPatientDetail(request, response, user);
    }

    private void forwardPatientDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        String patientId = request.getParameter("id").trim();
        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }

        LocalDate fromDate = parseDateParam(request.getParameter("fromDate"));
        LocalDate toDate = parseDateParam(request.getParameter("toDate"));
        String fromRaw = request.getParameter("fromDate");
        String toRaw = request.getParameter("toDate");
        boolean fromBlank = fromRaw == null || fromRaw.isBlank();
        boolean toBlank = toRaw == null || toRaw.isBlank();
        String historyFilterError = null;

        if (fromBlank != toBlank) {
            historyFilterError = "Vui lòng chọn đủ từ ngày và đến ngày.";
            if (!fromBlank) {
                request.setAttribute("fromDate", fromRaw.trim());
            }
            if (!toBlank) {
                request.setAttribute("toDate", toRaw.trim());
            }
            fromDate = null;
            toDate = null;
        } else if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            historyFilterError =
                    "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.";
            request.setAttribute("fromDate", fromDate.toString());
            request.setAttribute("toDate", toDate.toString());
            fromDate = null;
            toDate = null;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        DetailBundle bundle = patientDetailService.load(patientId, scopeDoctorId, fromDate, toDate);
        Patient patient = bundle.patient;

        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patient", patient);
        request.setAttribute("encounter", bundle.encounter);
        request.setAttribute("healthRecord", bundle.healthRecord);
        request.setAttribute("hasHealthRecord", bundle.healthRecord != null);
        request.setAttribute("history", bundle.history);
        request.setAttribute("currentUser", user);

        LocalDate today = LocalDate.now();
        request.setAttribute("historyToday", today.toString());
        request.setAttribute("historyQuick5From", quickRangeFrom(today, 5).toString());
        request.setAttribute("historyQuick10From", quickRangeFrom(today, 10).toString());
        request.setAttribute("historyQuick30From", quickRangeFrom(today, 30).toString());

        String activeQuickRange = resolveActiveQuickRange(fromDate, toDate);
        request.setAttribute("activeQuickRange", activeQuickRange);
        request.setAttribute("historyDateLabel", resolveHistoryDateLabel(fromDate, toDate, activeQuickRange));

        if (fromDate != null && toDate != null) {
            request.setAttribute("fromDate", fromDate.toString());
            request.setAttribute("toDate", toDate.toString());
        }
        if (historyFilterError != null) {
            request.setAttribute("historyFilterError", historyFilterError);
            request.setAttribute("activeQuickRange", "custom");
        }

        request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                .forward(request, response);
    }

    private LocalDate parseDateParam(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Ngày bắt đầu cho quick filter (bao gồm cả hôm nay).
     * Ví dụ 5 ngày: today-4 .. today.
     */
    static LocalDate quickRangeFrom(LocalDate today, int inclusiveDays) {
        if (today == null || inclusiveDays < 1) {
            return today;
        }
        return today.minusDays(inclusiveDays - 1L);
    }

    /**
     * Xác định option dropdown đang active: 5, 10, 30, all, hoặc custom.
     */
    private String resolveActiveQuickRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return "all";
        }
        LocalDate today = LocalDate.now();
        if (!toDate.equals(today)) {
            return "custom";
        }
        long inclusiveDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (inclusiveDays == 5) {
            return "5";
        }
        if (inclusiveDays == 10) {
            return "10";
        }
        if (inclusiveDays == 30) {
            return "30";
        }
        return "custom";
    }

    private String resolveHistoryDateLabel(LocalDate fromDate, LocalDate toDate, String activeQuickRange) {
        if (fromDate == null || toDate == null || "all".equals(activeQuickRange)) {
            return "Tất cả lịch sử";
        }
        switch (activeQuickRange) {
            case "5":
                return "5 ngày gần nhất";
            case "10":
                return "10 ngày gần nhất";
            case "30":
                return "30 ngày gần nhất";
            default:
                return fromDate.format(DISPLAY_DATE) + " - " + toDate.format(DISPLAY_DATE);
        }
    }
}
