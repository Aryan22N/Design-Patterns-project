package com.parking.bridge;

public class HourlyPricing implements PricingStrategy {
    private final double ratePerHour;

    public HourlyPricing(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    public HourlyPricing() {
        this(50.0);
    }

    @Override
    public double calcPrice(int hrs) {
        return Math.max(hrs, 1) * ratePerHour;
    }
}
