package com.parking.bridge;

import java.time.Duration;
import java.time.LocalDateTime;


public abstract class ParkingSpot {
    private final String spotId;
    protected PricingStrategy pricing;
    private boolean isOccupied;
    private LocalDateTime occupiedSince;
    private final String zone;        // "Premium" | "Regular"
    private final String pricingMode; // "FlatRate" | "Hourly" | "Dynamic"

    protected ParkingSpot(String spotId, PricingStrategy pricing, String zone) {
        this.spotId      = spotId;
        this.pricing     = pricing;
        this.zone        = zone;
        this.pricingMode = resolvePricingMode(pricing);
        this.isOccupied  = false;
    }

    /** Legacy constructor — defaults zone to "Regular". */
    protected ParkingSpot(String spotId, PricingStrategy pricing) {
        this(spotId, pricing, "Regular");
    }

    private static String resolvePricingMode(PricingStrategy p) {
        if (p instanceof FlatRatePricing) return "FlatRate";
        if (p instanceof DynamicPricing)  return "Dynamic";
        return "Hourly";
    }

    // ── Core accessors ────────────────────────────────────────

    public String getSpotId()      { return spotId; }
    public boolean isOccupied()    { return isOccupied; }
    public String getZone()        { return zone; }
    public String getPricingMode() { return pricingMode; }

    public void setPricing(PricingStrategy pricing) {
        this.pricing = pricing;
    }

    /**
     * Entry rule: occupy() rejects the request if the spot is
     * already taken (no double-parking).
     */
    public void occupy() {
        if (isOccupied) {
            throw new IllegalStateException("Spot " + spotId + " is already occupied");
        }
        isOccupied    = true;
        occupiedSince = LocalDateTime.now();
    }

    public void release() {
        isOccupied    = false;
        occupiedSince = null;
    }

    public double calculateFee(int hrs) {
        return pricing.calcPrice(hrs);
    }

    public LocalDateTime getOccupiedSince() {
        return occupiedSince;
    }

    public long hoursOccupied() {
        if (occupiedSince == null) return 0;
        return Math.max(1, Duration.between(occupiedSince, LocalDateTime.now()).toHours());
    }

    /** Human-readable rate description for UI display. */
    public String getRateDescription() {
        switch (pricingMode) {
            case "FlatRate": return "\u20b9" + (int) pricing.calcPrice(1) + "/hr";
            case "Dynamic":  return "\u20b9" + "15/hr (surge after 4h)";
            default:         return "\u20b9" + "20/hr";
        }
    }

    public abstract String getSpotType();
}
