package com.parking.abstractfactory;

import com.parking.bridge.ParkingSpot;


public abstract class ParkingSpotFactory {
    public abstract ParkingSpot createSmallSpot(String spotId);
    public abstract ParkingSpot createMediumSpot(String spotId);
    public abstract ParkingSpot createLargeSpot(String spotId);
}
