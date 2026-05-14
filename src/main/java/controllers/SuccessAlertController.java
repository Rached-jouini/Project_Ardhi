package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SuccessAlertController {

    @FXML
    private Label dureeLabel;
    @FXML
    private Label prixTotalLabel;
    @FXML
    private Label plantesListLabel;

    public void setData(long nbMois, float prixTotal, String plantes) {
        dureeLabel.setText("Durée estimée : " + nbMois + " mois");
        prixTotalLabel.setText("Prix Total : " + String.format("%.1f", prixTotal) + " TND");
        plantesListLabel.setText(plantes);
    }

    @FXML
    void closeAlert(ActionEvent event) {
        // Fermer la fenêtre pop-up
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
