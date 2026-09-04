package com.parking.vehicle;

public class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public String getType() {
        return "Bike";
    }

    @Override
    public int getSize() {
        return 1; // small
    }
}
