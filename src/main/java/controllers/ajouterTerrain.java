package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import models.Terrain;
import services.TerrainService;

import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.sql.SQLException;

public class ajouterTerrain {

    @FXML
    private TextField superficie_t;

    @FXML
    private TextField region_t;

    @FXML
    private TextField type_sole_t;

    @FXML
    private TextField statut_t;

    @FXML
    private TextField gps_coordinates_t;

    @FXML
    private TextField prix_location_t;

    @FXML
    private TextField coordonnees_gps_t;

    @FXML
    private Button btnSave;

    private TerrainService terrainService = new TerrainService();
    private Terrain terrainEnModification = null;

    public void setTerrainPourModification(Terrain t) {
        this.terrainEnModification = t;
        // Pré-remplissage des champs
        superficie_t.setText(String.valueOf(t.getSuperficie()));
        region_t.setText(t.getRegion());
        type_sole_t.setText(t.getType_sol());
        statut_t.setText(t.getStatut());
        gps_coordinates_t.setText(t.getGps_coordinates());
        prix_location_t.setText(String.valueOf(t.getPrix_location()));
        coordonnees_gps_t.setText(t.getCoordonnees_gps());
        
        // Changer le titre ou le bouton
        btnSave.setText("Mettre à jour le Terrain");
    }

    @FXML
    void save_terrain(ActionEvent event) {
        try {
            float superficie = Float.parseFloat(superficie_t.getText());
            String region = region_t.getText();
            String type_sol = type_sole_t.getText();
            String statut = statut_t.getText();
            String gps_map = gps_coordinates_t.getText();
            float prix = Float.parseFloat(prix_location_t.getText());
            String image360 = coordonnees_gps_t.getText();

            if (terrainEnModification == null) {
                // Mode AJOUT
                Terrain terrain = new Terrain(superficie, region, type_sol, statut, gps_map, prix, image360);
                terrainService.add(terrain);
                afficherSucces("Terrain ajouté avec succès !");
            } else {
                // Mode MODIFICATION
                terrainEnModification.setSuperficie(superficie);
                terrainEnModification.setRegion(region);
                terrainEnModification.setType_sol(type_sol);
                terrainEnModification.setStatut(statut);
                terrainEnModification.setGps_coordinates(gps_map);
                terrainEnModification.setPrix_location(prix);
                terrainEnModification.setCoordonnees_gps(image360);
                
                terrainService.update(terrainEnModification);
                afficherSucces("Terrain mis à jour avec succès !");
            }

            // Retour automatique à la liste après action
            retourListe(null);

        } catch (NumberFormatException e) {
            showError("Erreur de format", "Veuillez saisir des nombres valides.");
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherTerrains.fxml"));
            superficie_t.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Erreur Navigation", e.getMessage());
        }
    }

    @FXML
    void allerVersPlantes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherPlantes.fxml"));
            superficie_t.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Erreur Navigation", e.getMessage());
        }
    }

    @FXML
    void importerImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une Image 360°");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(coordonnees_gps_t.getScene().getWindow());
        if (selectedFile != null) {
            // On récupère l'URI du fichier pour qu'il soit compatible avec Image
            coordonnees_gps_t.setText(selectedFile.toURI().toString());
        }
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    void retourAccueil(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) superficie_t.getScene().getWindow();
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

