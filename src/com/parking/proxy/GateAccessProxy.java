package com.parking.proxy;

import com.parking.ticket.Ticket;

/**
 * Proxy pattern.
 * Controls access to RealGateSystem: validates the ticket first
 * (access check) and only delegates through if it's genuine and,
 * for exit, fully paid.
 */
public class GateAccessProxy implements GateSystem {
    private final RealGateSystem realGate;

    public GateAccessProxy(RealGateSystem realGate) {
        this.realGate = realGate;
    }

    @Override
    public void processEntry(Ticket t) {
        if (!validateTicket(t)) {
            throw new IllegalArgumentException("Invalid ticket, entry refused.");
        }
        realGate.processEntry(t);
    }

    @Override
    public void processExit(Ticket t) {
        if (!validateTicket(t)) {
            throw new IllegalArgumentException("Invalid ticket, exit refused.");
        }
        realGate.processExit(t);
    }

    /** Access check: validates ticket before reaching RealGateSystem. */
    public boolean validateTicket(Ticket t) {
        boolean valid = t != null && t.getVehiclePlate() != null && !t.getVehiclePlate().isBlank();
        System.out.println("[GateAccessProxy] Ticket validation for "
                + (t != null ? t.getVehiclePlate() : "null") + ": " + valid);
        return valid;
    }
}
