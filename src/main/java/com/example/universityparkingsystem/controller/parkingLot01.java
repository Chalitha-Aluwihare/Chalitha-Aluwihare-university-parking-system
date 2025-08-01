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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class parkingLot01 implements Initializable {

    @FXML
    private Button slotA1, slotA2, slotA3, slotA4,
            slotB1, slotB2, slotB3, slotB4,
            slotC1, slotC2, slotC3, slotC4;

    @FXML
    private Label timerA1, timerA2, timerA3, timerA4,
            timerB1, timerB2, timerB3, timerB4,
            timerC1, timerC2, timerC3, timerC4;

    private final String parkingLotName = "Lot01";
    private Map<String, Button> slotButtons = new HashMap<>();
    private Map<String, Label> slotTimers = new HashMap<>();
    private ScheduledExecutorService scheduler;
    private AnimationTimer animationTimer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeMaps();
        setupScheduler();
        startAnimationTimer();
    }

    private void initializeMaps() {
        slotButtons.put("A1", slotA1); slotButtons.put("A2", slotA2); slotButtons.put("A3", slotA3); slotButtons.put("A4", slotA4);
        slotButtons.put("B1", slotB1); slotButtons.put("B2", slotB2); slotButtons.put("B3", slotB3); slotButtons.put("B4", slotB4);
        slotButtons.put("C1", slotC1); slotButtons.put("C2", slotC2); slotButtons.put("C3", slotC3); slotButtons.put("C4", slotC4);

        slotTimers.put("A1", timerA1); slotTimers.put("A2", timerA2); slotTimers.put("A3", timerA3); slotTimers.put("A4", timerA4);
        slotTimers.put("B1", timerB1); slotTimers.put("B2", timerB2); slotTimers.put("B3", timerB3); slotTimers.put("B4", timerB4);
        slotTimers.put("C1", timerC1); slotTimers.put("C2", timerC2); slotTimers.put("C3", timerC3); slotTimers.put("C4", timerC4);
    }

    private void setupScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::updateSlots);
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void startAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateTimers();
            }
        };
        animationTimer.start();
    }

    private void updateSlots() {
        List<Slot> slots = Database.getSlots(parkingLotName);
        for (Slot slot : slots) {
            Button button = slotButtons.get(slot.getSlotNo());
            Label timerLabel = slotTimers.get(slot.getSlotNo());

            if (button != null && timerLabel != null) {
                if (slot.isAvailable() && !slot.isPreBooked()) {
                    button.setStyle("-fx-background-color: #2ECC71; -fx-background-radius: 20;");
                    button.setText("Available");
                } else if (slot.isPreBooked()) {
                    // This slot is pre-booked for a future time
                    button.setStyle("-fx-background-color: #FFA500; -fx-background-radius: 20;");
                    button.setText("Pre-booked");
                } else {
                    // This slot is currently occupied
                    button.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 20;");
                    button.setText("Occupied");

                    if(slot.getRemainingSeconds() <= 0) {
                        Database.deleteBooking(slot.getSlotNo(), slot.getParkingLot());
                        slot.setAvailable(true);
                        slot.setRemainingMinutes(0);
                        slot.setBookingEndTime(null);
                        Database.updateSlot(slot);
                    }
                }
            }
        }
    }

    private void updateTimers() {
        List<Slot> slots = Database.getSlots(parkingLotName);
        for (Slot slot : slots) {
            Label timerLabel = slotTimers.get(slot.getSlotNo());
            if (timerLabel != null) {
                long remainingSeconds;
                if (slot.isPreBooked()) {
                    remainingSeconds = slot.getRemainingSecondsToStart();
                } else {
                    remainingSeconds = slot.getRemainingSeconds();
                }

                if (remainingSeconds > 0) {
                    long hours = remainingSeconds / 3600;
                    long minutes = (remainingSeconds % 3600) / 60;
                    long seconds = remainingSeconds % 60;
                    timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                } else {
                    timerLabel.setText("");
                }
            }
        }
    }

    @FXML
    private void handleSlotClick(ActionEvent event) throws IOException {
        String slotNo = ((Button) event.getSource()).getId().replace("slot", "");
        Slot slot = Database.getSlot(slotNo, parkingLotName);

        if (slot != null) {
            if (slot.isPreBooked()) {
                // Prevent new bookings on a pre-booked slot to avoid conflicts
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Not Possible");
                alert.setHeaderText(null);
                alert.setContentText("This slot is already pre-booked for a future time. Please select another slot.");
                alert.showAndWait();
            } else if (slot.isAvailable()) {
                // If it's truly available (not pre-booked), proceed to booking screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                Parent root = loader.load();
                bookingController controller = loader.getController();
                controller.setSlotData(slotNo, parkingLotName);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Book Slot");
                stage.show();
            }
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
}
