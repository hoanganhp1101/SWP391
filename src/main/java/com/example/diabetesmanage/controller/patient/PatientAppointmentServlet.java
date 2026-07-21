package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.PatientPortalAuth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PatientAppointmentServlet", urlPatterns = {"/patient-appointments"})
public class PatientAppointmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        PatientDAO patientDAO = new PatientDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        Patient patientInfo = patientDAO.getPatientById(patientId);
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
        List<User> doctors = appointmentDAO.getAvailableDoctors();

        request.setAttribute("patientInfo", patientInfo);
        request.setAttribute("appointments", appointments);
        request.setAttribute("doctors", doctors);

        request.getRequestDispatcher("/WEB-INF/views/patient/patient-appointments.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        String action = trimToNull(request.getParameter("action"));
        String appointmentId = trimToNull(request.getParameter("appointmentId"));
        boolean isUpdate = "update".equals(action);
        String tieuDe = trimToNull(request.getParameter("tieuDe"));
        String bacSiId = trimToNull(request.getParameter("bacSiId"));
        String thoiGianHenRaw = trimToNull(request.getParameter("thoiGianHen"));
        String diaDiem = trimToNull(request.getParameter("diaDiem"));

        if (isUpdate && appointmentId == null) {
            redirectWithError(response, "Không tìm thấy lịch khám cần thay đổi.");
            return;
        }

        if (tieuDe == null || thoiGianHenRaw == null || diaDiem == null) {
            redirectWithError(response, "Vui lòng nhập đầy đủ tiêu đề, thời gian hẹn và địa điểm.");
            return;
        }

        Timestamp thoiGianHen;
        try {
            LocalDateTime appointmentTime = LocalDateTime.parse(thoiGianHenRaw);
            if (appointmentTime.isBefore(LocalDateTime.now())) {
                redirectWithError(response, "Thời gian hẹn phải ở hiện tại hoặc tương lai.");
                return;
            }
            thoiGianHen = Timestamp.valueOf(appointmentTime);
        } catch (IllegalArgumentException e) {
            redirectWithError(response, "Thời gian hẹn không hợp lệ.");
            return;
        }

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatientId(patientId);
        appointment.setBacSiId(bacSiId);
        appointment.setTieuDe(tieuDe);
        appointment.setThoiGianHen(thoiGianHen);
        appointment.setDiaDiem(diaDiem);

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        boolean success = isUpdate
                ? appointmentDAO.updateAppointment(appointment)
                : appointmentDAO.createAppointment(appointment);
        if (success) {
            response.sendRedirect("patient-appointments?" + (isUpdate ? "appointmentUpdated=1" : "appointmentCreated=1"));
        } else {
            redirectWithError(response, isUpdate
                    ? "Không thể thay đổi lịch khám. Chỉ lịch đang chờ khám mới được thay đổi."
                    : "Không thể đặt lịch khám. Vui lòng thử lại.");
        }
    }

    private void redirectWithError(HttpServletResponse response, String errorMessage) throws IOException {
        response.sendRedirect("patient-appointments?appointmentCreated=0&error=" +
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
