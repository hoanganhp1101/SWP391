package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.Prescription;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "PatientController", urlPatterns = {"/patient-manager"})
public class PatientAdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        PatientDAO patientDAO = new PatientDAO();
        PrescriptionDAO prescriptionDAO = new PrescriptionDAO(); // Khởi tạo DAO thuốc ở đây

        if (action != null && action.equals("view")) {
            try {
                String id = request.getParameter("id");

                // 1. Lấy thông tin cơ bản của bệnh nhân
                Patient patient = patientDAO.getPatientByIdAdmin(id);
                request.setAttribute("patient", patient);

                // 2. ĐỔ DỮ LIỆU ĐƠN THUỐC: Lấy lịch sử đơn thuốc (bao gồm cả mảng details bên trong)
                List<Prescription> prescriptionList = prescriptionDAO.getPrescriptionsForPatient(id);
                request.setAttribute("prescriptionList", prescriptionList);

                // Chuyển tiếp sang trang chi tiết hồ sơ
                request.getRequestDispatcher("/WEB-INF/views/admin/patient-detail.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/patient-manager");
            }
        } else {
            // Hiển thị danh sách bệnh nhân mặc định
            List<Patient> patientList = patientDAO.getAllPatients();
            request.setAttribute("patientList", patientList);
            request.getRequestDispatcher("/WEB-INF/views/admin/patient-manager.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        PatientDAO patientDAO = new PatientDAO();

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/patient-manager");
            return;
        }

        try {
            if (action.equals("delete")) {
                String id = request.getParameter("id");
                patientDAO.deletePatient(id);
            } else if (action.equals("update")) {
                Patient p = new Patient();
                p.setId(request.getParameter("id"));
                p.setTenBenhNhan(request.getParameter("hoTen"));
                p.setEmail(request.getParameter("email"));
                p.setSoDienThoai(request.getParameter("soDienThoai"));
                p.setLoaiTieuDuong(request.getParameter("loaiTieuDuong"));

                String ngaySinhStr = request.getParameter("ngaySinh");
                if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                    p.setNgaySinh(Date.valueOf(ngaySinhStr));
                }
                patientDAO.updatePatient(p);
            }
            // Không tạo tài khoản bệnh nhân ở đây — chỉ đăng ký từ trang login/register.
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/patient-manager");
    }
}