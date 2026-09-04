package com.parking.factory;

import com.parking.vehicle.Vehicle;

/**
 * Factory Method pattern.
 * Declares the factory method that subclasses override to
 * instantiate a specific concrete Vehicle.
 */
public abstract class VehicleFactory {
    public abstract Vehicle createVehicle(String plate);
}
