package com.example.diabetesmanage.context;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "diabcare_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "2004";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

            return DriverManager.getConnection(url, USERNAME, PASSWORD);

        } catch (Exception e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        if (DBContext.getConnection() != null) {
            System.out.println("Kết nối Database thành công rực rỡ!");
        } else {
            System.out.println("Kết nối thất bại!");
        }
    }
}

