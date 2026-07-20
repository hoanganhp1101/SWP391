package com.example.diabetesmanage.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

final class JdbcUtil {

    static void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    static void setDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        setNullableDouble(ps, index, value);
    }

    static void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }

    static void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

}
