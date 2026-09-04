package com.parking.observer;

public class MobileAppNotifier implements ParkingObserver {
    @Override
    public void update(String spotId, String status) {
        System.out.println("[MobileApp] Push notification: Spot " + spotId + " -> " + status);
    }
}
