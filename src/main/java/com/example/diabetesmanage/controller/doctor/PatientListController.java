package com.example.diabetesmanage.controller.doctor;

import com.example.diabetesmanage.dao.HealthRecordDAO;
import com.example.diabetesmanage.dao.LabResultDAO;
import com.example.diabetesmanage.dao.MedicalEncounterDAO;
import com.example.diabetesmanage.dao.PatientDAO;
import com.example.diabetesmanage.dao.PrescriptionDAO;
import com.example.diabetesmanage.model.HealthRecord;
import com.example.diabetesmanage.model.LabResult;
import com.example.diabetesmanage.model.MedicalEncounter;
import com.example.diabetesmanage.model.Patient;
import com.example.diabetesmanage.model.User;
import com.example.diabetesmanage.util.AuthContext;
import com.example.diabetesmanage.util.DoctorLayoutHelper;
import com.example.diabetesmanage.util.EncounterClinicalJson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/doctor/patient-list")
public class PatientListController extends HttpServlet {

    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        String keyword = request.getParameter("keyword");
        String glucose = request.getParameter("glucose");
        String hba1c = request.getParameter("hba1c");
        String bmi = request.getParameter("bmi");
        String action = request.getParameter("action");

        List<Patient> patients =
                patientDAO.searchPatients(keyword, glucose, hba1c, bmi, action, scopeDoctorId);

        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patients", patients);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientmanagement.jsp")
                .forward(request, response);
    }
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final LabResultDAO labResultDAO = new LabResultDAO();
    private final MedicalEncounterDAO encounterDAO = new MedicalEncounterDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthContext.requirePatientDataAccess(request, response);
        if (user == null) {
            return;
        }

        String patientId = request.getParameter("id");
        if (patientId == null || patientId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/patient-list");
            return;
        }

        if (!AuthContext.ensurePatientAccess(user, patientDAO, patientId, response)) {
            return;
        }
        String scopeDoctorId = AuthContext.scopeDoctorId(user);
        Patient patient = patientDAO.getPatientById(patientId.trim(), scopeDoctorId);
        MedicalEncounter encounter =
                encounterDAO.getLatestEncounterByPatient(patientId.trim(), scopeDoctorId);
        HealthRecord record = encounter != null
                ? healthRecordDAO.getByEncounterId(encounter.getId()) : null;
        // Hồ sơ tổng quan: gộp giá trị mới nhất của TỪNG chỉ số trên mọi encounter
        // (encounter cũ có CBC, encounter mới có sinh hóa vẫn hiển thị đủ cả hai).
        LabResult lab = labResultDAO.getLatestSummaryByPatientId(patientId.trim());
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
        if (record != null) {
            // Các trường lâm sàng không còn lưu trong health_records: lấy từ bảng nguồn.
            // Ưu tiên encounter hiện tại, sau đó lùi dần về encounter cũ hơn có dữ liệu.
            List<MedicalEncounter> encounterHistory = encounterDAO.searchEncounters(
                    scopeDoctorId, null, null, null, null, null, patientId.trim());
            Map<String, String> prescriptionAdvice =
                    prescriptionDAO.getAdviceForEncounterOrLatestPatient(
                            encounter != null ? encounter.getId() : null, patientId.trim());
            enrichClinicalFields(record, patient, encounterHistory, prescriptionAdvice);
        }
        DoctorLayoutHelper.prepare(request, user, "patients");
        request.setAttribute("patient", patient);
        request.setAttribute("encounter", encounter);
        request.setAttribute("healthRecord", record);
        request.setAttribute("hasHealthRecord", record != null);
        request.setAttribute("currentUser", user);
        request.getRequestDispatcher("/WEB-INF/views/doctor/patientdetail.jsp")
                .forward(request, response);
    }

    /**
     * Patient Detail: các trường lâm sàng không nằm trong health_records nên phải
     * tổng hợp từ bảng nguồn thật — patients (tiền sử, phân loại tiểu đường, chiều cao),
     * medical_encounters (triệu chứng, khám lâm sàng, chẩn đoán, hướng xử trí),
     * prescriptions (khuyến nghị điều trị, chế độ ăn, luyện tập).
     * encounterHistory đã sort mới nhất trước; lùi dần về bản ghi cũ hơn nếu bản mới NULL.
     */
    private void enrichClinicalFields(HealthRecord record, Patient patient,
                                      List<MedicalEncounter> encounterHistory,
                                      Map<String, String> prescriptionAdvice) {
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
                    String khamLamSang = resolveClinicalText(enc.getKhamLamSang());
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
            // Không có chẩn đoán "thật" ở bất kỳ encounter nào: dùng nhãn loại hồ sơ mới nhất.
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
    }

    /** kham_lam_sang có thể là JSON ({"noi_dung":...}) hoặc text thuần từ bản ghi cũ. */
    private String resolveClinicalText(String storedValue) {
        if (isBlank(storedValue)) {
            return null;
        }
        String jsonValue = EncounterClinicalJson.parseString(storedValue, "noi_dung");
        if (!isBlank(jsonValue)) {
            return jsonValue;
        }
        String trimmed = storedValue.trim();
        return trimmed.startsWith("{") ? null : trimmed;
    }

    /** Nhãn placeholder theo loại hồ sơ, không phải chẩn đoán/triệu chứng thật. */
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
}
