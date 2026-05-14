package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Equipement;
import services.EquipementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LocationEquipementController {

    @FXML private FlowPane cataloguePane;
    @FXML private TextField searchField;
    @FXML private Label cartCountLabel;
    @FXML private Label cartBtn;

    private EquipementService equipementService = new EquipementService();
    private List<Equipement> allEquipements = new ArrayList<>();

    @FXML
    public void initialize() {
        loadData();
        
        // Mettre à jour le compteur du panier automatiquement
        cartCountLabel.setText(String.valueOf(services.CartManager.getCount()));
        services.CartManager.getCartEntries().addListener((javafx.collections.ListChangeListener<services.CartManager.CartEntry>) c -> {
            cartCountLabel.setText(String.valueOf(services.CartManager.getCount()));
        });

        // Ouvrir le panier au clic
        cartBtn.getParent().setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PanierView.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                Scene scene = new Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                stage.show();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadData() {
        try {
            allEquipements = equipementService.select();
            renderCatalogue(allEquipements);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCatalogue(List<Equipement> equipements) {
        cataloguePane.getChildren().clear();
        for (Equipement e : equipements) {
            cataloguePane.getChildren().add(createEquipementCard(e));
        }
    }

    private VBox createEquipementCard(Equipement e) {
        VBox card = new VBox(15);
        card.setPrefWidth(220);
        card.setPrefHeight(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-cursor: hand;");
        card.setPadding(new Insets(0, 0, 15, 0));

        // Image Container
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefHeight(180);
        imgContainer.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 20 20 0 0;");
        
        ImageView imageView = new ImageView();
        try {
            String path = e.getPhoto();
            if (path == null || path.isEmpty()) {
                path = "/images/default_equip.png";
            } else if (!path.startsWith("http") && !path.startsWith("/")) {
                // Si c'est un chemin local absolu (ex: C:\...), on ajoute file:
                path = "file:" + path;
            }
            imageView.setImage(new Image(path, true));
        } catch (Exception ex) {
            System.err.println("Erreur chargement image : " + ex.getMessage());
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        // Badge Disponibilité
        Label badge = new Label(e.getQuantite_dispo() > 0 ? "DISPONIBLE" : "LOUÉ");
        String badgeColor = e.getQuantite_dispo() > 0 ? "#10B981" : "#EF4444";
        // Correction de la visibilité : Fond plein au lieu de transparent
        badge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 12;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));

        imgContainer.getChildren().addAll(imageView, badge);

        // Info Container
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(0, 15, 0, 15));
        
        Label nameLabel = new Label(e.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #1E293B;");
        
        Label typeLabel = new Label(e.getType());
        typeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12;");
        
        HBox priceBox = new HBox(2);
        priceBox.setAlignment(Pos.BASELINE_LEFT);
        Label priceLabel = new Label(String.format("%.2f", e.getPrix_location_jour()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #063D2F;");
        Label unitLabel = new Label(" DT / jour");
        unitLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11;");
        priceBox.getChildren().addAll(priceLabel, unitLabel);

        infoBox.getChildren().addAll(nameLabel, typeLabel, priceBox);
        
        card.getChildren().addAll(imgContainer, infoBox);

        // Events
        card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 10); -fx-translate-y: -5; -fx-cursor: hand;"));
        card.setOnMouseExited(ev -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-translate-y: 0; -fx-cursor: hand;"));
        
        card.setOnMouseClicked(ev -> showDetails(e));

        return card;
    }

    private void showDetails(Equipement e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEquipement.fxml"));
            Parent root = loader.load();
            
            DetailsEquipementController controller = loader.getController();
            controller.setEquipement(e);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT); // Pour les coins arrondis
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void search() {
        String query = searchField.getText().toLowerCase();
        List<Equipement> filtered = allEquipements.stream()
                .filter(e -> e.getNom().toLowerCase().contains(query) || e.getType().toLowerCase().contains(query))
                .toList();
        renderCatalogue(filtered);
    }

    @FXML void filterAll() { renderCatalogue(allEquipements); }
    @FXML void filterTracteurs() { filterByCategory("tracteur"); }
    @FXML void filterOutils() { filterByCategory("outil"); }
    @FXML void filterIrrigation() { filterByCategory("irrigation"); }
    @FXML void filterDrones() { filterByCategory("drone"); }

    private void filterByCategory(String category) {
        List<Equipement> filtered = allEquipements.stream()
                .filter(e -> e.getType().toLowerCase().contains(category.toLowerCase()))
                .toList();
        renderCatalogue(filtered);
    }
}
