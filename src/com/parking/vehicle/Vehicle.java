package com.parking.vehicle;

/**
 * Abstract product in the Factory Method pattern.
 * Represents any vehicle that can enter the parking lot.
 */
public abstract class Vehicle {
    private final String licensePlate;

    protected Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public abstract String getType();

    /** Rough size classification used later to pick a matching spot. */
    public abstract int getSize();

    @Override
    public String toString() {
        return getType() + "[" + licensePlate + "]";
    }
}
