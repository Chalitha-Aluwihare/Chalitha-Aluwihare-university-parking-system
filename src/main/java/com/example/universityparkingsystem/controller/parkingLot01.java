package com.example.universityparkingsystem.controller;

import com.example.universityparkingsystem.model.Database;
import com.example.universityparkingsystem.model.Slot;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class parkingLot01 implements Initializable {

    @FXML private Button slotA1, slotA2, slotA3, slotA4, slotB1, slotB2, slotB3, slotB4, slotC1, slotC2, slotC3, slotC4;
    @FXML private Label timerA1, timerA2, timerA3, timerA4, timerB1, timerB2, timerB3, timerB4, timerC1, timerC2, timerC3, timerC4;

    private final Map<String, Button> slotButtons = new HashMap<>();
    private final Map<String, Label> slotTimers = new HashMap<>();
    private final String parkingLotName = "Lot01";
    private ScheduledExecutorService scheduler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        slotButtons.put("A1", slotA1); slotTimers.put("A1", timerA1);
        slotButtons.put("A2", slotA2); slotTimers.put("A2", timerA2);
        slotButtons.put("A3", slotA3); slotTimers.put("A3", timerA3);
        slotButtons.put("A4", slotA4); slotTimers.put("A4", timerA4);
        slotButtons.put("B1", slotB1); slotTimers.put("B1", timerB1);
        slotButtons.put("B2", slotB2); slotTimers.put("B2", timerB2);
        slotButtons.put("B3", slotB3); slotTimers.put("B3", timerB3);
        slotButtons.put("B4", slotB4); slotTimers.put("B4", timerB4);
        slotButtons.put("C1", slotC1); slotTimers.put("C1", timerC1);
        slotButtons.put("C2", slotC2); slotTimers.put("C2", timerC2);
        slotButtons.put("C3", slotC3); slotTimers.put("C3", timerC3);
        slotButtons.put("C4", slotC4); slotTimers.put("C4", timerC4);


        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateSlots, 0, 5, TimeUnit.SECONDS);


        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateTimers();
            }
        }.start();
    }

    private void updateSlots() {
        List<Slot> slots = Database.getSlots(parkingLotName);
        Platform.runLater(() -> {
            for (Slot slot : slots) {
                Button button = slotButtons.get(slot.getSlotNo());
                if (button != null) {
                    if (!slot.isAvailable()) {
                        button.setStyle("-fx-background-color: #d42424;"); // Booked slot color
                        button.setDisable(true); // Disable booked slots
                    } else {
                        button.setStyle("-fx-background-color: #0cc54d;"); // Available slot color
                        button.setDisable(false); // Enable available slots
                    }
                }
            }
        });
    }


    private void updateTimers() {
        List<Slot> slots = Database.getSlots(parkingLotName);
        for (Slot slot : slots) {
            Label timerLabel = slotTimers.get(slot.getSlotNo());
            if (timerLabel != null) {
                if (!slot.isAvailable()) {
                    long remainingSeconds = slot.getRemainingSeconds();
                    if (remainingSeconds > 0) {
                        long hours = remainingSeconds / 3600;
                        long minutes = (remainingSeconds % 3600) / 60;
                        long seconds = remainingSeconds % 60;
                        timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                        timerLabel.setVisible(true);
                    } else {
                        // Booking has expired, make the slot available again
                        slot.setAvailable(true);
                        Database.updateSlot(slot);
                        timerLabel.setVisible(false);
                    }
                } else {
                    timerLabel.setVisible(false);
                }
            }
        }
    }

    @FXML
    private void handleSlotClick(ActionEvent event) {
        String slotNo = ((Button) event.getSource()).getId().replace("slot", "");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent root = loader.load();
            bookingController controller = loader.getController();
            controller.setSlotData(slotNo, parkingLotName);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Book Slot");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate to booking screen.");
        }
    }


    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        cleanupScheduler();
        URL fxmlLocation = getClass().getResource("/fxml/welcome.fxml");
        Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file for welcome not found"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }


    @FXML
    private void handleHome(ActionEvent event) throws IOException {
        cleanupScheduler();
        URL fxmlLocation = getClass().getResource("/fxml/welcome.fxml");
        Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file for welcome not found"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();
    }

    private void cleanupScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
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
