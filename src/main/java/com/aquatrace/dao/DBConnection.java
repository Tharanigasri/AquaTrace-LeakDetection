package com.aquatrace.dao;

import com.aquatrace.util.PasswordUtil;
import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:aquatrace.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "year INTEGER NOT NULL, " +
                    "month INTEGER NOT NULL, " +
                    "consumption_liters REAL NOT NULL, " +
                    "note TEXT DEFAULT '', " +
                    "UNIQUE(year, month))");

            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users(username, password_hash) VALUES (?, ?)")) {
                    ps.setString(1, "demo");
                    ps.setString(2, PasswordUtil.hash("aquatrace123"));
                    ps.executeUpdate();
                }
            }

            rs = s.executeQuery("SELECT COUNT(*) FROM bills");
            if (rs.next() && rs.getInt(1) == 0) {
                seedBills(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database init failed", e);
        }
    }

    private static void seedBills(Connection c) throws SQLException {
        double[] prev = {4300, 4200, 4500, 4800, 5200, 5600, 5800, 5700, 5300, 4900, 4500, 4400};
        double[] curr = {4350, 4250, 4550, 4850, 5250, 5650, 5850, 5700, 5350, 4950, 6300, 6160};
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO bills(year, month, consumption_liters, note) VALUES (?, ?, ?, '')")) {
            for (int m = 1; m <= 12; m++) {
                ps.setInt(1, 2025); ps.setInt(2, m); ps.setDouble(3, prev[m - 1]);
                ps.executeUpdate();
                ps.setInt(1, 2026); ps.setInt(2, m); ps.setDouble(3, curr[m - 1]);
                ps.executeUpdate();
            }
        }
    }
}