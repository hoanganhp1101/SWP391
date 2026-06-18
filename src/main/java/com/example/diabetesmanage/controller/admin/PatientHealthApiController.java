package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet(name = "PatientHealthApiController", urlPatterns = {"/api/patient/health-records"})
public class PatientHealthApiController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String patientId = request.getParameter("patientId");
        PrintWriter out = response.getWriter();

        if (patientId == null || patientId.trim().isEmpty()) {
            out.print("[]");
            return;
        }

        HealthRecordDAO recordDAO = new HealthRecordDAO();
        List<HealthRecord> records = recordDAO.getAllRecordsForChart(patientId);

        StringBuilder json = new StringBuilder();
        json.append("[");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");

        for (int i = 0; i < records.size(); i++) {
            HealthRecord r = records.get(i);
            if (r.getDuongHuyetMgdl() == null) continue;

            json.append("{");
            json.append("\"duongHuyet\":").append(r.getDuongHuyetMgdl()).append(",");
            json.append("\"thoiGian\":\"").append(r.getThoiGianDo() != null ? sdf.format(r.getThoiGianDo()) : "N/A").append("\"");
            json.append("}");

            if (i < records.size() - 1) {
                json.append(",");
            }
        }

        String resJson = json.toString();
        if (resJson.endsWith(",")) {
            resJson = resJson.substring(0, resJson.length() - 1);
        }
        resJson += "]";

        out.print(resJson);
        out.flush();
    }
}