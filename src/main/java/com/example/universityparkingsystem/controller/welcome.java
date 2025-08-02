package com.example.universityparkingsystem.controller;

import com.example.universityparkingsystem.model.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;


public class welcome implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Database.connect();
    }

    @FXML
    void handleLot01Button(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/parkingLot01.fxml");
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file for ParkingLot01 not found"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Parking Lot 01");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLot02Button(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/parkingLot02.fxml");
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation, "FXML file for ParkingLot02 not found"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Parking Lot 02");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
