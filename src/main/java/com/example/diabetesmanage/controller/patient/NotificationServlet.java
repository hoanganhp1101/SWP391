package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.MedicationLog;
import com.example.diabetesmanage.util.PatientPortalAuth;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/api/notifications"})
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        JsonArray notifications = new JsonArray();
        List<JsonObject> notifList = new java.util.ArrayList<>();

        // 1. Lấy thông báo cảnh báo AI (Alerts)
        AlertDAO alertDAO = new AlertDAO();
        List<Alert> alerts = alertDAO.getRecentAlerts(patientId);
        if (alerts != null) {
            for (Alert alert : alerts) {
                JsonObject notif = new JsonObject();
                notif.addProperty("id", "alert_" + alert.getId());
                notif.addProperty("type", "ai-alert");
                notif.addProperty("title", alert.getTieuDe() != null ? alert.getTieuDe() : "Cảnh báo sức khỏe");
                notif.addProperty("message", alert.getNoiDung() != null ? alert.getNoiDung() : "");
                notif.addProperty("time", alert.getThoiGianTao() != null ? alert.getThoiGianTao().toString() : "");
                notif.addProperty("icon", "fas fa-exclamation-triangle");
                notif.addProperty("color", "var(--danger)");
                notif.addProperty("bgColor", "var(--danger-light)");
                notif.addProperty("link", "patient-dashboard");
                notif.addProperty("sortTime", alert.getThoiGianTao() != null ? alert.getThoiGianTao().getTime() : 0L);
                notifList.add(notif);
            }
        }
        
        // 2. Lấy danh sách thuốc chưa uống trong ngày
        MedicationLogDAO logDAO = new MedicationLogDAO();
        List<MedicationLog> checklist = logDAO.getChecklistByDate(patientId, Date.valueOf(LocalDate.now()));
        if (checklist != null) {
            for (MedicationLog log : checklist) {
                if (!"da_uong".equals(log.getTrangThai())) {
                    JsonObject notif = new JsonObject();
                    notif.addProperty("id", "med_" + log.getMedicationId() + "_" + LocalDate.now().toString());
                    notif.addProperty("type", "med-reminder");
                    notif.addProperty("title", "Đến giờ uống thuốc!");
                    notif.addProperty("message", "Bạn chưa uống " + log.getTenThuoc() + " (" + log.getLieuLuong() + " " + log.getDonVi() + ").");
                    notif.addProperty("time", "Hôm nay");
                    notif.addProperty("icon", "fas fa-pills");
                    notif.addProperty("color", "var(--primary)");
                    notif.addProperty("bgColor", "var(--primary-light)");
                    notif.addProperty("link", "patient-prescriptions");
                    notif.addProperty("sortTime", System.currentTimeMillis()); // Ưu tiên hiện trên cùng
                    notifList.add(notif);
                }
            }
        }
        
        // 3. Lấy lịch khám sắp tới
        AppointmentDAO apptDAO = new AppointmentDAO();
        List<Appointment> appts = apptDAO.getUpcomingAppointments(patientId);
        if (appts != null) {
            for (Appointment appt : appts) {
                JsonObject notif = new JsonObject();
                notif.addProperty("id", "appt_" + appt.getId());
                notif.addProperty("type", "appointment");
                notif.addProperty("title", "Lịch tái khám sắp tới");
                notif.addProperty("message", (appt.getTieuDe() != null ? appt.getTieuDe() : "Khám định kỳ") + 
                                             " vào lúc " + (appt.getThoiGianHen() != null ? appt.getThoiGianHen().toString() : ""));
                notif.addProperty("time", appt.getThoiGianHen() != null ? appt.getThoiGianHen().toString() : "");
                notif.addProperty("icon", "fas fa-calendar-check");
                notif.addProperty("color", "var(--success)");
                notif.addProperty("bgColor", "var(--success-light)");
                notif.addProperty("link", "#");
                // Dùng thời gian hẹn làm mốc sắp xếp (nếu trong tương lai thì sẽ lên đầu)
                notif.addProperty("sortTime", appt.getThoiGianHen() != null ? appt.getThoiGianHen().getTime() : 0L);
                notifList.add(notif);
            }
        }

        // Sắp xếp giảm dần theo thời gian (mới nhất ở trên)
        notifList.sort((o1, o2) -> Long.compare(o2.get("sortTime").getAsLong(), o1.get("sortTime").getAsLong()));

        for (JsonObject obj : notifList) {
            notifications.add(obj);
        }
        
        response.getWriter().write(notifications.toString());
    }
}
