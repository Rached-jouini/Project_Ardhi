package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import models.Plante;
import services.PlanteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterPlanteController {

    @FXML
    private TextField nom_p;
    @FXML
    private TextField type_p;
    @FXML
    private DatePicker date_debut_p;
    @FXML
    private DatePicker date_fin_p;
    @FXML
    private TextField besoin_eau_p;
    @FXML
    private TextField description_p;
    @FXML
    private Button btnSave;

    private PlanteService planteService = new PlanteService();
    private Plante planteEnModification = null;

    public void setPlantePourModification(Plante p) {
        this.planteEnModification = p;
        nom_p.setText(p.getNom());
        type_p.setText(p.getType());
        if (p.getDate_debut_plantation() != null)
            date_debut_p.setValue(LocalDate.parse(p.getDate_debut_plantation()));
        if (p.getDate_fin_plantation() != null)
            date_fin_p.setValue(LocalDate.parse(p.getDate_fin_plantation()));
        besoin_eau_p.setText(p.getBesoin_eau());
        description_p.setText(p.getDescription());
        
        btnSave.setText("Mettre à jour la Plante");
    }

    @FXML
    void save_plante(ActionEvent event) {
        try {
            String nom = nom_p.getText();
            String type = type_p.getText();
            String debut = date_debut_p.getValue() != null ? date_debut_p.getValue().toString() : "";
            String fin = date_fin_p.getValue() != null ? date_fin_p.getValue().toString() : "";
            String eau = besoin_eau_p.getText();
            String desc = description_p.getText();

            if (planteEnModification == null) {
                Plante p = new Plante(nom, type, debut, fin, eau, desc);
                planteService.add(p);
                afficherSucces("Plante ajoutée !");
            } else {
                planteEnModification.setNom(nom);
                planteEnModification.setType(type);
                planteEnModification.setDate_debut_plantation(debut);
                planteEnModification.setDate_fin_plantation(fin);
                planteEnModification.setBesoin_eau(eau);
                planteEnModification.setDescription(desc);
                
                planteService.update(planteEnModification);
                afficherSucces("Plante mise à jour !");
            }
            retourListe(null);
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherPlantes.fxml"));
            nom_p.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Erreur Navigation", e.getMessage());
        }
    }

    @FXML
    void allerVersTerrains(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherTerrains.fxml"));
            nom_p.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Erreur Navigation", e.getMessage());
        }
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    void retourAccueil(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) nom_p.getScene().getWindow();
            stage.setTitle("Ardhi - Accueil");
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Retour a l'accueil impossible");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
