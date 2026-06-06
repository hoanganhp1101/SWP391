package com.example.diabetesmanage.context;

import com.example.diabetesmanage.context.DBContext;

public class TestDB {

    public static void main(String[] args) {

        boolean result = DBContext.testConnection();

        System.out.println("Result = " + result);
    }
}
