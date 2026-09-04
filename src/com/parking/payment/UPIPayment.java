package com.parking.payment;

public class UPIPayment implements PaymentMethod {
    @Override
    public boolean pay(double amount) {
        System.out.println("[UPIPayment] UPI payment successful: " + amount);
        return true;
    }
}
