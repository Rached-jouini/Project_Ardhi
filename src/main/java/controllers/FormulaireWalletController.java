package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import models.Wallet;
import services.WalletService;

public class FormulaireWalletController {

    @FXML private TextField nomField, montantField, decouvertField;
    @FXML private TextArea descField;

    private WalletService walletService = new WalletService();
    private Wallet walletToEdit = null;

    public void setWalletData(Wallet w) {
        this.walletToEdit = w;
        nomField.setText(w.getNom());
        montantField.setText(String.valueOf(w.getBudgetInitial()));
        decouvertField.setText(String.valueOf(w.getLimiteDecouvert()));
        descField.setText(w.getDescription());
    }

    @FXML
    void enregistrerWallet(ActionEvent event) {
        try {
            String nom = nomField.getText();
            if (nom == null || nom.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer un nom pour le wallet").show();
                return;
            }

            double montant = Double.parseDouble(montantField.getText());
            double decouvert = Double.parseDouble(decouvertField.getText().isEmpty() ? "0" : decouvertField.getText());
            
            if (montant < 0 || decouvert < 0) {
                new Alert(Alert.AlertType.WARNING, "Les montants ne peuvent pas être négatifs").show();
                return;
            }

            if (walletToEdit == null) {
                // MODE AJOUT
                Wallet w = new Wallet(nom, montant, montant, descField.getText(), "#085041");
                w.setLimiteDecouvert(decouvert);
                walletService.add(w);
            } else {
                // MODE MODIFICATION
                walletToEdit.setNom(nom);
                walletToEdit.setBudgetInitial(montant);
                walletToEdit.setLimiteDecouvert(decouvert);
                walletToEdit.setDescription(descField.getText());
                walletService.update(walletToEdit);
            }
            
            ((Stage) nomField.getScene().getWindow()).close();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }
}
