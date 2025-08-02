package com.example.universityparkingsystem;

import com.example.universityparkingsystem.model.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database connection
        Database.connect();

        // Load welcome screen
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
        primaryStage.setTitle("University Parking System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        Database.disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
