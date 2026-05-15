package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import models.Evenement;
import services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserAfficherEvenementsController {

    @FXML private VBox eventsContainer;
    @FXML private DatePicker filterDate;
    @FXML private WebView mapView;

    private EvenementService eventService = new EvenementService();
    private ObservableList<Evenement> allEvents = FXCollections.observableArrayList();
    private Evenement selectedEvent = null;

    @FXML
    public void initialize() {
        // Optimisation de la carte
        mapView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
        
        chargerDonnees();
        updateMap("Tunisie"); // Carte par défaut
    }

    private void chargerDonnees() {
        try {
            allEvents.setAll(eventService.select());
            renderEvents(allEvents);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderEvents(List<Evenement> events) {
        eventsContainer.getChildren().clear();
        for (Evenement ev : events) {
            eventsContainer.getChildren().add(createEventCard(ev));
        }
    }

    private VBox createEventCard(Evenement ev) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); " +
                     "-fx-border-color: #F1F5F9; -fx-border-radius: 15; -fx-border-width: 1; -fx-cursor: hand;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label(ev.getNom());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #1E293B;");
        Label type = new Label(ev.getType() + " • " + ev.getCulture_concernee());
        type.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11;");
        titleBox.getChildren().addAll(title, type);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusBadge = new Label(ev.getNombre_places() > 0 ? ev.getNombre_places() + " places" : "COMPLET");
        String badgeColor = ev.getNombre_places() > 0 ? "#10B981" : "#EF4444";
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "22; -fx-text-fill: " + badgeColor + "; -fx-font-weight: bold; -fx-font-size: 10; -fx-padding: 4 10; -fx-background-radius: 10;");
        
        header.getChildren().addAll(titleBox, spacer, statusBadge);

        Label desc = new Label(ev.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 13;");
        desc.setMaxHeight(60);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Label date = new Label("📅 " + ev.getDate().format(fmt));
        date.setStyle("-fx-text-fill: #063D2F; -fx-font-weight: bold; -fx-font-size: 12;");
        
        Label lieu = new Label("📍 " + ev.getLieu());
        lieu.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12;");

        footer.getChildren().addAll(date, lieu);

        card.getChildren().addAll(header, desc, footer);

        // Interaction
        card.setOnMouseClicked(e -> {
            selectedEvent = ev;
            updateMap(ev.getLieu());
            // Highlight effect
            eventsContainer.getChildren().forEach(n -> n.setStyle(n.getStyle().replace("-fx-border-color: #10B981;", "-fx-border-color: #F1F5F9;")));
            card.setStyle(card.getStyle().replace("-fx-border-color: #F1F5F9;", "-fx-border-color: #10B981;"));
        });

        return card;
    }

    private void updateMap(String location) {
        String loc = (location == null || location.isEmpty()) ? "Tunisie" : location;
        String url = "https://duckduckgo.com/?q=" + loc.replace(" ", "+") + "&iaxm=maps";
        mapView.getEngine().load(url);
    }

    @FXML
    void handleFilterDate(ActionEvent event) {
        LocalDate selectedDate = filterDate.getValue();
        if (selectedDate != null) {
            List<Evenement> filtered = allEvents.stream()
                    .filter(ev -> ev.getDate().isEqual(selectedDate))
                    .toList();
            renderEvents(filtered);
        }
    }

    @FXML
    void handleResetFilter(ActionEvent event) {
        filterDate.setValue(null);
        renderEvents(allEvents);
    }

    @FXML
    void handleInscrire(ActionEvent event) {
        if (selectedEvent == null) {
            showAlert("Selection requise", "Veuillez selectionner un evenement dans la liste pour vous inscrire.");
            return;
        }

        if (selectedEvent.getNombre_places() <= 0) {
            showAlert("Complet", "Desole, cet evenement est complet.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InscriptionController.fxml"));
            Parent root = loader.load();
            InscriptionController controller = loader.getController();
            controller.setEvenement(selectedEvent);

            Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleChatbot(ActionEvent event) {
        if (selectedEvent == null) {
            showAlert("Selection requise", "Veuillez selectionner un evenement pour activer l'assistant.");
            return;
        }
        // ... (Logique chatbot identique à l'originale, simplifiée ici pour la démo)
        openChatDialog(selectedEvent);
    }

    private void openChatDialog(Evenement selected) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ardhi AI Assistant");
        dialog.setHeaderText("Assistant pour : " + selected.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox chatContainer = new VBox(10);
        chatContainer.setPrefSize(400, 300);
        chatContainer.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc;");

        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        TextField inputField = new TextField();
        inputField.setPromptText("Posez une question...");
        Button sendBtn = new Button("Envoyer");
        sendBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white;");

        HBox inputArea = new HBox(5, inputField, sendBtn);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        VBox layout = new VBox(10, scrollPane, inputArea);
        dialog.getDialogPane().setContent(layout);

        addChatMessage(chatContainer, "Bonjour ! Je suis l'assistant Ardhi. Que voulez-vous savoir sur '" + selected.getNom() + "' ?", false);

        sendBtn.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                addChatMessage(chatContainer, text, true);
                inputField.clear();
                addChatMessage(chatContainer, getAIResponse(selected, text), false);
            }
        });
        dialog.showAndWait();
    }

    private void addChatMessage(VBox container, String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setStyle(isUser ? "-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 15 15 0 15;" 
                             : "-fx-background-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-padding: 8 12; -fx-background-radius: 15 15 15 0;");
        HBox row = new HBox(label);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.getChildren().add(row);
    }

    private String getAIResponse(Evenement ev, String question) {
        String q = question.toLowerCase();
        if (q.contains("lieu") || q.contains("où")) return "L'événement se déroule à " + ev.getLieu() + ".";
        if (q.contains("date") || q.contains("quand")) return "C'est prévu pour le " + ev.getDate().toString() + ".";
        if (q.contains("place")) return "Il reste " + ev.getNombre_places() + " places.";
        return "Détails : " + ev.getDescription();
    }

    // Navigation Sidebar
    @FXML void handleBackHome(ActionEvent event) throws IOException { loadScene(event, "/user-home.fxml"); }
    @FXML void handleOpenLocation(ActionEvent event) throws IOException { loadScene(event, "/LocationEquipement.fxml"); }
    @FXML void handleOpenFinance(ActionEvent event) throws IOException { loadScene(event, "/FinanceDashboard.fxml"); }

    private void loadScene(ActionEvent event, String resource) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = ((javafx.scene.Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
