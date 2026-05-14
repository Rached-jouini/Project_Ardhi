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
    private WalletService walletService = new WalletService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            List<Wallet> list = walletService.select();
            renderWallets(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void renderWallets(List<Wallet> list) {
        walletsFlowPane.getChildren().clear();
        for (Wallet w : list) {
            walletsFlowPane.getChildren().add(createWalletCard(w));
        }
    }

    private VBox createWalletCard(Wallet w) {
        VBox card = new VBox(15);
        card.setPrefSize(300, 220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");

        // Header: Icon + Name + Delete
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Icon Circle
        Label iconLabel = new Label("💰"); // On pourra changer l'icone selon le nom plus tard
        iconLabel.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center; -fx-font-size: 20;");
        
        VBox titleBox = new VBox(2);
        Label name = new Label(w.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 17; -fx-text-fill: #1F2937;");
        Label subTitle = new Label("Budget Alloué");
        subTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11;");
        titleBox.getChildren().addAll(name, subTitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0EA5E9; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
        btnEdit.setOnAction(e -> ouvrirModificationWallet(w));

        Button btnDelete = new Button("✕");
        btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
        btnDelete.setOnAction(e -> supprimerWallet(w));
        
        header.getChildren().addAll(iconLabel, titleBox, spacer, btnEdit, btnDelete);

        // Progress logic
        double progress = w.getBudgetInitial() > 0 ? w.getBudgetActuel() / w.getBudgetInitial() : 0;
        
        VBox progressSection = new VBox(8);
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(250);
        pb.setPrefHeight(10);
        
        // Color logic
        String color = "#10B981"; // Green
        if (progress < 0.2) color = "#EF4444"; // Red
        else if (progress < 0.5) color = "#F59E0B"; // Orange
        pb.setStyle("-fx-accent: " + color + "; -fx-background-radius: 10; -fx-control-inner-background: #F3F4F6;");

        HBox labels = new HBox();
        Label percent = new Label(String.format("%.0f%% restant", progress * 100));
        percent.setStyle("-fx-font-size: 11; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        labels.getChildren().add(percent);
        progressSection.getChildren().addAll(pb, labels);

        // Balance
        VBox balanceBox = new VBox(2);
        Label remaining = new Label(String.format("%.2f DT", w.getBudgetActuel()));
        remaining.setStyle("-fx-font-weight: 900; -fx-font-size: 24; -fx-text-fill: #111827;");
        Label total = new Label("sur " + w.getBudgetInitial() + " DT au total");
        total.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");
        
        balanceBox.getChildren().addAll(remaining, total);
        
        card.getChildren().addAll(header, progressSection, balanceBox);
        
        return card;
    }

    @FXML
    void ouvrirAjoutWallet(ActionEvent event) throws IOException {
        chargerFormulaire(null);
    }

    private void ouvrirModificationWallet(Wallet w) {
        try {
            chargerFormulaire(w);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void chargerFormulaire(Wallet w) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireWallet.fxml"));
        Parent root = loader.load();
        
        if (w != null) {
            FormulaireWalletController controller = loader.getController();
            controller.setWalletData(w);
        }
        
        Stage stage = new Stage();
        stage.setTitle(w == null ? "Nouveau Budget" : "Modifier Budget");
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
        loadData();
    }

    private void supprimerWallet(Wallet w) {
        try {
            walletService.delete(w.getId());
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void retourDashboard(ActionEvent event) {
        MainLayoutController.getInstance().loadPage("/FinanceDashboard.fxml");
    }
}
