# 🚗 Smart Parking Management System

> A design-pattern-driven Smart Parking Management System built with **Java, HTML, CSS and JavaScript**.

A modern parking management application that demonstrates how **Object-Oriented Design Patterns** can be applied to solve real-world parking problems such as vehicle creation, parking spot management, pricing, entry validation, gate access and real-time status notifications.

---

## ✨ Features

- 🚘 Vehicle entry and exit management
- 🅿️ Real-time parking spot status
- 🎫 Automatic parking ticket generation
- 💰 Multiple pricing strategies
- 🚦 Vehicle and spot validation before entry
- 🔐 Controlled gate access
- 🔔 Parking status notifications
- 🏭 Flexible vehicle and parking spot creation
- 📊 Interactive web dashboard
- 🌐 REST API powered by Spring Boot
- 🎨 Responsive HTML/CSS/JavaScript interface

---

# 🧠 Design Patterns Used

This project is primarily built to demonstrate the practical use of **Design Patterns** in a real-world Smart Parking System.

| Design Pattern | Implementation | Purpose |
|---|---|---|
| 🟣 **Singleton** | `ParkingLotManager` | Ensures a single parking lot manager instance |
| 🟢 **Factory Method** | `VehicleFactory` | Creates different vehicle types |
| 🟠 **Abstract Factory** | `ParkingSpotFactory` | Creates families of parking spots |
| 🔵 **Bridge** | `ParkingSpot` + `PricingStrategy` | Separates parking spots from pricing |
| 🔴 **Chain of Responsibility** | Entry Handlers | Processes vehicle entry step-by-step |
| 🟡 **Observer** | `ParkingObserver` | Notifies systems about parking status changes |
| 🟢 **Proxy** | `GateAccessProxy` | Controls access to the real gate system |

---

# 🏗️ System Architecture

```text
                    ┌─────────────────────────┐
                    │      WEB FRONTEND       │
                    │                         │
                    │ HTML + CSS + JavaScript │
                    └────────────┬────────────┘
                                 │
                                 │ HTTP / REST
                                 ▼
                    ┌─────────────────────────┐
                    │      SPRING BOOT        │
                    │                         │
                    │    ParkingController    │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │     ParkingService      │
                    └────────────┬────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
        ParkingLotManager     Factories         Pricing
          (Singleton)       (Factory)          (Strategy)
              │
              ├───────────────┐
              │               │
              ▼               ▼
          Observers        Entry Chain
          (Observer)       (Chain of Responsibility)
              │
              ▼
        Gate Access Proxy
             (Proxy)

```
