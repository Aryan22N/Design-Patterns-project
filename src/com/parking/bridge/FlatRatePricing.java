package com.parking.bridge;

public class FlatRatePricing implements PricingStrategy {
    private final double flatRate;

    /** Creates a FlatRatePricing with the given fixed rate (e.g. 50.0 or 100.0). */
    public FlatRatePricing(double flatRate) {
        this.flatRate = flatRate;
    }

    /** Default constructor — keeps backward compat; defaults to ₹100. */
    public FlatRatePricing() {
        this(100.0);
    }

    @Override
    public double calcPrice(int hrs) {
        return flatRate;
    }
}
