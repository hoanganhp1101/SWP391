package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.AppointmentDAO;
import com.example.diabetesmanage.model.Appointment;

/**
 * Cập nhật trạng thái lịch hẹn. Không tự tạo Medical Encounter — encounter chỉ tạo qua form khám bệnh.
 */
public class AppointmentStatusService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public boolean cancel(String appointmentId, String scopeDoctorId) {
        return appointmentDAO.updateStatus(appointmentId, Appointment.STATUS_HUY, scopeDoctorId);
    }

    /**
     * Đánh dấu đã khám: chỉ cập nhật trạng thái appointment.
     */
    public boolean markCompleted(String appointmentId, String scopeDoctorId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            return false;
        }

        Appointment appt = appointmentDAO.findById(appointmentId, scopeDoctorId);
        if (appt == null || !Appointment.STATUS_CHO_KHAM.equals(appt.getTrangThai())) {
            return false;
        }

        return appointmentDAO.updateStatus(appointmentId, Appointment.STATUS_DA_KHAM, scopeDoctorId);
    }
}
