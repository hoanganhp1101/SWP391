package com.example.diabetesmanage.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IotSimulatorServiceTest {

    private final IotSimulatorService service = new IotSimulatorService();

    @Test
    void measureNormalReturnsInExpectedRanges() {
        Map<String, Object> reading = service.measure(IotSimulatorService.Scenario.NORMAL, "luc_doi");

        double glucose = ((Number) reading.get("duongHuyet")).doubleValue();
        int sys = ((Number) reading.get("huyetApTamThu")).intValue();
        int dia = ((Number) reading.get("huyetApTamTruong")).intValue();
        int hr = ((Number) reading.get("nhipTim")).intValue();

        assertTrue(glucose >= 90 && glucose <= 130);
        assertTrue(sys >= 110 && sys <= 129);
        assertTrue(dia >= 70 && dia <= 84);
        assertTrue(hr >= 65 && hr <= 85);
        assertEquals("luc_doi", reading.get("thoiDiemDoDuong"));
        assertEquals("iot_simulator", reading.get("source"));
    }

    @Test
    void measureLowGlucoseFlagsStatus() {
        Map<String, Object> reading = service.measure(IotSimulatorService.Scenario.LOW_GLUCOSE, "sau_an_2h");
        double glucose = ((Number) reading.get("duongHuyet")).doubleValue();
        assertTrue(glucose < 70);
        assertTrue(String.valueOf(reading.get("statusLabel")).toLowerCase().contains("thấp"));
        assertEquals("sau_an_2h", reading.get("thoiDiemDoDuong"));
    }

    @Test
    void parseScenarioFallsBackToRandom() {
        assertEquals(IotSimulatorService.Scenario.HIGH_BP, service.parseScenario("high_bp"));
        assertEquals(IotSimulatorService.Scenario.RANDOM, service.parseScenario("unknown"));
    }
}
