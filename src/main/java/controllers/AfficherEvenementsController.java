package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Evenement;
import services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class AfficherEvenementsController {

    @FXML
    private TableView<Evenement> tableEvenements;

    @FXML
    private TableColumn<Evenement, String> colNom;

    @FXML
    private TableColumn<Evenement, String> colDescription;

    @FXML
    private TableColumn<Evenement, LocalDate> colDate;

    @FXML
    private TableColumn<Evenement, String> colLieu;

    @FXML
    private TableColumn<Evenement, String> colType;

    @FXML
    private TableColumn<Evenement, String> colCulture;

    @FXML
    private TableColumn<Evenement, Integer> colPlaces;

    @FXML
    private TableColumn<Evenement, String> colStatut;

    private EvenementService ps = new EvenementService();

    @FXML
    public void initialize() {
        // Liaison des colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCulture.setCellValueFactory(new PropertyValueFactory<>("culture_concernee"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombre_places"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            ObservableList<Evenement> list = FXCollections.observableArrayList(ps.select());
            tableEvenements.setItems(list);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setContentText("Veuillez sélectionner un événement à modifier.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvenementController.fxml"));
            Parent root = loader.load();

            ModifierEvenementController controller = loader.getController();
            controller.setEvenementData(selected);

            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleShow(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setContentText("Veuillez sélectionner un événement à afficher.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowEvenementController.fxml"));
            Parent root = loader.load();

            ShowEvenementController controller = loader.getController();
            controller.setEvenementData(selected);

            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setContentText("Veuillez sélectionner un événement à supprimer.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'événement : " + selected.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ps.delete(selected.getId());
                chargerDonnees(); // Rafraîchir la table
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EvenementController.fxml"));
            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleSwitchToUser(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UserAfficherEvenements.fxml"));
            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleVoirInscriptions(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherInscriptions.fxml"));
            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
