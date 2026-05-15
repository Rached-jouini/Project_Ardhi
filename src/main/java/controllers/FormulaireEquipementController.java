package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import models.Equipement;
import services.EquipementService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FormulaireEquipementController {

    @FXML private TextField nomTextField, typeTextField, qteTotaleTextField;
    @FXML private TextField qteDispoTextField, prixTextField, photoTextField, dureeVieTextField;
    @FXML private ComboBox<String> etatComboBox;
    @FXML private FlowPane photosContainer;
    @FXML private DatePicker dateMiseEnServicePicker;

    private List<String> selectedPhotoPaths = new ArrayList<>();

    private int currentEquipementId = -1; // -1 = Ajout, sinon = Modification

    @FXML
    public void initialize() {
        etatComboBox.setItems(FXCollections.observableArrayList("bon", "moyen", "mauvais"));
        etatComboBox.setValue("bon");
        dateMiseEnServicePicker.setValue(LocalDate.now());
    }

    /** Remplit le formulaire pour modification */
    public void setEquipementAModifier(Equipement eq) {
        this.currentEquipementId = eq.getId();
        nomTextField.setText(eq.getNom());
        typeTextField.setText(eq.getType());
        qteTotaleTextField.setText(String.valueOf(eq.getQuantite_totale()));
        qteDispoTextField.setText(String.valueOf(eq.getQuantite_dispo()));
        etatComboBox.setValue(eq.getEtat());
        prixTextField.setText(String.valueOf(eq.getPrix_location_jour()));
        
        if (eq.getDate_mise_en_service() != null) {
            dateMiseEnServicePicker.setValue(eq.getDate_mise_en_service());
        }
        dureeVieTextField.setText(String.valueOf(eq.getDuree_vie_annees()));

        this.selectedPhotoPaths = new ArrayList<>(eq.getPhotos());
        if (selectedPhotoPaths.isEmpty() && eq.getPhoto() != null && !eq.getPhoto().isEmpty()) {
            selectedPhotoPaths.add(eq.getPhoto());
        }
        
        photoTextField.setText(selectedPhotoPaths.size() + " photo(s) sélectionnée(s)");
        refreshPhotoPreviews();
    }

    private void refreshPhotoPreviews() {
        photosContainer.getChildren().clear();
        for (String path : selectedPhotoPaths) {
            try {
                ImageView iv = new ImageView(new Image("file:" + path));
                iv.setFitHeight(70);
                iv.setFitWidth(70);
                iv.setPreserveRatio(true);
                iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2); -fx-background-radius: 5;");
                
                // Optionnel: clic pour supprimer
                iv.setOnMouseClicked(e -> {
                    selectedPhotoPaths.remove(path);
                    refreshPhotoPreviews();
                    photoTextField.setText(selectedPhotoPaths.size() + " photo(s) sélectionnée(s)");
                });
                
                photosContainer.getChildren().add(iv);
            } catch (Exception e) {
                System.out.println("Erreur aperçu image : " + path);
            }
        }
    }

    @FXML
    void choisirPhotoAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des images");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(nomTextField.getScene().getWindow());
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File f : selectedFiles) {
                if (!selectedPhotoPaths.contains(f.getAbsolutePath())) {
                    selectedPhotoPaths.add(f.getAbsolutePath());
                }
            }
            photoTextField.setText(selectedPhotoPaths.size() + " photo(s) sélectionnée(s)");
            refreshPhotoPreviews();
        }
    }

    @FXML
    void ajouterEquipementAction(ActionEvent event) {
        try {
            // Validation
            if (nomTextField.getText().isBlank() || typeTextField.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Veuillez remplir au moins le nom et la catégorie.").show();
                return;
            }

            Equipement eq = new Equipement(
                nomTextField.getText().trim(),
                typeTextField.getText().trim().toLowerCase(),
                Integer.parseInt(qteTotaleTextField.getText()),
                Integer.parseInt(qteDispoTextField.getText()),
                etatComboBox.getValue(),
                selectedPhotoPaths.isEmpty() ? "" : selectedPhotoPaths.get(0), // Main photo is the first one
                Double.parseDouble(prixTextField.getText())
            );
            eq.setPhotos(selectedPhotoPaths);

            // Champs cycle de vie
            if (dateMiseEnServicePicker.getValue() != null) {
                eq.setDate_mise_en_service(dateMiseEnServicePicker.getValue());
            }
            if (!dureeVieTextField.getText().isBlank()) {
                eq.setDuree_vie_annees(Integer.parseInt(dureeVieTextField.getText().trim()));
            }

            if (currentEquipementId == -1) {
                new EquipementService().add(eq);
            } else {
                eq.setId(currentEquipementId);
                new EquipementService().update(eq);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("✅ Équipement enregistré avec succès !");
            alert.showAndWait();

            retourAction(event);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vérifiez les champs numériques (quantités, prix, durée de vie).").show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur : " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void retourAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/GestionEquipement.fxml"));
            javafx.scene.Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
