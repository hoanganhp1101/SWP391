package com.example.diabetesmanage.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mô phỏng thiết bị IoT đo chỉ số sức khỏe (đường huyết, huyết áp, nhịp tim).
 * Dùng cho demo/lab — không phải thiết bị thật.
 */
public class IotSimulatorService {

    public enum Scenario {
        NORMAL,
        HIGH_GLUCOSE,
        LOW_GLUCOSE,
        HIGH_BP,
        RANDOM
    }

    public Map<String, Object> measure(Scenario scenario, String timing) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Scenario effective = scenario == null ? Scenario.RANDOM : scenario;

        double glucose;
        int sys;
        int dia;
        int hr;

        switch (effective) {
            case HIGH_GLUCOSE:
                glucose = rnd.nextDouble(180, 280);
                sys = rnd.nextInt(128, 148);
                dia = rnd.nextInt(82, 95);
                hr = rnd.nextInt(85, 110);
                break;
            case LOW_GLUCOSE:
                glucose = rnd.nextDouble(55, 70);
                sys = rnd.nextInt(100, 118);
                dia = rnd.nextInt(62, 75);
                hr = rnd.nextInt(70, 95);
                break;
            case HIGH_BP:
                glucose = rnd.nextDouble(95, 140);
                sys = rnd.nextInt(145, 175);
                dia = rnd.nextInt(92, 110);
                hr = rnd.nextInt(80, 105);
                break;
            case NORMAL:
                glucose = rnd.nextDouble(90, 130);
                sys = rnd.nextInt(110, 129);
                dia = rnd.nextInt(70, 84);
                hr = rnd.nextInt(65, 85);
                break;
            default:
                glucose = rnd.nextDouble(70, 220);
                sys = rnd.nextInt(105, 160);
                dia = rnd.nextInt(65, 100);
                hr = rnd.nextInt(60, 110);
                break;
        }

        String resolvedTiming = resolveTiming(timing);

        Map<String, Object> reading = new LinkedHashMap<>();
        reading.put("deviceId", "SIM-GLUCO-" + (1000 + rnd.nextInt(9000)));
        reading.put("bpDeviceId", "SIM-BP-" + (1000 + rnd.nextInt(9000)));
        reading.put("hrDeviceId", "SIM-HR-" + (1000 + rnd.nextInt(9000)));
        reading.put("scenario", effective.name());
        reading.put("duongHuyet", Math.round(glucose * 10.0) / 10.0);
        reading.put("huyetApTamThu", sys);
        reading.put("huyetApTamTruong", dia);
        reading.put("nhipTim", hr);
        reading.put("thoiDiemDoDuong", resolvedTiming);
        reading.put("measuredAt", System.currentTimeMillis());
        reading.put("source", "iot_simulator");
        reading.put("statusLabel", statusLabel(glucose, sys, dia, hr));
        return reading;
    }

    public Scenario parseScenario(String raw) {
        if (raw == null || raw.isBlank()) {
            return Scenario.RANDOM;
        }
        try {
            return Scenario.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Scenario.RANDOM;
        }
    }

    private static String resolveTiming(String timing) {
        if (timing == null || timing.isBlank()) {
            return "luc_doi";
        }
        String value = timing.trim();
        if ("sau_an_1h".equals(value)
                || "sau_an_2h".equals(value)
                || "truoc_ngu".equals(value)
                || "luc_doi".equals(value)) {
            return value;
        }
        return "luc_doi";
    }

    private static String statusLabel(double glucose, int sys, int dia, int hr) {
        if (glucose < 70) {
            return "Cảnh báo: đường huyết thấp";
        }
        if (glucose > 180 || sys >= 140 || dia >= 90) {
            return "Cảnh báo: chỉ số bất thường — cần theo dõi sát";
        }
        if (hr > 100) {
            return "Nhịp tim nhanh — nên nghỉ và đo lại";
        }
        return "Trong ngưỡng theo dõi thường gặp";
    }
}
