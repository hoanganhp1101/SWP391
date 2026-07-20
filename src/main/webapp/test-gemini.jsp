<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.diabetesmanage.service.GeminiService" %>
<%@ page import="com.example.diabetesmanage.model.*" %>
<%@ page import="com.example.diabetesmanage.dao.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<html>
<head><title>Test Gemini</title></head>
<body>
<h1>Testing Gemini API</h1>
<pre>
<%
    try {
        out.println("Starting test...");
        PatientDAO patientDAO = new PatientDAO();
        String patientId = patientDAO.getDemoPatientId();
        out.println("Patient ID: " + patientId);
        
        if (patientId != null) {
            Patient patient = patientDAO.getPatientById(patientId);
            HealthRecordDAO hrDAO = new HealthRecordDAO();
            HealthRecord record = hrDAO.getLatestComprehensiveRecord(patientId);
            
            MasterFoodDAO foodDAO = new MasterFoodDAO();
            List<MasterFood> foods = foodDAO.getAllFoods().stream()
                    .filter(MasterFood::isTrangThai)
                    .collect(Collectors.toList());
                    
            out.println("Found " + foods.size() + " active foods.");
            
            GeminiService geminiService = new GeminiService();
            out.println("Calling geminiService.generateDailyDietPlan...");
            String jsonResponse = geminiService.generateDailyDietPlan(patient, record, foods);
            out.println("Response:\n" + jsonResponse);
        } else {
            out.println("No demo patient found.");
        }
    } catch (Exception e) {
        out.println("Exception: " + e.getMessage());
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
</pre>
</body>
</html>
