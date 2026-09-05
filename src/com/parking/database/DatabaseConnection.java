package com.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton Pattern for managing MySQL Database Connections.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private final String url      = "jdbc:mysql://localhost:3306/smart_parking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000&socketTimeout=3000";
    private final String username = "root";
    private final String password = "root";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Database initialization failed: " + e.getMessage());
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

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS parking_spots ("
                       + "spot_id VARCHAR(20) PRIMARY KEY,"
                       + "spot_type VARCHAR(20) NOT NULL,"
                       + "zone VARCHAR(20) NOT NULL,"
                       + "pricing_mode VARCHAR(20) NOT NULL,"
                       + "is_occupied TINYINT DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles ("
                       + "vehicle_id INT AUTO_INCREMENT PRIMARY KEY,"
                       + "license_plate VARCHAR(20) UNIQUE NOT NULL,"
                       + "vehicle_type VARCHAR(20) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS tickets ("
                       + "ticket_id VARCHAR(50) PRIMARY KEY,"
                       + "vehicle_id INT,"
                       + "spot_id VARCHAR(20),"
                       + "vehicle_plate VARCHAR(20),"
                       + "vehicle_type VARCHAR(20),"
                       + "zone VARCHAR(20),"
                       + "pricing_mode VARCHAR(20),"
                       + "entry_time DATETIME,"
                       + "exit_time DATETIME,"
                       + "fee DOUBLE DEFAULT 0,"
                       + "is_paid TINYINT DEFAULT 0,"
                       + "payment_method VARCHAR(20))");

            stmt.execute("CREATE TABLE IF NOT EXISTS payments ("
                       + "payment_id INT AUTO_INCREMENT PRIMARY KEY,"
                       + "ticket_id VARCHAR(50),"
                       + "amount DOUBLE,"
                       + "payment_method VARCHAR(20),"
                       + "payment_status VARCHAR(20),"
                       + "payment_time DATETIME)");

            stmt.execute("INSERT INTO parking_spots (spot_id, spot_type, zone, pricing_mode, is_occupied) VALUES "
                       + "('S-01', 'Small', 'Premium', 'FlatRate', 0),"
                       + "('S-02', 'Small', 'Premium', 'FlatRate', 0),"
                       + "('S-03', 'Small', 'Regular', 'Hourly', 0),"
                       + "('S-04', 'Small', 'Regular', 'Hourly', 0),"
                       + "('M-01', 'Medium', 'Premium', 'FlatRate', 0),"
                       + "('M-02', 'Medium', 'Premium', 'FlatRate', 0),"
                       + "('M-03', 'Medium', 'Regular', 'Hourly', 0),"
                       + "('M-04', 'Medium', 'Regular', 'Hourly', 0),"
                       + "('L-01', 'Large', 'Premium', 'FlatRate', 0),"
                       + "('L-02', 'Large', 'Premium', 'FlatRate', 0),"
                       + "('L-03', 'Large', 'Regular', 'Hourly', 0),"
                       + "('L-04', 'Large', 'Regular', 'Hourly', 0) "
                       + "ON DUPLICATE KEY UPDATE spot_type=VALUES(spot_type), zone=VALUES(zone)");
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Auto-migration warning: " + e.getMessage());
        }
    }
}
