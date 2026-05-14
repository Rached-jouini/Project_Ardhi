package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.AllocationPlante;
import models.LocationTerrain;
import models.Plante;
import models.Terrain;
import services.AllocationPlanteService;
import services.LocationTerrainService;
import services.PlanteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReserverTerrainController {

    @FXML
    private Label terrainInfoLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;

    private Terrain selectedTerrain;
    private LocationTerrainService locationService = new LocationTerrainService();
    private PlanteService planteService = new PlanteService();
    private AllocationPlanteService allocationService = new AllocationPlanteService();

    public void setTerrainData(Terrain t) {
        this.selectedTerrain = t;
        terrainInfoLabel.setText("Terrain à " + t.getRegion() + " - " + t.getSuperficie() + " m²");
        prixLabel.setText("Prix mensuel : " + t.getPrix_location() + " TND");
    }

    @FXML
    void confirmerLocation(ActionEvent event) {
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner les dates de début et de fin.", Alert.AlertType.ERROR);
            return;
        }

        LocalDate debutDate = dateDebutPicker.getValue();
        LocalDate finDate = dateFinPicker.getValue();
        LocalDate aujourdhui = LocalDate.now();

        // 1. Contrôle : Pas de date dans le passé
        if (debutDate.isBefore(aujourdhui)) {
            afficherAlerte("Date invalide", "La date de début ne peut pas être dans le passé.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Contrôle : Minimum 1 an (12 mois)
        long moisDifference = ChronoUnit.MONTHS.between(debutDate, finDate);
        if (moisDifference < 12) {
            afficherAlerte("Durée insuffisante", "La durée minimale de location est de 12 mois (1 an).", Alert.AlertType.WARNING);
            return;
        }

        String debut = debutDate.toString();
        String fin = finDate.toString();

        try {
            LocalDate d1 = dateDebutPicker.getValue();
            LocalDate d2 = dateFinPicker.getValue();
            
            // Calcul du nombre de mois (au moins 1 mois)
            long nbMois = ChronoUnit.MONTHS.between(d1, d2);
            if (nbMois <= 0) nbMois = 1; 

            float prixTotal = selectedTerrain.getPrix_location() * nbMois;

            // 1. Création de la location (ID utilisateur simulé à 1 pour le moment)
            LocationTerrain lt = new LocationTerrain(selectedTerrain.getId(), 1, debut, fin, selectedTerrain.getPrix_location(), prixTotal);
            locationService.add(lt);

            // 2. Allocation automatique des plantes de saison (basée sur toute la durée du contrat)
            List<Plante> plantesDeSaison = planteService.getPlantesPourSaisonSaisonnier(debut, fin);
            StringBuilder plantesAllouees = new StringBuilder();

            for (Plante p : plantesDeSaison) {
                AllocationPlante ap = new AllocationPlante(0, p.getId(), selectedTerrain.getId(), debut);
                allocationService.add(ap);
                
                String saison = getNomSaison(p.getDate_debut_plantation());
                plantesAllouees.append("- ").append(p.getNom())
                               .append(" (").append(saison).append(")\n");
            }

            // Affichage de l'alerte personnalisée au lieu de l'alerte standard
            afficherAlertePersonnalisee(nbMois, prixTotal, plantesAllouees.toString());
            
            // Retour au catalogue
            annuler(null);

        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors de la réservation : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CatalogueTerrains.fxml"));
            terrainInfoLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur navigation catalogue : " + e.getMessage());
        }
    }

    private void afficherAlertePersonnalisee(long nbMois, float prixTotal, String plantes) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SuccessAlert.fxml"));
            Parent root = loader.load();
            
            SuccessAlertController controller = loader.getController();
            controller.setData(nbMois, prixTotal, plantes.isEmpty() ? "Aucune plante trouvée." : plantes);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED); // Fenêtre sans bordures Windows pour plus de design
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Erreur affichage alerte personnalisée : " + e.getMessage());
            // Fallback sur l'alerte standard si l'FXML ne charge pas
            afficherAlerte("Félicitations", "Location enregistrée !\nTotal: " + prixTotal, Alert.AlertType.INFORMATION);
        }
    }

    private String getNomSaison(String dateMMDD) {
        if (dateMMDD == null || dateMMDD.length() < 2) return "Saison inconnue";
        int mois = Integer.parseInt(dateMMDD.substring(0, 2));
        
        if (mois >= 3 && mois <= 5) return "Printemps";
        if (mois >= 6 && mois <= 8) return "Été";
        if (mois >= 9 && mois <= 11) return "Automne";
        return "Hiver";
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void retourAccueil(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1150, 700);
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) terrainInfoLabel.getScene().getWindow();
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
