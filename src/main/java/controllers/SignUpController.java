package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import models.Utilisateur;
import services.EmailService;
import services.UtilisateurService;
import utils.ValidationUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

public class SignUpController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField telephoneField;
    @FXML
    private Label feedbackLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final EmailService emailService = new EmailService();

    @FXML
    public void handleSignUp(ActionEvent event) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String telephone = telephoneField.getText().trim();

        String validation = validateInputs(nom, prenom, email, password, telephone);
        if (validation != null) {
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            feedbackLabel.setText(validation);
            return;
        }

        try {
            if (utilisateurService.emailExists(email)) {
                feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                feedbackLabel.setText("Cet email existe deja.");
                return;
            }

            Utilisateur user = new Utilisateur(
                    nom,
                    prenom,
                    email,
                    password,
                    telephone,
                    LocalDate.now(),
                    "actif",
                    2
            );
            String verificationCode = String.format("%06d", new Random().nextInt(1_000_000));
            feedbackLabel.setStyle("-fx-text-fill: #2d6a43;");
            feedbackLabel.setText("Envoi de l'email de verification...");

            Thread emailThread = new Thread(() -> {
                try {
                    boolean success = emailService.sendSignupVerificationCode(email, verificationCode);
                    javafx.application.Platform.runLater(() -> {
                        if (!success) {
                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                            feedbackLabel.setText("Echec de l'envoi de l'email. Verifiez SMTP dans .env.");
                            return;
                        }

                        Optional<String> enteredCode = askForVerificationCode();
                        if (enteredCode.isEmpty()) {
                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                            feedbackLabel.setText("Verification annulee. Le compte n'a pas ete cree.");
                            return;
                        }

                        if (!verificationCode.equals(enteredCode.get().trim())) {
                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                            feedbackLabel.setText("Code de verification incorrect.");
                            return;
                        }

                        try {
                            utilisateurService.add(user);
                            feedbackLabel.setStyle("-fx-text-fill: #059669;");
                            feedbackLabel.setText("Inscription verifiee et reussie. Connectez-vous.");
                            loadScene(event, "/signin.fxml", "Ardhi - Authentification");
                        } catch (SQLException | IOException e) {
                            showError(e.getMessage());
                        }
                    });
                } catch (IllegalStateException ex) {
                    javafx.application.Platform.runLater(() -> {
                        feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        feedbackLabel.setText(ex.getMessage());
                    });
                }
            }, "signup-email-verification");
            emailThread.setDaemon(true);
            emailThread.start();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void goToSignIn(ActionEvent event) {
        try {
            loadScene(event, "/signin.fxml", "Ardhi - Authentification");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    private String validateInputs(String nom, String prenom, String email, String password, String telephone) {
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || telephone.isEmpty()) {
            return "Tous les champs sont obligatoires.";
        }
        if (!nom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$") || !prenom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$")) {
            return "Nom/Prenom invalides.";
        }
        if (!ValidationUtils.isValidEmail(email)) {
            return "Email invalide. Utilisez le format nom@domaine.tld.";
        }
        if (password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caracteres.";
        }
        if (!telephone.matches("^[0-9]{8,15}$")) {
            return "Telephone invalide (8 a 15 chiffres).";
        }
        return null;
    }

    private void loadScene(ActionEvent event, String resource, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<String> askForVerificationCode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Verification email");
        dialog.setHeaderText("Un code a ete envoye a votre adresse email.");
        dialog.setContentText("Entrez le code de verification:");
        return dialog.showAndWait();
    }
}
