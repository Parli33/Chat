package server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import utils.ConfigReader;
import exceptions.AuthenticationException;

import java.sql.*;

@Slf4j
public class DatabaseController {
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(ConfigReader.getInstance().getDb());

    }

    public static void createUsersTable() throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS USERS (USERNAME TEXT PRIMARY KEY, PASSWORD TEXT)"
            );
        }
    }

    public static void registerUser(String username, String password) throws SQLException {
        if (isUserExists(username)) {
            throw new SQLException("Пользователь с таким именем уже существует. Придумайте другое.");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO USERS (USERNAME, PASSWORD) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
        }
    }

    public static boolean authenticateUser(String username, String password) throws SQLException {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT PASSWORD FROM USERS WHERE USERNAME = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("PASSWORD");
                if (BCrypt.checkpw(password, storedPassword)) {
                    return true;
                } else {
                    throw new AuthenticationException("Неверный пароль.");
                }
            } else {
                throw new AuthenticationException("Пользователь не найден.");
            }
        }
    }

    private static boolean isUserExists(String username) throws SQLException {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT USERNAME FROM USERS WHERE USERNAME = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}