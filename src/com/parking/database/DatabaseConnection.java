package com.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Pattern for managing MySQL Database Connections.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private final String url      = "jdbc:mysql://localhost:3306/smart_parking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private final String username = "root";
    private final String password = "root";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
