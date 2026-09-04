package com.parking.factory;

import com.parking.vehicle.Car;
import com.parking.vehicle.Vehicle;

public class CarFactory extends VehicleFactory {
    @Override
    public Vehicle createVehicle(String plate) {
        return new Car(plate);
    }
}
