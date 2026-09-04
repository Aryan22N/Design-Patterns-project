package com.parking.observer;

/**
 * Observer pattern.
 * Any component that needs live availability updates
 * implements this interface and registers with ParkingLotManager.
 */
public interface ParkingObserver {
    void update(String spotId, String status);
}
