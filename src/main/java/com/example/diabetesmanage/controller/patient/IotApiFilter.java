package com.example.diabetesmanage.controller.patient;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Đảm bảo mọi lỗi trên /api/iot/* trả JSON, không trả trang HTML của Tomcat.
 */
@WebFilter(urlPatterns = {"/api/iot/*"})
public class IotApiFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse httpRes)) {
            chain.doFilter(request, response);
            return;
        }

        httpRes.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            System.err.println("[IotApiFilter] " + t.getMessage());
            t.printStackTrace();
            writeJsonError(httpRes, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    safeMessage(t.getMessage(), "Lỗi xử lý IoT API"));
        }
    }

    static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        if (!response.isCommitted()) {
            response.resetBuffer();
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
        }
        String safe = message == null ? "Lỗi" : message.replace("\\", "\\\\").replace("\"", "'");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + safe + "\"}");
        response.getWriter().flush();
    }

    private static String safeMessage(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return raw.length() > 300 ? raw.substring(0, 297) + "..." : raw;
    }
}
