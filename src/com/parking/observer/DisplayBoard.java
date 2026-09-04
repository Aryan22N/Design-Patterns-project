package com.parking.observer;

public class DisplayBoard implements ParkingObserver {
    @Override
    public void update(String spotId, String status) {
        System.out.println("[DisplayBoard] Spot " + spotId + " is now " + status);
    }
}
