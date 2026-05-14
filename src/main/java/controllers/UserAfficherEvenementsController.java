package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Evenement;
import services.EvenementService;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class UserAfficherEvenementsController {

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
    private TableColumn<Evenement, Integer> colPlaces;

    @FXML
    private DatePicker filterDate;

    @FXML
    private WebView mapView;

    private EvenementService ps = new EvenementService();
    private ObservableList<Evenement> allEvents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Mode compatibilité maximale : on se fait passer pour IE11
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombre_places"));

        // Écouteur de sélection pour mettre à jour la carte
        tableEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateMap(newSelection.getLieu());
            }
        });

        chargerDonnees();
        updateMap("Tunisie"); // Carte par défaut
    }

    private void updateMap(String location) {
        if (location == null || location.trim().isEmpty()) {
            location = "Tunisie";
        }
        // Utilisation de DuckDuckGo Maps (propulsé par Apple Maps HD)
        // C'est la solution la plus stable qui ne bloque jamais dans JavaFX
        String url = "https://duckduckgo.com/?q=" + location.replace(" ", "+") + "&iaxm=maps";
        
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
        mapView.getEngine().load(url);
    }
    private void chargerDonnees() {
        try {
            allEvents.setAll(ps.select());
            tableEvenements.setItems(allEvents);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleFilterDate(ActionEvent event) {
        LocalDate selectedDate = filterDate.getValue();
        if (selectedDate != null) {
            ObservableList<Evenement> filteredList = allEvents.filtered(ev -> 
                ev.getDate().isEqual(selectedDate)
            );
            tableEvenements.setItems(filteredList);
        }
    }

    @FXML
    void handleResetFilter(ActionEvent event) {
        filterDate.setValue(null);
        tableEvenements.setItems(allEvents);
    }

    @FXML
    void handleChatbot(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setContentText("Veuillez sélectionner un événement pour obtenir de l'aide.");
            alert.showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ardhi AI Assistant");
        dialog.setHeaderText("Assistant pour : " + selected.getNom());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox chatContainer = new VBox(10);
        chatContainer.setPrefSize(400, 300);
        chatContainer.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 10;");

        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        TextField inputField = new TextField();
        inputField.setPromptText("Posez une question sur cet événement...");
        Button sendBtn = new Button("Envoyer");
        sendBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white;");

        HBox inputArea = new HBox(5, inputField, sendBtn);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        VBox layout = new VBox(10, scrollPane, inputArea);
        dialog.getDialogPane().setContent(layout);

        addChatMessage(chatContainer, "Bonjour ! Je suis l'assistant Ardhi. Je peux vous en dire plus sur '" + selected.getNom() + "'. Que voulez-vous savoir ?", false);

        sendBtn.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                addChatMessage(chatContainer, text, true);
                inputField.clear();
                
                String aiResponse = getAIExplanation(selected, text);
                addChatMessage(chatContainer, aiResponse, false);
            }
        });

        dialog.showAndWait();
    }

    private String getAIExplanation(Evenement ev, String question) {
        String q = question.toLowerCase();
        
        if (q.contains("lieu") || q.contains("où") || q.contains("place")) {
            if (q.contains("combien") || q.contains("nombre")) {
                return "Il reste actuellement " + ev.getNombre_places() + " places disponibles pour cet événement.";
            }
            return "L'événement '" + ev.getNom() + "' se déroulera à " + ev.getLieu() + ".";
        } 
        
        if (q.contains("date") || q.contains("quand") || q.contains("moment")) {
            return "Cet événement est prévu pour le " + ev.getDate().toString() + ".";
        }
        
        if (q.contains("durée") || q.contains("temps") || q.contains("long")) {
            // On peut estimer ou dire que c'est dans la description
            return "La durée n'est pas explicitement spécifiée, mais voici ce que nous savons : " + ev.getDescription();
        }
        
        if (q.contains("type") || q.contains("catégorie") || q.contains("genre")) {
            return "C'est un événement de type '" + ev.getType() + "' concernant la culture : " + ev.getCulture_concernee() + ".";
        }
        
        if (q.contains("salut") || q.contains("bonjour") || q.contains("coucou")) {
            return "Bonjour ! Comment puis-je vous aider concernant l'événement '" + ev.getNom() + "' ?";
        }

        // Par défaut, donner la description mais de manière plus naturelle
        return "Pour '" + ev.getNom() + "', voici les détails : " + ev.getDescription();
    }

    private void addChatMessage(VBox container, String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(300);
        label.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-ai");
        
        HBox row = new HBox(label);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.getChildren().add(row);
    }

    @FXML
    void handleInscrire(ActionEvent event) {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setContentText("Veuillez sélectionner un événement pour vous inscrire.");
            alert.showAndWait();
            return;
        }

        if (selected.getNombre_places() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Plus de places");
            alert.setContentText("Désolé, cet événement est complet.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InscriptionController.fxml"));
            Parent root = loader.load();

            InscriptionController controller = loader.getController();
            controller.setEvenement(selected);

            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleSwitchToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherEvenementsController.fxml"));
            Stage stage = (Stage) tableEvenements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
