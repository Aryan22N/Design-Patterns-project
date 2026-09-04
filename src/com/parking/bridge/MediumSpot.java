package com.parking.bridge;

public class MediumSpot extends ParkingSpot {
    public MediumSpot(String spotId, PricingStrategy pricing, String zone) {
        super(spotId, pricing, zone);
    }
    public MediumSpot(String spotId, PricingStrategy pricing) {
        super(spotId, pricing);
    }
    @Override
    public String getSpotType() { return "Medium"; }
}
