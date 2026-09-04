package com.parking.abstractfactory;

import com.parking.bridge.HourlyPricing;
import com.parking.bridge.LargeSpot;
import com.parking.bridge.MediumSpot;
import com.parking.bridge.ParkingSpot;
import com.parking.bridge.SmallSpot;

/**
 * Concrete factory for the "Regular" zone: everyday hourly pricing.
 */
public class RegularZoneFactory extends ParkingSpotFactory {
    @Override
    public ParkingSpot createSmallSpot(String spotId) {
        return new SmallSpot(spotId, new HourlyPricing(), "Regular");
    }

    @Override
    public ParkingSpot createMediumSpot(String spotId) {
        return new MediumSpot(spotId, new HourlyPricing(), "Regular");
    }

    @Override
    public ParkingSpot createLargeSpot(String spotId) {
        return new LargeSpot(spotId, new HourlyPricing(), "Regular");
    }
}
