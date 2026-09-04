package com.parking.bridge;

/**
 * Bridge pattern - the "Implementor" side.
 * Encapsulates a pricing algorithm independently of the
 * spot hierarchy so spot type and pricing model can vary independently.
 */
public interface PricingStrategy {
    double calcPrice(int hrs);
}
