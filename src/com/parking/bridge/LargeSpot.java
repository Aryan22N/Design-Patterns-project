package com.parking.bridge;

public class LargeSpot extends ParkingSpot {
    public LargeSpot(String spotId, PricingStrategy pricing, String zone) {
        super(spotId, pricing, zone);
    }
    /** Legacy constructor. */
    public LargeSpot(String spotId, PricingStrategy pricing) {
        super(spotId, pricing);
    }
    @Override
    public String getSpotType() { return "Large"; }
}
