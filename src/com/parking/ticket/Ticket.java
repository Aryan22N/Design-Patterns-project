package com.parking.ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data object produced on vehicle entry and consumed on exit.
 * Bridges the fee calculation (via the spot's PricingStrategy,
 * applied by ParkingLotManager) with the exit/payment flow.
 *
 * Enhanced: ticketId, zone, pricingMode, toJson() for UI integration.
 */
public class Ticket {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String ticketId;
    private final String vehiclePlate;
    private final String vehicleType;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private final String spotId;
    private final String zone;
    private final String pricingMode;
    private double fee;
    private boolean isPaid;
    private String paymentMethod;

    public Ticket(String vehiclePlate, String vehicleType, String spotId, String zone, String pricingMode) {
        this.ticketId     = "TKT-" + LocalDateTime.now().format(FMT) + "-" + String.format("%03d", COUNTER.getAndIncrement());
        this.vehiclePlate = vehiclePlate;
        this.vehicleType  = vehicleType;
        this.spotId       = spotId;
        this.zone         = zone;
        this.pricingMode  = pricingMode;
        this.entryTime    = LocalDateTime.now();
        this.isPaid       = false;
    }

    /* ── legacy constructor so old Main.java still compiles ── */
    public Ticket(String vehiclePlate, String vehicleType, String spotId) {
        this(vehiclePlate, vehicleType, spotId, "Regular", "Hourly");
    }

    // ── Getters ──────────────────────────────────────────────

    public String getTicketId()      { return ticketId; }
    public String getVehiclePlate()  { return vehiclePlate; }
    public String getVehicleType()   { return vehicleType; }
    public String getSpotId()        { return spotId; }
    public String getZone()          { return zone; }
    public String getPricingMode()   { return pricingMode; }
    public boolean isPaid()          { return isPaid; }
    public double getFee()           { return fee; }
    public String getPaymentMethod() { return paymentMethod; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime()  { return exitTime; }

    /** Duration in whole hours (minimum 1) since entry. */
    public int getDuration() {
        LocalDateTime end = exitTime != null ? exitTime : LocalDateTime.now();
        long hrs = ChronoUnit.HOURS.between(entryTime, end);
        return (int) Math.max(1, hrs);
    }

    public double calculateFee(double calculatedFee) {
        this.fee = calculatedFee;
        return fee;
    }

    public void markPaid(String method) {
        this.isPaid        = true;
        this.paymentMethod = method;
        this.exitTime      = LocalDateTime.now();
    }

    /** Backward-compat overload. */
    public void markPaid() {
        markPaid("Unknown");
    }

    // ── JSON serialisation ────────────────────────────────────

    /** Returns a single-line JSON string representing this ticket. */
    public String toJson() {
        return "{"
            + "\"ticketId\":\""      + ticketId                          + "\","
            + "\"plate\":\""         + vehiclePlate                      + "\","
            + "\"vehicleType\":\""   + vehicleType                       + "\","
            + "\"spotId\":\""        + spotId                            + "\","
            + "\"zone\":\""          + zone                              + "\","
            + "\"pricingMode\":\""   + pricingMode                       + "\","
            + "\"entryTime\":\""     + entryTime.format(DISPLAY_FMT)     + "\","
            + "\"exitTime\":\""      + (exitTime != null ? exitTime.format(DISPLAY_FMT) : "") + "\","
            + "\"hoursParked\":"     + getDuration()                     + ","
            + "\"fee\":"             + fee                               + ","
            + "\"paid\":"            + isPaid                            + ","
            + "\"paymentMethod\":\"" + (paymentMethod != null ? paymentMethod : "") + "\""
            + "}";
    }

    @Override
    public String toString() {
        return "Ticket{id=" + ticketId + ", plate=" + vehiclePlate + ", type=" + vehicleType
                + ", spot=" + spotId + ", zone=" + zone + ", fee=" + fee + ", paid=" + isPaid + "}";
    }
}
