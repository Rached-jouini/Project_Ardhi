package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.SourceFinancement;
import models.TransactionFinanciere;
import models.Wallet;
import services.SourceFinancementService;
import services.TransactionFinanciereService;
import services.WalletService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FormulaireTransactionController {

    @FXML private ToggleButton btnRevenu, btnDepense;
    @FXML private TextField montantField;
    @FXML private ComboBox<Wallet> walletCombo;
    @FXML private ComboBox<String> modePaiementCombo;
    @FXML private ComboBox<SourceFinancement> cardCombo;
    @FXML private VBox cardSelectionBox;
    @FXML private TextArea descField;
    @FXML private ComboBox<String> prefixCombo;
    @FXML private TextField phoneField;

    @FXML private ToggleGroup typeGroup;

    private WalletService walletService = new WalletService();
    private TransactionFinanciereService transactionService = new TransactionFinanciereService();
    private SourceFinancementService sourceService = new SourceFinancementService();

    @FXML
    public void initialize() {
        // Initialiser les indicatifs pays
        prefixCombo.setItems(FXCollections.observableArrayList("🇹🇳 +216", "🇫🇷 +33", "🇩🇿 +213", "🇲🇦 +212", "🇱🇾 +218"));
        prefixCombo.setValue("🇹🇳 +216"); // Valeur par défaut

        // Forcer la selection par defaut
        btnRevenu.setSelected(true);
        
        // Empecher de desélectionner (il faut toujours un choix)
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });

        // Charger les modes de paiement
        modePaiementCombo.setItems(FXCollections.observableArrayList("Espèces", "Carte Bancaire", "Virement"));
        
        // Ajouter un écouteur direct pour le changement de mode
        modePaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleModePaiementChange();
        });

        try {
            List<Wallet> wallets = walletService.select();
            walletCombo.setItems(FXCollections.observableArrayList(wallets));

            // Charger les cartes disponibles
            List<SourceFinancement> sources = sourceService.select();
            List<SourceFinancement> cartes = sources.stream()
                .filter(s -> s.getType().equalsIgnoreCase("Carte") || 
                             s.getType().equalsIgnoreCase("C") || 
                             s.getNom().toLowerCase().contains("visa") || 
                             s.getNom().toLowerCase().contains("stb"))
                .collect(Collectors.toList());
            cardCombo.setItems(FXCollections.observableArrayList(cartes));
            System.out.println("Cartes trouvées en base : " + cartes.size());
            
            // Custom display for Card ComboBox
            cardCombo.setCellFactory(lv -> new ListCell<SourceFinancement>() {
                @Override protected void updateItem(SourceFinancement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNom() + " (" + item.getMontant() + " DT)");
                }
            });
            cardCombo.setButtonCell(cardCombo.getCellFactory().call(null));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void handleModePaiementChange() {
        String mode = modePaiementCombo.getValue();
        System.out.println("Mode sélectionné : [" + mode + "]");
        
        if (mode != null && mode.contains("Carte")) {
            cardSelectionBox.setVisible(true);
            cardSelectionBox.setManaged(true);
            System.out.println("Affichage du champ Carte : OK");
        } else {
            cardSelectionBox.setVisible(false);
            cardSelectionBox.setManaged(false);
        }
    }

    @FXML
    void enregistrerTransaction(ActionEvent event) {
        try {
            if (montantField.getText() == null || montantField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer un montant").show();
                return;
            }

            double montant = Double.parseDouble(montantField.getText());
            
            if (montant <= 0) {
                new Alert(Alert.AlertType.WARNING, "Le montant doit être supérieur à zéro").show();
                return;
            }

            String type = btnRevenu.isSelected() ? "Revenu" : "Depense";
            Wallet selectedWallet = walletCombo.getValue();
            String modePaiement = modePaiementCombo.getValue();

            if (selectedWallet == null || modePaiement == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs").show();
                return;
            }

            // METIER AVANCE : Verification du budget AVEC DECOUVERT
            double disponible = selectedWallet.getBudgetActuel() + selectedWallet.getLimiteDecouvert();
            
            if (type.equals("Depense") && montant > disponible) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Limite depassee !");
                alert.setHeaderText("Fonds insuffisants (Meme avec decouvert)");
                alert.setContentText("Votre budget actuel est de " + selectedWallet.getBudgetActuel() + 
                                     " DT et votre decouvert autorise est de " + selectedWallet.getLimiteDecouvert() + " DT.");
                alert.show();
                return;
            }

            // Enregistrement
            TransactionFinanciere t = new TransactionFinanciere();
            t.setMontant(montant);
            t.setType(type);
            t.setDescription(descField.getText());
            t.setWalletId(selectedWallet.getId());
            t.setModePaiement(modePaiement);
            
            transactionService.add(t);

            // MISE À JOUR DES SOLDES (Wallet + Carte)
            if (type.equals("Depense")) {
                walletService.deduireDepense(selectedWallet.getId(), montant);
                
                // Si paiement par carte, on déduit aussi de la carte
                if ("Carte Bancaire".equals(modePaiement) && cardCombo.getValue() != null) {
                    SourceFinancement selectedCard = cardCombo.getValue();
                    sourceService.updateMontant(selectedCard.getId(), selectedCard.getMontant() - montant);
                }

                // Petite alerte si on entre dans le decouvert
                if (montant > selectedWallet.getBudgetActuel()) {
                    new Alert(Alert.AlertType.INFORMATION, "Note : Vous utilisez votre decouvert autorise.").show();
                }
            } else {
                // Pour un Revenu
                // (Optionnel : update balance logic for revenue if needed)
                if ("Carte Bancaire".equals(modePaiement) && cardCombo.getValue() != null) {
                    SourceFinancement selectedCard = cardCombo.getValue();
                    sourceService.updateMontant(selectedCard.getId(), selectedCard.getMontant() + montant);
                }
            }
            
            // --- NOUVEAU : ENVOI SMS ---
            if (phoneField.getText() != null && !phoneField.getText().isEmpty()) {
                String fullNumber = prefixCombo.getValue().split(" ")[1] + phoneField.getText();
                String msg = "ARDHI : Transaction de " + montant + " DT (" + type + ") effectuee via " + modePaiement + ".";
                services.SmsService.sendSms(fullNumber, msg);
            }

            ((Stage) montantField.getScene().getWindow()).close();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }
}
