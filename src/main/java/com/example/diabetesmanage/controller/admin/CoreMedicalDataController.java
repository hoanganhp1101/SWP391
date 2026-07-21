package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.MasterFoodDAO;
import com.example.diabetesmanage.dao.MasterMedicationDAO;
import com.example.diabetesmanage.dao.EducationalContentDAO;
import com.example.diabetesmanage.model.MasterFood;
import com.example.diabetesmanage.model.MasterMedication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@WebServlet(name = "CoreMedicalDataController", urlPatterns = {"/admin/core-medical-data"})
public class CoreMedicalDataController extends HttpServlet {

    private MasterMedicationDAO medicationDAO;
    private MasterFoodDAO foodDAO;
    private EducationalContentDAO educationalContentDAO;

    @Override
    public void init() throws ServletException {
        medicationDAO = new MasterMedicationDAO();
        foodDAO = new MasterFoodDAO();
        educationalContentDAO = new EducationalContentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<MasterMedication> medications = medicationDAO.getAllMedications();
        List<MasterFood> foods = foodDAO.getAllFoods();

        request.setAttribute("totalMedications", medications.size());
        request.setAttribute("activeMedications", medications.stream().filter(MasterMedication::isTrangThai).count());
        request.setAttribute("insulinCount", medications.stream().filter(this::isInsulin).count());
        request.setAttribute("totalFoods", foods.size());
        request.setAttribute("activeFoods", foods.stream().filter(MasterFood::isTrangThai).count());
        request.setAttribute("highGiFoods", foods.stream().filter(food -> food.getChiSoGI() != null && food.getChiSoGI() >= 70).count());
        request.setAttribute("educationContents", educationalContentDAO.countAll());
        request.setAttribute("activeEducationContents", educationalContentDAO.countActive());

        request.getRequestDispatcher("/WEB-INF/views/admin/core-medical-data.jsp").forward(request, response);
    }

    boolean isInsulin(MasterMedication medication) {
        String joined = ((medication.getTenThuoc() == null ? "" : medication.getTenThuoc()) + " " +
                (medication.getHoatChat() == null ? "" : medication.getHoatChat()) + " " +
                (medication.getLoaiThuoc() == null ? "" : medication.getLoaiThuoc()))
                .toLowerCase(Locale.ROOT);
        return joined.contains("insulin");
    }
}
