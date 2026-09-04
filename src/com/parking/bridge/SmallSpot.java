package com.parking.bridge;

public class SmallSpot extends ParkingSpot {
    public SmallSpot(String spotId, PricingStrategy pricing, String zone) {
        super(spotId, pricing, zone);
    }
    public SmallSpot(String spotId, PricingStrategy pricing) {
        super(spotId, pricing);
    }
    @Override
    public String getSpotType() { return "Small"; }
}
