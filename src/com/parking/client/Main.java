package com.parking.client;

import com.parking.factory.BikeFactory;
import com.parking.factory.CarFactory;
import com.parking.factory.TruckFactory;
import com.parking.factory.VehicleFactory;
import com.parking.singleton.ParkingLotManager;
import com.parking.observer.DisplayBoard;
import com.parking.observer.MobileAppNotifier;
import com.parking.payment.CardPayment;
import com.parking.payment.CashPayment;
import com.parking.payment.PaymentMethod;
import com.parking.payment.UPIPayment;
import com.parking.proxy.GateAccessProxy;
import com.parking.proxy.RealGateSystem;
import com.parking.bridge.ParkingSpot;
import com.parking.abstractfactory.ParkingSpotFactory;
import com.parking.abstractfactory.PremiumZoneFactory;
import com.parking.abstractfactory.RegularZoneFactory;
import com.parking.database.SpotDAO;
import com.parking.database.TicketDAO;
import com.parking.database.VehicleDAO;
import com.parking.ticket.Ticket;
import com.parking.vehicle.Vehicle;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * CLI command dispatcher.
 *
 * Usage:
 *   java com.parking.client.Main QUERY_SPOTS <vehicleType> <zone>
 *   java com.parking.client.Main BOOK  <vehicleType> <plate> <zone> <spotId> <spotType> <paymentMethod>
 *   java com.parking.client.Main EXIT  <ticketId> <plate> <zone> <spotId> <spotType> <hoursParked>
 *   java com.parking.client.Main PAY   <ticketId> <plate> <vehicleType> <zone> <spotId> <spotType> <hoursParked> <paymentMethod>
 *
 * Each command prints exactly ONE line of JSON to stdout.
 */
public class Main {

    private static final String[][] SPOT_CATALOG = {
        { "S-01", "Small",  "Premium" },
        { "S-02", "Small",  "Premium" },
        { "S-03", "Small",  "Regular" },
        { "S-04", "Small",  "Regular" },
        { "M-01", "Medium", "Premium" },
        { "M-02", "Medium", "Premium" },
        { "M-03", "Medium", "Regular" },
        { "M-04", "Medium", "Regular" },
        { "L-01", "Large",  "Premium" },
        { "L-02", "Large",  "Premium" },
        { "L-03", "Large",  "Regular" },
        { "L-04", "Large",  "Regular" },
    };

    private static final VehicleDAO vehicleDAO = new VehicleDAO();
    private static final TicketDAO  ticketDAO  = new TicketDAO();
    private static final SpotDAO    spotDAO    = new SpotDAO();

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        if (args.length < 1) {
            printError("No command specified. Use QUERY_SPOTS | BOOK | EXIT | PAY");
            return;
        }

