package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import models.LocationEquipement;
import models.Equipement;
import services.LocationEquipementService;
import services.EquipementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoriqueLocationsController {

    @FXML private VBox locationsContainer;

    private LocationEquipementService locationService = new LocationEquipementService();
    private EquipementService equipementService = new EquipementService();

    @FXML
    public void initialize() {
        loadLocations();
    }

    private void loadLocations() {
        locationsContainer.getChildren().clear();
        try {
            // Dans un vrai projet, on filtrerait par l'ID de l'utilisateur connecté
            List<LocationEquipement> list = locationService.select();
            
            if (list.isEmpty()) {
                Label emptyLabel = new Label("Vous n'avez pas encore effectue de location.");
                emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
                locationsContainer.getChildren().add(emptyLabel);
                return;
            }

            for (int i = list.size() - 1; i >= 0; i--) {
                locationsContainer.getChildren().add(createLocationCard(list.get(i)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createLocationCard(LocationEquipement loc) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); " +
                     "-fx-border-color: #F1F5F9; -fx-border-radius: 15; -fx-border-width: 1;");

        // Icon/Badge
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        Label icon = new Label("🚜");
        icon.setStyle("-fx-font-size: 24; -fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 12;");
        iconBox.getChildren().add(icon);

        // Content
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        String equipName = "Equipement #" + loc.getId_equipement();
        try {
            Equipement e = equipementService.findById(loc.getId_equipement());
            if (e != null) equipName = e.getNom();
        } catch (SQLException ex) { ex.printStackTrace(); }

        Label title = new Label(equipName);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #1E293B;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Label dates = new Label("Du " + loc.getDate_location().format(fmt) + " au " + loc.getDate_retour_prevue().format(fmt));
        dates.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        details.getChildren().addAll(title, dates);

        // Status & Price
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label price = new Label(String.format("%.2f DT", loc.getCout_total()));
        price.setStyle("-fx-font-weight: 900; -fx-font-size: 18; -fx-text-fill: #063D2F;");

        Label statusBadge = new Label(loc.getStatut().toUpperCase());
        String statusColor = loc.getStatut().equalsIgnoreCase("en_cours") ? "#10B981" : "#64748B";
        statusBadge.setStyle("-fx-background-color: " + statusColor + "22; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold; -fx-font-size: 10; -fx-padding: 4 10; -fx-background-radius: 10;");

        statusBox.getChildren().addAll(price, statusBadge);

        card.getChildren().addAll(iconBox, details, statusBox);
        return card;
    }

    @FXML
    void handleBackHome(ActionEvent event) throws IOException {
        loadScene(event, "/user-home.fxml");
    }

    @FXML
    void handleOpenCatalogue(ActionEvent event) throws IOException {
        loadScene(event, "/LocationEquipement.fxml");
    }

    @FXML
    void handleOpenCart(ActionEvent event) throws IOException {
        loadScene(event, "/PanierView.fxml");
    }

    private void loadScene(ActionEvent event, String resource) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}
