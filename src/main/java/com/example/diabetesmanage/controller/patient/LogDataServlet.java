package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.HealthRecord;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogDataServlet", urlPatterns = {"/logData"})
public class LogDataServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String patientId = request.getParameter("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            PatientDAO patientDAO = new PatientDAO();
            patientId = patientDAO.getDemoPatientId();
        }

        String glucoseStr = request.getParameter("duong_huyet");
        String nhipTimStr = request.getParameter("nhip_tim");
        String haThuStr = request.getParameter("huyet_ap_thu");
        String haTruongStr = request.getParameter("huyet_ap_truong");
        String thoiDiem = request.getParameter("thoi_diem");
        String ghiChu = request.getParameter("ghi_chu");

        HealthRecord record = new HealthRecord();
        record.setPatientId(patientId);
        record.setThoiDiemDoDuong(thoiDiem);
        record.setGhiChu(ghiChu);
        
        try {
            if (glucoseStr != null && !glucoseStr.trim().isEmpty()) {
                record.setDuongHuyetMgdl(Double.parseDouble(glucoseStr));
            }
            if (nhipTimStr != null && !nhipTimStr.trim().isEmpty()) {
                record.setNhipTim(Integer.parseInt(nhipTimStr));
            }
            if (haThuStr != null && !haThuStr.trim().isEmpty()) {
                record.setHuyetApTamThu(Integer.parseInt(haThuStr));
            }
            if (haTruongStr != null && !haTruongStr.trim().isEmpty()) {
                record.setHuyetApTamTruong(Integer.parseInt(haTruongStr));
            }
            
            HealthRecordDAO dao = new HealthRecordDAO();
            dao.insertHealthRecord(record);
            
            response.sendRedirect(request.getContextPath() + "/patient-dashboard");
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/patient-dashboard?error=InvalidInput");
        }
    }
}
