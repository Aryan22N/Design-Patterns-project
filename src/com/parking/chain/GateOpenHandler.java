package com.parking.chain;

/**
 * Terminal handler: opens the gate once every prior check passed.
 */
public class GateOpenHandler extends EntryHandler {
    @Override
    public void handle(EntryRequest request) {
        System.out.println("[GateOpenHandler] Gate opening for "
                + request.getVehicle().getLicensePlate());
        passToNext(request);
    }
}