        String cmd = args[0].toUpperCase();
        try {
            switch (cmd) {
                case "QUERY_SPOTS": handleQuerySpots(args); break;
                case "BOOK":        handleBook(args);       break;
                case "EXIT":        handleExit(args);       break;
                case "PAY":         handlePay(args);        break;
                default:
                    printError("Unknown command: " + cmd);
            }
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    private static void handleQuerySpots(String[] args) {
        String vehicleType = args.length > 1 ? args[1] : "Car";
        String zone        = args.length > 2 ? args[2] : "any";

        int vehicleSize = resolveVehicleSize(vehicleType);

        ParkingLotManager mgr = buildSeededManager();

        List<ParkingSpot> eligible = mgr.findSpotsByVehicleSize(vehicleSize, zone);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"vehicleType\":\"").append(vehicleType).append("\",");
        sb.append("\"zone\":\"").append(zone).append("\",");
        sb.append("\"vehicleSize\":").append(vehicleSize).append(",");
        sb.append("\"eligibleSpots\":[");
        for (int i = 0; i < eligible.size(); i++) {
            ParkingSpot s = eligible.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"spotId\":\"").append(s.getSpotId()).append("\",")
              .append("\"spotType\":\"").append(s.getSpotType()).append("\",")
              .append("\"zone\":\"").append(s.getZone()).append("\",")
              .append("\"pricingMode\":\"").append(s.getPricingMode()).append("\",")
              .append("\"rate\":\"").append(s.getRateDescription()).append("\",")
              .append("\"available\":").append(!s.isOccupied())
              .append("}");
        }
        sb.append("]}");
        System.out.println(sb);
    }

    private static void handleBook(String[] args) {
        String vehicleType   = args[1];
        String plate         = args[2];
        String zone          = args[3];
        String spotId        = args[4];
        String spotType      = args[5];
        String paymentMethod = args[6];

        ParkingLotManager mgr = buildSeededManager();

        // 1. Create vehicle via Factory Method
        VehicleFactory vf = resolveVehicleFactory(vehicleType);
        Vehicle vehicle = vf.createVehicle(plate);

        // 2. Find the requested spot
        ParkingSpot spot = resolveSpot(mgr, spotId);

        // 3. Run Chain of Responsibility + issue ticket
        java.io.PrintStream originalOut = System.out;
        String chainOutput = "";
        String gateOutput = "";
        Ticket ticket = null;

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(baos));
            ticket = mgr.processVehicleEntry(vehicle, spot, true);
            chainOutput = baos.toString().trim();
        } finally {
            System.setOut(originalOut);
        }

        // 4. Persist Vehicle & Ticket to MySQL Database
        int vehicleId = vehicleDAO.getOrCreateVehicle(vehicle);
        ticketDAO.saveTicket(ticket, vehicleId);

        // 5. Run GateAccessProxy
        PaymentMethod pm = resolvePaymentMethod(paymentMethod);
        RealGateSystem realGate = new RealGateSystem(pm, spot);
        GateAccessProxy gateProxy = new GateAccessProxy(realGate);

        try {
            java.io.ByteArrayOutputStream baos2 = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(baos2));
            gateProxy.processEntry(ticket);
            gateOutput = baos2.toString().trim();
        } finally {
            System.setOut(originalOut);
        }

        String[] chainLines  = chainOutput.isEmpty()  ? new String[0] : chainOutput.split("\n");
        String[] gateLines   = gateOutput.isEmpty()   ? new String[0] : gateOutput.split("\n");

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"status\":\"BOOKED\",")
          .append("\"ticketId\":\"").append(ticket.getTicketId()).append("\",")
          .append("\"plate\":\"").append(plate).append("\",")
          .append("\"vehicleType\":\"").append(vehicleType).append("\",")
          .append("\"zone\":\"").append(zone).append("\",")
          .append("\"spotId\":\"").append(spotId).append("\",")
          .append("\"spotType\":\"").append(spotType).append("\",")
          .append("\"pricingMode\":\"").append(spot.getPricingMode()).append("\",")
          .append("\"rateDescription\":\"").append(spot.getRateDescription()).append("\",")
          .append("\"entryTime\":\"").append(ticket.getEntryTime()).append("\",")
          .append("\"chainLog\":[");
        for (int i = 0; i < chainLines.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(chainLines[i].trim().replace("\"", "'")).append("\"");
        }
        for (String gl : gateLines) {
            sb.append(",\"").append(gl.trim().replace("\"", "'")).append("\"");
        }
        sb.append("],")
          .append("\"observerLog\":[");
        List<String> obsLog = mgr.getObserverLog();
        for (int i = 0; i < obsLog.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(obsLog.get(i).replace("\"", "'")).append("\"");
        }
        sb.append("]}");
        System.out.println(sb);
    }

    private static void handleExit(String[] args) {
        String ticketId   = args[1];
        String plate      = args[2];
        String zone       = args[3];
        String spotId     = args[4];
        String spotType   = args[5];
        int    hours      = Integer.parseInt(args[6]);

        ParkingLotManager mgr  = buildSeededManager();
        ParkingSpot spot = resolveSpot(mgr, spotId);

        // Compute fee via Strategy/Bridge pricing
        double fee = spot.calculateFee(hours);

        // Persist exit details to MySQL database
        ticketDAO.updateExitDetails(ticketId, fee);

        String feeBreakdown = buildFeeBreakdown(spot.getPricingMode(), hours, fee);

        System.out.println("{"
            + "\"status\":\"READY_TO_PAY\","
            + "\"ticketId\":\"" + ticketId + "\","
            + "\"plate\":\"" + plate + "\","
            + "\"zone\":\"" + zone + "\","
            + "\"spotId\":\"" + spotId + "\","
            + "\"spotType\":\"" + spotType + "\","
            + "\"pricingMode\":\"" + spot.getPricingMode() + "\","
            + "\"hoursParked\":" + hours + ","
            + "\"feeBreakdown\":\"" + feeBreakdown + "\","
            + "\"totalFee\":" + fee
            + "}");
    }

    private static void handlePay(String[] args) {
        String ticketId     = args[1];
        String plate        = args[2];
        String vehicleType  = args[3];
        String zone         = args[4];
        String spotId       = args[5];
        String spotType     = args[6];
        int    hours        = Integer.parseInt(args[7]);
        String paymentMethod= args[8];

        ParkingLotManager mgr = buildSeededManager();
        ParkingSpot spot = resolveSpot(mgr, spotId);

        Ticket ticket = new Ticket(plate, vehicleType, spotId, zone, spot.getPricingMode());
        double fee = spot.calculateFee(hours);
        ticket.calculateFee(fee);

        PaymentMethod pm = resolvePaymentMethod(paymentMethod);
        RealGateSystem realGate = new RealGateSystem(pm, spot);
        GateAccessProxy gateProxy = new GateAccessProxy(realGate);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream old = System.out;
        System.setOut(new java.io.PrintStream(baos));

        realGate.collectPayment(ticket, fee);
        gateProxy.processExit(ticket);
        mgr.notifySpotFreed(spotId);

        System.setOut(old);

        // Persist payment & update status in MySQL Database
        ticketDAO.updatePaymentStatus(ticketId, fee, paymentMethod);

        String feeBreakdown = buildFeeBreakdown(spot.getPricingMode(), hours, fee);

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"status\":\"PAID\",")
          .append("\"ticketId\":\"").append(ticketId).append("\",")
          .append("\"plate\":\"").append(plate).append("\",")
          .append("\"vehicleType\":\"").append(vehicleType).append("\",")
          .append("\"zone\":\"").append(zone).append("\",")
          .append("\"spotId\":\"").append(spotId).append("\",")
          .append("\"spotType\":\"").append(spotType).append("\",")
          .append("\"pricingMode\":\"").append(spot.getPricingMode()).append("\",")
          .append("\"hoursParked\":").append(hours).append(",")
          .append("\"feeBreakdown\":\"").append(feeBreakdown).append("\",")
          .append("\"totalFee\":").append(fee).append(",")
          .append("\"paymentMethod\":\"").append(paymentMethod).append("\",")
          .append("\"observerLog\":[");
        List<String> obsLog = mgr.getObserverLog();
        for (int i = 0; i < obsLog.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(obsLog.get(i).replace("\"", "'")).append("\"");
        }
        sb.append("]}");
        System.out.println(sb);
    }

    private static ParkingLotManager buildSeededManager() {
        ParkingLotManager mgr = ParkingLotManager.getInstance();

        if (mgr.getSpots().isEmpty()) {
            mgr.registerObserver(new DisplayBoard());
            mgr.registerObserver(new MobileAppNotifier());
        }

        // Always sync spots & availability from MySQL database via SpotDAO
        mgr.loadSpotsFromDAO();

        // If MySQL table had no spots, seed using Abstract Factories
        if (mgr.getSpots().isEmpty()) {
            ParkingSpotFactory premiumFactory = new PremiumZoneFactory();
            ParkingSpotFactory regularFactory = new RegularZoneFactory();
            for (String[] entry : SPOT_CATALOG) {
                String sid   = entry[0];
                String stype = entry[1];
                String szone = entry[2];
                ParkingSpotFactory factory = szone.equals("Premium") ? premiumFactory : regularFactory;
                ParkingSpot spot;
                switch (stype) {
                    case "Small":  spot = factory.createSmallSpot(sid);  break;
                    case "Medium": spot = factory.createMediumSpot(sid); break;
                    default:       spot = factory.createLargeSpot(sid);  break;
                }
                mgr.registerSpot(spot);
            }
        }
        return mgr;
    }

    private static int resolveVehicleSize(String vehicleType) {
        switch (vehicleType.toLowerCase()) {
            case "bike": return 1;
            case "truck": return 3;
            default: return 2; // Car
        }
    }

    private static VehicleFactory resolveVehicleFactory(String vehicleType) {
        switch (vehicleType.toLowerCase()) {
            case "bike":  return new BikeFactory();
            case "truck": return new TruckFactory();
            default:      return new CarFactory();
        }
    }

    private static PaymentMethod resolvePaymentMethod(String method) {
        switch (method.toLowerCase()) {
            case "cash": return new CashPayment();
            case "upi":  return new UPIPayment();
            default:     return new CardPayment();
        }
    }

    private static String buildFeeBreakdown(String pricingMode, int hours, double fee) {
        String rs = "\u20b9";
        switch (pricingMode) {
            case "FlatRate":
                return "Flat rate: " + rs + String.format("%.2f", fee);
            case "Dynamic":
                double surgeMultiplier = hours > 4 ? 1.5 : 1.0;
                return hours + "h \u00d7 " + rs + "15/hr" + (surgeMultiplier > 1.0 ? " \u00d7 1.5 surge" : "") + " = " + rs + String.format("%.2f", fee);
            default:
                return hours + "h \u00d7 " + rs + "50/hr = " + rs + String.format("%.2f", fee);
        }
    }

    private static ParkingSpot resolveSpot(ParkingLotManager mgr, String spotId) {
        Optional<ParkingSpot> exact = mgr.getSpots().stream()
            .filter(s -> s.getSpotId().equalsIgnoreCase(spotId) || s.getSpotId().replace("-", "").equalsIgnoreCase(spotId.replace("-", "")))
            .findFirst();
        if (exact.isPresent()) return exact.get();

        String sid = spotId.toUpperCase();
        if (sid.contains("C-") || sid.contains("S-") || sid.startsWith("S")) {
            return mgr.getSpots().stream().filter(s -> s.getSpotType().equalsIgnoreCase("Small")).findFirst().orElse(mgr.getSpots().get(0));
        }
        if (sid.contains("M-") || sid.startsWith("M")) {
            return mgr.getSpots().stream().filter(s -> s.getSpotType().equalsIgnoreCase("Medium")).findFirst().orElse(mgr.getSpots().get(0));
        }
        if (sid.contains("L-") || sid.contains("EV-") || sid.startsWith("L")) {
            return mgr.getSpots().stream().filter(s -> s.getSpotType().equalsIgnoreCase("Large")).findFirst().orElse(mgr.getSpots().get(0));
        }
        return mgr.getSpots().get(0);
    }

    private static void printError(String msg) {
        System.out.println("{\"status\":\"ERROR\",\"message\":\"" + (msg != null ? msg.replace("\"", "'") : "unknown") + "\"}");
    }
}
