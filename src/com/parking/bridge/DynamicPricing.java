package com.parking.bridge;

/**
 * Simple surge-style pricing: rate increases the longer the
 * vehicle stays, to encourage turnover.
 */
public class DynamicPricing implements PricingStrategy {
    private static final double BASE_RATE = 15.0;

    @Override
    public double calcPrice(int hrs) {
        int h = Math.max(hrs, 1);
        double surgeMultiplier = 1 + (h > 4 ? 0.5 : 0);
        return h * BASE_RATE * surgeMultiplier;
    }
}
