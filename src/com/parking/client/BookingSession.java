package com.parking.client;

import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory session registry.
 * Holds active bookings (ticketId → session data) so that
 * EXIT and PAY commands can look up an in-flight booking.
 *
 * Singleton — one instance per JVM process.
 * (Node.js keeps a persistent session map too; this handles
 *  cases where Java is called in a single long-lived process.)
 */
public class BookingSession {

    private static final BookingSession INSTANCE = new BookingSession();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static BookingSession getInstance() { return INSTANCE; }

    /** A single booked session. */
    public static class Session {
        public final Ticket      ticket;
        public final ParkingSpot spot;
        public final LocalDateTime entryTime;

        public Session(Ticket ticket, ParkingSpot spot) {
            this.ticket    = ticket;
            this.spot      = spot;
            this.entryTime = LocalDateTime.now();
        }
    }

    private final Map<String, Session> sessions = new HashMap<>();

    private BookingSession() {}

    public void put(String ticketId, Session s) { sessions.put(ticketId, s); }
    public Session get(String ticketId)          { return sessions.get(ticketId); }
    public void remove(String ticketId)          { sessions.remove(ticketId); }
}
