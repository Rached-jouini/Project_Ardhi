package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Capitale;
import models.SourceType;
import services.CapitalService;
import services.SourceTypeService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class AjouterCapitaleController {

    @FXML private TextField montantInitialTextField;
    @FXML private ComboBox<String> deviseComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private TableView<SourceType> sourceTypeTable;
    @FXML private TableColumn<SourceType, Integer> colId;
    @FXML private TableColumn<SourceType, String> colNom;
    @FXML private TableColumn<SourceType, Void> colAction;

    @FXML
    public void initialize() {
        deviseComboBox.setItems(FXCollections.observableArrayList("DT", "EUR", "USD"));
        deviseComboBox.setValue("DT");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        loadSourceTypes();
        addActionsToTable();
    }

    private void loadSourceTypes() {
        SourceTypeService stService = new SourceTypeService();
        try {
            sourceTypeTable.setItems(FXCollections.observableArrayList(stService.select()));
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    private void addActionsToTable() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 5;");
                btnEdit.setOnAction(event -> {
                    SourceType st = getTableView().getItems().get(getIndex());
                    handleEditSourceType(st);
                });

                btnDelete.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-background-radius: 5;");
                btnDelete.setOnAction(event -> {
                    SourceType st = getTableView().getItems().get(getIndex());
                    handleDeleteSourceType(st);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void handleEditSourceType(SourceType st) {
        TextInputDialog dialog = new TextInputDialog(st.getNom());
        dialog.setTitle("Modification");
        dialog.setHeaderText("Modifier le type de source");
        dialog.setContentText("Nouveau nom :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            st.setNom(newName);
            SourceTypeService stService = new SourceTypeService();
            try {
                stService.update(st);
                loadSourceTypes();
                showInfo("Succes", "Type de source modifie !");
            } catch (SQLException e) {
                showError("Erreur", e.getMessage());
            }
        });
    }

    private void handleDeleteSourceType(SourceType st) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer '" + st.getNom() + "' ?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            SourceTypeService stService = new SourceTypeService();
            try {
                stService.delete(st.getId());
                loadSourceTypes();
                showInfo("Succes", "Type de source supprime !");
            } catch (SQLException e) {
                showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    void handleAddNewSourceType(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouveau Type");
        dialog.setHeaderText("Ajouter une categorie");
        dialog.setContentText("Nom :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            SourceTypeService stService = new SourceTypeService();
            try {
                stService.add(new SourceType(name));
                loadSourceTypes();
            } catch (SQLException e) {
                showError("Erreur d'ajout", e.getMessage());
            }
        });
    }

    @FXML
    void ajouterCapitaleAction(ActionEvent event) {
        double montant = Double.parseDouble(montantInitialTextField.getText());
        String devise = deviseComboBox.getValue();
        String desc = descriptionTextArea.getText();

        Capitale capitale = new Capitale(montant, montant, devise, LocalDate.now(), desc);
        CapitalService cs = new CapitalService();
        try {
            cs.add(capitale);
            showInfo("Succes", "Capital mis a jour !");
            retourAction(event);
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    void retourAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FinanceDashboard.fxml"));
            montantInitialTextField.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(message);
        a.show();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(message);
        a.show();
    }
}
