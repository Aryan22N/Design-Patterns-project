package com.parking.vehicle;

public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public String getType() {
        return "Truck";
    }

    @Override
    public int getSize() {
        return 3; // large
    }
}
