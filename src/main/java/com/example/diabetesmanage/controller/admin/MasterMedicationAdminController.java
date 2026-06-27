package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.MasterMedicationDAO;
import com.example.diabetesmanage.model.MasterMedication;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MasterMedicationController", urlPatterns = {"/admin/master-medications"})
public class MasterMedicationAdminController extends HttpServlet {

    private MasterMedicationDAO masterMedicationDAO;

    @Override
    public void init() throws ServletException {
        masterMedicationDAO = new MasterMedicationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy danh sách thuốc từ DAO và gửi sang JSP
        List<MasterMedication> medList = masterMedicationDAO.getAllMedications();
        request.setAttribute("medList", medList);
        request.getRequestDispatcher("/WEB-INF/views/admin/master-medication-manager.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/admin/master-medications");
            return;
        }

        try {
            if (action.equals("delete")) {
                String id = request.getParameter("id");
                masterMedicationDAO.deleteMedication(id);
            }
            else if (action.equals("add") || action.equals("update")) {
                MasterMedication med = new MasterMedication();
                med.setTenThuoc(request.getParameter("tenThuoc"));
                med.setHoatChat(request.getParameter("hoatChat"));
                med.setDonViTinh(request.getParameter("donViTinh"));
                med.setLoaiThuoc(request.getParameter("loaiThuoc"));
                med.setHuongDanGoc(request.getParameter("huongDanGoc"));

                // Trạng thái từ checkbox (true/on = Hoạt động, false/null = Tạm khóa)
                String trangThaiStr = request.getParameter("trangThai");
                med.setTrangThai("true".equalsIgnoreCase(trangThaiStr) || "on".equalsIgnoreCase(trangThaiStr));

                if (action.equals("add")) {
                    masterMedicationDAO.addMedication(med);
                } else {
                    med.setId(request.getParameter("id"));
                    masterMedicationDAO.updateMedication(med);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect về trang danh sách thuốc
        response.sendRedirect(request.getContextPath() + "/admin/master-medications");
    }
}