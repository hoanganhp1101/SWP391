package com.example.diabetesmanage.service.medical;

import com.example.diabetesmanage.dao.HealthRecordDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <b>WRITE-ONLY</b>: MedicalEncounter → PATCH {@code health_records} (1 snapshot / bệnh nhân).
 *
 * <p>Mỗi encounter chỉ cập nhật field có dữ liệu (COALESCE merge). Không xóa snapshot.
 * Lần đầu bệnh nhân chưa có dòng → tạo đúng 1 snapshot; các lần sau chỉ UPDATE.
 */
public class HealthRecordSnapshotService {

    private static final Logger LOG = Logger.getLogger(HealthRecordSnapshotService.class.getName());

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();

    public void prepareSchema() {
        healthRecordDAO.prepareSnapshotSchema();
    }

    public void applyEncounterToSnapshot(
            Connection con,
            EncounterCreateRequest form,
            String patientId,
            String doctorId,
            String encounterId
    ) throws SQLException {
        syncEncounterFieldsForSnapshot(form);
        form.prepareSnapshotPatch();
        healthRecordDAO.upsertSnapshotFromEncounter(
                con, form, patientId, doctorId, encounterId);
        LOG.log(Level.INFO,
                "applyEncounterToSnapshot completed patientId={0} encounterId={1}",
                new Object[]{patientId, encounterId});
    }

    private void syncEncounterFieldsForSnapshot(EncounterCreateRequest form) {
        if (!form.isTaiKhamNoiTiet()) {
            return;
        }
        String symptoms = firstNonBlank(form.getTrieuChung(), form.getLyDoKham());
        if (symptoms != null) {
            form.setTrieuChung(symptoms);
            form.setLyDoKham(symptoms);
        }
        String history = firstNonBlank(form.getTienSuBenh(), form.getQuaTrinhBenhLy());
        if (history != null && isBlank(form.getQuaTrinhBenhLy())) {
            form.setQuaTrinhBenhLy(history);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    /** HealthRecord không bị xóa khi xóa encounter. */
    public void handleEncounterDeleted(Connection con, String patientId, String encounterId)
            throws SQLException {
        healthRecordDAO.deleteSnapshotIfLastEncounter(con, patientId, encounterId);
    }
}
