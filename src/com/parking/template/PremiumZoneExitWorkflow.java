package com.parking.template;

import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;

/**
 * Concrete implementation of ParkingExitWorkflow for Premium parking zones.
 * Overrides the discount hook to reward long-stay patrons and VIP customers.
 */
public class PremiumZoneExitWorkflow extends ParkingExitWorkflow {

    @Override
    protected double calculateDiscount(Ticket ticket, ParkingSpot spot, int hours, double baseFee) {
        // Loyalty perk: 10% discount if parked for more than 3 hours in premium zone
        if (hours >= 3) {
            double discount = baseFee * 0.10;
            System.out.println("[PremiumZoneExitWorkflow] Applied 10% long-stay premium member discount: ₹" + discount);
            return discount;
        }
        return 0.0;
    }

    @Override
    protected void logExit(Ticket ticket, double fee) {
        System.out.println("[PremiumZoneExitWorkflow] Priority exit concierge logged for " + ticket.getVehiclePlate());
    }
}
