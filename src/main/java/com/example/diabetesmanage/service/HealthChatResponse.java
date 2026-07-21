package com.example.diabetesmanage.service;

import com.example.diabetesmanage.service.HealthChatGuard.Status;

/**
 * Kết quả xử lý chat sức khỏe trả về cho servlet/UI.
 */
public class HealthChatResponse {

    private final Status status;
    private final String reply;
    private final boolean success;

    public HealthChatResponse(Status status, String reply, boolean success) {
        this.status = status;
        this.reply = reply;
        this.success = success;
    }

    public Status getStatus() {
        return status;
    }

    public String getReply() {
        return reply;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatusCode() {
        if (status == Status.OUT_OF_SCOPE) {
            return "out_of_scope";
        }
        if (status == Status.EMERGENCY) {
            return "emergency";
        }
        if (status == Status.BLOCKED) {
            return "blocked";
        }
        return "answered";
    }
}
