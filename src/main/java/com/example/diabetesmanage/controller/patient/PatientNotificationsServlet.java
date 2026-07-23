package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.AlertDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Alert;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.PatientPortalAuth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.dao.MedicationLogDAO;
import com.example.diabetesmanage.model.Appointment;
import com.example.diabetesmanage.model.MedicationLog;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

@WebServlet(name = "PatientNotificationsServlet", urlPatterns = {"/patient-notifications"})
public class PatientNotificationsServlet extends HttpServlet {

    public static class NotificationItem {
        public String id;
        public String type; // alert, medication, appointment
        public String title;
        public String message;
        public String time;
        public String icon;
        public String colorClass; // e.g. danger, primary, success
        public long sortTime;

        public String getId() { return id; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getTime() { return time; }
        public String getIcon() { return icon; }
        public String getColorClass() { return colorClass; }
        public long getSortTime() { return sortTime; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Require authentication
        String patientId = PatientPortalAuth.requirePatientId(request, response);
        if (patientId == null) {
            return;
        }

        try {
            // Lấy thôngInitial bệnh nhân để hiển thị ở sidebar/header
            PatientDAO patientDAO = new PatientDAO();
            Patient patientInfo = patientDAO.getPatientById(patientId);
            request.setAttribute("patientInfo", patientInfo);

            List<NotificationItem> allNotifs = new ArrayList<>();

            // 1. Cảnh báo (Alerts)
            AlertDAO alertDAO = new AlertDAO();
            List<Alert> allAlerts = alertDAO.getAllAlerts(patientId);
            if (allAlerts != null) {
                for (Alert alert : allAlerts) {
                    NotificationItem item = new NotificationItem();
                    item.id = "alert_" + alert.getId();
                    item.type = "alert";
                    item.title = alert.getTieuDe() != null ? alert.getTieuDe() : "Cảnh báo sức khỏe";
                    item.message = alert.getNoiDung() != null ? alert.getNoiDung() : "";
                    item.time = alert.getThoiGianTao() != null ? alert.getThoiGianTao().toString().substring(0, 16) : "";
                    
                    if ("nguy_hiem".equals(alert.getMucDo()) || "cao".equals(alert.getMucDo())) {
                        item.icon = "fas fa-exclamation-triangle";
                        item.colorClass = "danger";
                    } else if ("trung_binh".equals(alert.getMucDo())) {
                        item.icon = "fas fa-exclamation-circle";
                        item.colorClass = "warning";
                    } else {
                        item.icon = "fas fa-info-circle";
                        item.colorClass = "primary";
                    }
                    
                    item.sortTime = alert.getThoiGianTao() != null ? alert.getThoiGianTao().getTime() : 0L;
                    allNotifs.add(item);
                }
            }

            // 2. Thuốc chưa uống trong ngày
            MedicationLogDAO logDAO = new MedicationLogDAO();
            List<MedicationLog> checklist = logDAO.getChecklistByDate(patientId, Date.valueOf(LocalDate.now()));
            if (checklist != null) {
                List<String> listThuocChuaUong = new ArrayList<>();
                for (MedicationLog log : checklist) {
                    if (!"da_uong".equals(log.getTrangThai())) {
                        listThuocChuaUong.add(log.getTenThuoc() + " (" + log.getLieuLuong() + " " + log.getDonVi() + ")");
                    }
                }
                if (!listThuocChuaUong.isEmpty()) {
                    NotificationItem item = new NotificationItem();
                    item.id = "med_all_" + LocalDate.now().toString();
                    item.type = "medication";
                    item.title = "Đến giờ uống thuốc!";
                    item.message = "Bạn chưa uống các thuốc: " + String.join(", ", listThuocChuaUong) + ".";
                    item.time = "Hôm nay";
                    item.icon = "fas fa-pills";
                    item.colorClass = "primary";
                    item.sortTime = System.currentTimeMillis();
                    allNotifs.add(item);
                }
            }

            // 3. Lịch hẹn sắp tới
            AppointmentDAO apptDAO = new AppointmentDAO();
            List<Appointment> appts = apptDAO.getUpcomingAppointments(patientId);
            if (appts != null) {
                for (Appointment appt : appts) {
                    NotificationItem item = new NotificationItem();
                    item.id = "appt_" + appt.getId();
                    item.type = "appointment";
                    item.title = "Lịch tái khám sắp tới";
                    item.message = (appt.getTieuDe() != null ? appt.getTieuDe() : "Khám định kỳ") + 
                                   " vào lúc " + (appt.getThoiGianHen() != null ? appt.getThoiGianHen().toString().substring(0, 16) : "");
                    item.time = appt.getThoiGianHen() != null ? appt.getThoiGianHen().toString().substring(0, 16) : "";
                    item.icon = "fas fa-calendar-check";
                    item.colorClass = "success";
                    item.sortTime = appt.getThoiGianHen() != null ? appt.getThoiGianHen().getTime() : 0L;
                    allNotifs.add(item);
                }
            }

            // Sắp xếp
            allNotifs.sort((o1, o2) -> Long.compare(o2.sortTime, o1.sortTime));
            
            request.setAttribute("allNotifs", allNotifs);

            request.getRequestDispatcher("/WEB-INF/views/patient/patient-notifications.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi tải dữ liệu.");
        }
    }
}
