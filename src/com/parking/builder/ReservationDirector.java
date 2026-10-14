package com.parking.builder;

/**
 * Director class (GoF Builder Pattern).
 * Encapsulates pre-configured, standard reservation recipes using ParkingReservation.Builder.
 */
public class ReservationDirector {

    /**
     * Builds a standard VIP reservation with premium zone, valet service, and priority card billing.
     */
    public static ParkingReservation constructVIPReservation(String plate, String vehicleType, String spotId) {
        return new ParkingReservation.Builder(plate, vehicleType)
                .inZone("Premium")
                .forSpot(spotId, "Large")
                .withPaymentMethod("Card")
                .withValet(true)
                .withPromoCode("VIPMEMBER")
                .withPreBookedHours(4)
                .build();
    }

    /**
     * Builds an EV charging reservation with EV charging hook enabled and UPI quick billing.
     */
    public static ParkingReservation constructEVReservation(String plate, String spotId) {
        return new ParkingReservation.Builder(plate, "Car")
                .inZone("Premium")
                .forSpot(spotId, "Medium")
                .withPaymentMethod("UPI")
                .withEVCharging(true)
                .withPromoCode("GOGREEN")
                .withPreBookedHours(3)
                .build();
    }

    /**
     * Builds a standard commuter reservation.
     */
    public static ParkingReservation constructStandardReservation(String plate, String vehicleType, String spotId, String spotType) {
        return new ParkingReservation.Builder(plate, vehicleType)
                .inZone("Regular")
                .forSpot(spotId, spotType)
                .withPaymentMethod("UPI")
                .withPreBookedHours(1)
                .build();
    }
}
