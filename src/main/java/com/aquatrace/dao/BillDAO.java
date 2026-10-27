package com.aquatrace.dao;

import com.aquatrace.model.Bill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private Bill map(ResultSet rs) throws SQLException {
        return new Bill(rs.getInt("id"), rs.getInt("year"), rs.getInt("month"),
                rs.getDouble("consumption_liters"), rs.getString("note"));
    }

    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM bills ORDER BY year, month")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public List<Bill> findByYear(int year) {
        List<Bill> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bills WHERE year = ? ORDER BY month")) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    /** Insert, or update if that year+month already exists. */
    public void save(Bill b) {
        String sql = "INSERT INTO bills(year, month, consumption_liters, note) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(year, month) DO UPDATE SET " +
                "consumption_liters = excluded.consumption_liters, note = excluded.note";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getYear());
            ps.setInt(2, b.getMonth());
            ps.setDouble(3, b.getConsumptionLiters());
            ps.setString(4, b.getNote());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void updateNote(int year, int month, String note) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE bills SET note = ? WHERE year = ? AND month = ?")) {
            ps.setString(1, note);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bills WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}