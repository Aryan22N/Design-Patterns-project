package com.parking.bridge;

public class FlatRatePricing implements PricingStrategy {
    private final double flatRate;

    /** Creates a FlatRatePricing with the given per-hour rate (e.g. 50.0 → ₹50/hr for Small, 100.0 → ₹100/hr for Medium). */
    public FlatRatePricing(double flatRate) {
        this.flatRate = flatRate;
    }

    /** Default constructor — keeps backward compat; defaults to ₹100. */
    public FlatRatePricing() {
        this(100.0);
    }

    @Override
    public double calcPrice(int hrs) {
        return flatRate * Math.max(hrs, 1);
    }
}
