package com.example.universityparkingsystem.model;

import javafx.util.Duration;
import java.time.LocalDateTime;

public class Slot {
    private final int slotNo;
    private boolean available;
    private int remainingMinutes;
    private final String parkingLot;
    private LocalDateTime bookingEndTime;

    public Slot(int slotNo, boolean available, int remainingMinutes, String parkingLot) {
        this.slotNo = slotNo;
        this.available = available;
        this.remainingMinutes = remainingMinutes;
        this.parkingLot = parkingLot;

        if (!available && remainingMinutes > 0) {
            this.bookingEndTime = LocalDateTime.now().plusMinutes(remainingMinutes);
        }
    }

    public void bookSlot(int durationMinutes, String licensePlate) {
        this.available = false;
        this.remainingMinutes = durationMinutes;
        this.bookingEndTime = LocalDateTime.now().plusMinutes(durationMinutes);

        Booking booking = new Booking(
                slotNo,
                parkingLot,
                licensePlate,
                durationMinutes,
                LocalDateTime.now()
        );

        Database.createBooking(booking);
        Database.updateSlot(this);
    }

    public long getRemainingSeconds() {
        if (bookingEndTime == null) return 0;
        long secondsLeft = java.time.Duration.between(LocalDateTime.now(), bookingEndTime).getSeconds();
        return Math.max(secondsLeft, 0);
    }

    // Getters
    public int getSlotNo() {
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

    public void setAvailable(boolean available) {
        this.available = available;
        Database.updateSlot(this);
    }

    public void setRemainingMinutes(int minutes) {
        this.remainingMinutes = minutes;
        Database.updateSlot(this);
    }

    public LocalDateTime getBookingEndTime() {
        return bookingEndTime;
    }

    public void setBookingEndTime(LocalDateTime bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }
}
