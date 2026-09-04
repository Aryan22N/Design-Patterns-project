package com.parking.chain;

/**
 * Confirms the vehicle type is allowed in the lot.
 */
public class VehicleTypeCheckHandler extends EntryHandler {
    @Override
    public void handle(EntryRequest request) {
        String type = request.getVehicle().getType();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Vehicle type is not recognized, entry denied.");
        }
        System.out.println("[VehicleTypeCheckHandler] " + type + " is allowed.");
        passToNext(request);
    }
}
