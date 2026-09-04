package com.parking.database;

import com.parking.ticket.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Data Access Object for Parking Tickets and Payments.
 */
public class TicketDAO {

    /**
     * Inserts a new booking ticket into tickets table.
     */
    public boolean saveTicket(Ticket ticket, int vehicleId) {
        String sql = "INSERT INTO tickets (ticket_id, vehicle_id, spot_id, vehicle_plate, vehicle_type, zone, pricing_mode, entry_time, is_paid) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticket.getTicketId());
            ps.setInt(2, vehicleId > 0 ? vehicleId : 1);
            ps.setString(3, ticket.getSpotId());
            ps.setString(4, ticket.getVehiclePlate());
            ps.setString(5, ticket.getVehicleType());
            ps.setString(6, ticket.getZone());
            ps.setString(7, ticket.getPricingMode());

            // Convert ticket entry time string or default to current time
            LocalDateTime entryTime = LocalDateTime.now();
            ps.setTimestamp(8, Timestamp.valueOf(entryTime));

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error saving ticket " + ticket.getTicketId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates exit time and calculated fee on EXIT.
     */
    public boolean updateExitDetails(String ticketId, double fee) {
        String sql = "UPDATE tickets SET exit_time = NOW(), fee = ? WHERE ticket_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, fee);
            ps.setString(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Error updating exit details for ticket " + ticketId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates payment status on PAY and logs to payments table.
     */
    public boolean updatePaymentStatus(String ticketId, double fee, String paymentMethod) {
        String updateTicketSql = "UPDATE tickets SET is_paid = 1, fee = ?, payment_method = ? WHERE ticket_id = ?";
        String insertPaymentSql = "INSERT INTO payments (ticket_id, amount, payment_method, payment_status, payment_time) VALUES (?, ?, ?, 'SUCCESS', NOW())";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTicket = conn.prepareStatement(updateTicketSql);
                 PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql)) {

                psTicket.setDouble(1, fee);
                psTicket.setString(2, paymentMethod);
                psTicket.setString(3, ticketId);
                psTicket.executeUpdate();

                psPayment.setString(1, ticketId);
                psPayment.setDouble(2, fee);
                psPayment.setString(3, paymentMethod);
                psPayment.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[TicketDAO] Error during payment transaction for ticket " + ticketId + ": " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[TicketDAO] Connection error during payment for ticket " + ticketId + ": " + e.getMessage());
            return false;
        }
    }
}
