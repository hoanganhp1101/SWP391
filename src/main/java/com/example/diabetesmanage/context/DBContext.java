package com.example.diabetesmanage.context;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String DB_NAME = "diabcare_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Tu262004@";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";

            return DriverManager.getConnection(url, USERNAME, PASSWORD);

        } catch (Exception e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        if (DBContext.getConnection() != null) {
            System.out.println("Kết nối Database thành công!");
        } else {
            System.out.println("Kết nối thất bại!");
        }
    }
}
