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

    /**
     * Books the slot for a given duration, license plate, and start time.
     * @param durationMinutes The total booking duration in minutes.
     * @param licensePlate The license plate of the vehicle.
     * @param startTime The start time of the booking.
     */
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

    /**
     * Calculates the remaining booking time in seconds.
     * @return The number of seconds left, or 0 if no booking exists.
     */
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

    /**
     * Sets the booking end time for the slot.
     * This method was missing and caused the compilation error in Database.java.
     * @param bookingEndTime The new booking end time.
     */
    public void setBookingEndTime(LocalDateTime bookingEndTime) {
        this.bookingEndTime = bookingEndTime;
    }
}
