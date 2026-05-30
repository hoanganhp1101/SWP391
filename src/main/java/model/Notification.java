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
public class Notification {
    private UUID id;

    private UUID alertId;
    private UUID nguoiNhanId;

    private String kenhGui;

    private String tieuDe;
    private String noiDung;

    private String trangThai;

    private Timestamp thoiGianGui;
    private Timestamp thoiGianDoc;

    private String maLoi;
}
