package com.example.diabetesmanage.controller.patient;

import com.example.diabetesmanage.service.GeminiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@WebServlet(name = "ExtractMedicalPDFServlet", urlPatterns = {"/api/extract-pdf"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 11)
public class ExtractMedicalPDFServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Part filePart = request.getPart("pdfFile");
            if (filePart == null || filePart.getSize() == 0) {
                out.print("{\"error\": \"Không tìm thấy file PDF.\"}");
                return;
            }

            // Extract text from PDF
            String pdfText = "";
            try (InputStream fileContent = filePart.getInputStream();
                 PDDocument document = PDDocument.load(fileContent)) {
                
                PDFTextStripper pdfStripper = new PDFTextStripper();
                pdfText = pdfStripper.getText(document);
            } catch (Exception e) {
                out.print("{\"error\": \"Lỗi khi đọc file PDF: " + e.getMessage() + "\"}");
                return;
            }

            if (pdfText == null || pdfText.trim().isEmpty()) {
                out.print("{\"error\": \"Không thể trích xuất văn bản từ PDF (file có thể là ảnh quét).\"}");
                return;
            }

            // Limit text size to avoid Gemini token limits (e.g. max 5000 chars)
            if (pdfText.length() > 5000) {
                pdfText = pdfText.substring(0, 5000);
            }

            // Call Gemini API
            GeminiService geminiService = new GeminiService();
            String jsonResult = geminiService.extractMedicalDataFromText(pdfText);

            out.print(jsonResult);

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\": \"Lỗi hệ thống: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}
