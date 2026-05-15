package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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

public class AdminGestionBudgetsController {

    @FXML private FlowPane walletsFlowPane;

    private WalletService walletService = new WalletService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            List<Wallet> wallets = walletService.select();
            renderWallets(wallets);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void renderWallets(List<Wallet> list) {
        walletsFlowPane.getChildren().clear();
        for (Wallet w : list) {
            walletsFlowPane.getChildren().add(createAdminWalletCard(w));
        }
    }

    private VBox createAdminWalletCard(Wallet w) {
        VBox card = new VBox(15);
        card.setPrefSize(280, 220); // Taille adaptée pour inclure les boutons
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 8);");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("💰");
        iconLabel.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center; -fx-font-size: 18;");
        
        VBox titleBox = new VBox(2);
        Label name = new Label(w.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #1F2937;");
        Label subTitle = new Label("Budget Systeme");
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
        
        // BOUTONS D'ACTION (ADMIN SEULEMENT)
        HBox adminActions = new HBox(10);
        adminActions.setAlignment(Pos.CENTER_RIGHT);
        adminActions.setPadding(new Insets(10, 0, 0, 0));

        Button btnEdit = new Button("✏️ Modifier");
        btnEdit.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> ouvrirModificationWallet(w));

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 8; -fx-font-size: 10; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce budget ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) supprimerWallet(w);
            });
        });

        adminActions.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(header, progressSection, remaining, adminActions);
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
    void handleBackToDashboard(ActionEvent event) throws IOException {
        loadScene(event, "/admin-dashboard.fxml", "Ardhi - Dashboard Admin");
    }

    private void loadScene(ActionEvent event, String resource, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        javafx.scene.Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}
