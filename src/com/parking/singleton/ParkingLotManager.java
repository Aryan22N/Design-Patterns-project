package com.parking.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.parking.chain.EntryHandler;
import com.parking.chain.EntryRequest;
import com.parking.chain.GateOpenHandler;
import com.parking.chain.PaymentValidationHandler;
import com.parking.chain.SpotAvailabilityHandler;
import com.parking.chain.VehicleTypeCheckHandler;
import com.parking.database.SpotDAO;
import com.parking.observer.ParkingObserver;
import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;
import com.parking.vehicle.Vehicle;

public class ParkingLotManager {

    private static ParkingLotManager instance;

    private final List<ParkingObserver> observers    = new ArrayList<>();
    private final List<ParkingSpot>     spots        = new ArrayList<>();
    private final List<String>          chainLog     = new ArrayList<>();
    private final List<String>          observerLog  = new ArrayList<>();
    private final EntryHandler          entryChain;
    private final SpotDAO               spotDAO      = new SpotDAO();

    private ParkingLotManager() {
        EntryHandler vehicleTypeCheck  = new VehicleTypeCheckHandler();
        EntryHandler spotAvailability  = new SpotAvailabilityHandler();
        EntryHandler paymentValidation = new PaymentValidationHandler();
        EntryHandler gateOpen          = new GateOpenHandler();

        vehicleTypeCheck.setNext(spotAvailability);
        spotAvailability.setNext(paymentValidation);
        paymentValidation.setNext(gateOpen);

        this.entryChain = vehicleTypeCheck;
    }

    public static synchronized ParkingLotManager getInstance() {
        if (instance == null) {
            instance = new ParkingLotManager();
        }
        return instance;
    }

    public void loadSpotsFromDAO() {
        List<ParkingSpot> loaded = spotDAO.getAllSpots();
        if (!loaded.isEmpty()) {
            spots.clear();
            spots.addAll(loaded);
        }
    }

    public void registerSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public List<ParkingSpot> findSpotsByVehicleSize(int vehicleSize, String zone) {
        return spots.stream()
            .filter(s -> {
                String type = s.getSpotType().toLowerCase();
                boolean sizeOk;
                switch (vehicleSize) {
                    case 1:  sizeOk = type.equals("small"); break;
                    case 2:  sizeOk = type.equals("medium"); break;
                    default: sizeOk = type.equals("large"); break;
                }
                boolean zoneOk = (zone == null || zone.isBlank() || zone.equalsIgnoreCase("any"))
                    || s.getZone().equalsIgnoreCase(zone);
                return sizeOk && zoneOk;
            })
            .collect(Collectors.toList());
    }

    public Optional<ParkingSpot> findFreeSpotByType(String type) {
        return spots.stream()
                .filter(s -> s.getSpotType().equalsIgnoreCase(type) && !s.isOccupied())
                .findFirst();
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    public void registerObserver(ParkingObserver o) {
        observers.add(o);
    }

    public synchronized void notifyObservers(String spotId, String status) {
        observerLog.clear();
        for (ParkingObserver o : observers) {
            o.update(spotId, status);
            observerLog.add(o.getClass().getSimpleName() + ": Spot " + spotId + " \u2192 " + status);
        }
    }

    public synchronized void notifySpotFreed(String spotId) {
        // Update in-memory spot
        spots.stream()
             .filter(s -> s.getSpotId().equalsIgnoreCase(spotId) || s.getSpotId().replace("-", "").equalsIgnoreCase(spotId.replace("-", "")))
             .findFirst()
             .ifPresent(s -> s.release());

        // Persist to MySQL Database via SpotDAO
        spotDAO.updateSpotStatus(spotId, false);

        notifyObservers(spotId, "AVAILABLE");
    }

    public synchronized Ticket processVehicleEntry(Vehicle v, ParkingSpot spot, boolean paymentMethodValid) {
        chainLog.clear();
        EntryRequest request = new EntryRequest(v, spot, paymentMethodValid);
        entryChain.handle(request);

        // Occupy in-memory spot
        spot.occupy();

        // Update database spot status via SpotDAO
        spotDAO.updateSpotStatus(spot.getSpotId(), true);

        Ticket ticket = new Ticket(
            v.getLicensePlate(),
            v.getType(),
            spot.getSpotId(),
            spot.getZone(),
            spot.getPricingMode()
        );
        notifyObservers(spot.getSpotId(), "OCCUPIED");
        return ticket;
    }

    /** Returns chain-of-responsibility log lines captured during last entry. */
    public synchronized List<String> getChainLog()    { return new ArrayList<>(chainLog); }

    /** Returns observer notification log lines from last notify call. */
    public synchronized List<String> getObserverLog() { return new ArrayList<>(observerLog); }

    /** Append a message to the chain log (called by handler subclasses). */
    public synchronized void logChain(String msg) {
        chainLog.add(msg);
    }
}
