/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.security.Timestamp;
import java.util.UUID;

/**
 *
 * @author iac26
 */
public class HealthRecord {
    private UUID id;
    private UUID patientId;
    private UUID nhapBoi;

    private Double duongHuyetMgdl;
    private String thoiDiemDoDuong;

    private Integer huyetApTamThu;
    private Integer huyetApTamTruong;
    private Integer nhipTim;

    private Double canNangKg;
    private Double bmi;
    private Double hba1cPercent;

    private Double cholesterolMmol;
    private Double triglycerideMmol;

    private Double carbsG;

    private Double soGioNgu;

    private Integer lieuLuongInsulinUi;
    private String loaiInsulinTiem;

    private String ghiChu;

    private Timestamp thoiGianDo;
    private Timestamp ngayTao;
}
