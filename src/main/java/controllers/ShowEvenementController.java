package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Evenement;

import java.io.IOException;

public class ShowEvenementController {

    @FXML
    private Label nom;

    @FXML
    private Label description;

    @FXML
    private Label date;

    @FXML
    private Label lieu;

    @FXML
    private Label type;

    @FXML
    private Label culture;

    @FXML
    private Label places;

    @FXML
    private Label statut;

    public void setEvenementData(Evenement ev) {
        nom.setText(ev.getNom());
        description.setText(ev.getDescription());
        date.setText(ev.getDate().toString());
        lieu.setText(ev.getLieu());
        type.setText(ev.getType());
        culture.setText(ev.getCulture_concernee());
        places.setText(String.valueOf(ev.getNombre_places()));
        statut.setText(ev.getStatut());
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherEvenementsController.fxml"));
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
