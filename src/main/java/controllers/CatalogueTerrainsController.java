package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Terrain;
import services.TerrainService;
import utils.Viewer360;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CatalogueTerrainsController {

    @FXML
    private FlowPane terrainContainer;

    private TerrainService terrainService = new TerrainService();

    @FXML
    public void initialize() {
        chargerCatalogue();
    }

    private void chargerCatalogue() {
        try {
            List<Terrain> terrains = terrainService.select();
            terrainContainer.getChildren().clear();

            for (Terrain t : terrains) {
                // On affiche uniquement les terrains disponibles
                if ("disponible".equalsIgnoreCase(t.getStatut())) {
                    terrainContainer.getChildren().add(creerCarteTerrain(t));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement catalogue : " + e.getMessage());
        }
    }

    private VBox creerCarteTerrain(Terrain t) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        
        try {
            String path = t.getCoordonnees_gps();
            if (path != null && !path.isEmpty()) {
                // Correction du chemin si nécessaire (ajout de file: pour les chemins locaux)
                if (!path.startsWith("http") && !path.startsWith("file:")) {
                    path = "file:" + path;
                }
                Image img = new Image(path, true); 
                imageView.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image terrain : " + e.getMessage());
        }

        Label title = new Label(t.getRegion() + " 🔄");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D5D34;");

        Label details = new Label(t.getSuperficie() + " m² - " + t.getType_sol());
        details.getStyleClass().add("label-standard");

        Label price = new Label(t.getPrix_location() + " TND / mois");
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #10B981;");

        Button btnLouer = new Button("Louer ce terrain");
        btnLouer.getStyleClass().add("button-secondary");
        btnLouer.setMaxWidth(Double.MAX_VALUE);
        
        btnLouer.setOnAction(event -> allerVersReservation(t));

        Button btn360 = new Button("Voir en 360° 🔄");
        btn360.getStyleClass().add("button-primary");
        btn360.setMaxWidth(Double.MAX_VALUE);
        btn360.setOnAction(event -> ouvrirVue360(t));

        Button btnMaps = new Button("Localisation 📍");
        btnMaps.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;");
        btnMaps.setMaxWidth(Double.MAX_VALUE);
        btnMaps.setOnAction(event -> ouvrirGoogleMaps(t));

        card.getChildren().addAll(imageView, title, details, price, btn360, btnMaps, btnLouer);
        return card;
    }

    private void ouvrirVue360(Terrain t) {
        if (t.getCoordonnees_gps() != null && !t.getCoordonnees_gps().isEmpty()) {
            Viewer360 viewer = new Viewer360();
            viewer.display(t.getCoordonnees_gps());
        } else {
            System.out.println("Aucune image 360 disponible pour ce terrain.");
        }
    }

    private void ouvrirGoogleMaps(Terrain t) {
        if (t.getGps_coordinates() != null && !t.getGps_coordinates().isEmpty()) {
            try {
                // Encodage des coordonnées pour l'URL
                String destination = URLEncoder.encode(t.getGps_coordinates(), StandardCharsets.UTF_8.toString());
                String url = "https://www.google.com/maps/dir/?api=1&destination=" + destination;
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("Erreur ouverture Google Maps : " + e.getMessage());
            }
        } else {
            System.out.println("Coordonnées GPS manquantes pour ce terrain.");
        }
    }

    private void allerVersReservation(Terrain t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverTerrain.fxml"));
            Parent root = loader.load();
            
            ReserverTerrainController controller = loader.getController();
            controller.setTerrainData(t);
            
            terrainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur navigation réservation : " + e.getMessage());
        }
    }

    @FXML
    void passerALAdmin(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherTerrains.fxml"));
            terrainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur navigation admin : " + e.getMessage());
        }
    }

    @FXML
    void retourAccueil(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) terrainContainer.getScene().getWindow();
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
