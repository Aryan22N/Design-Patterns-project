package com.parking.proxy;

import com.parking.ticket.Ticket;

/**
 * Proxy pattern subject interface, shared by RealGateSystem
 * and the GateAccessProxy that guards it.
 */
public interface GateSystem {
    void processEntry(Ticket t);
    void processExit(Ticket t);
}
