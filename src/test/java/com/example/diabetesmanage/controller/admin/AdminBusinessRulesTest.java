package com.example.diabetesmanage.controller.admin;

import com.example.diabetesmanage.dao.AdminReportDAO;
import com.example.diabetesmanage.dao.HighRiskPatientDAO;
import com.example.diabetesmanage.model.MasterMedication;
import com.example.diabetesmanage.model.PrescriptionDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AdminBusinessRulesTest {
    @Test
    void reportPeriodOnlyAcceptsSupportedValues() {
        AdminReportController controller = new AdminReportController(mock(AdminReportDAO.class), mock(HighRiskPatientDAO.class));
        assertEquals(7, controller.parsePeriodDays("7"));
        assertEquals(90, controller.parsePeriodDays("90"));
        assertEquals(30, controller.parsePeriodDays("365"));
        assertEquals(30, controller.parsePeriodDays(null));
    }

    @Test
    void insulinDetectionUsesNameActiveIngredientAndType() {
        CoreMedicalDataController controller = new CoreMedicalDataController();
        MasterMedication byType = new MasterMedication();
        byType.setLoaiThuoc("Insulin tác dụng nhanh");
        MasterMedication unrelated = new MasterMedication();
        unrelated.setTenThuoc("Metformin");
        assertTrue(controller.isInsulin(byType));
        assertFalse(controller.isInsulin(unrelated));
    }

    @Test
    void prescriptionRowsAreTrimmedAndIncompleteRowsIgnored() {
        PrescriptionController controller = new PrescriptionController();
        List<PrescriptionDetail> details = controller.buildPrescriptionDetails(
                new String[]{" med-1 ", "med-2", ""},
                new String[]{" 1 viên ", "", "5 ml"},
                new String[]{" sáng ", "tối", "trưa"});

        assertEquals(1, details.size());
        assertEquals("med-1", details.get(0).getMedicationId());
        assertEquals("1 viên", details.get(0).getLieuLuong());
        assertEquals("sáng", details.get(0).getTanSuat());
    }

    @Test
    void mismatchedPrescriptionArraysUseOnlyCompleteCommonLength() {
        PrescriptionController controller = new PrescriptionController();
        List<PrescriptionDetail> details = controller.buildPrescriptionDetails(
                new String[]{"med-1", "med-2"}, new String[]{"1 viên"}, new String[]{"sáng", "tối"});
        assertEquals(1, details.size());
    }
}
