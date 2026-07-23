package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.HighRiskPatient;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class HighRiskPatientDAOTest {
    private final HighRiskPatientDAO dao = new HighRiskPatientDAO();

    @Test
    void multipleDangerSignalsProduceCappedCriticalRisk() {
        HighRiskPatient patient = new HighRiskPatient();
        patient.setLatestGlucose(320.0);
        patient.setLatestHba1c(9.5);
        patient.setSystolicBloodPressure(185);
        patient.setDiastolicBloodPressure(121);
        patient.setBmi(32.0);
        patient.setRecentAlertCount(4);
        patient.setUnreadDoctorAlertCount(2);
        patient.setLastMeasurementTime(Timestamp.from(Instant.now().minus(10, ChronoUnit.DAYS)));

        dao.calculateRisk(patient);

        assertEquals(100, patient.getRiskScore());
        assertEquals("critical", patient.getRiskLevel());
        assertFalse(patient.getRiskReasons().isEmpty());
    }

    @Test
    void normalFreshMeasurementsRemainLowRisk() {
        HighRiskPatient patient = new HighRiskPatient();
        patient.setLatestGlucose(110.0);
        patient.setLatestHba1c(6.2);
        patient.setSystolicBloodPressure(125);
        patient.setDiastolicBloodPressure(80);
        patient.setBmi(23.0);
        patient.setLastMeasurementTime(Timestamp.from(Instant.now()));

        dao.calculateRisk(patient);

        assertEquals(0, patient.getRiskScore());
        assertEquals("low", patient.getRiskLevel());
        assertEquals(1, patient.getRiskReasons().size());
    }

    @Test
    void missingMeasurementsRequireMonitoring() {
        HighRiskPatient patient = new HighRiskPatient();
        dao.calculateRisk(patient);
        assertEquals(27, patient.getRiskScore());
        assertEquals("medium", patient.getRiskLevel());
    }
}
