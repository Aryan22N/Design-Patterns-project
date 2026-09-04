package com.parking.vehicle;

/**
 * Abstract product in the Factory Method pattern.
 * Represents any vehicle that can enter the parking lot.
 */
public abstract class Vehicle {
    private final String licensePlate;

    protected Vehicle(String licensePlate) {
        if (licensePlate == null || !licensePlate.trim().matches("^[A-Za-z0-9 -]{4,15}$")) {
            throw new IllegalArgumentException("Invalid vehicle number format: " + licensePlate);
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
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
