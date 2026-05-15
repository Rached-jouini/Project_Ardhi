package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Utilisateur;
import utils.SessionManager;

import java.io.IOException;

public class UserHomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label joinedLabel;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            welcomeLabel.setText("Bienvenue");
            roleLabel.setText("Invite");
            emailLabel.setText("-");
            statusLabel.setText("-");
            joinedLabel.setText("-");
            return;
        }

        String displayName = (currentUser.getPrenom() + " " + currentUser.getNom()).trim();
        welcomeLabel.setText("Bienvenue " + displayName);
        roleLabel.setText(currentUser.getIdRole() == 1 ? "Administrateur" : "Utilisateur");
        emailLabel.setText(currentUser.getEmail());
        statusLabel.setText(currentUser.getStatut());
        joinedLabel.setText(currentUser.getDateInscription() != null
                ? currentUser.getDateInscription().toString()
                : "-");
    }

    @FXML
    public void handleOpenFinanceDashboard(ActionEvent event) throws IOException {
        openFxml(event, "/FinanceDashboard.fxml", "Ardhi - Finance Dashboard", true);
    }

    @FXML
    public void handleOpenTransactions(ActionEvent event) throws IOException {
        openFxml(event, "/TransactionsView.fxml", "Ardhi - Mes Transactions", true);
    }

    @FXML
    public void handleOpenWallets(ActionEvent event) throws IOException {
        openFxml(event, "/GestionWallets.fxml", "Ardhi - Comptes et Cartes", true);
    }

    @FXML
    public void handleOpenModule(ActionEvent event) {
        String moduleName = ((Button) event.getSource()).getText();
        try {
            // Dispatch selon le nom du bouton
            if (moduleName.contains("Catalogue")) {
                openFxml(event, "/CatalogueTerrains.fxml", "Ardhi - Catalogue", false);
            } else if (moduleName.contains("Evenements")) {
                openFxml(event, "/UserAfficherEvenements.fxml", "Ardhi - Evenements", true);
            } else if (moduleName.contains("Location")) {
                openFxml(event, "/LocationEquipement.fxml", "Ardhi - Location", true);
            } else if (moduleName.contains("Finance") || moduleName.contains("Capitale")) {
                openFxml(event, "/FinanceDashboard.fxml", "Ardhi - Finance Dashboard", true);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Module");
                alert.setHeaderText(moduleName);
                alert.setContentText("Cette section est en cours de developpement.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge un FXML. Si useArdhiCss est true, applique ardhi.css (style users).
     * Sinon les FXML terrain/plante embarquent déjà leur style.css via stylesheets="@style.css".
     */
    private void openFxml(ActionEvent event, String fxmlPath, String title, boolean useArdhiCss) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root, 1150, 700);
        if (useArdhiCss) {
            scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
    }

    @FXML
    public void handleOpenProfile(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/profile.fxml"));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Ardhi - Profil");
        stage.setScene(scene);
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        Parent root = FXMLLoader.load(getClass().getResource("/signin.fxml"));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Ardhi - Authentification");
        stage.setScene(scene);
    }
}
