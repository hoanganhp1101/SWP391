package com.example.diabetesmanage;

import com.example.diabetesmanage.dao.AIAnalysisDAO;
import com.example.diabetesmanage.model.AIAnalysis;

public class TestGeminiService {
    public static void main(String[] args) {
        System.out.println("Starting DB query test...");
        try {
            AIAnalysisDAO dao = new AIAnalysisDAO();
            AIAnalysis latest = dao.getLatestAnalysis("eef63e1f-5f05-11f1-ae5a-088fc37960fe");
            if (latest != null) {
                System.out.println("SUCCESS_FOUND_DB: " + latest.getId() + " | Risk: " + latest.getDiemNguyCo() + " | Warning: " + latest.getMucCanhBao());
            } else {
                System.out.println("DB_QUERY_EMPTY: No analysis found in database for patient.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
