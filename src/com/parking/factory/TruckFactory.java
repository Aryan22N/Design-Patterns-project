package com.parking.factory;

import com.parking.vehicle.Truck;
import com.parking.vehicle.Vehicle;

public class TruckFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String plate) {
        return new Truck(plate);
    }
}
