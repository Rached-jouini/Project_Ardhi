package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Equipement;
import models.LocationEquipement;
import models.SourceFinancement;
import models.TransactionFinanciere;
import models.Wallet;
import services.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class PanierController {

    // UI Bindings
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    @FXML private Label cartSummaryLabel;
    @FXML private Label durationLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private ComboBox<String> paiementCombo;
    @FXML private ComboBox<Wallet> walletSelectionCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button confirmBtn;

    // Section carte (hidden by default)
    @FXML private VBox carteBancaireSection;
    @FXML private ComboBox<SourceFinancement> carteCombo;
    @FXML private HBox soldCarteBox;
    @FXML private Label soldCarteLabel;
    @FXML private Label soldCarteStatusLabel;

    // Services
    private final TransactionFinanciereService transactionService = new TransactionFinanciereService();
    private final EquipementService equipementService = new EquipementService();
    private final LocationEquipementService locationService = new LocationEquipementService();
    private final WalletService walletService = new WalletService();
    private final SourceFinancementService sourceService = new SourceFinancementService();


    @FXML
    public void initialize() {
        paiementCombo.getItems().addAll("Espèces", "Carte Bancaire", "Virement", "Wallet");

        // Listener : afficher/masquer la section carte
        paiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> onPaiementChanged(newVal));

        // Listener : afficher solde quand une carte est choisie
        carteCombo.valueProperty().addListener((obs, oldVal, newVal) -> onCarteChanged(newVal));

        // Dates par défaut
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now().plusDays(1));
        dateDebutPicker.valueProperty().addListener((obs, o, n) -> updateTotal());
        dateFinPicker.valueProperty().addListener((obs, o, n) -> updateTotal());

        loadAllWallets();
        loadCartes();
        refreshCart();
    }

    private void loadAllWallets() {
        try {
            List<Wallet> wallets = walletService.select();
            
            // Filtrer pour ne garder que les wallets de location/équipement
            List<Wallet> filteredWallets = wallets.stream()
                    .filter(w -> w.getNom().toLowerCase().contains("location") || 
                                 w.getNom().toLowerCase().contains("equipement") ||
                                 w.getNom().toLowerCase().contains("équipement"))
                    .collect(Collectors.toList());
            
            walletSelectionCombo.getItems().setAll(filteredWallets);
            
            // Personnalisation de l'affichage
            walletSelectionCombo.setCellFactory(lv -> new ListCell<Wallet>() {
                @Override protected void updateItem(Wallet item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText("📂 " + item.getNom() + " (" + String.format("%.2f", item.getBudgetActuel()) + " DT)");
                }
            });
            walletSelectionCombo.setButtonCell(walletSelectionCombo.getCellFactory().call(null));

            // Présélection intelligente
            for (Wallet w : wallets) {
                if (w.getNom().toLowerCase().contains("location") || 
                    w.getNom().toLowerCase().contains("equipement") ||
                    w.getNom().toLowerCase().contains("équipement")) {
                    walletSelectionCombo.setValue(w);
                    break;
                }
            }
            if (walletSelectionCombo.getValue() == null && !wallets.isEmpty()) {
                walletSelectionCombo.setValue(wallets.get(0));
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement wallets: " + e.getMessage());
        }
    }

    /** Charge les cartes bancaires depuis source_financement */
    private void loadCartes() {
        try {
            List<SourceFinancement> sources = sourceService.select();
            List<SourceFinancement> cartes = sources.stream()
                    .filter(s -> s.getType() != null &&
                            (s.getType().toLowerCase().contains("carte") ||
                             s.getType().toLowerCase().contains("bank") ||
                             s.getType().toLowerCase().contains("bancaire")))
                    .collect(Collectors.toList());

            carteCombo.getItems().setAll(cartes);

            // Personnaliser l'affichage dans la ComboBox
            carteCombo.setCellFactory(lv -> new ListCell<SourceFinancement>() {
                @Override
                protected void updateItem(SourceFinancement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("💳  " + item.getNom() + "  —  " + String.format("%.2f DT", item.getMontant()));
                    }
                }
            });
            carteCombo.setButtonCell(carteCombo.getCellFactory().call(null));

        } catch (SQLException e) {
            System.err.println("Erreur chargement cartes: " + e.getMessage());
        }
    }

    /** Réagit au changement de mode de paiement */
    private void onPaiementChanged(String mode) {
        boolean isCarte = "Carte Bancaire".equals(mode);

        // Afficher/masquer la section carte avec animation
        carteBancaireSection.setVisible(isCarte);
        carteBancaireSection.setManaged(isCarte);

        if (isCarte && carteCombo.getItems().isEmpty()) {
            // Aucune carte trouvée — message d'avertissement
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Aucune carte trouvée");
            a.setContentText("Vous n'avez pas encore de carte bancaire enregistrée.\n" +
                             "Rendez-vous dans 'Mes Comptes & Cartes' pour en ajouter une.");
            a.show();
        }

        // Réinitialiser la sélection carte si on change de mode
        if (!isCarte) {
            carteCombo.setValue(null);
            soldCarteBox.setVisible(false);
            soldCarteBox.setManaged(false);
        }

        updateTotal();
    }

    /** Réagit à la sélection d'une carte — affiche son solde */
    private void onCarteChanged(SourceFinancement carte) {
        if (carte == null) {
            soldCarteBox.setVisible(false);
            soldCarteBox.setManaged(false);
            return;
        }

        soldCarteBox.setVisible(true);
        soldCarteBox.setManaged(true);

        double solde = carte.getMontant();
        double total = getCurrentTotal();

        soldCarteLabel.setText(String.format("%.2f DT", solde));

        if (solde >= total) {
            soldCarteLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
            soldCarteStatusLabel.setText("✅ Solde suffisant");
            soldCarteStatusLabel.setStyle("-fx-text-fill: #059669;");
        } else {
            soldCarteLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            soldCarteStatusLabel.setText("❌ Solde insuffisant");
            soldCarteStatusLabel.setStyle("-fx-text-fill: #DC2626;");
        }
    }


    private void refreshCart() {
        cartItemsContainer.getChildren().clear();
        int totalItems = CartManager.getCount();
        cartSummaryLabel.setText(totalItems + " article(s) dans le panier");

        for (CartManager.CartEntry entry : CartManager.getCartEntries()) {
            cartItemsContainer.getChildren().add(createCartItemRow(entry));
        }
        updateTotal();
    }

    @FXML
    private void updateTotal() {
        long days = getDays();
        durationLabel.setText("Durée : " + days + " jour(s)");

        double total = getCurrentTotal();
        totalLabel.setText(String.format("%.2f DT", total));

        // Mettre à jour le statut wallet
        Wallet selectedWallet = walletSelectionCombo.getValue();
        if (selectedWallet != null) {
            walletBalanceLabel.setText(String.format("%.2f DT", selectedWallet.getBudgetActuel()));
            walletBalanceLabel.setStyle(selectedWallet.getBudgetActuel() >= total
                ? "-fx-text-fill: #10B981; -fx-font-weight: bold;"
                : "-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }

        // Re-évaluer le solde carte si une est sélectionnée
        if (carteCombo.getValue() != null) {
            onCarteChanged(carteCombo.getValue());
        }
    }

    private double getCurrentTotal() {
        long days = getDays();
        return CartManager.getCartEntries().stream()
                .mapToDouble(entry -> entry.getEquipement().getPrix_location_jour() * entry.getQuantity() * days)
                .sum();
    }

    private long getDays() {
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            long d = ChronoUnit.DAYS.between(dateDebutPicker.getValue(), dateFinPicker.getValue());
            return d > 0 ? d : 1;
        }
        return 1;
    }

    private HBox createCartItemRow(CartManager.CartEntry entry) {
        Equipement e = entry.getEquipement();
        long days = getDays();

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 12 15; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2); " +
                     "-fx-border-color: #F1F5F9; -fx-border-radius: 12; -fx-border-width: 1;");

        Label icon = new Label("🚜");
        icon.setStyle("-fx-font-size: 22;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(e.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 14;");

        Label details = new Label(String.format("%.2f DT/j  ×  %d unité(s)  ×  %d jour(s)",
                e.getPrix_location_jour(), entry.getQuantity(), days));
        details.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11;");

        info.getChildren().addAll(name, details);

        Label subtotal = new Label(String.format("%.2f DT", e.getPrix_location_jour() * entry.getQuantity() * days));
        subtotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #063D2F; -fx-font-size: 14;");

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; " +
                           "-fx-background-radius: 8; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11;");
        removeBtn.setOnAction(ev -> {
            CartManager.getCartEntries().remove(entry);
            refreshCart();
        });

        row.getChildren().addAll(icon, info, subtotal, removeBtn);
        return row;
    }

    @FXML
    void validerLocation() {
        // Anti-double clic
        confirmBtn.setDisable(true);
        confirmBtn.setText("⏳  Traitement...");

        if (CartManager.getCount() == 0) { resetConfirmBtn(); return; }

        if (paiementCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez choisir un mode de paiement.").show();
            resetConfirmBtn(); return;
        }

        // Validation carte bancaire
        if ("Carte Bancaire".equals(paiementCombo.getValue())) {
            if (carteCombo.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une carte bancaire.").show();
                resetConfirmBtn(); return;
            }
            double soldeCarte = carteCombo.getValue().getMontant();
            double total = getCurrentTotal();
            if (soldeCarte < total) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Solde insuffisant");
                a.setHeaderText("❌ Solde de la carte insuffisant !");
                a.setContentText(String.format(
                    "Carte : %s\nSolde : %.2f DT\nMontant requis : %.2f DT\nDéficit : %.2f DT",
                    carteCombo.getValue().getNom(), soldeCarte, total, total - soldeCarte));
                a.showAndWait();
                resetConfirmBtn(); return;
            }
        }

        try {
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();

            if (debut == null || fin == null || !fin.isAfter(debut)) {
                new Alert(Alert.AlertType.WARNING, "La date de fin doit être après la date de début.").show();
                resetConfirmBtn(); return;
            }

            long days = ChronoUnit.DAYS.between(debut, fin);
            double total = getCurrentTotal();

            String itemsNames = CartManager.getCartEntries().stream()
                    .map(entry -> entry.getEquipement().getNom() + " x" + entry.getQuantity())
                    .collect(Collectors.joining(", "));

            // 1. Transaction financière
            Wallet selectedWallet = walletSelectionCombo.getValue();
            if (selectedWallet == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un budget (Wallet).").show();
                resetConfirmBtn(); return;
            }

            TransactionFinanciere t = new TransactionFinanciere();
            t.setMontant(total);
            t.setType("Depense");
            t.setDescription("Location (" + days + "j) : " + itemsNames);
            t.setModePaiement(paiementCombo.getValue());
            t.setWalletId(selectedWallet.getId());
            transactionService.add(t);

            // 2. Déduire du mode de paiement choisi
            if ("Wallet".equals(paiementCombo.getValue())) {
                walletService.deduireDepense(selectedWallet.getId(), total);
            } else if ("Carte Bancaire".equals(paiementCombo.getValue()) && carteCombo.getValue() != null) {
                // Déduction de la carte
                SourceFinancement carteChoisie = carteCombo.getValue();
                sourceService.updateMontant(carteChoisie.getId(), carteChoisie.getMontant() - total);
                
                // AUSSI déduction du wallet sélectionné pour garder le budget à jour
                walletService.deduireDepense(selectedWallet.getId(), total);
            }

            // 3. Locations + mise à jour stock
            for (CartManager.CartEntry entry : CartManager.getCartEntries()) {
                LocationEquipement loc = new LocationEquipement();
                loc.setId_equipement(entry.getEquipement().getId());
                loc.setId_utilisateur(1);
                loc.setDate_location(debut);
                loc.setDate_retour_prevue(fin);
                loc.setStatut("en_cours");
                loc.setEtat_retour(null);
                loc.setCout_total(entry.getEquipement().getPrix_location_jour() * entry.getQuantity() * days);
                locationService.add(loc);
                equipementService.updateStock(entry.getEquipement().getId(), entry.getQuantity());
            }

            // 4. Fenêtre de confirmation premium
            String paiementDetail = "Carte Bancaire".equals(paiementCombo.getValue()) && carteCombo.getValue() != null
                    ? "Carte Bancaire – " + carteCombo.getValue().getNom()
                    : paiementCombo.getValue();

            afficherConfirmation(itemsNames, debut.toString(), fin.toString(), days, paiementDetail, total);

            CartManager.clearCart();
            close();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la validation");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            resetConfirmBtn();
        }
    }

    /** Ouvre la fenêtre de confirmation premium */
    private void afficherConfirmation(String equipements, String dateDebut, String dateFin,
                                      long duree, String paiement, double total) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationLocation.fxml"));
            Parent root = loader.load();

            ConfirmationLocationController ctrl = loader.getController();
            ctrl.setData(equipements, dateDebut, dateFin, duree, paiement, total);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            // Fallback si le FXML échoue
            new Alert(Alert.AlertType.INFORMATION,
                "Location confirmée ! Total : " + String.format("%.2f DT", total)).show();
        }
    }

    private void resetConfirmBtn() {
        confirmBtn.setDisable(false);
        confirmBtn.setText("✅  CONFIRMER LA LOCATION");
    }

    @FXML
    void close() {
        ((Stage) totalLabel.getScene().getWindow()).close();
    }
}
