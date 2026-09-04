package com.parking.payment;

public class CashPayment implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("[CashPayment] Received cash: " + amount);
        return true;
    }
}
