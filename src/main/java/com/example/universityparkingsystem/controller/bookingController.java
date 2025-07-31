package com.example.universityparkingsystem.controller;

import com.example.universityparkingsystem.model.Booking;
import com.example.universityparkingsystem.model.Database;
import com.example.universityparkingsystem.model.Slot;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class bookingController {

    @FXML private TextField slotNoField;
    @FXML private TextField licensePlateField;
    @FXML private TextField hoursField;
    @FXML private TextField minutesField;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    private int slotNo;
    private String parkingLot;

    // Called from parking lot controller to set which slot and parking lot are being booked
    public void setSlotData(int slotNo, String parkingLot) {
        this.slotNo = slotNo;
        this.parkingLot = parkingLot;

        if (slotNoField != null) {
            slotNoField.setText("Slot No. " + slotNo);
            slotNoField.setDisable(true);
        }

        // Load previous booking if exists and fill fields
        Slot slot = Database.getAllSlots().stream()
                .filter(s -> s.getSlotNo() == slotNo && s.getParkingLot().equals(parkingLot))
                .findFirst()
                .orElse(null);

        if (slot != null && !slot.isAvailable()) {
            Booking booking = Database.getBookingBySlot(slotNo, parkingLot);
            if (booking != null) {
                licensePlateField.setText(booking.getLicensePlate());
                int remaining = slot.getRemainingMinutes();
                hoursField.setText(String.valueOf(remaining / 60));
                minutesField.setText(String.valueOf(remaining % 60));
            }
        }
    }

    @FXML
    private void handleConfirmBooking() {
        try {
            String licensePlate = licensePlateField.getText().trim();
            int hours = Integer.parseInt(hoursField.getText().trim());
            int minutes = Integer.parseInt(minutesField.getText().trim());
            int totalMinutes = hours * 60 + minutes;

            if (licensePlate.isEmpty()) {
                showAlert("Error", "License plate cannot be empty");
                return;
            }
            if (totalMinutes <= 0) {
                showAlert("Error", "Booking duration must be greater than zero");
                return;
            }

            Slot slot = Database.getAllSlots().stream()
                    .filter(s -> s.getSlotNo() == slotNo && s.getParkingLot().equals(parkingLot))
                    .findFirst()
                    .orElse(null);

            if (slot != null) {
                slot.bookSlot(totalMinutes, licensePlate);
                navigateToParkingLot();
            } else {
                showAlert("Error", "Slot not found");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for hours and minutes");
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

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back to parking lot");
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
