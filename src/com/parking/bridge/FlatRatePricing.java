package com.parking.bridge;

public class FlatRatePricing implements PricingStrategy {
    private static final double FLAT_RATE = 100.0;

    @Override
    public double calcPrice(int hrs) {
        return FLAT_RATE;
    }
}
