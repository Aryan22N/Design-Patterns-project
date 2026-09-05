package com.parking.payment;

public class CardPayment implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("[CardPayment] Charged card: " + amount);
        return true;
    }
}
 