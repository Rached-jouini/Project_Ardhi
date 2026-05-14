package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Equipement;
import services.EquipementService;

import java.io.IOException;
import java.sql.SQLException;

public class GestionEquipementController {

    @FXML private TableView<Equipement> equipementTable;
    @FXML private TableColumn<Equipement, String> colPhoto;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, Integer> colQteTotale;
    @FXML private TableColumn<Equipement, Integer> colQteDispo;
    @FXML private TableColumn<Equipement, String> colEtat;
    @FXML private TableColumn<Equipement, Double> colPrix;
    @FXML private TableColumn<Equipement, Void> colActions;
    @FXML private TextField rechercheField;

    private ObservableList<Equipement> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQteTotale.setCellValueFactory(new PropertyValueFactory<>("quantite_totale"));
        colQteDispo.setCellValueFactory(new PropertyValueFactory<>("quantite_dispo"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix_location_jour"));
        colPhoto.setCellValueFactory(new PropertyValueFactory<>("photo"));

        colPhoto.setCellFactory(param -> new TableCell<Equipement, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);
                if (empty || photoPath == null || photoPath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image("file:" + photoPath, 40, 40, true, true));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(new Label("🖼️"));
                    }
                }
            }
        });

        setupActionsColumn();
        loadEquipements();
        setupSearch();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Equipement, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                container.setAlignment(javafx.geometry.Pos.CENTER);

                editBtn.setOnAction(event -> {
                    Equipement eq = getTableView().getItems().get(getIndex());
                    modifierEquipement(eq);
                });

                deleteBtn.setOnAction(event -> {
                    Equipement eq = getTableView().getItems().get(getIndex());
                    supprimerEquipement(eq);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void modifierEquipement(Equipement eq) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireEquipement.fxml"));
            Parent root = loader.load();
            FormulaireEquipementController controller = loader.getController();
            controller.setEquipementAModifier(eq); // I will add this method to FormulaireEquipementController
            
            // On utilise le singleton pour changer la page
            MainLayoutController.getInstance().setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerEquipement(Equipement eq) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'équipement : " + eq.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet équipement ? Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                new EquipementService().delete(eq.getId());
                loadEquipements(); // Recharger la table
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression : " + e.getMessage()).show();
            }
        }
    }

    private void loadEquipements() {
        EquipementService service = new EquipementService();
        try {
            masterData.setAll(service.select());
            equipementTable.setItems(masterData);
        } catch (SQLException e) {
            System.out.println("Erreur chargement: " + e.getMessage());
        }
    }

    private void setupSearch() {
        FilteredList<Equipement> filteredData = new FilteredList<>(masterData, p -> true);
        rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(eq -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (eq.getNom().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (eq.getType().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<Equipement> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(equipementTable.comparatorProperty());
        equipementTable.setItems(sortedData);
    }

    @FXML
    void ouvrirFormulaireAjout(ActionEvent event) {
        MainLayoutController.getInstance().loadPage("/FormulaireEquipement.fxml");
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        MainLayoutController.getInstance().loadPage("/FinanceDashboard.fxml");
    }
}
