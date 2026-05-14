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
import models.Terrain;
import services.TerrainService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherTerrainsController {

    @FXML
    private TableView<Terrain> terrainTable;
    @FXML
    private TableColumn<Terrain, String> colRegion;
    @FXML
    private TableColumn<Terrain, Float> colSuperficie;
    @FXML
    private TableColumn<Terrain, String> colType;
    @FXML
    private TableColumn<Terrain, String> colStatut;
    @FXML
    private TableColumn<Terrain, Float> colPrix;
    @FXML
    private TableColumn<Terrain, Void> colActions;

    private TerrainService terrainService = new TerrainService();
    private ObservableList<Terrain> terrainList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_sol"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix_location"));

        ajouterBoutonsActions();
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            List<Terrain> data = terrainService.select();
            terrainList.setAll(data);
            terrainTable.setItems(terrainList);
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les terrains : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Terrain, Void>, TableCell<Terrain, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Terrain, Void> call(final TableColumn<Terrain, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");

                    {
                        btnEdit.getStyleClass().add("button-secondary");
                        btnEdit.setOnAction(event -> {
                            Terrain terrain = getTableView().getItems().get(getIndex());
                            modifierTerrain(terrain);
                        });

                        btnDelete.getStyleClass().add("button-alert");
                        btnDelete.setOnAction(event -> {
                            Terrain terrain = getTableView().getItems().get(getIndex());
                            supprimerTerrain(terrain);
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

    private void modifierTerrain(Terrain t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            Parent root = loader.load();
            
            // On récupère le contrôleur de la vue ajout
            ajouterTerrain controller = loader.getController();
            // On lui passe l'objet pour le mode édition
            controller.setTerrainPourModification(t);
            
            terrainTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void supprimerTerrain(Terrain t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer ce terrain ?");
        alert.setContentText("Région : " + t.getRegion());

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                terrainService.delete(t.getId());
                chargerDonnees(); // Rafraîchir la liste
            } catch (SQLException e) {
                afficherAlerte("Erreur", "Suppression impossible : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void allerVersAjout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterTerrain.fxml"));
            terrainTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void allerVersPlantes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherPlantes.fxml"));
            terrainTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation vers les plantes impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void passerAuClient(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CatalogueTerrains.fxml"));
            terrainTable.getScene().setRoot(root);
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation vers le client impossible : " + e.getMessage(), Alert.AlertType.ERROR);
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
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/admin-dashboard.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) terrainTable.getScene().getWindow();
            stage.setTitle("Ardhi - Dashboard Admin");
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Retour a l'accueil admin impossible");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
