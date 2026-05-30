/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.security.MessageDigest;
import java.util.Base64;

/**
 *
 * @author ASUS
 */
public class Encode {
    
    public String Encode(String str){
        String salt = "fgdtebxcrt213rsAS";
        String result = null;
        
        str = str + salt;
        
        try {
        byte[] DataByte = str.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            result = Base64.getEncoder().encodeToString(md.digest(DataByte));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
