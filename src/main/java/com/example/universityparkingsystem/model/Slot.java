package com.example.universityparkingsystem.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class Slot {
    private final String slotNo;
    private boolean available;
    private int remainingMinutes;
    private final String parkingLot;
    private LocalDateTime bookingEndTime;

    public Slot(String slotNo, boolean available, int remainingMinutes, String parkingLot) {
        this.slotNo = slotNo;
        this.available = available;
        this.remainingMinutes = remainingMinutes;
        this.parkingLot = parkingLot;

        if (!available && remainingMinutes > 0) {
            this.bookingEndTime = LocalDateTime.now().plusMinutes(remainingMinutes);
        }
    }


    public void bookSlot(int durationMinutes, String licensePlate, LocalDateTime startTime) {
        this.available = false;
        this.remainingMinutes = durationMinutes;
        this.bookingEndTime = startTime.plusMinutes(durationMinutes);

        // Create a new Booking object
        Booking booking = new Booking(
                slotNo,
                parkingLot,
                licensePlate,
                durationMinutes,
                startTime
        );

        // Update the database with the new booking and slot status
        Database.createBooking(booking);
        Database.updateSlot(this);
    }


    public long getRemainingSeconds() {
        if (bookingEndTime == null) return 0;
        long secondsLeft = Duration.between(LocalDateTime.now(), bookingEndTime).getSeconds();
        return Math.max(secondsLeft, 0);
    }

    // Getters
    public String getSlotNo() {
        return slotNo;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }

    public String getParkingLot() {
        return parkingLot;
    }

    public LocalDateTime getBookingEndTime() {
        return bookingEndTime;
    }

    // Setters
    public void setAvailable(boolean available) {
        this.available = available;
    }


    public void setBookingEndTime(LocalDateTime bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }
}
