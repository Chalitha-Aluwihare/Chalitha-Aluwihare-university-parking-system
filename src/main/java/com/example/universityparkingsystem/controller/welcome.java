package com.example.universityparkingsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class welcome {

    @FXML
    void handleGetStarted(ActionEvent event) {
        try {
            // Corrected path: assumes the FXML is in src/main/resources/fxml/parkingLot02.fxml
            URL fxmlLocation = getClass().getResource("/fxml/parkingLot02.fxml");

            // Check if the resource was found
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file not found"));

            // Switch to new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace(); // You can log this or show an alert if needed
        }
    }
}
