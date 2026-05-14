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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.EmailService;
import services.UtilisateurService;
import utils.ValidationUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class ForgotPasswordController {

    @FXML
    private VBox step1Box;
    @FXML
    private VBox step2Box;
    @FXML
    private VBox step3Box;
    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label statusLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final EmailService emailService = new EmailService();

    private String targetEmail;
    private String generatedCode;

    @FXML
    public void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            setStatus("Veuillez entrer votre adresse email.", true);
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            setStatus("Format email invalide. Exemple: nom@domaine.tn", true);
            return;
        }

        try {
            if (!utilisateurService.emailExists(email)) {
                setStatus("Aucun compte n'est associe a cette adresse email.", true);
                return;
            }
        } catch (SQLException e) {
            showError(e.getMessage());
            return;
        }

        targetEmail = email;
        generatedCode = String.format("%06d", new Random().nextInt(1_000_000));
        setStatus("Envoi du code en cours...", false);

        Thread emailThread = new Thread(() -> {
            try {
                boolean success = emailService.sendResetCode(targetEmail, generatedCode);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        setStatus("Le code a ete envoye a votre adresse email.", false);
                        step1Box.setVisible(false);
                        step1Box.setManaged(false);
                        step2Box.setVisible(true);
                        step2Box.setManaged(true);
                    } else {
                        setStatus("Echec de l'envoi de l'email. Verifiez votre configuration SMTP.", true);
                    }
                });
            } catch (IllegalStateException ex) {
                javafx.application.Platform.runLater(() -> setStatus(ex.getMessage(), true));
            }
        }, "forgot-password-email");
        emailThread.setDaemon(true);
        emailThread.start();
    }

    @FXML
    public void handleVerifyCode() {
        String inputCode = codeField.getText().trim();

        if (inputCode.isEmpty()) {
            setStatus("Veuillez saisir le code de verification.", true);
            return;
        }
        if (!inputCode.equals(generatedCode)) {
            setStatus("Le code de verification est incorrect.", true);
            return;
        }

        setStatus("Code verifie. Vous pouvez definir un nouveau mot de passe.", false);
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step3Box.setVisible(true);
        step3Box.setManaged(true);
    }

    @FXML
    public void handleUpdatePassword(ActionEvent event) {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            setStatus("Veuillez remplir tous les champs.", true);
            return;
        }
        if (newPassword.length() < 6) {
            setStatus("Le mot de passe doit contenir au moins 6 caracteres.", true);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            setStatus("Les deux mots de passe doivent etre identiques.", true);
            return;
        }

        try {
            boolean updated = utilisateurService.updatePasswordByEmail(targetEmail, newPassword);
            if (!updated) {
                setStatus("Impossible de mettre a jour le mot de passe.", true);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succes");
            alert.setHeaderText(null);
            alert.setContentText("Votre mot de passe a ete mis a jour.");
            alert.showAndWait();
            goBackToSignIn(event);
        } catch (SQLException | IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void goBackToSignIn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/signin.fxml"));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Ardhi - Authentification");
        stage.setScene(scene);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #059669;");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
