package com.example.diabetesmanage.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminReportStatsTest {
    @Test
    void ratesAreZeroWhenThereIsNoData() {
        AdminReportStats stats = new AdminReportStats();
        assertEquals(0, stats.getGlucoseControlRate());
        assertEquals(0, stats.getAppointmentCompletionRate());
    }

    @Test
    void ratesAreRoundedToNearestPercent() {
        AdminReportStats stats = new AdminReportStats();
        stats.setControlledGlucoseCount(2);
        stats.setHighGlucoseCount(1);
        stats.setTotalAppointments(6);
        stats.setCompletedAppointments(5);
        assertEquals(67, stats.getGlucoseControlRate());
        assertEquals(83, stats.getAppointmentCompletionRate());
    }
}
