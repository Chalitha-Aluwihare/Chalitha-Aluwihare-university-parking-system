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

    // New method for booking with a specific start time
    public void bookSlot(int durationMinutes, String licensePlate, LocalDateTime startTime) {
        // Only set available to false if the booking starts now. Otherwise, it's a pre-booking.
        if (startTime.isBefore(LocalDateTime.now().plusMinutes(1))) {
            this.available = false;
        }

        this.remainingMinutes = durationMinutes;
        this.bookingEndTime = startTime.plusMinutes(durationMinutes);

        Booking booking = new Booking(
                slotNo,
                parkingLot,
                licensePlate,
                durationMinutes,
                startTime
        );

        Database.createBooking(booking);
        Database.updateSlot(this);
    }

    // Existing method for immediate booking
    public void bookSlot(int durationMinutes, String licensePlate) {
        this.bookSlot(durationMinutes, licensePlate, LocalDateTime.now());
    }

    public long getRemainingSeconds() {
        if (bookingEndTime == null) return 0;
        long secondsLeft = Duration.between(LocalDateTime.now(), bookingEndTime).getSeconds();
        return Math.max(secondsLeft, 0);
    }

    public long getRemainingSecondsToStart() {
        Booking booking = Database.getBookingBySlot(this.slotNo, this.parkingLot);
        if (booking != null && booking.getStartTime().isAfter(LocalDateTime.now())) {
            long secondsLeft = Duration.between(LocalDateTime.now(), booking.getStartTime()).getSeconds();
            return Math.max(secondsLeft, 0);
        }
        return 0;
    }

    public boolean isPreBooked() {
        Booking booking = Database.getBookingBySlot(this.slotNo, this.parkingLot);
        return booking != null && booking.getStartTime().isAfter(LocalDateTime.now());
    }

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

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setRemainingMinutes(int minutes) {
        this.remainingMinutes = minutes;
    }

    public LocalDateTime getBookingEndTime() {
        return bookingEndTime;
    }

    public void setBookingEndTime(LocalDateTime bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }
}
