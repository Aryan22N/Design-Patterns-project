package com.parking.abstractfactory;

import com.parking.bridge.DynamicPricing;
import com.parking.bridge.FlatRatePricing;
import com.parking.bridge.LargeSpot;
import com.parking.bridge.MediumSpot;
import com.parking.bridge.ParkingSpot;
import com.parking.bridge.SmallSpot;

/**
 * Concrete factory for the "Premium" zone: flat rate for small & medium
 * spots and surge/dynamic pricing for large spots.
 */
public class PremiumZoneFactory extends ParkingSpotFactory {
    @Override
    public ParkingSpot createSmallSpot(String spotId) {
        return new SmallSpot(spotId, new FlatRatePricing(50.0), "Premium");
    }

    @Override
    public ParkingSpot createMediumSpot(String spotId) {
        return new MediumSpot(spotId, new FlatRatePricing(), "Premium");
    }

    @Override
    public ParkingSpot createLargeSpot(String spotId) {
        return new LargeSpot(spotId, new DynamicPricing(), "Premium");
    }
}
