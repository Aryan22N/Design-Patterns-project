package com.parking.payment;

/**
 * Strategy-style interface used by the payment handling subsystem
 * (Cash / Card / UPI). RealGateSystem "collects via" one of these.
 */
public interface PaymentMethod {
    boolean pay(double amount);
}
