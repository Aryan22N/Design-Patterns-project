package com.parking.builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a rich reservation request constructed using the Builder Pattern.
 * 
 * Problem Solved:
 * Prevents "telescoping constructors" and fragile positional string parameter lists
 * when booking spots with multiple mandatory and optional attributes (promo codes,
 * pre-booked hours, VIP perks, EV charging, contact info).
 */
public class ParkingReservation {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Mandatory fields
    private final String licensePlate;
    private final String vehicleType;

    // Optional / Configurable fields
    private final String zone;
    private final String spotId;
    private final String spotType;
    private final String paymentMethod;
    private final String promoCode;
    private final int preBookedHours;
    private final boolean requiresEVCharging;
    private final boolean requiresValet;
    private final String contactPhone;
    private final String customerEmail;
    private final LocalDateTime reservationTime;

    /**
     * Private constructor enforced so creation MUST occur through the Builder.
     */
    private ParkingReservation(Builder builder) {
        this.licensePlate        = builder.licensePlate;
        this.vehicleType         = builder.vehicleType;
        this.zone                = builder.zone;
        this.spotId              = builder.spotId;
        this.spotType            = builder.spotType;
        this.paymentMethod       = builder.paymentMethod;
        this.promoCode           = builder.promoCode;
        this.preBookedHours      = builder.preBookedHours;
        this.requiresEVCharging  = builder.requiresEVCharging;
        this.requiresValet       = builder.requiresValet;
        this.contactPhone        = builder.contactPhone;
        this.customerEmail       = builder.customerEmail;
        this.reservationTime     = builder.reservationTime != null ? builder.reservationTime : LocalDateTime.now();
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public String getLicensePlate()       { return licensePlate; }
    public String getVehicleType()        { return vehicleType; }
    public String getZone()               { return zone; }
    public String getSpotId()             { return spotId; }
    public String getSpotType()           { return spotType; }
    public String getPaymentMethod()      { return paymentMethod; }
    public String getPromoCode()          { return promoCode; }
    public int getPreBookedHours()        { return preBookedHours; }
    public boolean isRequiresEVCharging() { return requiresEVCharging; }
    public boolean isRequiresValet()      { return requiresValet; }
    public String getContactPhone()       { return contactPhone; }
    public String getCustomerEmail()      { return customerEmail; }
    public LocalDateTime getReservationTime() { return reservationTime; }

    public String toJson() {
        return "{"
            + "\"plate\":\"" + licensePlate + "\","
            + "\"vehicleType\":\"" + vehicleType + "\","
            + "\"zone\":\"" + zone + "\","
            + "\"spotId\":\"" + spotId + "\","
            + "\"spotType\":\"" + spotType + "\","
            + "\"paymentMethod\":\"" + paymentMethod + "\","
            + "\"promoCode\":\"" + (promoCode != null ? promoCode : "") + "\","
            + "\"preBookedHours\":" + preBookedHours + ","
            + "\"evCharging\":" + requiresEVCharging + ","
            + "\"valet\":" + requiresValet + ","
            + "\"reservationTime\":\"" + reservationTime.format(FMT) + "\""
            + "}";
    }

    @Override
    public String toString() {
        return "ParkingReservation{"
            + "plate='" + licensePlate + '\''
            + ", type='" + vehicleType + '\''
            + ", spot='" + spotId + '\''
            + ", zone='" + zone + '\''
            + ", promo='" + promoCode + '\''
            + ", ev=" + requiresEVCharging
            + ", valet=" + requiresValet
            + '}';
    }

    // ── Fluent Builder ──────────────────────────────────────────────────────

    public static class Builder {
        // Mandatory
        private final String licensePlate;
        private final String vehicleType;

        // Optional with sensible enterprise defaults
        private String zone = "Regular";
        private String spotId;
        private String spotType = "Medium";
        private String paymentMethod = "Card";
        private String promoCode;
        private int preBookedHours = 1;
        private boolean requiresEVCharging = false;
        private boolean requiresValet = false;
        private String contactPhone;
        private String customerEmail;
        private LocalDateTime reservationTime;

        /**
         * Enforces mandatory fields at compile-time via Builder constructor.
         */
        public Builder(String licensePlate, String vehicleType) {
            if (licensePlate == null || licensePlate.trim().isEmpty()) {
                throw new IllegalArgumentException("[Builder Error] License plate cannot be null or empty.");
            }
            if (vehicleType == null || vehicleType.trim().isEmpty()) {
                throw new IllegalArgumentException("[Builder Error] Vehicle type cannot be null or empty.");
            }
            this.licensePlate = licensePlate.trim().toUpperCase();
            this.vehicleType  = vehicleType.trim();
        }

        public Builder inZone(String zone) {
            if (zone != null && !zone.isBlank()) {
                this.zone = zone.trim();
            }
            return this;
        }

        public Builder forSpot(String spotId, String spotType) {
            this.spotId = spotId;
            if (spotType != null && !spotType.isBlank()) {
                this.spotType = spotType.trim();
            }
            return this;
        }

        public Builder withPaymentMethod(String paymentMethod) {
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                this.paymentMethod = paymentMethod.trim();
            }
            return this;
        }

        public Builder withPromoCode(String promoCode) {
            if (promoCode != null && !promoCode.isBlank()) {
                this.promoCode = promoCode.trim().toUpperCase();
            }
            return this;
        }

        public Builder withPreBookedHours(int hours) {
            this.preBookedHours = Math.max(1, hours);
            return this;
        }

        public Builder withEVCharging(boolean evCharging) {
            this.requiresEVCharging = evCharging;
            return this;
        }

        public Builder withValet(boolean valet) {
            this.requiresValet = valet;
            return this;
        }

        public Builder withContact(String phone, String email) {
            this.contactPhone  = phone;
            this.customerEmail = email;
            return this;
        }

        public Builder atTime(LocalDateTime reservationTime) {
            this.reservationTime = reservationTime;
            return this;
        }

        /**
         * Validates consistency across properties before creating the immutable ParkingReservation.
         */
        public ParkingReservation build() {
            if (!this.licensePlate.matches("^[A-Za-z0-9 -]{4,15}$")) {
                throw new IllegalArgumentException("[Builder Error] Invalid license plate format: " + this.licensePlate);
            }
            if (this.requiresEVCharging && !this.spotType.equalsIgnoreCase("Large") && !this.spotType.equalsIgnoreCase("Medium")) {
                System.out.println("[Builder Warning] EV charging requested on small spot; verifying electrical conduit compatibility.");
            }
            return new ParkingReservation(this);
        }
    }
}
