package com.parking.chain;

/**
 * Rejects entry if the assigned spot is already occupied.
 */
public class SpotAvailabilityHandler extends EntryHandler {
    @Override
    public void handle(EntryRequest request) {
        if (request.getCandidateSpot() == null || request.getCandidateSpot().isOccupied()) {
            throw new IllegalStateException("No available spot, entry denied.");
        }
        System.out.println("[SpotAvailabilityHandler] Spot "
                + request.getCandidateSpot().getSpotId() + " is free.");
        passToNext(request);
    }
}
