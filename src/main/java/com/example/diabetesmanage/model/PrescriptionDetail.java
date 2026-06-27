package com.example.diabetesmanage.model;

public class PrescriptionDetail {
    private String id;
    private String prescriptionId;
    private String medicationId;
    private String lieuLuong;
    private String tanSuat;

    // Khai báo thêm đối tượng thuốc gốc để hiển thị tên thuốc dễ dàng trên giao diện
    private MasterMedication thuocGoc;

    public PrescriptionDetail() {}

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }
    public String getLieuLuong() { return lieuLuong; }
    public void setLieuLuong(String lieuLuong) { this.lieuLuong = lieuLuong; }
    public String getTanSuat() { return tanSuat; }
    public void setTanSuat(String tanSuat) { this.tanSuat = tanSuat; }
    public MasterMedication getThuocGoc() { return thuocGoc; }
    public void setThuocGoc(MasterMedication thuocGoc) { this.thuocGoc = thuocGoc; }
}