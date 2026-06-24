package com.example.diabetesmanage.context;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "diabcare_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Tu262004@";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

            return DriverManager.getConnection(url, USERNAME, PASSWORD);

        } catch (Exception e) {
            System.err.println("Lá»—i káº¿t ná»‘i CSDL: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        if (DBContext.getConnection() != null) {
            System.out.println("Káº¿t ná»‘i Database thÃ nh cÃ´ng rá»±c rá»¡!");
        } else {
            System.out.println("Káº¿t ná»‘i tháº¥t báº¡i!");
        }
    }
}
