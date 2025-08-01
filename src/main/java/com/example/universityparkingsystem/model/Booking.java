package com.example.universityparkingsystem.model;

import java.time.LocalDateTime;

public class Booking {
    private String slotNo;
    private String parkingLot;
    private String licensePlate;
    private int durationMinutes;
    private LocalDateTime startTime;

    public Booking(String slotNo, String parkingLot, String licensePlate,
                   int durationMinutes, LocalDateTime startTime) {
        this.slotNo = slotNo;
        this.parkingLot = parkingLot;
        this.licensePlate = licensePlate;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
    }

    public String getSlotNo() { return slotNo; }
    public String getParkingLot() { return parkingLot; }
    public String getLicensePlate() { return licensePlate; }
    public int getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
}
