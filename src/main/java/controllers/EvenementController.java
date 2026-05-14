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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;

public class EvenementController {

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


    @FXML
    private javafx.scene.web.WebView mapView;

    private EvenementService ps = new EvenementService();

    @FXML
    public void initialize() {
        // Mode compatibilité maximale : on se fait passer pour IE11
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");

        // Test si le WebView fonctionne
        mapView.getEngine().loadContent("<html><body style='background-color: #f4f4f4; display: flex; justify-content: center; align-items: center; height: 100vh; font-family: sans-serif;'><h3>Chargement de la carte...</h3></body></html>");

        // Initialiser la carte avec une vue par défaut (Tunisie)
        updateMap("Tunisie");

        // Ajouter un écouteur sur le champ lieu pour mettre à jour la carte en temps réel
        lieu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateMap(newValue);
            }
        });
    }

    private void updateMap(String location) {
        if (location == null || location.trim().isEmpty()) {
            location = "Tunisie";
        }
        // Solution DuckDuckGo Maps (Apple Maps HD) - Stable et sans blocage
        String url = "https://duckduckgo.com/?q=" + location.replace(" ", "+") + "&iaxm=maps";
        
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
        mapView.getEngine().load(url);
    }


    @FXML
    void ajouter(ActionEvent event) {
        if (nom.getText().isEmpty() || description.getText().isEmpty() || datePicker.getValue() == null ||
                lieu.getText().isEmpty() || type.getText().isEmpty() || culture_concernee.getText().isEmpty() ||
                nombre_places.getText().isEmpty() || statut.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs, y compris la date.");
            alert.showAndWait();
            return;
        }

        try {
            int places = Integer.parseInt(nombre_places.getText());
            ps.add(new Evenement(nom.getText(), description.getText(), datePicker.getValue(), lieu.getText(), type.getText(), culture_concernee.getText(), places, statut.getText()));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Événement ajouté avec succès !");
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
