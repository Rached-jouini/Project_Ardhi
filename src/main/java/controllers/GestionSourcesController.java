package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import models.SourceFinancement;
import models.Wallet;
import services.SourceFinancementService;
import services.WalletService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GestionSourcesController {

    @FXML private VBox mainContainer;
    private SourceFinancementService sourceService = new SourceFinancementService();
    private WalletService walletService = new WalletService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            List<SourceFinancement> sources = sourceService.select();
            List<Wallet> wallets = walletService.select();
            renderSources(sources, wallets);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderSources(List<SourceFinancement> sources, List<Wallet> wallets) {
        mainContainer.getChildren().clear();
        
        // --- SECTION 1 : CARTES BANCAIRES ---
        Label titleCards = new Label("MES CARTES BANCAIRES");
        titleCards.setStyle("-fx-font-weight: bold; -fx-text-fill: #064E3B; -fx-font-size: 14; -fx-letter-spacing: 1;");
        
        FlowPane cardsPane = new FlowPane(30, 30);
        cardsPane.setPadding(new javafx.geometry.Insets(15, 0, 40, 0));
        
        // --- SECTION 2 : WALLETS & BUDGETS (STYLE CAPTURE) ---
        Label titleBudgets = new Label("MES WALLETS & BUDGETS ALLOUÉS");
        titleBudgets.setStyle("-fx-font-weight: bold; -fx-text-fill: #064E3B; -fx-font-size: 14; -fx-letter-spacing: 1;");
        
        FlowPane budgetsPane = new FlowPane(25, 25);
        budgetsPane.setPadding(new javafx.geometry.Insets(15, 0, 40, 0));

        // --- SECTION 3 : AUTRES PORTEFEUILLES & CASH ---
        Label titleWallets = new Label("AUTRES PORTEFEUILLES & CASH");
        titleWallets.setStyle("-fx-font-weight: bold; -fx-text-fill: #064E3B; -fx-font-size: 14; -fx-letter-spacing: 1;");
        
        FlowPane walletsPane = new FlowPane(25, 25);
        walletsPane.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        boolean hasCards = false;
        boolean hasWallets = false;

        // Rendu des sources de financement
        for (SourceFinancement sf : sources) {
            String nameLower = sf.getNom().toLowerCase();
            if (nameLower.contains("globalnet") || nameLower.contains("telecom") || nameLower.contains("ooredoo") || nameLower.contains("3s") || nameLower.contains("centrale")) {
                continue;
            }

            if (nameLower.contains("stb") || nameLower.contains("visa") || nameLower.contains("biat") || nameLower.contains("master") || nameLower.contains("banque")) {
                cardsPane.getChildren().add(createCreditCard(sf));
                hasCards = true;
            } else {
                walletsPane.getChildren().add(createWalletStyleCard(sf));
                hasWallets = true;
            }
        }

        // Rendu des Wallets (Budgets)
        for (Wallet w : wallets) {
            budgetsPane.getChildren().add(createBudgetCard(w));
        }

        if (hasCards) mainContainer.getChildren().addAll(titleCards, cardsPane);
        if (!wallets.isEmpty()) mainContainer.getChildren().addAll(titleBudgets, budgetsPane);
        if (hasWallets) mainContainer.getChildren().addAll(titleWallets, walletsPane);
        
        if (!hasCards && !hasWallets && wallets.isEmpty()) {
            mainContainer.getChildren().add(new Label("Aucun compte ou budget trouvé."));
        }
    }

    private Pane createCreditCard(SourceFinancement sf) {
        VBox card = new VBox(20);
        card.setPrefSize(340, 210);
        
        String nameLower = sf.getNom().toLowerCase();
        LinearGradient gradient;
        String brandLogo = "💳";
        String brandName = sf.getNom().toUpperCase();

        // --- DÉTECTION DU STYLE SELON LA BANQUE OU L'OPÉRATEUR ---
        if (nameLower.contains("stb")) {
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#059669")), new Stop(1, Color.web("#10B981")));
            brandLogo = "🏦";
            brandName = "STB Bank - Agri";
        } else if (nameLower.contains("biat")) {
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#B91C1C")), new Stop(1, Color.web("#1E3A8A")));
            brandLogo = "🏦";
            brandName = "BIAT - Finance";
        } else if (nameLower.contains("centrale") || nameLower.contains("banque")) {
            // Style Or / Noir pour les banques centrales
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#374151")), new Stop(1, Color.web("#111827")));
            brandLogo = "🏛️";
            brandName = sf.getNom().toUpperCase();
        } else if (nameLower.contains("ooredoo") || nameLower.contains("orange")) {
            // Style Rouge / Orange
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#EF4444")), new Stop(1, Color.web("#F97316")));
            brandLogo = "📶";
            brandName = sf.getNom().toUpperCase();
        } else if (nameLower.contains("telecom") || nameLower.contains("globalnet") || nameLower.contains("3s")) {
            // Style Tech Bleu
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0369A1")), new Stop(1, Color.web("#0EA5E9")));
            brandLogo = "🌐";
            brandName = sf.getNom().toUpperCase();
        } else if (nameLower.contains("visa") || nameLower.contains("master")) {
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#1E1B4B")), new Stop(1, Color.web("#312E81")));
            brandLogo = "💳";
            brandName = "Ardhi Card (Visa)";
        } else if (nameLower.contains("espèces") || nameLower.contains("cash") || nameLower.contains("caisse")) {
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#064E3B")), new Stop(1, Color.web("#065F46")));
            brandLogo = "💵";
            brandName = "Caisse Espèces";
        } else {
            // Gris Anthracite par défaut (plus pro)
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#4B5563")), new Stop(1, Color.web("#1F2937")));
        }

        card.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(20), null)));
        card.setPadding(new javafx.geometry.Insets(25));
        card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 10);");

        // Header: Bank Logo & Name
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label logoLabel = new Label(brandLogo);
        logoLabel.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8; -fx-padding: 5; -fx-text-fill: white; -fx-font-size: 18;");
        
        Label bankLabel = new Label(brandName);
        bankLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        
        header.getChildren().addAll(logoLabel, bankLabel);

        // Center: Amount
        VBox amountBox = new VBox(5);
        amountBox.setAlignment(Pos.CENTER_LEFT);
        Label balTitle = new Label("Solde disponible");
        balTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11;");
        
        Label amount = new Label(String.format("%.2f DT", sf.getMontant()));
        amount.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 28;");
        
        // Si solde négatif, on peut mettre une petite alerte visuelle
        if (sf.getMontant() < 0) {
            amount.setStyle("-fx-text-fill: #FECACA; -fx-font-weight: 900; -fx-font-size: 28;"); // Rouge clair
        }
        
        amountBox.getChildren().addAll(balTitle, amount);

        // Footer: Card Detail & Delete
        HBox footer = new HBox();
        footer.setAlignment(Pos.BOTTOM_LEFT);
        
        VBox infoBox = new VBox(2);
        Label number = new Label();
        String fullNum = sf.getDescription();
        if (fullNum != null && fullNum.length() >= 4) {
            String last4 = fullNum.substring(fullNum.length() - 4);
            number.setText("**** **** **** " + last4);
        } else {
            number.setText("**** **** **** ****");
        }
        number.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-family: 'Courier New'; -fx-letter-spacing: 2;");
        
        String typeLabel = sf.getDescription() != null ? sf.getDescription() : "Compte de gestion";
        Label descLabel = new Label(typeLabel);
        descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 10;");
        
        infoBox.getChildren().addAll(number, descLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnDelete = new Button("✕");
        btnDelete.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> supprimerSource(sf));
        
        footer.getChildren().addAll(infoBox, spacer, btnDelete);

        card.getChildren().addAll(header, amountBox, footer);
        
        return card;
    }

    private Pane createWalletStyleCard(SourceFinancement sf) {
        HBox card = new HBox(15);
        card.setPrefSize(280, 100);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label iconLabel = new Label("💰");
        iconLabel.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 10; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center; -fx-font-size: 20;");

        VBox details = new VBox(2);
        Label name = new Label(sf.getNom().toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 14;");
        Label amount = new Label(String.format("%.2f DT", sf.getMontant()));
        amount.setStyle("-fx-font-weight: 900; -fx-font-size: 18; -fx-text-fill: #111827;");
        details.getChildren().addAll(name, amount);

        card.getChildren().addAll(iconLabel, details);
        return card;
    }

    private Pane createBudgetCard(Wallet w) {
        VBox card = new VBox(15);
        card.setPrefSize(280, 180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("💰");
        iconLabel.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center; -fx-font-size: 18; -fx-text-fill: #10B981;");
        
        VBox titleBox = new VBox(2);
        Label name = new Label(w.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 15;");
        Label sub = new Label("Budget Alloué");
        sub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 10;");
        titleBox.getChildren().addAll(name, sub);
        
        header.getChildren().addAll(iconLabel, titleBox);

        // Progress Bar
        double progress = w.getBudgetInitial() > 0 ? (w.getBudgetActuel() / w.getBudgetInitial()) : 0;
        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(240);
        bar.setStyle("-fx-accent: #10B981; -fx-control-inner-background: #F3F4F6; -fx-background-color: transparent; -fx-padding: 0;");
        
        // Percentage label
        double percentValue = progress * 100;
        Label percent = new Label(String.format("%.0f%% restant", percentValue));
        percent.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 10;");
        if (percentValue < 20) percent.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 10;");

        VBox content = new VBox(8);
        Label amount = new Label(String.format("%.2f DT", w.getBudgetActuel()));
        amount.setStyle("-fx-font-weight: 900; -fx-font-size: 20; -fx-text-fill: #111827;");
        
        Label total = new Label(String.format("sur %.1f DT au total", w.getBudgetInitial()));
        total.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10;");
        
        content.getChildren().addAll(bar, percent, amount, total);

        card.getChildren().addAll(header, content);
        return card;
    }

    @FXML
    void ouvrirAjoutSource(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireCartePremium.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Nouvelle Carte Bancaire");
        stage.setScene(new Scene(root));
        stage.showAndWait();
        loadData();
    }

    private void supprimerSource(SourceFinancement sf) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce compte ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    sourceService.delete(sf.getId());
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }
}
