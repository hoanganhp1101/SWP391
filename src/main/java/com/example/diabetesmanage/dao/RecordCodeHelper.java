package com.example.diabetesmanage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

final class RecordCodeHelper {

    private RecordCodeHelper() {
    }

    static String resolve(ResultSet rs, String codeColumn) throws SQLException {
        String code = rs.getString(codeColumn);
        if (code != null && !code.isBlank()) {
            return code;
        }
        String id = rs.getString("id");
        return id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "N/A";
    }
}
