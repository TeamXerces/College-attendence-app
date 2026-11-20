package com.college.attendance.util;

import java.sql.*;
import java.util.*;

public final class Jdbc {
    private Jdbc() {}

    public static List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) { return rows(rs); }
        }
    }

    public static Map<String, Object> one(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public static long insert(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
            return -1;
        }
    }

    public static long count(String sql, Object... params) throws SQLException {
        Map<String, Object> row = one(sql, params);
        if (row == null || row.isEmpty()) return 0;
        Object value = row.values().iterator().next();
        return value == null ? 0 : ((Number) value).longValue();
    }

    public static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    private static List<Map<String, Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) row.put(meta.getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
            result.add(row);
        }
        return result;
    }
}
