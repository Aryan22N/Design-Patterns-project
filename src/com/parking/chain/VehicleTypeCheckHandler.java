package com.parking.chain;

/**
 * Confirms the vehicle type is allowed in the lot.
 */
public class VehicleTypeCheckHandler extends EntryHandler {
    @Override
    public void handle(EntryRequest request) {
        String type = request.getVehicle().getType();
        String plate = request.getVehicle().getLicensePlate();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Vehicle type is not recognized, entry denied.");
        }
        if (plate == null || !plate.trim().matches("^[A-Za-z0-9 -]{4,15}$")) {
            throw new IllegalArgumentException("Invalid vehicle number format, entry denied.");
        }
        System.out.println("[VehicleTypeCheckHandler] " + type + " [" + plate + "] is valid & allowed.");
        passToNext(request);
    }
}
