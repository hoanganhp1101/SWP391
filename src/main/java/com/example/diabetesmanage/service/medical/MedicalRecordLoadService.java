package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.medical.MedicalRecordDetailView;
import com.example.diabetesmanage.model.medical.MedicationChip;

import java.util.List;

public class MedicalRecordLoadService {

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final MedicalRecordViewService viewService = new MedicalRecordViewService();

    public MedicalRecordDetailView loadDetailViewByRecordId(String recordId) {
        HealthRecord record = healthRecordDAO.getHealthRecordRecordById(recordId);
        if (record == null) {
            return null;
        }
        return buildDetailView(record);
    }

    public MedicalRecordDetailView loadDetailViewByPatientId(String patientId) {
        HealthRecord record = healthRecordDAO.getLatestHealthRecordByPatientId(patientId);
        if (record == null) {
            return null;
        }
        return buildDetailView(record);
    }

    public HealthRecord getRecordById(String recordId) {
        return healthRecordDAO.getHealthRecordRecordById(recordId);
    }

    public HealthRecord getLatestRecordByPatientId(String patientId) {
        return healthRecordDAO.getLatestHealthRecordByPatientId(patientId);
    }

    private MedicalRecordDetailView buildDetailView(HealthRecord record) {
        String patientId = record.getPatient() != null ? record.getPatient().getId() : null;

        MedicalEncounter encounter = patientId != null
                ? encounterDAO.getLatestByPatientId(patientId)
                : null;

        LabResult lab = null;
        if (encounter != null && encounter.getId() != null) {
            lab = labResultDAO.getByEncounterId(encounter.getId());
        }
        if (lab == null && patientId != null) {
            lab = labResultDAO.getLatestByPatientId(patientId);
        }

        List<MedicationChip> medications = patientId != null
                ? encounterDAO.getMedicationsByPatientId(patientId)
                : List.of();

        List<String> recommendations = patientId != null
                ? encounterDAO.getRecommendationsByPatientId(patientId)
                : List.of();

        return viewService.buildView(record, lab, encounter, medications, recommendations);
    }
}
