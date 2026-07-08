package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bước 2 - Treatment Plan: lưu chẩn đoán + đơn thuốc + hướng xử trí cho một encounter ĐÃ tồn tại.
 *
 * <p>KHÔNG tạo lại Medical Encounter. Toàn bộ ghi trong một transaction. Idempotent:
 * xóa prescription/medication cũ của encounter trước khi insert lại, nên bấm "Lưu hồ sơ"
 * nhiều lần (hoặc quay lại trang) không sinh dữ liệu trùng.
 */
public class TreatmentPlanService {

    private static final Logger LOG = Logger.getLogger(TreatmentPlanService.class.getName());

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public void save(String encounterId, String patientId, String doctorId, EncounterCreateRequest form)
            throws SQLException {

        if (encounterId == null || encounterId.isBlank()) {
            throw new SQLException("encounter_id is required");
        }
        if (!encounterDAO.existsById(encounterId)) {
            throw new SQLException("Encounter không tồn tại, không thể lưu hồ sơ: " + encounterId);
        }

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối database");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            encounterDAO.updateTreatmentPlan(
                    con, encounterId,
                    form.getChanDoanChinh(), form.getChanDoanPhu(), form.getHuongXuTri());

            if (form.getPhanLoaiTieuDuong() != null && !form.getPhanLoaiTieuDuong().isBlank()) {
                patientDAO.updateLoaiTieuDuong(con, patientId, form.getPhanLoaiTieuDuong());
            }

            // Idempotent: xóa đơn thuốc/thuốc cũ của encounter rồi insert lại.
            medicationDAO.deleteByEncounterId(con, encounterId);
            prescriptionDAO.deleteByEncounterId(con, encounterId);

            if (form.hasPrescriptionData()) {
                String prescriptionId = prescriptionDAO.insert(
                        con, form, patientId, doctorId, encounterId);
                if (form.hasMedications()) {
                    medicationDAO.insertAll(con, prescriptionId, form.getMedications());
                }
            }

            con.commit();
            LOG.log(Level.INFO, "Treatment plan saved encounterId={0} patientId={1}",
                    new Object[]{encounterId, patientId});
        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
            }
            LOG.log(Level.SEVERE, "Rollback treatment plan encounterId=" + encounterId, ex);
            throw ex;
        } finally {
            con.setAutoCommit(previousAutoCommit);
            con.close();
        }
    }
}
