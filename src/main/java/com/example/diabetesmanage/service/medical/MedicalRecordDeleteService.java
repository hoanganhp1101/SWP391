package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.service.medical.HealthRecordSnapshotService;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.MedicalEncounter;

import java.sql.Connection;
import java.sql.SQLException;

public class MedicalRecordDeleteService {

    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final HealthRecordSnapshotService snapshotService = new HealthRecordSnapshotService();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();

    public void deleteByEncounterId(String encounterId, String scopeDoctorId) throws SQLException {
        MedicalEncounter encounter = encounterDAO.getEncounterById(encounterId, scopeDoctorId);
        if (encounter == null) {
            throw new SQLException("Không tìm thấy hồ sơ khám bệnh");
        }

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối database");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            medicationDAO.deleteByEncounterId(con, encounterId);
            prescriptionDAO.deleteByEncounterId(con, encounterId);
            labResultDAO.deleteByEncounterId(con, encounterId);
            encounterDAO.deleteById(con, encounterId);
            snapshotService.handleEncounterDeleted(con, encounter.getPatientId(), encounterId);
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(previousAutoCommit);
            con.close();
        }
    }
}
