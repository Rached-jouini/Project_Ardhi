package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Demarrage sur le nouveau Layout avec Sidebar (Style SaaS Pro)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            primaryStage.setTitle("Ardhi - Authentification");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("Erreur chargement FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
