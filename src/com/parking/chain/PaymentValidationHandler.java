package com.parking.chain;

/**
 * Confirms the payment method / channel is valid before opening the gate.
 */
public class PaymentValidationHandler extends EntryHandler {
    @Override
    public void handle(EntryRequest request) {
        if (!request.isPaymentMethodValid()) {
            throw new IllegalStateException("Payment method invalid, entry denied.");
        }
        System.out.println("[PaymentValidationHandler] Payment channel verified.");
        passToNext(request);
    }
}
