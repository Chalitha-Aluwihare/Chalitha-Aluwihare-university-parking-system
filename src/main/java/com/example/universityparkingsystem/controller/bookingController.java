package com.example.universityparkingsystem.controller;

import com.example.universityparkingsystem.model.Booking;
import com.example.universityparkingsystem.model.Database;
import com.example.universityparkingsystem.model.Slot;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class bookingController {

    @FXML private TextField slotNoField;
    @FXML private TextField licensePlateField;
    @FXML private TextField hoursField;
    @FXML private TextField minutesField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    private String slotNo;
    private String parkingLot;

    public void setSlotData(String slotNo, String parkingLot) {
        this.slotNo = slotNo;
        this.parkingLot = parkingLot;

        if (slotNoField != null) {
            slotNoField.setText("Slot No. " + slotNo);
            slotNoField.setDisable(true);
        }

        Slot slot = Database.getSlot(slotNo, parkingLot);

        if (slot != null && !slot.isAvailable()) {
            Booking booking = Database.getBookingBySlot(slotNo, parkingLot);
            if (booking != null) {
                licensePlateField.setText(booking.getLicensePlate());
                licensePlateField.setDisable(true);
                hoursField.setDisable(true);
                minutesField.setDisable(true);
                datePicker.setDisable(true);
                timeField.setDisable(true);
            }
        }
    }

    @FXML
    private void handleConfirmBooking() {
        try {
            String licensePlate = licensePlateField.getText();

            // Handle duration
            int hours = 0;
            int minutes = 0;
            try {
                if (!hoursField.getText().isEmpty()) {
                    hours = Integer.parseInt(hoursField.getText());
                }
                if (!minutesField.getText().isEmpty()) {
                    minutes = Integer.parseInt(minutesField.getText());
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numbers for hours and minutes.");
                return;
            }
            int durationMinutes = hours * 60 + minutes;

            if (licensePlate.isEmpty() || durationMinutes <= 0) {
                showAlert("Error", "Please enter a valid license plate and duration.");
                return;
            }

            // Handle optional pre-booking date and time
            LocalDateTime bookingStartTime;
            LocalDate selectedDate = datePicker.getValue();
            String selectedTimeStr = timeField.getText();

            if (selectedDate != null && !selectedTimeStr.isEmpty()) {
                try {
                    LocalTime selectedTime = LocalTime.parse(selectedTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    bookingStartTime = LocalDateTime.of(selectedDate, selectedTime);
                } catch (DateTimeParseException e) {
                    showAlert("Error", "Invalid time format. Please use HH:mm (e.g., 21:00).");
                    return;
                }
            } else {
                bookingStartTime = LocalDateTime.now();
            }

            if (bookingStartTime.isBefore(LocalDateTime.now())) {
                showAlert("Error", "Booking start time cannot be in the past.");
                return;
            }

            Slot slot = Database.getSlot(slotNo, parkingLot);
            if (slot != null && slot.isAvailable()) {
                slot.bookSlot(durationMinutes, licensePlate, bookingStartTime);
                navigateToParkingLot();
            } else {
                showAlert("Error", "Slot not found or is not available. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred during booking. Please try again.");
        }
    }

    @FXML
    private void handleBack() {
        navigateToParkingLot();
    }

    private void navigateToParkingLot() {
        try {
            String fxmlPath = parkingLot.equals("Lot01") ?
                    "/fxml/parkingLot01.fxml" : "/fxml/parkingLot02.fxml";

            URL fxmlLocation = getClass().getResource(fxmlPath);
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file for parking lot not found: " + fxmlPath));
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back to parking lot.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
