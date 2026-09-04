package com.parking.vehicle;

public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public String getType() {
        return "Car";
    }

    @Override
    public int getSize() {
        return 2; // medium
    }
}
