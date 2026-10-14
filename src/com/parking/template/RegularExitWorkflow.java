package com.parking.template;

import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;

/**
 * Concrete implementation of ParkingExitWorkflow for regular parking zones.
 * Standard processing with standard gate and no special discounts.
 */
public class RegularExitWorkflow extends ParkingExitWorkflow {

    @Override
    protected double calculateDiscount(Ticket ticket, ParkingSpot spot, int hours, double baseFee) {
        // Standard regular zone: no promotional discount applied
        return 0.0;
    }

    @Override
    protected void logExit(Ticket ticket, double fee) {
        System.out.println("[RegularExitWorkflow] Processed standard exit for " + ticket.getVehiclePlate());
    }
}
