package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DetailTerrainController {

    @FXML
    private Label regionLabel;

    @FXML
    private Label superficieLabel;

    @FXML
    private Label prixLabel;

    public void setRegionLabel(String region) {
        this.regionLabel.setText(region);
    }

    public void setSuperficieLabel(String superficie) {
        this.superficieLabel.setText(superficie + " m²");
    }

    public void setPrixLabel(String prix) {
        this.prixLabel.setText(prix + " TND");
    }

    @FXML
    void retourALaListe(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            javafx.scene.Parent root = loader.load();
            regionLabel.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
        }
    }

    @FXML
    void retourAccueil(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) regionLabel.getScene().getWindow();
            stage.setTitle("Ardhi - Accueil");
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Retour a l'accueil impossible");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
