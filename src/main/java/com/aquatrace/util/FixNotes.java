package com.aquatrace.util;

import com.aquatrace.dao.DBConnection;
import java.sql.*;

public class FixNotes {
    public static void main(String[] args) throws Exception {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            int n = s.executeUpdate("UPDATE bills SET note = '' WHERE year = 2026 AND month IN (11, 12)");
            System.out.println("Cleared notes: " + n);
        }
    }
}