package com.parking.template;

/**
 * Result data carrier produced by ParkingExitWorkflow.
 */
public class ExitResult {
    private final String ticketId;
    private final double baseFee;
    private final double discount;
    private final double finalFee;
    private final String status;

    public ExitResult(String ticketId, double baseFee, double discount, double finalFee, String status) {
        this.ticketId = ticketId;
        this.baseFee = baseFee;
        this.discount = discount;
        this.finalFee = finalFee;
        this.status = status;
    }

    public String getTicketId() { return ticketId; }
    public double getBaseFee()  { return baseFee; }
    public double getDiscount() { return discount; }
    public double getFinalFee() { return finalFee; }
    public String getStatus()   { return status; }
}
