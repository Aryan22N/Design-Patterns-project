package com.parking.factory;

import com.parking.vehicle.Bike;
import com.parking.vehicle.Vehicle;

public class BikeFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String plate) {
        return new Bike(plate);
    }
}
