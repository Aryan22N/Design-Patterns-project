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
import com.parking.observer.ParkingObserver;
import com.parking.bridge.ParkingSpot;
import com.parking.ticket.Ticket;
import com.parking.vehicle.Vehicle;

/**
 * Singleton pattern.
 * Ensures only ONE instance of ParkingLotManager exists and
 * coordinates the whole system: registers spots, runs the entry
 * validation chain, issues tickets and notifies observers of the
 * updated spot status. Same instance is reused across every
 * entry/exit request.
 *
 * Enhanced: chainLog, findSpotsByVehicleSize() for UI integration.
 */
public class ParkingLotManager {

    private static ParkingLotManager instance;

    private final List<ParkingObserver> observers    = new ArrayList<>();
    private final List<ParkingSpot>     spots        = new ArrayList<>();
    private final List<String>          chainLog     = new ArrayList<>();
    private final List<String>          observerLog  = new ArrayList<>();
    private final EntryHandler          entryChain;

    private ParkingLotManager() {
        // Wire up the Chain of Responsibility once, at construction time.
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

    // ── Spot management ──────────────────────────────────────

    public void registerSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    /**
     * Returns spots compatible with a vehicle's size in the requested zone.
     * Vehicle size rules (from Vehicle.getSize()):
     *   1 (Bike)  → Small spot only
     *   2 (Car)   → Medium spot only
     *   3 (Truck) → Large spot only
     */
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
                // If zone is "any" or blank, skip zone filtering
                boolean zoneOk = (zone == null || zone.isBlank() || zone.equalsIgnoreCase("any"))
                    || s.getZone().equalsIgnoreCase(zone);
                return sizeOk && zoneOk;
            })
            .collect(Collectors.toList());
    }

    /** Finds the first free spot of a given type, e.g. "Compact". */
    public Optional<ParkingSpot> findFreeSpotByType(String type) {
        return spots.stream()
                .filter(s -> s.getSpotType().equalsIgnoreCase(type) && !s.isOccupied())
                .findFirst();
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    // ── Observer management ───────────────────────────────────

    public void registerObserver(ParkingObserver o) {
        observers.add(o);
    }

    public void notifyObservers(String spotId, String status) {
        observerLog.clear();
        for (ParkingObserver o : observers) {
            o.update(spotId, status);
            // Capture the observer class name for JSON output
            observerLog.add(o.getClass().getSimpleName() + ": Spot " + spotId + " \u2192 " + status);
        }
    }

    public void notifySpotFreed(String spotId) {
        notifyObservers(spotId, "AVAILABLE");
    }

    // ── Entry processing ──────────────────────────────────────

    /**
     * Runs the vehicle through the entry validation chain and,
     * if every handler passes, issues a Ticket for the given spot.
     */
    public Ticket processVehicleEntry(Vehicle v, ParkingSpot spot, boolean paymentMethodValid) {
        chainLog.clear();
        EntryRequest request = new EntryRequest(v, spot, paymentMethodValid);
        entryChain.handle(request);

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

    // ── Log accessors ─────────────────────────────────────────

    /** Returns chain-of-responsibility log lines captured during last entry. */
    public List<String> getChainLog()    { return chainLog; }

    /** Returns observer notification log lines from last notify call. */
    public List<String> getObserverLog() { return observerLog; }

    /** Append a message to the chain log (called by handler subclasses). */
    public void logChain(String msg) {
        chainLog.add(msg);
    }
}
