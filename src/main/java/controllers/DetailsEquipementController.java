package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Equipement;
import services.CartManager;

public class DetailsEquipementController {

    @FXML private ImageView mainImageView;
    @FXML private Label statusBadge, typeLabel, nameLabel, descLabel, priceLabel, quantityLabel;
    @FXML private Label totalRevenueLabel, maintenanceCostLabel, netProfitLabel;
    @FXML private Button addToCartBtn;

    private int quantity = 1;
    private Equipement equipement;

    public void setEquipement(Equipement e) {
        this.equipement = e;

        nameLabel.setText(e.getNom());
        typeLabel.setText(e.getType().toUpperCase());
        priceLabel.setText(String.format("%.2f DT", e.getPrix_location_jour()));
        quantityLabel.setText("1");

        // État / Disponibilité
        if (e.getQuantite_dispo() > 0) {
            statusBadge.setText("DISPONIBLE (" + e.getQuantite_dispo() + ")");
            statusBadge.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20; -fx-font-weight: bold;");
            addToCartBtn.setDisable(false);
            addToCartBtn.setText("AJOUTER AU PANIER");
        } else {
            statusBadge.setText("NON DISPONIBLE");
            statusBadge.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20; -fx-font-weight: bold;");
            addToCartBtn.setDisable(true);
            addToCartBtn.setText("ÉPUISÉ");
        }

        // Image
        try {
            String path = e.getPhoto();
            if (path == null || path.isEmpty()) {
                path = "/images/default_equip.png";
            } else if (!path.startsWith("http") && !path.startsWith("/")) {
                path = "file:" + path;
            }
            mainImageView.setImage(new Image(path, true));

            // ANALYSE FINANCIERE (NOUVEAU)
            services.LocationEquipementService ls = new services.LocationEquipementService();
            services.TransactionFinanciereService ts = new services.TransactionFinanciereService();
            
            double rev = ls.getTotalRevenue(e.getId());
            double maint = ts.getMaintenanceCosts(e.getNom());
            double net = rev - maint;
            
            totalRevenueLabel.setText(String.format("%.2f DT", rev));
            maintenanceCostLabel.setText(String.format("%.2f DT", maint));
            netProfitLabel.setText(String.format("%.2f DT", net));
            netProfitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: " + (net >= 0 ? "#10B981" : "#EF4444") + ";");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void incrementQuantity() {
        // Vérifier la quantité déjà dans le panier pour cet équipement
        int alreadyInCart = getAlreadyInCart();
        int maxAllowed = equipement.getQuantite_dispo() - alreadyInCart;

        if (quantity < maxAllowed) {
            quantity++;
            quantityLabel.setText(String.valueOf(quantity));
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Stock insuffisant");
            alert.setHeaderText("Quantité maximum atteinte !");
            alert.setContentText("Il ne reste que " + equipement.getQuantite_dispo() + " unité(s) disponible(s) "
                    + (alreadyInCart > 0 ? "(dont " + alreadyInCart + " déjà dans votre panier)." : "."));
            alert.show();
        }
    }

    @FXML
    void decrementQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML
    void addToCart() {
        int alreadyInCart = getAlreadyInCart();
        int totalWanted = alreadyInCart + quantity;

        if (totalWanted > equipement.getQuantite_dispo()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Stock insuffisant");
            alert.setHeaderText("Impossible d'ajouter au panier");
            alert.setContentText("Vous demandez " + totalWanted + " unité(s) mais il n'y en a que "
                    + equipement.getQuantite_dispo() + " disponible(s).");
            alert.show();
            return;
        }

        CartManager.addToCart(equipement, quantity);
        close();
    }

    private int getAlreadyInCart() {
        for (CartManager.CartEntry entry : CartManager.getCartEntries()) {
            if (entry.getEquipement().getId() == equipement.getId()) {
                return entry.getQuantity();
            }
        }
        return 0;
    }

    @FXML
    void close() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }
}
