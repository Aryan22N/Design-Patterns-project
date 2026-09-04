package com.parking.database;

import com.parking.abstractfactory.ParkingSpotFactory;
import com.parking.abstractfactory.PremiumZoneFactory;
import com.parking.abstractfactory.RegularZoneFactory;
import com.parking.bridge.ParkingSpot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Parking Spots.
 * 
 * CRITICAL RULE: SpotDAO reads metadata from DB, but uses Abstract Factory Pattern
 * (PremiumZoneFactory / RegularZoneFactory) to instantiate ParkingSpot objects.
 */
public class SpotDAO {

    private final ParkingSpotFactory premiumFactory = new PremiumZoneFactory();
    private final ParkingSpotFactory regularFactory = new RegularZoneFactory();

    /**
     * Loads all spots from MySQL database using Abstract Factories.
     */
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        String sql = "SELECT spot_id, spot_type, zone, pricing_mode, is_occupied FROM parking_spots";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                String spotType = rs.getString("spot_type");
                String zone = rs.getString("zone");
                boolean isOccupied = rs.getBoolean("is_occupied");

                ParkingSpotFactory factory = zone != null && zone.equalsIgnoreCase("Premium") 
                        ? premiumFactory 
                        : regularFactory;

                ParkingSpot spot;
                if (spotType == null) {
                    spot = factory.createMediumSpot(spotId);
                } else if (spotType.equalsIgnoreCase("Small")) {
                    spot = factory.createSmallSpot(spotId);
                } else if (spotType.equalsIgnoreCase("Medium")) {
                    spot = factory.createMediumSpot(spotId);
                } else {
                    spot = factory.createLargeSpot(spotId);
                }

                if (isOccupied) {
                    spot.occupy();
                } else {
                    spot.release();
                }
                spots.add(spot);
            }
        } catch (SQLException e) {
            System.err.println("[SpotDAO] Error fetching spots from database: " + e.getMessage());
        }
        return spots;
    }

    /**
     * Updates spot occupancy status in MySQL database.
     */
    public boolean updateSpotStatus(String spotId, boolean isOccupied) {
        String sql = "UPDATE parking_spots SET is_occupied = ? WHERE spot_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isOccupied);
            ps.setString(2, spotId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[SpotDAO] Error updating spot status for " + spotId + ": " + e.getMessage());
            return false;
        }
    }
}
