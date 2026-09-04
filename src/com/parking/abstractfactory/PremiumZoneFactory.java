package com.parking.abstractfactory;

import com.parking.bridge.DynamicPricing;
import com.parking.bridge.FlatRatePricing;
import com.parking.bridge.LargeSpot;
import com.parking.bridge.MediumSpot;
import com.parking.bridge.ParkingSpot;
import com.parking.bridge.SmallSpot;


public class PremiumZoneFactory extends ParkingSpotFactory {
    @Override
    public ParkingSpot createSmallSpot(String spotId) {
        return new SmallSpot(spotId, new FlatRatePricing(150.0), "Premium");
    }

    @Override
    public ParkingSpot createMediumSpot(String spotId) {
        return new MediumSpot(spotId, new FlatRatePricing(250.0), "Premium");
    }

    @Override
    public ParkingSpot createLargeSpot(String spotId) {
        return new LargeSpot(spotId, new FlatRatePricing(500.0), "Premium");
    }
}
