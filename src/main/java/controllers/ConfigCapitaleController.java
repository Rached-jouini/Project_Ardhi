package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.SourceFinancement;
import services.SourceFinancementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ConfigCapitaleController {

    @FXML private ComboBox<String> deviseCombo;
    @FXML private TextField seuilField;
    @FXML private TextArea descField;
    @FXML private Label totalCapitalLabel, riskLabel;
    @FXML private ProgressBar riskBar;
    @FXML private PieChart repartitionChart;
    @FXML private FlowPane sourcesFlowPane;

    private SourceFinancementService sourceService = new SourceFinancementService();

    @FXML
    public void initialize() {
        deviseCombo.setItems(FXCollections.observableArrayList("TND", "EUR", "USD"));
        deviseCombo.setValue("TND");
        loadData();
    }

    private void loadData() {
        try {
            // On ne garde que les sources "Capitales" (Interne, Externe, Emprunt)
            // On exclut les cartes personnelles
            List<SourceFinancement> allSources = sourceService.select();
            List<SourceFinancement> capitalSources = allSources.stream()
                .filter(sf -> !sf.getType().equalsIgnoreCase("CARTE"))
                .filter(sf -> !sf.getNom().toLowerCase().contains("visa"))
                .filter(sf -> !sf.getNom().toLowerCase().contains("mastercard"))
                .toList();

            renderSourceCards(capitalSources);
            updateAnalytics(capitalSources);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderSourceCards(List<SourceFinancement> list) {
        sourcesFlowPane.getChildren().clear();
        for (SourceFinancement sf : list) {
            sourcesFlowPane.getChildren().add(createCard(sf));
        }
    }

    private VBox createCard(SourceFinancement sf) {
        VBox card = new VBox(10);
        card.setPrefSize(220, 140);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        // Header: Nom + Actions
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(sf.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #063D2F;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> modifierSource(sf));
        
        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> supprimerSource(sf));

        header.getChildren().addAll(nameLabel, spacer, btnEdit, btnDelete);

        // Type Badge
        Label typeLabel = new Label(sf.getType().toUpperCase());
        typeLabel.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 10; -fx-text-fill: #6B7280;");

        // Amount
        Label amountLabel = new Label(String.format("%.2f DT", sf.getMontant()));
        amountLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #085041;");

        card.getChildren().addAll(header, typeLabel, amountLabel);
        return card;
    }

    private void updateAnalytics(List<SourceFinancement> list) {
        double total = 0;
        double emprunts = 0;
        repartitionChart.getData().clear();

        for (SourceFinancement sf : list) {
            total += sf.getMontant();
            if (sf.getType().equalsIgnoreCase("Emprunt")) {
                emprunts += sf.getMontant();
            }
        }

        // Deuxième boucle pour les pourcentages du PieChart
        for (SourceFinancement sf : list) {
            double percentage = total > 0 ? (sf.getMontant() / total) * 100 : 0;
            String label = String.format("%s (%.1f%%)", sf.getNom(), percentage);
            repartitionChart.getData().add(new PieChart.Data(label, sf.getMontant()));
        }

        repartitionChart.setLabelsVisible(true);
        repartitionChart.setLabelLineLength(5); // Retour à une distance raisonnable
        repartitionChart.setLegendVisible(true);
        repartitionChart.setLegendSide(Side.BOTTOM); // La "petite barre" en bas avec les noms et %

        totalCapitalLabel.setText(String.format("%.2f DT", total));

        if (total > 0) {
            double ratioDette = emprunts / total;
            riskBar.setProgress(ratioDette);
            if (ratioDette > 0.5) {
                riskLabel.setText("Dependance bancaire elevee");
                riskLabel.setStyle("-fx-text-fill: #F87171;");
                riskBar.setStyle("-fx-accent: #F87171;");
            } else {
                riskLabel.setText("Situation stable");
                riskLabel.setStyle("-fx-text-fill: #5DCAA5;");
                riskBar.setStyle("-fx-accent: #5DCAA5;");
            }
        }
    }

    @FXML
    void ouvrirAjoutSource(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireSource.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Source");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void supprimerSource(SourceFinancement sf) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette source ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    sourceService.delete(sf.getId());
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void modifierSource(SourceFinancement sf) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireSource.fxml"));
            Parent root = loader.load();
            FormulaireSourceController controller = loader.getController();
            controller.setData(sf);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Source");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void enregistrerConfigAction(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Configuration validee !").show();
    }

    @FXML
    void retourDashboard(ActionEvent event) {
        MainLayoutController.getInstance().loadPage("/FinanceDashboard.fxml");
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        MainLayoutController.getInstance().loadPage("/Home.fxml");
    }
}
