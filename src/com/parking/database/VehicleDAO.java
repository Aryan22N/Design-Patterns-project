package com.parking.database;

import com.parking.vehicle.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for Vehicles.
 * 
 * Vehicle objects are instantiated via Factory Method (VehicleFactory)
 * prior to being passed to VehicleDAO for persistence.
 */
public class VehicleDAO {

    /**
     * Inserts vehicle if not existing and returns its vehicle_id.
     */
    public int getOrCreateVehicle(Vehicle vehicle) {
        String insertSql = "INSERT INTO vehicles (license_plate, vehicle_type) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE vehicle_type = VALUES(vehicle_type)";
        String selectSql = "SELECT vehicle_id FROM vehicles WHERE license_plate = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, vehicle.getLicensePlate());
                psInsert.setString(2, vehicle.getType());
                psInsert.executeUpdate();
            }

            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setString(1, vehicle.getLicensePlate());
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("vehicle_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[VehicleDAO] Error saving vehicle " + vehicle.getLicensePlate() + ": " + e.getMessage());
        }
        return -1;
    }
}
