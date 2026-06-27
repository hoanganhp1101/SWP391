package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.MasterFoodDAO;
import com.example.diabetesmanage.model.MasterFood;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MasterFoodController", urlPatterns = {"/admin/master-foods"})
public class MasterFoodAdminController extends HttpServlet {

    private MasterFoodDAO masterFoodDAO;

    @Override
    public void init() throws ServletException {
        masterFoodDAO = new MasterFoodDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy danh sách thực phẩm từ DAO và gửi sang JSP
        List<MasterFood> foodList = masterFoodDAO.getAllFoods();
        request.setAttribute("foodList", foodList);
        request.getRequestDispatcher("/WEB-INF/views/admin/master-food-manager.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/admin/master-foods");
            return;
        }

        try {
            if (action.equals("delete")) {
                String id = request.getParameter("id");
                masterFoodDAO.deleteFood(id);
            }
            else if (action.equals("add") || action.equals("update")) {
                MasterFood food = new MasterFood();
                food.setTenThucPham(request.getParameter("tenThucPham"));
                food.setDonViKhauPhan(request.getParameter("donViKhauPhan"));

                // Ép kiểu các giá trị số và kiểm tra null
                food.setCarbsG(Double.parseDouble(request.getParameter("carbsG")));

                String caloStr = request.getParameter("caloKcal");
                food.setCaloKcal((caloStr != null && !caloStr.isEmpty()) ? Double.parseDouble(caloStr) : null);

                String giStr = request.getParameter("chiSoGI");
                food.setChiSoGI((giStr != null && !giStr.isEmpty()) ? Double.parseDouble(giStr) : null);

                // Trạng thái thường truyền lên từ checkbox (nếu có check thì là "true" hoặc "on")
                String trangThaiStr = request.getParameter("trangThai");
                food.setTrangThai("true".equalsIgnoreCase(trangThaiStr) || "on".equalsIgnoreCase(trangThaiStr));

                if (action.equals("add")) {
                    masterFoodDAO.addFood(food);
                } else {
                    food.setId(request.getParameter("id"));
                    masterFoodDAO.updateFood(food);
                }
            }
        } catch (NumberFormatException e) {
            // Log lỗi nếu định dạng số không hợp lệ
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sau khi xử lý xong (Thêm/Sửa/Xóa), redirect về trang danh sách để tải lại dữ liệu mới nhất
        response.sendRedirect(request.getContextPath() + "/admin/master-foods");
    }
}