package com.example.universityparkingsystem.controller;

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
import java.io.IOException;
import java.util.Objects;
import java.time.LocalDateTime;

public class bookingController {

    @FXML private TextField slotNoField;
    @FXML private TextField licensePlateField;
    @FXML private TextField hoursField;
    @FXML private TextField minutesField;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    private String slotNo;
    private String parkingLot;


    public void setSlotData(String slotNo, String parkingLot) {
        this.slotNo = slotNo;
        this.parkingLot = parkingLot;

        if (slotNoField != null) {
            slotNoField.setText("Slot No. " + slotNo);
        }
    }


    @FXML
    private void handleConfirmBooking() {
        try {
            String licensePlate = licensePlateField.getText();
            String hoursText = hoursField.getText();
            String minutesText = minutesField.getText();

            if (licensePlate == null || licensePlate.trim().isEmpty() || hoursText == null || hoursText.trim().isEmpty() || minutesText == null || minutesText.trim().isEmpty()) {
                showAlert("Error", "Please enter a valid license plate and duration.");
                return;
            }

            int hours = Integer.parseInt(hoursText);
            int minutes = Integer.parseInt(minutesText);
            int durationMinutes = hours * 60 + minutes;

            if (durationMinutes <= 0) {
                showAlert("Error", "Please enter a valid duration greater than zero.");
                return;
            }

            Slot slot = Database.getSlot(slotNo, parkingLot);

            if (slot == null || !slot.isAvailable()) {
                showAlert("Error", "Selected slot is not available or does not exist.");
                return;
            }

            // Book the specific slot with the user-defined duration
            slot.bookSlot(durationMinutes, licensePlate, LocalDateTime.now());

            showAlert("Success", "Booking confirmed!\nYour slot is " + parkingLot + " - " + slotNo);

            // Navigate back to the parking lot screen after a successful booking
            navigateToParkingLot();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for hours and minutes.");
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void navigateToParkingLot() {
        try {
            String fxmlPath = "/fxml/" + (parkingLot.equals("Lot01") ? "parkingLot01.fxml" : "parkingLot02.fxml");
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back to the parking lot.");
        }
    }


    @FXML
    private void handleBack() {
        navigateToParkingLot();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
