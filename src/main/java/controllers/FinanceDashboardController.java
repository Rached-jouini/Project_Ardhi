package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import models.Capitale;
import models.TransactionFinanciere;
import models.Wallet;
import services.CapitalService;
import services.SourceFinancementService;
import services.PdfService;
import services.TransactionFinanciereService;
import services.WalletService;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class FinanceDashboardController implements Initializable {

    @FXML private Label totalCapitalLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label eurLabel, usdLabel;
    @FXML private HBox cardsContainer;
    @FXML private ComboBox<String> chartScaleCombo;
    @FXML private BarChart<String, Number> revenueTrendChart;
    @FXML private VBox walletProgressVBox;
    @FXML private VBox recentTransactionsVBox;
    @FXML private VBox sourcesListVBox;

    private TransactionFinanciereService transactionService = new TransactionFinanciereService();
    private SourceFinancementService sourceService = new SourceFinancementService();
    private List<TransactionFinanciere> cachedTransactions = new java.util.ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le sélecteur d'échelle
        chartScaleCombo.getItems().addAll("Mensuel", "Trimestriel", "Annuel");
        chartScaleCombo.setValue("Mensuel");
        chartScaleCombo.valueProperty().addListener((obs, old, val) -> updateChart(val));
        
        refreshData();
    }

    private void refreshData() {
        TransactionFinanciereService ts = new TransactionFinanciereService();

        try {
            // --- CALCULS FINANCIERS CENTRAUX ---
            List<models.SourceFinancement> sources = sourceService.select();
            // Calculer le capital en excluant les cartes bancaires
            double capitalInitial = sources.stream()
                    .filter(s -> s.getType() == null || 
                            (!s.getType().toLowerCase().contains("carte") &&
                             !s.getType().toLowerCase().contains("bank") &&
                             !s.getType().toLowerCase().contains("bancaire") &&
                             !s.getNom().toLowerCase().contains("visa") &&
                             !s.getNom().toLowerCase().contains("mastercard")))
                    .mapToDouble(models.SourceFinancement::getMontant).sum();
            
            List<TransactionFinanciere> allTransactions = transactionService.select();
            double totalRev = allTransactions.stream().filter(t -> t.getType().equalsIgnoreCase("Revenu")).mapToDouble(TransactionFinanciere::getMontant).sum();
            double totalExp = allTransactions.stream().filter(t -> t.getType().equalsIgnoreCase("Depense")).mapToDouble(TransactionFinanciere::getMontant).sum();
            
            double soldeActuel = capitalInitial + totalRev - totalExp;
            
            // Mise à jour des Labels principaux
            totalCapitalLabel.setText(String.format("%.2f DT", soldeActuel));
            totalRevenueLabel.setText(String.format("%.2f DT", totalRev));
            totalExpenseLabel.setText(String.format("%.2f DT", totalExp));

            // API : Conversion sur le solde REEL
            double eur = services.CurrencyService.convertTNDtoEUR(soldeActuel);
            double usd = services.CurrencyService.convertTNDtoUSD(soldeActuel);
            eurLabel.setText(String.format("%.2f €", eur));
            usdLabel.setText(String.format("%.2f $", usd));

            // Affichage de la liste des sources (SANS les cartes bancaires)
            sourcesListVBox.getChildren().clear();
            for (models.SourceFinancement sf : sources) {
                // Exclure les cartes bancaires (elles ont leur propre section)
                if (sf.getType() != null && 
                    (sf.getType().toLowerCase().contains("carte") ||
                     sf.getType().toLowerCase().contains("bank") ||
                     sf.getType().toLowerCase().contains("bancaire") ||
                     sf.getNom().toLowerCase().contains("visa") ||
                     sf.getNom().toLowerCase().contains("mastercard"))) {
                    continue;
                }
                
                HBox row = new HBox(10);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 5; -fx-border-color: #F3F4F6; -fx-border-width: 0 0 1 0;");
                
                Label name = new Label(sf.getNom());
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 14;");
                
                Label type = new Label(sf.getType());
                type.setStyle("-fx-font-size: 10; -fx-background-color: #E5E7EB; -fx-padding: 2 7; -fx-background-radius: 5;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label amount = new Label(String.format("%.2f DT", sf.getMontant()));
                amount.setStyle("-fx-font-weight: bold; -fx-text-fill: #063D2F; -fx-font-size: 14;");
                
                row.getChildren().addAll(name, type, spacer, amount);
                sourcesListVBox.getChildren().add(row);
            }

            // Graphique d'evolution dynamique
            cachedTransactions = new java.util.ArrayList<>(allTransactions);
            updateChart(chartScaleCombo.getValue());

            // Génération DYNAMIQUE des CARTES BANCAIRES uniquement
            cardsContainer.getChildren().clear();
            
            // Filtrer pour ne garder que les cartes bancaires (pas les sources de capital)
            List<models.SourceFinancement> cartesBancaires = sources.stream()
                    .filter(s -> s.getType() != null && 
                            (s.getType().toLowerCase().contains("carte") ||
                             s.getType().toLowerCase().contains("bank") ||
                             s.getType().toLowerCase().contains("bancaire")))
                    .collect(java.util.stream.Collectors.toList());
            
            // Couleurs de gradient pour les cartes bancaires
            String[][] cardStyles = {
                {"#1E293B", "#0F172A"},  // Dark blue (Visa)
                {"#4C1D95", "#5B21B6"},  // Purple (Mastercard)
                {"#063D2F", "#085041"},  // Dark green
                {"#7C2D12", "#9A3412"},  // Amber
                {"#164E63", "#155E75"},  // Teal
            };
            
            for (int idx = 0; idx < cartesBancaires.size(); idx++) {
                models.SourceFinancement sf = cartesBancaires.get(idx);
                String[] colors = cardStyles[idx % cardStyles.length];
                
                VBox card = new VBox(12);
                card.setPrefHeight(150);
                card.setPrefWidth(280);
                card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colors[0] + ", " + colors[1] + "); " +
                        "-fx-background-radius: 20; -fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 8);");
                
                // Header (icône + nom)
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label iconLabel = new Label("💳");
                iconLabel.setStyle("-fx-font-size: 20; -fx-text-fill: white;");
                Label nameLabel = new Label(sf.getNom());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: white; -fx-opacity: 0.9;");
                header.getChildren().addAll(iconLabel, nameLabel);
                
                // Spacer
                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);
                
                // Label "Solde disponible"
                Label subLabel = new Label("Solde disponible");
                subLabel.setStyle("-fx-font-size: 10; -fx-text-fill: white; -fx-opacity: 0.6;");
                
                // Montant
                Label montantLabel = new Label(String.format("%.2f DT", sf.getMontant()));
                montantLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: white;");
                
                // Numéro de carte masqué
                if (sf.getDescription() != null && !sf.getDescription().isEmpty()) {
                    String desc = sf.getDescription();
                    String digits = desc.replaceAll("[^0-9]", "");
                    if (digits.length() >= 4) {
                        String masked = "**** **** **** " + digits.substring(digits.length() - 4);
                        Label maskedLabel = new Label(masked);
                        maskedLabel.setStyle("-fx-font-size: 12; -fx-text-fill: white; -fx-opacity: 0.6;");
                        
                        // Numéro complet en petit
                        Label fullNumLabel = new Label(desc);
                        fullNumLabel.setStyle("-fx-font-size: 9; -fx-text-fill: white; -fx-opacity: 0.4;");
                        
                        card.getChildren().addAll(header, subLabel, montantLabel, maskedLabel, fullNumLabel);
                    } else {
                        card.getChildren().addAll(header, spacer, subLabel, montantLabel);
                    }
                } else {
                    card.getChildren().addAll(header, spacer, subLabel, montantLabel);
                }
                
                cardsContainer.getChildren().add(card);
            }

            // === SOLDE DES BUDGETS (Wallets) ===
            walletProgressVBox.getChildren().clear();
            WalletService ws = new WalletService();
            List<Wallet> walletsList = ws.select();
            
            String[] walletColors = {"#10B981", "#3B82F6", "#F59E0B", "#8B5CF6", "#EF4444", "#06B6D4"};
            
            for (int wi = 0; wi < walletsList.size(); wi++) {
                Wallet w = walletsList.get(wi);
                String color = walletColors[wi % walletColors.length];
                double pct = w.getBudgetInitial() > 0 ? (w.getBudgetActuel() / w.getBudgetInitial()) * 100 : 0;
                
                VBox walletRow = new VBox(4);
                
                // Nom + pourcentage
                HBox nameRow = new HBox(8);
                nameRow.setAlignment(Pos.CENTER_LEFT);
                Label wName = new Label(w.getNom());
                wName.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #374151;");
                Region wSpacer = new Region();
                HBox.setHgrow(wSpacer, Priority.ALWAYS);
                Label wPct = new Label(String.format("%.0f%%", pct));
                String pctColor = pct > 50 ? "#10B981" : pct > 20 ? "#F59E0B" : "#EF4444";
                wPct.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-text-fill: " + pctColor + ";");
                nameRow.getChildren().addAll(wName, wSpacer, wPct);
                
                // Barre de progression
                StackPane progressBar = new StackPane();
                progressBar.setPrefHeight(8);
                progressBar.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 4;");
                
                Region fill = new Region();
                fill.setPrefHeight(8);
                double fillPct = Math.min(Math.max(pct, 0), 100);
                fill.setMaxWidth(Double.MAX_VALUE);
                fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
                // Bind width to percentage of parent
                fill.prefWidthProperty().bind(progressBar.widthProperty().multiply(fillPct / 100.0));
                StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                progressBar.getChildren().add(fill);
                
                // Montants
                HBox amountRow = new HBox();
                amountRow.setAlignment(Pos.CENTER_LEFT);
                Label wAmount = new Label(String.format("%.0f DT / %.0f DT", w.getBudgetActuel(), w.getBudgetInitial()));
                wAmount.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF;");
                amountRow.getChildren().add(wAmount);
                
                walletRow.getChildren().addAll(nameRow, progressBar, amountRow);
                walletProgressVBox.getChildren().add(walletRow);
            }
            
            // === DERNIERES TRANSACTIONS ===
            recentTransactionsVBox.getChildren().clear();
            List<TransactionFinanciere> recentTx = allTransactions.stream()
                    .sorted((a, b) -> b.getDate_operation().compareTo(a.getDate_operation()))
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
            
            for (TransactionFinanciere tx : recentTx) {
                HBox txRow = new HBox(10);
                txRow.setAlignment(Pos.CENTER_LEFT);
                txRow.setStyle("-fx-padding: 8 12; -fx-background-color: #F9FAFB; -fx-background-radius: 10;");
                
                boolean isRevenu = tx.getType().equalsIgnoreCase("Revenu");
                
                // Icône
                Label txIcon = new Label(isRevenu ? "📈" : "📉");
                txIcon.setStyle("-fx-font-size: 16;");
                
                // Info
                VBox txInfo = new VBox(2);
                HBox.setHgrow(txInfo, Priority.ALWAYS);
                String desc = tx.getDescription() != null ? tx.getDescription() : tx.getType();
                if (desc.length() > 30) desc = desc.substring(0, 27) + "...";
                Label txDesc = new Label(desc);
                txDesc.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-text-fill: #374151;");
                Label txDate = new Label(tx.getDate_operation().toString() + "  •  " + tx.getModePaiement());
                txDate.setStyle("-fx-font-size: 9; -fx-text-fill: #9CA3AF;");
                txInfo.getChildren().addAll(txDesc, txDate);
                
                // Montant
                Label txAmount = new Label((isRevenu ? "+" : "-") + String.format("%.2f DT", tx.getMontant()));
                txAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: " + (isRevenu ? "#10B981" : "#EF4444") + ";");
                
                txRow.getChildren().addAll(txIcon, txInfo, txAmount);
                recentTransactionsVBox.getChildren().add(txRow);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void goToSettings(ActionEvent event) throws IOException {
        MainLayoutController.getInstance().loadPage("/ConfigCapitale.fxml");
    }

    @FXML
    void goToWallets(ActionEvent event) throws IOException {
        MainLayoutController.getInstance().loadPage("/GestionWallets.fxml");
    }

    @FXML
    void goToAddTransaction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireTransaction.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Nouvelle Transaction");
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
        refreshData(); // Rafraichir le dashboard apres l'ajout
    }

    @FXML
    void handleBackHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/user-home.fxml"));
        totalCapitalLabel.getScene().setRoot(root);
    }

    @FXML
    void handleOpenTransactions(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/TransactionsView.fxml"));
        totalCapitalLabel.getScene().setRoot(root);
    }

    @FXML
    void handleOpenWallets(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/GestionWallets.fxml"));
        totalCapitalLabel.getScene().setRoot(root);
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            totalCapitalLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Met à jour le graphique selon l'échelle sélectionnée */
    private void updateChart(String scale) {
        revenueTrendChart.getData().clear();

        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenus");
        XYChart.Series<String, Number> expSeries = new XYChart.Series<>();
        expSeries.setName("Dépenses");

        int anneeActuelle = LocalDate.now().getYear();

        if ("Annuel".equals(scale)) {
            // Afficher les 3 dernières années
            for (int y = anneeActuelle - 2; y <= anneeActuelle; y++) {
                final int year = y;
                double rev = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Revenu") && t.getDate_operation().getYear() == year)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                double exp = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Depense") && t.getDate_operation().getYear() == year)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                revSeries.getData().add(new XYChart.Data<>(String.valueOf(year), rev));
                expSeries.getData().add(new XYChart.Data<>(String.valueOf(year), exp));
            }
        } else if ("Trimestriel".equals(scale)) {
            // Afficher les 4 trimestres de l'année en cours
            String[] trimNoms = {"T1 (Jan-Mar)", "T2 (Avr-Jun)", "T3 (Jul-Sep)", "T4 (Oct-Déc)"};
            for (int q = 0; q < 4; q++) {
                final int moisDebut = q * 3 + 1;
                final int moisFin = q * 3 + 3;
                double rev = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Revenu")
                                && t.getDate_operation().getYear() == anneeActuelle
                                && t.getDate_operation().getMonthValue() >= moisDebut
                                && t.getDate_operation().getMonthValue() <= moisFin)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                double exp = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Depense")
                                && t.getDate_operation().getYear() == anneeActuelle
                                && t.getDate_operation().getMonthValue() >= moisDebut
                                && t.getDate_operation().getMonthValue() <= moisFin)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                revSeries.getData().add(new XYChart.Data<>(trimNoms[q], rev));
                expSeries.getData().add(new XYChart.Data<>(trimNoms[q], exp));
            }
        } else {
            // Mensuel : afficher les 12 mois de l'année en cours
            String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"};
            for (int m = 1; m <= 12; m++) {
                final int mois = m;
                double rev = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Revenu")
                                && t.getDate_operation().getYear() == anneeActuelle
                                && t.getDate_operation().getMonthValue() == mois)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                double exp = cachedTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase("Depense")
                                && t.getDate_operation().getYear() == anneeActuelle
                                && t.getDate_operation().getMonthValue() == mois)
                        .mapToDouble(TransactionFinanciere::getMontant).sum();
                revSeries.getData().add(new XYChart.Data<>(moisNoms[m - 1], rev));
                expSeries.getData().add(new XYChart.Data<>(moisNoms[m - 1], exp));
            }
        }

        revenueTrendChart.getData().addAll(revSeries, expSeries);
    }

    /** Génère un rapport PDF des transactions */
    @FXML
    void generatePDF(ActionEvent event) {
        try {
            PdfService pdfService = new PdfService();
            String fileName = "Bilan_Financier_" + LocalDate.now().toString();
            String path = pdfService.generateFinancialReport(cachedTransactions, fileName);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rapport PDF");
            alert.setHeaderText("Fichier généré avec succès !");
            alert.setContentText("Le rapport a été enregistré sur votre bureau :\n" + path);
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération : " + e.getMessage()).show();
        }
    }
}
