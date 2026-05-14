package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.CheckinServer;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Démarrer le serveur de validation QR Code
        CheckinServer.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserAfficherEvenements.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Ardhi - Espace Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Arrêter le serveur proprement quand l'app se ferme
        primaryStage.setOnCloseRequest(e -> CheckinServer.stop());
    }
}
