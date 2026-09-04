package com.parking.bridge;

public class HourlyPricing implements PricingStrategy {
    private static final double RATE_PER_HOUR = 20.0;

    @Override
    public double calcPrice(int hrs) {
        return Math.max(hrs, 1) * RATE_PER_HOUR;
    }
}
