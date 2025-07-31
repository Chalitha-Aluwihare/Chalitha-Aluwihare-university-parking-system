package com.example.universityparkingsystem.controller;

import com.example.universityparkingsystem.model.Database;
import com.example.universityparkingsystem.model.Slot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class parkingLot02 implements Initializable {

    @FXML
    private Label countdown0, countdown1, countdown2, countdown3, countdown4,
            countdown5, countdown6, countdown7, countdown8, countdown9;

    @FXML
    private Button slotBtn0, slotBtn1, slotBtn2, slotBtn3, slotBtn4,
            slotBtn5, slotBtn6, slotBtn7, slotBtn8, slotBtn9;

    private final List<Slot> slots = new ArrayList<>();
    private final Label[] countdowns = new Label[10];
    private final Button[] buttons = new Button[10];
    private final Timeline[] timelines = new Timeline[10];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Database.connect();
        List<Slot> allSlots = Database.getAllSlots();
        List<Slot> lot02Slots = new ArrayList<>();

        for (Slot slot : allSlots) {
            if ("Lot02".equals(slot.getParkingLot())) {
                lot02Slots.add(slot);
            }
        }

        Label[] labels = {countdown0, countdown1, countdown2, countdown3, countdown4,
                countdown5, countdown6, countdown7, countdown8, countdown9};
        Button[] btns = {slotBtn0, slotBtn1, slotBtn2, slotBtn3, slotBtn4,
                slotBtn5, slotBtn6, slotBtn7, slotBtn8, slotBtn9};

        System.arraycopy(labels, 0, countdowns, 0, labels.length);
        System.arraycopy(btns, 0, buttons, 0, btns.length);

        for (int i = 0; i < lot02Slots.size() && i < buttons.length; i++) {
            Slot slot = lot02Slots.get(i);
            slots.add(slot);
            updateSlotUI(i, slot);
        }
    }

    private void updateSlotUI(int index, Slot slot) {
        Button button = buttons[index];
        Label label = countdowns[index];

        if (timelines[index] != null) {
            timelines[index].stop();
        }

        if (slot.isAvailable()) {
            button.setStyle("-fx-background-color: #0bbf0f;");
            label.setText("Available");
            button.setDisable(false);
            button.setOnAction(e -> openBookingPage(e, slot.getSlotNo()));
        } else {
            button.setStyle("-fx-background-color: #ff4545;");
            button.setDisable(true);
            startCountdown(index, slot);
        }
    }

    private void startCountdown(int index, Slot slot) {
        Label label = countdowns[index];
        Button button = buttons[index];

        timelines[index] = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long secondsLeft = slot.getRemainingSeconds();
            if (secondsLeft > 0) {
                label.setText(formatTime((int) secondsLeft));
            } else {
                timelines[index].stop();
                slot.setAvailable(true);
                slot.setRemainingMinutes(0);
                slot.setBookingEndTime(null);
                Database.updateSlot(slot);

                button.setDisable(false);
                button.setStyle("-fx-background-color: green;");
                label.setText("Available");
                button.setOnAction(e -> openBookingPage(null, slot.getSlotNo()));
            }
        }));

        timelines[index].setCycleCount(Timeline.INDEFINITE);
        timelines[index].play();
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void openBookingPage(ActionEvent event, int slotNo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent root = loader.load();

            bookingController controller = loader.getController();
            controller.setSlotData(slotNo, "Lot02");

            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) buttons[0].getScene().getWindow();
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Failed to open booking.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
