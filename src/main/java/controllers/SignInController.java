package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.GoogleUserInfo;
import models.Utilisateur;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import services.FaceRecognitionService;
import services.GoogleOAuthService;
import services.UtilisateurService;
import utils.SessionManager;
import utils.ValidationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private ToggleGroup roleGroup;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();

    @FXML
    public void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Email et mot de passe sont obligatoires.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            feedbackLabel.setText("L'email doit etre au format nom@domaine.tld.");
            return;
        }

        // Recuperation du role selectionne (optionnel pour la redirection)
        ToggleButton selectedRole = (ToggleButton) roleGroup.getSelectedToggle();
        String roleName = selectedRole != null ? selectedRole.getText() : "Aucun";
        System.out.println("Tentative de connexion en tant que : " + roleName);

        try {
            Utilisateur connected = utilisateurService.authenticate(email, password);
            if (connected == null) {
                feedbackLabel.setText("Identifiants invalides.");
                return;
            }
            if (!"actif".equalsIgnoreCase(connected.getStatut())) {
                feedbackLabel.setText("Ce compte est inactif. Contactez l'administration.");
                return;
            }
            if (connected.isBanned()) {
                showBanMessage(connected);
                return;
            }

            processSuccessfulLogin(event, connected);
        } catch (SQLException | IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleGoogleSignIn(ActionEvent event) {
        feedbackLabel.setStyle("-fx-text-fill: #2d6a43;");
        feedbackLabel.setText("Ouverture de Google Sign-In...");

        Thread oauthThread = new Thread(() -> {
            try {
                GoogleUserInfo googleUser = googleOAuthService.signIn();
                Platform.runLater(() -> {
                    if (googleUser == null || googleUser.email == null || googleUser.email.isBlank()) {
                        feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        feedbackLabel.setText("Impossible de recuperer l'email Google. Verifiez la configuration OAuth.");
                        return;
                    }

                    if (Boolean.FALSE.equals(googleUser.email_verified)) {
                        feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        feedbackLabel.setText("Le compte Google est connecte, mais l'email n'est pas verifie.");
                        return;
                    }

                    try {
                        Utilisateur connected = utilisateurService.findOrCreateGoogleUser(googleUser);
                        if (connected == null) {
                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                            feedbackLabel.setText("Impossible de recuperer ou creer le compte Google.");
                            return;
                        }
                        if (!"actif".equalsIgnoreCase(connected.getStatut())) {
                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                            feedbackLabel.setText("Ce compte est inactif. Contactez l'administration.");
                            return;
                        }
                        if (connected.isBanned()) {
                            showBanMessage(connected);
                            return;
                        }

                        processSuccessfulLogin(event, connected);
                    } catch (SQLException | IOException e) {
                        showError(e.getMessage());
                    }
                });
            } catch (IllegalStateException ex) {
                Platform.runLater(() -> {
                    feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                    feedbackLabel.setText(ex.getMessage());
                });
            }
        }, "google-oauth-thread");

        oauthThread.setDaemon(true);
        oauthThread.start();
    }

    @FXML
    public void handleFaceIdLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            feedbackLabel.setText("Saisissez d'abord votre email pour utiliser Face ID.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            feedbackLabel.setText("Entrez un email valide avant d'utiliser Face ID.");
            return;
        }

        Utilisateur targetUser;
        try {
            targetUser = utilisateurService.findByEmail(email);
            if (targetUser == null) {
                feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                feedbackLabel.setText("Aucun compte ne correspond a cet email.");
                return;
            }
            if (!"actif".equalsIgnoreCase(targetUser.getStatut())) {
                feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                feedbackLabel.setText("Ce compte est inactif. Contactez l'administration.");
                return;
            }
            if (targetUser.isBanned()) {
                showBanMessage(targetUser);
                return;
            }
        } catch (SQLException e) {
            showError(e.getMessage());
            return;
        }

        feedbackLabel.setStyle("-fx-text-fill: #2d6a43;");
        feedbackLabel.setText("Demarrage de Face ID pour " + email + "...");

        FaceRecognitionService faceService;
        try {
            faceService = new FaceRecognitionService();
            faceService.loadModel();
        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            feedbackLabel.setText(e.getMessage());
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Connexion Face ID");

        VBox root = new VBox(10);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label infoLabel = new Label("Recherche de visage...");
        ImageView cameraView = new ImageView();
        cameraView.setFitWidth(420);
        cameraView.setFitHeight(300);
        root.getChildren().addAll(infoLabel, cameraView);

        stage.setScene(new Scene(root, 470, 390));
        stage.show();

        FaceRecognitionService finalFaceService = faceService;
        Utilisateur finalTargetUser = targetUser;
        new Thread(() -> runFaceIdLogin(event, stage, infoLabel, cameraView, finalFaceService, finalTargetUser), "face-id-login").start();
    }

    @FXML
    public void goToSignUp(ActionEvent event) {
        try {
            loadScene(event, "/signup.fxml", "Ardhi - Inscription");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void goToForgotPassword(ActionEvent event) {
        try {
            loadScene(event, "/forgot-password.fxml", "Ardhi - Recuperation");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    private void openTargetScene(ActionEvent event, Utilisateur connected) throws IOException {
        if (connected.getIdRole() == 1) {
            loadScene(event, "/admin-dashboard.fxml", "Ardhi - Dashboard Admin");
        } else {
            loadScene(event, "/user-home.fxml", "Ardhi - Accueil");
        }
    }

    private void processSuccessfulLogin(ActionEvent event, Utilisateur connected) throws IOException {
        SessionManager.setCurrentUser(connected);
        openTargetScene(event, connected);
    }

    private void showBanMessage(Utilisateur user) {
        feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
        String until = user.getBanUntil() != null
                ? user.getBanUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "date inconnue";
        feedbackLabel.setText("Compte banni jusqu'au " + until + ".");
    }

    private void runFaceIdLogin(ActionEvent event,
                                Stage stage,
                                Label infoLabel,
                                ImageView cameraView,
                                FaceRecognitionService faceService,
                                Utilisateur targetUser) {
        VideoCapture capture = null;
        try {
            capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                throw new IllegalStateException("Impossible d'ouvrir la webcam.");
            }

            boolean loggedIn = false;
            int consecutiveMatches = 0;
            int lastMatchedUser = -1;
            Mat frame = new Mat();

            while (!loggedIn && stage.isShowing() && capture.isOpened()) {
                if (!capture.read(frame) || frame.empty()) {
                    continue;
                }

                Mat grayMat = new Mat();
                cvtColor(frame, grayMat, COLOR_BGR2GRAY);
                Rect[] faces = faceService.detectFaces(grayMat);

                if (faces.length > 0) {
                    Rect face = faces[0];
                    Mat faceROI = new Mat(grayMat, face);
                    Mat resizedFace = new Mat();
                    resize(faceROI, resizedFace, new Size(200, 200));
                    int recognizedUserId = faceService.recognize(resizedFace);

                    if (recognizedUserId != -1) {
                        if (recognizedUserId != targetUser.getId()) {
                            consecutiveMatches = 0;
                            lastMatchedUser = -1;
                            Platform.runLater(() -> infoLabel.setText("Ce visage ne correspond pas a l'email saisi."));
                            Image image = matToImage(frame);
                            if (image != null) {
                                Platform.runLater(() -> cameraView.setImage(image));
                            }
                            Thread.sleep(100);
                            continue;
                        }

                        if (recognizedUserId == lastMatchedUser) {
                            consecutiveMatches++;
                        } else {
                            consecutiveMatches = 1;
                            lastMatchedUser = recognizedUserId;
                        }

                        if (consecutiveMatches >= 5) {
                            Utilisateur user = utilisateurService.findById(recognizedUserId);
                            if (user != null) {
                                loggedIn = true;
                                Utilisateur matchedUser = user;
                                Platform.runLater(() -> {
                                    try {
                                        if (!"actif".equalsIgnoreCase(matchedUser.getStatut())) {
                                            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                                            feedbackLabel.setText("Ce compte est inactif.");
                                        } else if (matchedUser.isBanned()) {
                                            showBanMessage(matchedUser);
                                        } else {
                                            processSuccessfulLogin(event, matchedUser);
                                        }
                                    } catch (IOException e) {
                                        showError(e.getMessage());
                                    } finally {
                                        stage.close();
                                    }
                                });
                                break;
                            }
                        } else {
                            Platform.runLater(() -> infoLabel.setText("Verification... ne bougez pas."));
                        }
                    } else {
                        consecutiveMatches = 0;
                        lastMatchedUser = -1;
                        Platform.runLater(() -> infoLabel.setText("Visage non reconnu, veuillez reessayer."));
                    }
                } else {
                    Platform.runLater(() -> infoLabel.setText("Aucun visage detecte."));
                }

                Image image = matToImage(frame);
                if (image != null) {
                    Platform.runLater(() -> cameraView.setImage(image));
                }

                Thread.sleep(100);
            }
        } catch (Exception ex) {
            Platform.runLater(() -> {
                feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                feedbackLabel.setText("Erreur Face ID: " + ex.getMessage());
                if (stage.isShowing()) {
                    stage.close();
                }
            });
        } finally {
            stopCapture(capture);
        }
    }

    private void stopCapture(VideoCapture capture) {
        try {
            if (capture != null) {
                capture.release();
            }
        } catch (Exception ignored) {
        }
    }

    private Image matToImage(Mat mat) {
        try {
            BytePointer bytePointer = new BytePointer();
            imencode(".png", mat, bytePointer);
            byte[] byteArray = bytePointer.getStringBytes();
            return new Image(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            return null;
        }
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
}
