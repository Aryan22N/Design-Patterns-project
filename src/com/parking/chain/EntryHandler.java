package com.parking.chain;

/**
 * Chain of Responsibility pattern.
 * Each concrete handler validates one aspect of the entry request
 * and, if it passes, forwards the request to the next handler.
 */
public abstract class EntryHandler {
    protected EntryHandler next;

    public void setNext(EntryHandler h) {
        this.next = h;
    }

    public abstract void handle(EntryRequest request);

    protected void passToNext(EntryRequest request) {
        if (next != null) {
            next.handle(request);
        } else {
            System.out.println("[Chain] All checks passed, entry authorized.");
        }
    }
}
