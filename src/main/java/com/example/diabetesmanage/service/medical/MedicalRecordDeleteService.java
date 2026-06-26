package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.context.DBContext;
import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.MedicationDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.MedicalEncounter;

import java.sql.Connection;
import java.sql.SQLException;

public class MedicalRecordDeleteService {

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();

    public void deleteByHealthRecordId(String recordId, String scopeDoctorId) throws SQLException {
        HealthRecord record = healthRecordDAO.getHealthRecordRecordById(recordId, scopeDoctorId);
        if (record == null) {
            throw new SQLException("Không tìm thấy hồ sơ bệnh án");
        }

        String patientId = record.getPatient() != null ? record.getPatient().getId() : null;
        MedicalEncounter encounter = null;
        if (patientId != null && record.getThoiGianDo() != null) {
            encounter = encounterDAO.getClosestByPatientAndTime(patientId, record.getThoiGianDo());
        }

        Connection con = DBContext.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối database");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            if (encounter != null && encounter.getId() != null) {
                String encounterId = encounter.getId();
                medicationDAO.deleteByEncounterId(con, encounterId);
                prescriptionDAO.deleteByEncounterId(con, encounterId);
                labResultDAO.deleteByEncounterId(con, encounterId);
                encounterDAO.deleteById(con, encounterId);
            }
            healthRecordDAO.deleteById(con, recordId);
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
