package com.example.diabetesmanage.model.medical;

public class MedicalRecordDetailView {

    private String recordId;
    private String recordCode;
    private String patientName;
    private String patientCode;
    private String examDate;
    private String department = "Khoa Nội tiết";
    private String doctorName = "Bác sĩ phụ trách";

    private InternalMedicineSection internalMedicine = new InternalMedicineSection();
    private BloodCountSection bloodCount = new BloodCountSection();
    private BiochemistrySection biochemistry = new BiochemistrySection();

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public InternalMedicineSection getInternalMedicine() {
        return internalMedicine;
    }

    public void setInternalMedicine(InternalMedicineSection internalMedicine) {
        this.internalMedicine = internalMedicine;
    }

    public BloodCountSection getBloodCount() {
        return bloodCount;
    }

    public void setBloodCount(BloodCountSection bloodCount) {
        this.bloodCount = bloodCount;
    }

    public BiochemistrySection getBiochemistry() {
        return biochemistry;
    }

    public void setBiochemistry(BiochemistrySection biochemistry) {
        this.biochemistry = biochemistry;
    }
}
