package com.example.diabetesmanage.util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Log SQLException với đủ SQLState, errorCode và ngữ cảnh thực thi.
 */
public final class SqlDiagnostics {

    private SqlDiagnostics() {
    }

    public static void log(
            Logger logger,
            Level level,
            String operation,
            String sql,
            Object[] parameters,
            SQLException ex
    ) {
        if (logger == null || ex == null) {
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("SQL failure [").append(operation).append(']');
        if (sql != null && !sql.isBlank()) {
            message.append(" sql=").append(sql.replaceAll("\\s+", " ").trim());
        }
        if (parameters != null && parameters.length > 0) {
            message.append(" params=");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    message.append(',');
                }
                message.append('[').append(i + 1).append("]=").append(parameters[i]);
            }
        }
        message.append(" SQLState=").append(ex.getSQLState());
        message.append(" errorCode=").append(ex.getErrorCode());
        message.append(" message=").append(ex.getMessage());
        logger.log(level, message.toString(), ex);
    }
}
