package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Wallet;
import services.WalletService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GestionWalletsController {

    @FXML private FlowPane walletsFlowPane;
    @FXML private FlowPane cardsFlowPane;

    private WalletService walletService = new WalletService();
    private services.SourceFinancementService sourceService = new services.SourceFinancementService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            // Budgets en lecture seule pour l'utilisateur
            List<Wallet> wallets = walletService.select();
            renderWallets(wallets);

            // Cartes bancaires avec gestion totale pour l'utilisateur
            List<models.SourceFinancement> sources = sourceService.select();
            List<models.SourceFinancement> cards = sources.stream()
                .filter(s -> s.getType() != null && s.getType().toLowerCase().contains("carte"))
                .collect(java.util.stream.Collectors.toList());
            renderCards(cards);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void renderWallets(List<Wallet> list) {
        walletsFlowPane.getChildren().clear();
        for (Wallet w : list) {
            walletsFlowPane.getChildren().add(createWalletCard(w));
        }
    }

    private void renderCards(List<models.SourceFinancement> cards) {
        cardsFlowPane.getChildren().clear();
        for (models.SourceFinancement c : cards) {
            cardsFlowPane.getChildren().add(createCardUI(c));
        }
    }

    private VBox createCardUI(models.SourceFinancement sf) {
        VBox card = new VBox(12);
        card.setPrefSize(280, 160);
        
        String startColor = "#1E293B", endColor = "#0F172A"; 
        if (sf.getNom().toUpperCase().contains("VISA")) { startColor = "#1E293B"; endColor = "#0F172A"; }
        else if (sf.getNom().toUpperCase().contains("MASTERCARD")) { startColor = "#B91C1C"; endColor = "#7F1D1D"; }
        else if (sf.getNom().toUpperCase().contains("POSTE")) { startColor = "#0369A1"; endColor = "#075985"; }

        card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + startColor + ", " + endColor + "); " +
                     "-fx-background-radius: 20; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 8);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("💳");
        icon.setStyle("-fx-font-size: 20; -fx-text-fill: white;");
        Label name = new Label(sf.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13;");
        header.getChildren().addAll(icon, name);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label amount = new Label(String.format("%.2f DT", sf.getMontant()));
        amount.setStyle("-fx-font-weight: bold; -fx-font-size: 22; -fx-text-fill: white;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnDelete = new Button("✕ Supprimer");
        btnDelete.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 10; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            try {
                sourceService.delete(sf.getId());
                loadData();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        actions.getChildren().add(btnDelete);
        card.getChildren().addAll(header, spacer, amount, actions);
        return card;
    }

    private VBox createWalletCard(Wallet w) {
        VBox card = new VBox(15);
        card.setPrefSize(280, 180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 8);");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("💰");
        iconLabel.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center; -fx-font-size: 18;");
        
        VBox titleBox = new VBox(2);
        Label name = new Label(w.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #1F2937;");
        Label subTitle = new Label("Budget Alloue");
        subTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10;");
        titleBox.getChildren().addAll(name, subTitle);
        header.getChildren().addAll(iconLabel, titleBox);

        double progress = w.getBudgetInitial() > 0 ? w.getBudgetActuel() / w.getBudgetInitial() : 0;
        VBox progressSection = new VBox(8);
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(240);
        pb.setPrefHeight(8);
        
        String color = "#10B981";
        if (progress < 0.2) color = "#EF4444";
        else if (progress < 0.5) color = "#F59E0B";
        pb.setStyle("-fx-accent: " + color + "; -fx-background-radius: 10;");

        Label percent = new Label(String.format("%.0f%% restant", progress * 100));
        percent.setStyle("-fx-font-size: 10; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        progressSection.getChildren().addAll(pb, percent);

        Label remaining = new Label(String.format("%.2f DT", w.getBudgetActuel()));
        remaining.setStyle("-fx-font-weight: 900; -fx-font-size: 20; -fx-text-fill: #111827;");
        
        card.getChildren().addAll(header, progressSection, remaining);
        return card;
    }

    @FXML
    void ouvrirAjoutCarte(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireCartePremium.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Nouvelle Carte Bancaire");
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
        loadData();
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) throws IOException {
        loadScene(event, "/FinanceDashboard.fxml", "Ardhi - Finance Dashboard");
    }

    @FXML
    void handleBackToHome(ActionEvent event) throws IOException {
        loadScene(event, "/user-home.fxml", "Ardhi - Accueil");
    }

    @FXML
    void handleOpenTransactions(ActionEvent event) throws IOException {
        loadScene(event, "/TransactionsView.fxml", "Ardhi - Mes Transactions");
    }

    private void loadScene(ActionEvent event, String resource, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        javafx.scene.Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}
