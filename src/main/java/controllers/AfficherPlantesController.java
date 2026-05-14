package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Plante;
import services.PlanteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherPlantesController {

    @FXML
    private TableView<Plante> planteTable;
    @FXML
    private TableColumn<Plante, String> colNom;
    @FXML
    private TableColumn<Plante, String> colType;
    @FXML
    private TableColumn<Plante, String> colDebut;
    @FXML
    private TableColumn<Plante, String> colFin;
    @FXML
    private TableColumn<Plante, String> colEau;
    @FXML
    private TableColumn<Plante, String> colDesc;
    @FXML
    private TableColumn<Plante, Void> colActions;

    private PlanteService planteService = new PlanteService();
    private ObservableList<Plante> planteList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut_plantation"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("date_fin_plantation"));
        colEau.setCellValueFactory(new PropertyValueFactory<>("besoin_eau"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        ajouterBoutonsActions();
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            List<Plante> data = planteService.select();
            planteList.setAll(data);
            planteTable.setItems(planteList);
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les plantes : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Plante, Void>, TableCell<Plante, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Plante, Void> call(final TableColumn<Plante, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");

                    {
                        btnEdit.getStyleClass().add("button-secondary");
                        btnEdit.setOnAction(event -> {
                            Plante p = getTableView().getItems().get(getIndex());
                            modifierPlante(p);
                        });

                        btnDelete.getStyleClass().add("button-alert");
                        btnDelete.setOnAction(event -> {
                            Plante p = getTableView().getItems().get(getIndex());
                            supprimerPlante(p);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox container = new HBox(10, btnEdit, btnDelete);
                            setGraphic(container);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void modifierPlante(Plante p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPlante.fxml"));
            Parent root = loader.load();
            AjouterPlanteController controller = loader.getController();
            controller.setPlantePourModification(p);
            planteTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void supprimerPlante(Plante p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer " + p.getNom() + " ?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                planteService.delete(p.getId());
                chargerDonnees();
            } catch (SQLException e) {
                afficherAlerte("Erreur", "Suppression impossible : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void allerVersAjout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterPlante.fxml"));
            planteTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void allerVersTerrains(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherTerrains.fxml"));
            planteTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
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
            javafx.stage.Stage stage = (javafx.stage.Stage) planteTable.getScene().getWindow();
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
