package com.aquatrace.dao;

import com.aquatrace.model.User;
import com.aquatrace.util.PasswordUtil;
import java.sql.*;

public class UserDAO {
    public User findByUsername(String username) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    public User login(String username, String password) {
        User u = findByUsername(username);
        if (u != null && u.getPasswordHash().equals(PasswordUtil.hash(password))) return u;
        return null;
    }

    public boolean register(String username, String password) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username, password_hash) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }
}