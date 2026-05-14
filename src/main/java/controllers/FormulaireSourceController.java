package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.SourceFinancement;
import services.SourceFinancementService;

import java.sql.SQLException;

public class FormulaireSourceController {

    @FXML private TextField nomField, montantField;
    @FXML private ComboBox<String> typeCombo;

    private SourceFinancementService sourceService = new SourceFinancementService();
    private SourceFinancement sourceToEdit = null;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Interne", "Externe", "Emprunt"));
        typeCombo.setValue("Interne");
    }

    public void setData(SourceFinancement sf) {
        this.sourceToEdit = sf;
        nomField.setText(sf.getNom());
        typeCombo.setValue(sf.getType());
        montantField.setText(String.valueOf(sf.getMontant()));
    }

    @FXML
    void enregistrerAction(ActionEvent event) {
        String nom = nomField.getText();
        String type = typeCombo.getValue();
        String montantStr = montantField.getText();

        if (nom.isEmpty() || montantStr.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").show();
            return;
        }

        try {
            double montant = Double.parseDouble(montantStr);
            
            if (sourceToEdit == null) {
                // AJOUT
                SourceFinancement sf = new SourceFinancement(nom, type, montant, "");
                sourceService.add(sf);
            } else {
                // MODIFICATION
                sourceToEdit.setNom(nom);
                sourceToEdit.setType(type);
                sourceToEdit.setMontant(montant);
                sourceService.update(sourceToEdit);
            }
            
            ((Stage) nomField.getScene().getWindow()).close();
            
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Le montant doit être un nombre valide !").show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void annulerAction(ActionEvent event) {
        ((Stage) nomField.getScene().getWindow()).close();
    }
}
