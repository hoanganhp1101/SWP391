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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/doctor/patient-list")
public class PatientListController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PatientListController.class.getName());
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

        consumeFlash(request);
        applyQueryError(request);

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
        if (patientId == null || patientId.isBlank()) {
            setFlash(request, "error", "Thiếu mã bệnh nhân.");
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }
        forwardPatientDetail(request, response, user);
    }

    private void forwardPatientDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        String patientId = request.getParameter("id").trim();

        try {
            if (!patientDAO.exists(patientId)) {
                response.sendRedirect(request.getContextPath() + "/doctor/patient-list?error=notfound");
                return;
            }
            if (AuthContext.isDoctor(user) && user.getId() != null
                    && !patientDAO.isAssignedToDoctor(patientId, user.getId())) {
                response.sendRedirect(request.getContextPath() + "/doctor/patient-list?error=forbidden");
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
                fromDate = null;
                toDate = null;
            } else if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                historyFilterError = "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.";
                fromDate = null;
                toDate = null;
            }

            // Không scope theo bác sĩ khi load chi tiết — đã check quyền ở trên
            DetailBundle bundle = patientDetailService.load(patientId, null, fromDate, toDate);
            Patient patient = bundle != null ? bundle.patient : null;
            if (patient == null) {
                response.sendRedirect(request.getContextPath() + "/doctor/patient-list?error=load");
                return;
            }

            DoctorLayoutHelper.prepare(request, user, "patients");
            request.setAttribute("patient", patient);
            request.setAttribute("encounter", bundle.encounter);
            request.setAttribute("healthRecord", bundle.healthRecord);
            request.setAttribute("hasHealthRecord", bundle.healthRecord != null);
            request.setAttribute("history",
                    bundle.history != null ? bundle.history : Collections.emptyList());
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
            } else {
                if (!fromBlank) {
                    request.setAttribute("fromDate", fromRaw.trim());
                }
                if (!toBlank) {
                    request.setAttribute("toDate", toRaw.trim());
                }
            }
            if (historyFilterError != null) {
                request.setAttribute("historyFilterError", historyFilterError);
                request.setAttribute("activeQuickRange", "custom");
            }

            if (patient.getNgaySinh() != null) {
                request.setAttribute("ngaySinhIso", patient.getNgaySinh().toString());
            }
            if (patient.getNgayChanDoanTieuDuong() != null) {
                request.setAttribute("ngayChanDoanIso", patient.getNgayChanDoanTieuDuong().toString());
            }
            if (patient.getNgayCapNhat() != null) {
                request.setAttribute("ngayCapNhatDisplay",
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(patient.getNgayCapNhat()));
            }

            request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Lỗi mở chi tiết bệnh nhân id=" + patientId, e);
            // Xóa dữ liệu hồ sơ để trang lỗi không render lại đoạn JSP bị lỗi
            request.removeAttribute("healthRecord");
            request.removeAttribute("hasHealthRecord");
            request.removeAttribute("history");
            request.removeAttribute("encounter");
            request.removeAttribute("patient");
            request.setAttribute("detailError",
                    "Không thể mở chi tiết bệnh nhân: " + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? " — " + e.getMessage() : ""));
            if (!response.isCommitted()) {
                DoctorLayoutHelper.prepare(request, user, "patients");
                request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                        .forward(request, response);
            }
        }
    }

    private void applyQueryError(HttpServletRequest request) {
        String error = request.getParameter("error");
        if (error == null || error.isBlank()) {
            return;
        }
        switch (error) {
            case "missing" -> request.setAttribute("flashError", "Thiếu mã bệnh nhân.");
            case "notfound" -> request.setAttribute("flashError", "Không tìm thấy bệnh nhân.");
            case "forbidden" -> request.setAttribute("flashError", "Bạn không có quyền xem bệnh nhân này.");
            case "load" -> request.setAttribute("flashError", "Không tải được hồ sơ bệnh nhân.");
            default -> request.setAttribute("flashError", "Không mở được chi tiết bệnh nhân.");
        }
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

    static LocalDate quickRangeFrom(LocalDate today, int inclusiveDays) {
        if (today == null || inclusiveDays < 1) {
            return today;
        }
        return today.minusDays(inclusiveDays - 1L);
    }

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
