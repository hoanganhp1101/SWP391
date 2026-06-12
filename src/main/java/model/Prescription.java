/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.security.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author iac26
 */
public class Prescription {
    private UUID id;
    private UUID patientId;
    private UUID bacSiId;

    private Date ngayKeDon;

    private String chanDoan;
    private String huongDieuTri;
    private String cheDoAn;
    private String luyenTap;

    private Timestamp ngayTaiKham;

    private String ghiChu;

    private Timestamp ngayTao;
}
