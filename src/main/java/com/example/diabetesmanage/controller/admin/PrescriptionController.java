package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.MasterMedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MasterMedication;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;
import com.example.diabetesmanage.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PrescriptionController", urlPatterns = {"/admin/prescribe"})
public class PrescriptionController extends HttpServlet {

    private MasterMedicationDAO medicationDAO;
    private PrescriptionDAO prescriptionDAO;
    private PatientDAO patientDAO; // Giả định bạn đã có hàm getPatientById()

    @Override
    public void init() throws ServletException {
        medicationDAO = new MasterMedicationDAO();
        prescriptionDAO = new PrescriptionDAO();
        patientDAO = new PatientDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String patientId = request.getParameter("patientId");

        // Cần truyền thông tin bệnh nhân và danh sách thuốc sang giao diện
        // Patient patient = patientDAO.getPatientById(patientId); // Nếu bạn có hàm này
        request.setAttribute("patientId", patientId);

        List<MasterMedication> medList = medicationDAO.getAllMedications();
        request.setAttribute("medList", medList);

        request.getRequestDispatcher("/WEB-INF/views/admin/prescription-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser"); // Lấy admin/bác sĩ đang đăng nhập

        String patientId = request.getParameter("patientId");
        String ghiChu = request.getParameter("ghiChu");

        // Nhận mảng dữ liệu từ form do người dùng có thể thêm nhiều loại thuốc
        String[] medicationIds = request.getParameterValues("medicationId[]");
        String[] lieuLuongs = request.getParameterValues("lieuLuong[]");
        String[] tanSuats = request.getParameterValues("tanSuat[]");

        if (medicationIds != null && medicationIds.length > 0) {
            Prescription prescription = new Prescription();
            prescription.setPatientId(patientId);
            prescription.setBacSiId(loginUser != null ? loginUser.getId() : "admin-id");

            List<PrescriptionDetail> details = new ArrayList<>();
            for (int i = 0; i < medicationIds.length; i++) {
                if (medicationIds[i] != null && !medicationIds[i].trim().isEmpty()) {
                    PrescriptionDetail detail = new PrescriptionDetail();
                    detail.setMedicationId(medicationIds[i]);
                    detail.setLieuLuong(lieuLuongs[i]);
                    detail.setTanSuat(tanSuats[i]);
                    details.add(detail);
                }
            }

            prescriptionDAO.createPrescription(prescription, details);
        }

        // Kê xong thì quay lại danh sách bệnh nhân
        response.sendRedirect(request.getContextPath() + "/patient-manager");
    }
}