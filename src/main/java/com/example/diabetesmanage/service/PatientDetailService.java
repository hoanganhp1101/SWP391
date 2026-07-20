package com.example.diabetesmanage.service;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tải dữ liệu cho trang chi tiết bệnh nhân và xuất PDF.
 */
public class PatientDetailService {

    private final PatientDAO patientDAO = new PatientDAO();
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public DetailBundle load(String patientId, String scopeDoctorId) {
        return load(patientId, scopeDoctorId, null, null);
    }

    public DetailBundle load(String patientId, String scopeDoctorId,
                             LocalDate fromDate, LocalDate toDate) {
        DetailBundle bundle = new DetailBundle();
        if (patientId == null || patientId.isBlank()) {
            return bundle;
        }
        String trimmedId = patientId.trim();
        bundle.patient = patientDAO.getPatientById(trimmedId, scopeDoctorId);
        bundle.encounter = encounterDAO.getLatestEncounterByPatient(trimmedId, scopeDoctorId);
        if (fromDate != null && toDate != null) {
            bundle.history = encounterDAO.getHistoryByPatientAndDateRange(
                    trimmedId, scopeDoctorId, fromDate, toDate);
        } else {
            bundle.history = encounterDAO.getHistoryByPatientId(trimmedId, scopeDoctorId);
        }
        bundle.healthRecord = buildHealthRecord(trimmedId, scopeDoctorId, bundle.patient, bundle.encounter);
        return bundle;
    }

    private HealthRecord buildHealthRecord(String patientId, String scopeDoctorId,
                                           Patient patient, MedicalEncounter encounter) {
        HealthRecord record = healthRecordDAO.getLatestByPatientId(patientId);
        LabResult lab = labResultDAO.getLatestSummaryByPatientId(patientId);
        if (record == null && (lab != null || encounter != null)) {
            record = new HealthRecord();
            record.setPatient(patient);
            if (encounter != null) {
                record.setEncounterId(encounter.getId());
                record.setThoiGianDo(encounter.getNgayKham());
            } else {
                record.setThoiGianDo(lab.getNgayXetNghiem());
            }
        }
        if (record != null && lab != null) {
            record.setHba1cPercent(lab.getHba1c());
            record.setCholesterolMmol(lab.getCholesterolTp());
            record.setTriglycerideMmol(lab.getTriglyceride());
            record.setHdlMmol(lab.getHdlC());
            record.setLdlMmol(lab.getLdlC());
            record.setWbc(lab.getWbc());
            record.setRbc(lab.getRbc());
            record.setHgb(lab.getHgb());
            record.setHct(lab.getHct());
            record.setPlt(lab.getPlt());
            record.setAst(lab.getAst());
            record.setAlt(lab.getAlt());
            record.setUre(lab.getUre());
            record.setCreatinine(lab.getCreatinine());
        }
        if (record == null) {
            return null;
        }

        List<MedicalEncounter> encounterHistory = encounterDAO.searchEncounters(
                scopeDoctorId, null, null, null, null, null, patientId);
        Map<String, String> prescriptionAdvice =
                prescriptionDAO.getAdviceForEncounterOrLatestPatient(
                        encounter != null ? encounter.getId() : null, patientId);
        if (patient != null) {
            record.setTienSuBenh(patient.getTienSuBenh());
            record.setPhanLoaiTieuDuong(patient.getLoaiTieuDuong());
            record.setChieuCaoCm(patient.getChieuCaoCm());
        }

        if (encounterHistory != null) {
            for (MedicalEncounter enc : encounterHistory) {
                if (isBlank(record.getTrieuChung())) {
                    String trieuChung = EncounterClinicalJson.parseString(
                            enc.getKhamLamSang(), "trieu_chung");
                    if (isBlank(trieuChung) && !isTypeLabel(enc.getLyDoKham())) {
                        trieuChung = enc.getLyDoKham();
                    }
                    if (!isBlank(trieuChung)) {
                        record.setTrieuChung(trieuChung.trim());
                    }
                }
                if (isBlank(record.getKhamLamSang())) {
                    String storedValue = enc.getKhamLamSang();
                    String khamLamSang = null;
                    if (!isBlank(storedValue)) {
                        String jsonValue = EncounterClinicalJson.parseString(storedValue, "noi_dung");
                        if (!isBlank(jsonValue)) {
                            khamLamSang = jsonValue;
                        } else {
                            String trimmed = storedValue.trim();
                            khamLamSang = trimmed.startsWith("{") ? null : trimmed;
                        }
                    }
                    if (!isBlank(khamLamSang)) {
                        record.setKhamLamSang(khamLamSang.trim());
                    }
                }
                if (isBlank(record.getChanDoanChinh()) && !isBlank(enc.getChanDoanChinh())
                        && !isTypeLabel(enc.getChanDoanChinh())) {
                    record.setChanDoanChinh(enc.getChanDoanChinh().trim());
                }
                if (isBlank(record.getChanDoanPhu()) && !isBlank(enc.getChanDoanPhu())) {
                    record.setChanDoanPhu(enc.getChanDoanPhu().trim());
                }
                if (isBlank(record.getHuongXuTri()) && !isBlank(enc.getHuongXuTri())) {
                    record.setHuongXuTri(enc.getHuongXuTri().trim());
                }
            }
            if (isBlank(record.getChanDoanChinh())) {
                for (MedicalEncounter enc : encounterHistory) {
                    if (!isBlank(enc.getChanDoanChinh())) {
                        record.setChanDoanChinh(enc.getChanDoanChinh().trim());
                        break;
                    }
                }
            }
        }

        if (prescriptionAdvice != null) {
            record.setKhuyenNghiDieuTri(prescriptionAdvice.get("huong_dieu_tri"));
            record.setCheDoAn(prescriptionAdvice.get("che_do_an"));
            record.setLuyenTap(prescriptionAdvice.get("luyen_tap"));
        }
        return record;
    }

    private boolean isTypeLabel(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return "Bệnh án tái khám Nội tiết".equalsIgnoreCase(trimmed)
                || "Kết quả xét nghiệm máu tổng quát".equalsIgnoreCase(trimmed)
                || "Kết quả sinh hóa máu".equalsIgnoreCase(trimmed);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static final class DetailBundle {
        public Patient patient;
        public MedicalEncounter encounter;
        public HealthRecord healthRecord;
        public List<MedicalEncounter> history;
    }
}
