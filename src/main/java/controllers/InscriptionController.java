package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Evenement;
import models.Inscription;
import services.InscriptionService;

import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import services.EmailService;
import utils.SessionManager;
import controllers.InscriptionController;

public class InscriptionController {

    @FXML
    private Label eventTitle;

    @FXML
    private TextField nomUser;

    @FXML
    private TextField emailUser;

    private Evenement selectedEvent;
    private InscriptionService is = new InscriptionService();
    private EmailService emailService = new EmailService();

    public void setEvenement(Evenement ev) {
        this.selectedEvent = ev;
        eventTitle.setText("S'inscrire à : " + ev.getNom());
    }

    @FXML
    void handleRegister(ActionEvent event) {
        if (nomUser.getText().isEmpty() || emailUser.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setContentText("Veuillez remplir votre nom et votre email.");
            alert.showAndWait();
            return;
        }

        // Désactiver le bouton pour éviter les double-clics
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        source.setDisable(true);
        ((javafx.scene.control.Button) source).setText("⏳ Traitement...");

        // Lancer tout en arrière-plan pour ne pas bloquer l'interface
        String nomSaisi   = nomUser.getText();
        String emailSaisi = emailUser.getText();

        new Thread(() -> {
            try {
                // Étape 1 : Enregistrement BDD
                int currentUserId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 1;
                Inscription ins = new Inscription(selectedEvent.getId(), currentUserId, nomSaisi, emailSaisi);
                int inscriptionId = is.add(ins);

                // Étape 2 : Génération QR Code
                String qrData = "https://httpbin.org/get?Validation=REUSSIE&Inscription_ID=" + inscriptionId + "&Plateforme=Ardhi";
                String fileName = "pass_" + inscriptionId + ".png";
                File qrFile = new File(fileName);
                try {
                    com.google.zxing.qrcode.QRCodeWriter qrWriter = new com.google.zxing.qrcode.QRCodeWriter();
                    com.google.zxing.common.BitMatrix bitMatrix = qrWriter.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 300, 300);
                    com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrFile.toPath());
                } catch (Exception e) {
                    System.err.println("Erreur QR Code : " + e.getMessage());
                }

                // Étape 3 : Envoi du mail avec QR
                String subject = "🎫 Votre Pass d'entrée pour " + selectedEvent.getNom();
                String htmlBody = "<html><body style='font-family: Arial, sans-serif; background-color: #121212; color: white; padding: 20px;'>" +
                        "<div style='max-width: 500px; margin: auto; background-color: #1a1a1a; padding: 30px; border-radius: 15px; text-align: left;'>" +
                        "<h1 style='font-size: 24px; margin-bottom: 20px;'>Félicitations <span style='color: #4ade80;'>" + nomSaisi + "</span> !</h1>" +
                        "<p style='font-size: 16px; line-height: 1.5; color: #e5e7eb;'>Votre inscription à l'événement <strong>" + selectedEvent.getNom() + "</strong> est confirmée.</p>" +
                        "<div style='background-color: #2D6A4F; padding: 15px; border-radius: 10px; margin: 20px 0;'>" +
                        "<p style='margin: 0;'>✅ <strong>Votre Pass QR Code est joint à cet e-mail.</strong><br>" +
                        "Veuillez télécharger l'image jointe et la présenter à l'entrée.</p></div>" +
                        "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #333; color: #9ca3af; font-size: 14px;'>" +
                        "📍 " + selectedEvent.getLieu() + "<br>📅 " + selectedEvent.getDate() +
                        "</div></div></body></html>";

                emailService.sendEmailWithAttachment(emailSaisi, subject, htmlBody, fileName);

                // Étape 4 : Déclenchement automatique de la présence + certificat dans 5 min
                new services.CheckinService().validatePresence(inscriptionId);

                // Nettoyage du fichier QR temporaire
                if (qrFile.exists()) qrFile.delete();

                // Étape 5 : Retour sur le thread JavaFX pour afficher la popup
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Inscription Réussie");
                    alert.setHeaderText("Félicitations !");
                    alert.setContentText("✅ Inscription validée et Pass QR envoyé par e-mail.");
                    alert.showAndWait();
                    goBack();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    source.setDisable(false);
                    ((javafx.scene.control.Button) source).setText("Confirmer l'inscription");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Problème : " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    void handleCancel(ActionEvent event) {
        goBack();
    }

    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UserAfficherEvenements.fxml"));
            Stage stage = (Stage) nomUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
