package com.example.diabetesmanage.dao;

import com.example.diabetesmanage.model.EducationalContent;
import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EducationalContentDAO {

    public List<EducationalContent> getAllContents(String category, String status, String keyword) {
        ensureTable();
        List<EducationalContent> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM educational_contents WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!isBlank(category)) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        if ("active".equals(status)) {
            sql.append(" AND active = 1");
        } else if ("inactive".equals(status)) {
            sql.append(" AND active = 0");
        }
        if (!isBlank(keyword)) {
            sql.append(" AND (title LIKE ? OR summary LIKE ? OR content LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY display_order ASC, updated_at DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapContent(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean addContent(EducationalContent content) {
        ensureTable();
        String sql = "INSERT INTO educational_contents " +
                "(id, title, category, summary, content, target_audience, display_order, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, content.getTitle());
            ps.setString(3, content.getCategory());
            ps.setString(4, content.getSummary());
            ps.setString(5, content.getContent());
            ps.setString(6, content.getTargetAudience());
            ps.setInt(7, content.getDisplayOrder());
            ps.setBoolean(8, content.isActive());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateContent(EducationalContent content) {
        ensureTable();
        String sql = "UPDATE educational_contents SET title=?, category=?, summary=?, content=?, target_audience=?, " +
                "display_order=?, active=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, content.getTitle());
            ps.setString(2, content.getCategory());
            ps.setString(3, content.getSummary());
            ps.setString(4, content.getContent());
            ps.setString(5, content.getTargetAudience());
            ps.setInt(6, content.getDisplayOrder());
            ps.setBoolean(7, content.isActive());
            ps.setString(8, content.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteContent(String id) {
        ensureTable();
        String sql = "DELETE FROM educational_contents WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countAll() {
        ensureTable();
        return count("SELECT COUNT(*) FROM educational_contents");
    }

    public int countActive() {
        ensureTable();
        return count("SELECT COUNT(*) FROM educational_contents WHERE active = 1");
    }

    public int countByCategory(String category) {
        ensureTable();
        String sql = "SELECT COUNT(*) FROM educational_contents WHERE category = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS educational_contents (" +
                "id CHAR(36) NOT NULL, " +
                "title VARCHAR(200) NOT NULL, " +
                "category VARCHAR(60) NOT NULL, " +
                "summary VARCHAR(500) DEFAULT NULL, " +
                "content TEXT NOT NULL, " +
                "target_audience VARCHAR(60) DEFAULT 'benh_nhan', " +
                "display_order INT NOT NULL DEFAULT 0, " +
                "active TINYINT(1) NOT NULL DEFAULT 1, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (id), " +
                "INDEX idx_edu_category (category), " +
                "INDEX idx_edu_active (active)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int count(String sql) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private EducationalContent mapContent(ResultSet rs) throws Exception {
        EducationalContent content = new EducationalContent();
        content.setId(rs.getString("id"));
        content.setTitle(rs.getString("title"));
        content.setCategory(rs.getString("category"));
        content.setSummary(rs.getString("summary"));
        content.setContent(rs.getString("content"));
        content.setTargetAudience(rs.getString("target_audience"));
        content.setDisplayOrder(rs.getInt("display_order"));
        content.setActive(rs.getBoolean("active"));
        content.setCreatedAt(rs.getTimestamp("created_at"));
        content.setUpdatedAt(rs.getTimestamp("updated_at"));
        return content;
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else {
                ps.setObject(i + 1, param);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
