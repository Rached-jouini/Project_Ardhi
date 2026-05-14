package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import models.SourceFinancement;
import services.SourceFinancementService;

import java.sql.SQLException;

public class FormulaireCarteController {

    @FXML private TextField numeroField, nomTitulaireField, expiryField, soldeField;
    @FXML private PasswordField cvvField;
    @FXML private Label previewNumber, previewName, previewExpiry, previewSolde, cardTypeLabel;
    @FXML private VBox visualCard;

    private SourceFinancementService sourceService = new SourceFinancementService();

    @FXML
    public void onNumeroTyped() {
        String input = numeroField.getText().replaceAll("\\s", "");
        if (input.length() > 16) input = input.substring(0, 16);
        
        // Formattage XXXX XXXX XXXX XXXX
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (i > 0 && i % 4 == 0) formatted.append(" ");
            formatted.append(input.charAt(i));
        }
        
        numeroField.setText(formatted.toString());
        numeroField.positionCaret(formatted.length()); // Garder le curseur à la fin

        if (input.isEmpty()) {
            previewNumber.setText("**** **** **** ****");
        } else {
            previewNumber.setText(formatted.toString());
        }

        // Détection du type
        if (input.startsWith("4")) {
            cardTypeLabel.setText("VISA");
            updateCardStyle("#1E293B", "#0F172A");
        } else if (input.startsWith("5")) {
            cardTypeLabel.setText("MASTERCARD");
            updateCardStyle("#B91C1C", "#7F1D1D");
        } else if (input.startsWith("6")) {
            cardTypeLabel.setText("POSTE");
            updateCardStyle("#0369A1", "#075985");
        } else {
            cardTypeLabel.setText("CARD");
            updateCardStyle("#374151", "#111827");
        }
    }

    @FXML
    public void onNomTyped() {
        previewName.setText(nomTitulaireField.getText().toUpperCase());
    }

    @FXML
    public void onExpiryTyped() {
        String input = expiryField.getText().replaceAll("/", "");
        if (input.length() > 4) input = input.substring(0, 4);
        
        String formatted = input;
        if (input.length() > 2) {
            formatted = input.substring(0, 2) + "/" + input.substring(2);
        }
        
        expiryField.setText(formatted);
        expiryField.positionCaret(formatted.length());
        previewExpiry.setText(formatted);
    }

    @FXML
    public void onSoldeTyped() {
        previewSolde.setText(soldeField.getText() + " DT");
    }

    private void updateCardStyle(String startColor, String endColor) {
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(startColor)), new Stop(1, Color.web(endColor)));
        visualCard.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(20), null)));
    }

    @FXML
    void enregistrer() {
        try {
            SourceFinancement sf = new SourceFinancement();
            sf.setNom(cardTypeLabel.getText() + " - " + nomTitulaireField.getText());
            sf.setType("Carte"); // Maintenant que la base est agrandie, on peut utiliser le mot complet
            sf.setMontant(Double.parseDouble(soldeField.getText()));
            sf.setDescription(numeroField.getText()); 

            sourceService.add(sf);
            annuler();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void annuler() {
        ((Stage) numeroField.getScene().getWindow()).close();
    }
}
