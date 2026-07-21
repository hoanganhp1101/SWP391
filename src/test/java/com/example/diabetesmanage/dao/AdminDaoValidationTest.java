package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.Prescription;
import com.example.diabetesmanage.model.PrescriptionDetail;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminDaoValidationTest {
    @Test
    void prescriptionRejectsMissingRequiredReferencesBeforeOpeningConnection() {
        Prescription prescription = new Prescription();
        prescription.setPatientId("patient-1");
        assertFalse(new PrescriptionDAO().createPrescription(
                prescription, Collections.singletonList(new PrescriptionDetail())));
    }

    @Test
    void assignmentRejectsBlankIdsBeforeOpeningConnection() {
        PatientAssignmentDAO dao = new PatientAssignmentDAO();
        assertFalse(dao.assignDoctor("", "doctor-1"));
        assertFalse(dao.assignDoctor("patient-1", " "));
    }
}
