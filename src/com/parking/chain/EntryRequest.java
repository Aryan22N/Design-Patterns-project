package com.parking.chain;

import com.parking.bridge.ParkingSpot;
import com.parking.vehicle.Vehicle;

/**
 * Carries everything the chain of handlers needs to validate
 * a vehicle entry request in one pass.
 */
public class EntryRequest {
    private final Vehicle vehicle;
    private final ParkingSpot candidateSpot;
    private final boolean paymentMethodValid;

    public EntryRequest(Vehicle vehicle, ParkingSpot candidateSpot, boolean paymentMethodValid) {
        this.vehicle = vehicle;
        this.candidateSpot = candidateSpot;
        this.paymentMethodValid = paymentMethodValid;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getCandidateSpot() {
        return candidateSpot;
    }

    public boolean isPaymentMethodValid() {
        return paymentMethodValid;
    }
}
