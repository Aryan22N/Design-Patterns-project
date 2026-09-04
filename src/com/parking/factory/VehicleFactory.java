package com.parking.factory;

import com.parking.vehicle.Vehicle;

public abstract class VehicleFactory {
    public abstract Vehicle createVehicle(String plate);
}
