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
public class Alert {
    private UUID id;
    private UUID patientId;
    private UUID aiAnalysisId;

    private String loaiCanhBao;
    private String mucDo;

    private String tieuDe;
    private String noiDung;

    private boolean daDocBn;
    private boolean daDocBs;

    private UUID xuLyBoi;

    private String ghiChuXuLy;

    private Timestamp thoiGianTao;
    private Timestamp thoiGianXuLy;
}
