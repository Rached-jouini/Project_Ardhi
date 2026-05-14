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
import models.Inscription;
import services.InscriptionService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AfficherInscriptionsController {

    @FXML private TableView<Inscription> inscriptionsTable;
    @FXML private TableColumn<Inscription, String> colNom;
    @FXML private TableColumn<Inscription, String> colEmail;
    @FXML private TableColumn<Inscription, LocalDateTime> colDate;
    @FXML private TableColumn<Inscription, String> colStatut;
    @FXML private TextField searchField;

    private InscriptionService is = new InscriptionService();
    private ObservableList<Inscription> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut_participation"));

        // Formatage de la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Coloration des statuts
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Présent")) {
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;"); // Vert
                    } else if (item.equals("Absent")) {
                        setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;"); // Rouge
                    } else {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); // Orange (En attente)
                    }
                }
            }
        });

        loadData();

        // Barre de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });
    }

    private void loadData() {
        try {
            masterData.setAll(is.getAll());
            inscriptionsTable.setItems(masterData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData(String query) {
        if (query == null || query.isEmpty()) {
            inscriptionsTable.setItems(masterData);
            return;
        }
        String lowerCaseQuery = query.toLowerCase();
        ObservableList<Inscription> filteredList = FXCollections.observableArrayList();
        for (Inscription ins : masterData) {
            if (ins.getNom().toLowerCase().contains(lowerCaseQuery) || 
                ins.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(ins);
            }
        }
        inscriptionsTable.setItems(filteredList);
    }

    @FXML
    void marquerAbsent(ActionEvent event) {
        updateSelectedStatus("Absent");
    }

    @FXML
    void marquerPresent(ActionEvent event) {
        updateSelectedStatus("Présent");
    }

    private void updateSelectedStatus(String status) {
        Inscription selected = inscriptionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                is.updateStatus(selected.getId(), status);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un client dans le tableau.");
            alert.show();
        }
    }

    @FXML
    void navEvenements(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherEvenementsController.fxml"));
        Stage stage = (Stage) inscriptionsTable.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
