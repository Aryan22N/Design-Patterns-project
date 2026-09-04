package com.parking.bridge;

public class EVChargingSpot extends ParkingSpot {
    public EVChargingSpot(String spotId, PricingStrategy pricing, String zone) {
        super(spotId, pricing, zone);
    }
    /** Legacy constructor. */
    public EVChargingSpot(String spotId, PricingStrategy pricing) {
        super(spotId, pricing);
    }
    @Override
    public String getSpotType() { return "EV"; }
}
