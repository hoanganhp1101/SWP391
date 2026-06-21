package controller.doctor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PatientRecordsServlet", urlPatterns = {"/doctor/patient-records"})
public class PatientRecordsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientId = request.getParameter("patientId");
        request.setAttribute("patientId", patientId == null ? "" : patientId.trim());
        request.getRequestDispatcher("/WEB-INF/views/doctor/patient-records.jsp").forward(request, response);
    }
}
