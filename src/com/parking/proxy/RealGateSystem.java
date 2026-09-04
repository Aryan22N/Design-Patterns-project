package com.parking.proxy;

import com.parking.payment.PaymentMethod;
import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;

/**
 * The real service. Physically opens/closes the gate and, on exit,
 * collects payment via whichever PaymentMethod is configured.
 *
 * Exit rule: processExit() only succeeds if ticket.isPaid == true,
 * once payment has been collected the associated ParkingSpot is released.
 */
public class RealGateSystem implements GateSystem {
    private PaymentMethod payment;
    private final ParkingSpot spot;

    public RealGateSystem(PaymentMethod payment, ParkingSpot spot) {
        this.payment = payment;
        this.spot = spot;
    }

    public void setPayment(PaymentMethod payment) {
        this.payment = payment;
    }

    @Override
    public void processEntry(Ticket t) {
        spot.occupy();
        System.out.println("[RealGateSystem] Entry gate opened for " + t.getVehiclePlate());
    }

    @Override
    public void processExit(Ticket t) {
        if (!t.isPaid()) {
            boolean success = collectPayment(t, t.getFee());
            if (!success) {
                throw new IllegalStateException("Payment failed, gate stays closed.");
            }
        }
        spot.release();
        System.out.println("[RealGateSystem] Exit gate opened for " + t.getVehiclePlate()
                + ", spot " + spot.getSpotId() + " released.");
    }

    public boolean collectPayment(Ticket t, double amt) {
        boolean paid = payment.pay(amt);
        if (paid) {
            t.markPaid();
        }
        return paid;
    }
}
