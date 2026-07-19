/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.diabetesmanage.util;

import java.security.MessageDigest;

/**
 *
 * @author ASUS
 */
public class Encode {

    public String Encode(String str) {
        // Nếu DB dùng hàm SHA2() thuần túy thì không cần cộng chuỗi salt ở đây
        if (str == null) {
            return null;
        }

        String result = null;
        try {
            // 1. Đổi sang thuật toán SHA-256 để khớp với SHA2 của DB
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dataByte = str.getBytes("UTF-8");
            byte[] digest = md.digest(dataByte);

            // 2. Chuyển đổi mảng byte thành chuỗi HEX (Thập lục phân) thay vì Base64
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            result = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
