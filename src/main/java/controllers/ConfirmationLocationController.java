package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmationLocationController {

    @FXML private Label equipementsLabel;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label dureeLabel;
    @FXML private Label paiementLabel;
    @FXML private Label totalLabel;

    /** Appelé depuis PanierController pour injecter les données */
    public void setData(String equipements, String dateDebut, String dateFin,
                        long duree, String paiement, double total) {
        equipementsLabel.setText(equipements);
        dateDebutLabel.setText(dateDebut);
        dateFinLabel.setText(dateFin);
        dureeLabel.setText(duree + " jour(s)");
        paiementLabel.setText(paiement);
        totalLabel.setText(String.format("%.2f DT", total));
    }

    @FXML
    void fermer() {
        ((Stage) totalLabel.getScene().getWindow()).close();
    }
}
