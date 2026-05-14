package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import models.Evenement;
import services.EvenementService;

import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ModifierEvenementController {

    @FXML
    private TextField nom;

    @FXML
    private TextField description;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField lieu;

    @FXML
    private TextField type;

    @FXML
    private TextField culture_concernee;

    @FXML
    private TextField nombre_places;

    @FXML
    private TextField statut;

    private int currentId;
    private EvenementService ps = new EvenementService();

    // Méthode pour charger les données de l'événement à modifier
    public void setEvenementData(Evenement ev) {
        this.currentId = ev.getId();
        nom.setText(ev.getNom());
        description.setText(ev.getDescription());
        datePicker.setValue(ev.getDate());
        lieu.setText(ev.getLieu());
        type.setText(ev.getType());
        culture_concernee.setText(ev.getCulture_concernee());
        nombre_places.setText(String.valueOf(ev.getNombre_places()));
        statut.setText(ev.getStatut());
    }

    @FXML
    void modifier(ActionEvent event) {
        if (nom.getText().isEmpty() || description.getText().isEmpty() || datePicker.getValue() == null ||
            lieu.getText().isEmpty() || type.getText().isEmpty() || culture_concernee.getText().isEmpty() ||
            nombre_places.getText().isEmpty() || statut.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs.");
            alert.showAndWait();
            return;
        }

        try {
            int places = Integer.parseInt(nombre_places.getText());
            Evenement ev = new Evenement(currentId, nom.getText(), description.getText(), datePicker.getValue(), lieu.getText(), type.getText(), culture_concernee.getText(), places, statut.getText());
            ps.update(ev);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Événement modifié avec succès !");
            alert.showAndWait();

            // Retour à la liste
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherEvenementsController.fxml"));
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de format");
            alert.setHeaderText(null);
            alert.setContentText("Le nombre de places doit être un nombre entier.");
            alert.showAndWait();
        } catch (SQLException | IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
