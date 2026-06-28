package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.model.HealthRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <b>READ-ONLY</b> service cho UI và dashboard.
 *
 * <pre>
 * MedicalEncounter → (write) HealthRecordSnapshotService → health_records
 *                                                              ↓
 *                                                    HealthRecordService (this)
 *                                                              ↓
 *                                                         UI / JSP
 * </pre>
 *
 * <p>Quy tắc:
 * <ul>
 *   <li>UI LUÔN đọc từ bảng {@code health_records}</li>
 *   <li>TUYỆT ĐỐI không query {@code medical_encounters} để render sức khỏe</li>
 *   <li>TUYỆT ĐỐI không aggregate / merge runtime từ nhiều encounter</li>
 *   <li>{@code null} → JSP: "Chưa có dữ liệu"</li>
 * </ul>
 */
public class HealthRecordService {

    private static final Logger LOG = Logger.getLogger(HealthRecordService.class.getName());

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();

    /**
     * Baseline read: latest health_records by patient_id only.
     * Independent from medical_encounters / lab_results.
     */
    public HealthRecord getByPatientId(String patientId, String scopeDoctorId) {
        if (patientId == null || patientId.isBlank()) {
            return null;
        }
        String normalizedPatientId = patientId.trim();
        HealthRecord record = healthRecordDAO.findSnapshotByPatientId(normalizedPatientId, null);
        if (record != null) {
            LOG.log(Level.FINE, "getByPatientId patientId={0} recordId={1}",
                    new Object[]{normalizedPatientId, record.getId()});
            return record;
        }
        LOG.log(Level.WARNING, "getByPatientId no health_records row for patientId={0}", normalizedPatientId);
        return null;
    }

    /**
     * Dashboard / phân tích rủi ro: một snapshot / bệnh nhân từ health_records.
     */
    public Map<String, List<HealthRecord>> getSnapshotsGroupedByPatient(String scopeDoctorId) {
        Map<String, List<HealthRecord>> grouped = new LinkedHashMap<>();
        for (HealthRecord record : healthRecordDAO.listLatestSnapshotPerPatient(scopeDoctorId)) {
            String patientKey = record.getPatient() != null ? record.getPatient().getId() : null;
            if (patientKey == null || patientKey.isBlank()) {
                patientKey = record.getId();
            }
            if (patientKey == null || patientKey.isBlank()) {
                continue;
            }
            grouped.put(patientKey, List.of(record));
        }
        return grouped;
    }
}
