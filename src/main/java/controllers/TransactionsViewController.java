package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.TransactionFinanciere;
import models.Wallet;
import services.SmsService;
import services.TransactionFinanciereService;
import services.WalletService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsViewController {

    @FXML private ToggleButton btnRevenu, btnDepense;
    @FXML private ToggleGroup typeGroup;
    @FXML private TextField montantField, phoneField;
    @FXML private ComboBox<Wallet> walletCombo;
    @FXML private ComboBox<String> modePaiementCombo, prefixCombo;
    @FXML private TextArea descField;
    @FXML private VBox historyContainer;

    private TransactionFinanciereService transactionService = new TransactionFinanciereService();
    private WalletService walletService = new WalletService();
    private SmsService smsService = new SmsService();
    private Map<Integer, String> walletNames = new HashMap<>();

    @FXML
    public void initialize() {
        setupForm();
        loadHistory();
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) throws java.io.IOException {
        loadScene(event, "/FinanceDashboard.fxml", "Ardhi - Finance Dashboard");
    }

    @FXML
    void handleBackToHome(ActionEvent event) throws java.io.IOException {
        loadScene(event, "/user-home.fxml", "Ardhi - Accueil");
    }

    @FXML
    void handleOpenWallets(ActionEvent event) throws java.io.IOException {
        loadScene(event, "/GestionWallets.fxml", "Ardhi - Comptes et Cartes");
    }

    private void loadScene(ActionEvent event, String resource, String title) throws java.io.IOException {
        javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(resource));
        javafx.scene.Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    private void setupForm() {
        // --- GESTION DES TOGGLE BUTTONS (REVENU/DEPENSE) ---
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == btnRevenu) {
                btnRevenu.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
                btnDepense.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 10; -fx-font-weight: bold;");
            } else if (newVal == btnDepense) {
                btnDepense.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
                btnRevenu.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-background-radius: 10; -fx-font-weight: bold;");
            }
        });
        // Style initial (Revenu est sélectionné par défaut)
        btnRevenu.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
        btnDepense.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 10; -fx-font-weight: bold;");

        // Mode de paiement
        modePaiementCombo.setItems(FXCollections.observableArrayList("Espèces", "Carte Bancaire", "Virement", "Chèque"));
        
        // Indicatifs pays
        prefixCombo.setItems(FXCollections.observableArrayList("+216", "+33", "+1", "+212", "+213"));
        prefixCombo.setValue("+216");

        // Charger les wallets
        try {
            List<Wallet> wallets = walletService.select();
            walletCombo.setItems(FXCollections.observableArrayList(wallets));
            
            // Pour l'affichage dans l'historique
            for (Wallet w : wallets) {
                walletNames.put(w.getId(), w.getNom());
            }
            
            // Custom display for ComboBox
            walletCombo.setCellFactory(lv -> new ListCell<Wallet>() {
                @Override protected void updateItem(Wallet item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
            walletCombo.setButtonCell(new ListCell<Wallet>() {
                @Override protected void updateItem(Wallet item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() {
        historyContainer.getChildren().clear();
        try {
            List<TransactionFinanciere> list = transactionService.select();
            // Inverser la liste pour avoir les plus récents en haut
            for (int i = list.size() - 1; i >= 0; i--) {
                historyContainer.getChildren().add(createTransactionCard(list.get(i)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createTransactionCard(TransactionFinanciere tf) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 4);");

        boolean isRevenu = "REVENU".equalsIgnoreCase(tf.getType()) || tf.getType().contains("+");
        
        // Icone
        Label iconLabel = new Label(isRevenu ? "🔼" : "🔽");
        iconLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 10; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center; -fx-font-size: 16;", 
                isRevenu ? "#ECFDF5" : "#FEE2E2", 
                isRevenu ? "#059669" : "#EF4444"));

        // Details
        VBox details = new VBox(2);
        String walletName = walletNames.getOrDefault(tf.getWalletId(), "Compte Principal");
        Label title = new Label(tf.getDescription() != null && !tf.getDescription().isEmpty() ? tf.getDescription() : "Transaction " + walletName);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-font-size: 14;");
        
        String dateStr = tf.getDate_operation().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label subTitle = new Label(dateStr + " • " + (tf.getModePaiement() != null ? tf.getModePaiement() : "Espèces"));
        subTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11;");
        
        details.getChildren().addAll(title, subTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Montant
        Label amountLabel = new Label((isRevenu ? "+ " : "- ") + String.format("%.2f DT", tf.getMontant()));
        amountLabel.setStyle(String.format("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: %s;", isRevenu ? "#059669" : "#EF4444"));

        card.getChildren().addAll(iconLabel, details, spacer, amountLabel);
        return card;
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        try {
            // Validation simple
            if (montantField.getText().isEmpty() || walletCombo.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir le montant et choisir un wallet.");
                return;
            }

            double montant = Double.parseDouble(montantField.getText());
            String type = btnRevenu.isSelected() ? "REVENU" : "DEPENSE";
            Wallet wallet = walletCombo.getValue();
            String mode = modePaiementCombo.getValue();
            String notes = descField.getText();

            TransactionFinanciere tf = new TransactionFinanciere();
            tf.setType(type);
            tf.setMontant(montant);
            tf.setWalletId(wallet.getId());
            tf.setModePaiement(mode);
            tf.setDescription(notes);

            // 1. Enregistrer en base
            transactionService.add(tf);

            // 2. Mettre à jour le solde du Wallet
            if ("DEPENSE".equals(type)) {
                walletService.deduireDepense(wallet.getId(), montant);
            } else {
                // Pour les revenus, on peut ajouter une méthode similaite ou faire un update
                wallet.setBudgetActuel(wallet.getBudgetActuel() + montant);
                walletService.update(wallet);
            }

            // 3. Envoyer SMS si numéro présent
            if (!phoneField.getText().isEmpty()) {
                String fullNumber = prefixCombo.getValue() + phoneField.getText().replace(" ", "");
                String message = String.format("ARDHI: %s de %.2f DT effectue avec succes. Nouveau solde: %.2f DT.", 
                                                type, montant, wallet.getBudgetActuel() + (type.equals("REVENU") ? 0 : -montant));
                smsService.sendSms(fullNumber, message);
            }

            // 4. Rafraîchir
            loadHistory();
            clearForm();
            showAlert("Succès", "Transaction enregistrée !");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème avec la base de données.");
        }
    }

    private void clearForm() {
        montantField.clear();
        phoneField.clear();
        descField.clear();
        walletCombo.setValue(null);
        modePaiementCombo.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
