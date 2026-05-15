package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Evenement;
import services.EvenementService;

import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.web.WebView;

public class EvenementController {

    @FXML private TextField nom;
    @FXML private TextArea description;
    @FXML private DatePicker datePicker;
    @FXML private TextField lieu;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField culture_concernee;
    @FXML private TextField nombre_places;
    @FXML private WebView mapView;

    private EvenementService ps = new EvenementService();

    @FXML
    public void initialize() {
        // On remet les vrais noms car la DB est normalement réparée
        typeCombo.setItems(FXCollections.observableArrayList(
            "Formation", "Salon", "Atelier", "Seminaire", "Conference", "Visite"
        ));
        typeCombo.setValue("Formation");

        statutCombo.setItems(FXCollections.observableArrayList(
            "Ouvert", "Ferme", "Annule", "Termine"
        ));
        statutCombo.setValue("Ouvert");

        // Configuration WebView
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
        updateMap("Tunisie");

        // Ecouteur sur le champ lieu pour la carte
        lieu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateMap(newValue);
            }
        });
    }

    private void updateMap(String location) {
        String url = "https://duckduckgo.com/?q=" + location.replace(" ", "+") + "&iaxm=maps";
        mapView.getEngine().load(url);
    }

    @FXML
    void ajouter(ActionEvent event) {
        if (nom.getText().isEmpty() || description.getText().isEmpty() || datePicker.getValue() == null ||
                lieu.getText().isEmpty() || typeCombo.getValue() == null || culture_concernee.getText().isEmpty() ||
                nombre_places.getText().isEmpty() || statutCombo.getValue() == null) {

            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.").show();
            return;
        }

        try {
            int places = Integer.parseInt(nombre_places.getText());
            ps.add(new Evenement(
                nom.getText(), 
                description.getText(), 
                datePicker.getValue(), 
                lieu.getText(), 
                typeCombo.getValue(), 
                culture_concernee.getText(), 
                places, 
                statutCombo.getValue()
            ));

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Evenement ajoute avec succes !");
            alert.showAndWait();

            handleRetourListe(event);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Le nombre de places doit etre un nombre entier.").show();
        } catch (SQLException | IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    @FXML
    void handleRetourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherEvenementsController.fxml"));
        Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}
