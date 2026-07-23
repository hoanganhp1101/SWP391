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
        HttpSession session = request.getSession(false);
        User loginUser = null;
        if (session != null) {
            Object value = session.getAttribute("loginUser");
            if (!(value instanceof User)) {
                value = session.getAttribute("adminUser");
            }
            if (!(value instanceof User)) {
                value = session.getAttribute("user");
            }
            if (value instanceof User) {
                loginUser = (User) value;
            }
        }
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String patientId = request.getParameter("patientId");
        String ghiChu = request.getParameter("ghiChu");

        // Nhận mảng dữ liệu từ form do người dùng có thể thêm nhiều loại thuốc
        String[] medicationIds = request.getParameterValues("medicationId[]");
        String[] lieuLuongs = request.getParameterValues("lieuLuong[]");
        String[] tanSuats = request.getParameterValues("tanSuat[]");

        if (medicationIds != null && medicationIds.length > 0) {
            Prescription prescription = new Prescription();
            prescription.setPatientId(patientId);
            prescription.setBacSiId(loginUser.getId());
            if (ghiChu != null) {
                prescription.setGhiChu(ghiChu.trim());
            }

            List<PrescriptionDetail> details = buildPrescriptionDetails(medicationIds, lieuLuongs, tanSuats);
            if (!details.isEmpty()) {
                prescriptionDAO.createPrescription(prescription, details);
            }
        }

        // Kê xong thì quay lại danh sách bệnh nhân
        response.sendRedirect(request.getContextPath() + "/patient-manager");
    }

    /**
     * Ghép các mảng form thành danh sách chi tiết đơn; bỏ dòng thiếu/blank, cắt theo độ dài chung.
     */
    List<PrescriptionDetail> buildPrescriptionDetails(String[] medicationIds, String[] lieuLuongs, String[] tanSuats) {
        List<PrescriptionDetail> details = new ArrayList<>();
        if (medicationIds == null || lieuLuongs == null || tanSuats == null) {
            return details;
        }
        int n = Math.min(medicationIds.length, Math.min(lieuLuongs.length, tanSuats.length));
        for (int i = 0; i < n; i++) {
            String medId = medicationIds[i] == null ? "" : medicationIds[i].trim();
            String lieuLuong = lieuLuongs[i] == null ? "" : lieuLuongs[i].trim();
            String tanSuat = tanSuats[i] == null ? "" : tanSuats[i].trim();
            if (medId.isEmpty() || lieuLuong.isEmpty() || tanSuat.isEmpty()) {
                continue;
            }
            PrescriptionDetail detail = new PrescriptionDetail();
            detail.setMedicationId(medId);
            detail.setLieuLuong(lieuLuong);
            detail.setTanSuat(tanSuat);
            details.add(detail);
        }
        return details;
    }
}