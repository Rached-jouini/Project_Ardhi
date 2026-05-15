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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.SessionManager;
import utils.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ProfileController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label joinedLabel;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label feedbackLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur currentUser;
    private String pendingPhotoPath;

    @FXML
    public void initialize() {
        Circle clip = new Circle(56, 56, 56);
        profileImageView.setClip(clip);

        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            feedbackLabel.setText("Aucun utilisateur connecte.");
            return;
        }

        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        roleLabel.setText(currentUser.getIdRole() == 1 ? "Administrateur" : "Utilisateur");
        statusLabel.setText(currentUser.isBanned() ? "Banni" : currentUser.getStatut());
        joinedLabel
                .setText(currentUser.getDateInscription() != null ? currentUser.getDateInscription().toString() : "-");
        loadProfileImage(currentUser.getPhoto());
    }

    @FXML
    public void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            Path uploadDir = Path.of("uploads", "profile");
            Files.createDirectories(uploadDir);
            String extension = extractExtension(selectedFile.getName());
            String fileName = "user_" + currentUser.getId() + "_" + System.currentTimeMillis() + extension;
            Path destPath = uploadDir.resolve(fileName);
            Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            pendingPhotoPath = destPath.toString();
            profileImageView.setImage(new Image(selectedFile.toURI().toString()));
            feedbackLabel.setStyle("-fx-text-fill: #2d6a43;");
            feedbackLabel.setText("Photo prete a etre enregistree.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image : " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProfile() {
        if (currentUser == null) {
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            setFeedback("Nom, prenom et email sont obligatoires.", true);
            return;
        }
        if (!nom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$") || !prenom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$")) {
            setFeedback("Nom/Prenom invalides.", true);
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            setFeedback("Email invalide. Utilisez le format nom@domaine.tld.", true);
            return;
        }
        if (!telephone.isEmpty() && !telephone.matches("^[0-9]{8,15}$")) {
            setFeedback("Telephone invalide.", true);
            return;
        }

        try {
            Utilisateur existing = utilisateurService.findByEmail(email);
            if (existing != null && existing.getId() != currentUser.getId()) {
                setFeedback("Cet email est deja utilise.", true);
                return;
            }

            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setTelephone(telephone.isBlank() ? null : telephone);
            if (!password.isBlank()) {
                currentUser.setMotDePasse(password);
            }
            if (pendingPhotoPath != null) {
                currentUser.setPhoto(pendingPhotoPath);
            }

            utilisateurService.updateProfile(currentUser);
            currentUser = utilisateurService.findById(currentUser.getId());
            SessionManager.setCurrentUser(currentUser);
            pendingPhotoPath = null;
            loadProfileImage(currentUser.getPhoto());
            setFeedback("Profil mis a jour avec succes.", false);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleSetupFaceId() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecte.");
            return;
        }

        try {
            FaceIdSetupController setupController = new FaceIdSetupController();
            setupController.startSetup(currentUser.getId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Face ID", e.getMessage());
        }
    }

    @FXML
    public void handleBackHome(ActionEvent event) throws IOException {
        String resource = "/user-home.fxml";
        if (currentUser != null && currentUser.getIdRole() == 1) { // 1 = Admin
            resource = "/admin-dashboard.fxml";
        }
        
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        goToScene(event, "/signin.fxml", "Ardhi - Authentification");
    }

    private void goToScene(ActionEvent event, String resource, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
    }

    private void loadProfileImage(String photoPath) {
        Image image = null;

        try {
            if (photoPath != null && !photoPath.isBlank()) {
                if (photoPath.startsWith("http://") || photoPath.startsWith("https://")
                        || photoPath.startsWith("file:")) {
                    image = new Image(photoPath, true);
                } else {
                    File file = new File(photoPath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (image == null || image.isError()) {
            image = buildFallbackImage();
        }
        profileImageView.setImage(image);
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : ".png";
    }

    private void setFeedback(String message, boolean error) {
        feedbackLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #2d6a43;");
        feedbackLabel.setText(message);
    }

    private Image buildFallbackImage() {
        WritableImage image = new WritableImage(112, 112);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < 112; y++) {
            for (int x = 0; x < 112; x++) {
                writer.setColor(x, y, Color.web("#d9e7dc"));
            }
        }
        return image;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
