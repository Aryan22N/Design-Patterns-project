package com.parking.template;

import com.parking.bridge.ParkingSpot;
import com.parking.payment.PaymentMethod;
import com.parking.proxy.GateAccessProxy;
import com.parking.proxy.RealGateSystem;
import com.parking.singleton.ParkingLotManager;
import com.parking.ticket.Ticket;

/**
 * Template Method Pattern.
 * Defines the invariant skeleton of the vehicle exit and payment workflow.
 * Subclasses customize specific steps (such as discounts, surcharges, or custom gates)
 * without altering the fundamental execution sequence.
 */
public abstract class ParkingExitWorkflow {

    /**
     * The Template Method. Marked final so subclasses cannot alter the step sequence.
     */
    public final ExitResult executeExit(
            Ticket ticket,
            ParkingSpot spot,
            int hours,
            PaymentMethod paymentMethod,
            ParkingLotManager mgr) {

        // Step 1: Invariant validation
        validateTicket(ticket);

        // Step 2: Base fee calculation via Bridge/Strategy
        double baseFee = spot.calculateFee(hours);

        // Step 3: Hook method - allow subclasses to apply discounts or zone surcharges
        double discount = calculateDiscount(ticket, spot, hours, baseFee);
        double finalFee = Math.max(0.0, baseFee - discount);
        ticket.calculateFee(finalFee);

        // Step 4: Collect payment via PaymentMethod (Strategy)
        collectPayment(ticket, paymentMethod, finalFee, spot);

        // Step 5: Release the parking spot
        releaseSpot(spot);

        // Step 6: Notify observers about freed spot
        notifyObservers(mgr, spot.getSpotId());

        // Step 7: Open physical gate via Proxy
        openExitGate(ticket, paymentMethod, spot);

        // Step 8: Post-processing / audit logging hook
        logExit(ticket, finalFee);

        return new ExitResult(ticket.getTicketId(), baseFee, discount, finalFee, "PAID");
    }

    protected void validateTicket(Ticket ticket) {
        if (ticket == null || ticket.getVehiclePlate() == null || ticket.getVehiclePlate().isBlank()) {
            throw new IllegalArgumentException("[Template: Exit] Invalid ticket presented.");
        }
        System.out.println("[Template: Exit] Ticket validated for " + ticket.getVehiclePlate());
    }

    protected void collectPayment(Ticket ticket, PaymentMethod paymentMethod, double fee, ParkingSpot spot) {
        RealGateSystem realGate = new RealGateSystem(paymentMethod, spot);
        boolean success = realGate.collectPayment(ticket, fee);
        if (!success) {
            throw new IllegalStateException("[Template: Exit] Payment failed via " + paymentMethod.getClass().getSimpleName());
        }
    }

    protected void releaseSpot(ParkingSpot spot) {
        spot.release();
    }

    protected void notifyObservers(ParkingLotManager mgr, String spotId) {
        mgr.notifySpotFreed(spotId);
    }

    protected void openExitGate(Ticket ticket, PaymentMethod paymentMethod, ParkingSpot spot) {
        RealGateSystem realGate = new RealGateSystem(paymentMethod, spot);
        GateAccessProxy gateProxy = new GateAccessProxy(realGate);
        gateProxy.processExit(ticket);
    }

    /** Hook method with default implementation (no discount). Overridden by subclasses. */
    protected double calculateDiscount(Ticket ticket, ParkingSpot spot, int hours, double baseFee) {
        return 0.0;
    }

    /** Hook method for logging or telemetry. */
    protected void logExit(Ticket ticket, double fee) {
        System.out.println("[Template: Exit] Exit completed for ticket " + ticket.getTicketId() + " (Fee: ₹" + fee + ")");
    }
}
