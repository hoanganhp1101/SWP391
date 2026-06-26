package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/export-patients")
public class ExportPatientController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String keyword = request.getParameter("keyword");
        String risk = request.getParameter("risk");

        List<Patient> patients = patientDAO.searchPatients(keyword, risk, scopeDoctorId);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader("Content-Disposition", "attachment; filename=patients.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patients");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Patient Code");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Age");
            header.createCell(4).setCellValue("Gender");
            header.createCell(5).setCellValue("Diabetes Type");

            int rowNum = 1;
            for (Patient p : patients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getPatientCode());
                row.createCell(1).setCellValue(p.getUser().getHoTen());
                row.createCell(2).setCellValue(p.getUser().getEmail());
                row.createCell(3).setCellValue(p.getTuoi());
                row.createCell(4).setCellValue(p.getGioiTinh());
                row.createCell(5).setCellValue(p.getLoaiTieuDuong());
            }

            workbook.write(response.getOutputStream());
        }
    }
}
