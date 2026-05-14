package controllers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import services.FaceRecognitionService;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

public class FaceIdSetupController {

    private static final int REQUIRED_CAPTURES = 20;

    private FaceRecognitionService faceService;
    private int userId;
    private int captureCount;
    private VideoCapture capture;
    private boolean capturing;

    public void startSetup(int userId) {
        this.faceService = new FaceRecognitionService();
        this.userId = userId;
        this.captureCount = 0;

        Stage stage = new Stage();
        stage.setTitle("Configuration Face ID");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label infoLabel = new Label("Placez votre visage devant la camera.");
        ImageView cameraView = new ImageView();
        cameraView.setFitWidth(420);
        cameraView.setFitHeight(300);

        Button startBtn = new Button("Commencer l'enregistrement");
        startBtn.getStyleClass().add("btn-primary");
        startBtn.setOnAction(e -> {
            if (!capturing) {
                capturing = true;
                startBtn.setDisable(true);
                new Thread(() -> captureFrames(cameraView, infoLabel, stage), "face-id-capture").start();
            }
        });

        root.getChildren().addAll(infoLabel, cameraView, startBtn);
        stage.setOnCloseRequest(e -> stopCapture());
        stage.setScene(new Scene(root, 470, 430));
        stage.show();

        new Thread(() -> startPreview(cameraView), "face-id-preview").start();
    }

    private void startPreview(ImageView cameraView) {
        try {
            capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                throw new IllegalStateException("Impossible d'ouvrir la webcam.");
            }

            Mat frame = new Mat();
            while (!capturing && capture != null && capture.isOpened()) {
                if (capture.read(frame) && !frame.empty()) {
                    Image image = matToImage(frame);
                    if (image != null) {
                        Platform.runLater(() -> cameraView.setImage(image));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void captureFrames(ImageView cameraView, Label infoLabel, Stage stage) {
        try {
            Path uploadDir = Path.of("uploads", "face_id");
            Files.createDirectories(uploadDir);

            Mat frame = new Mat();
            while (captureCount < REQUIRED_CAPTURES && capture != null && capture.isOpened()) {
                if (!capture.read(frame) || frame.empty()) {
                    continue;
                }

                Mat gray = new Mat();
                cvtColor(frame, gray, COLOR_BGR2GRAY);
                Rect[] faces = faceService.detectFaces(gray);

                if (faces.length > 0) {
                    Rect face = faces[0];
                    Mat faceRoi = new Mat(gray, face);
                    Mat resized = new Mat();
                    resize(faceRoi, resized, new Size(200, 200));

                    Path filePath = uploadDir.resolve(userId + "_" + System.currentTimeMillis() + ".jpg");
                    imwrite(filePath.toAbsolutePath().toString(), resized);
                    captureCount++;
                    Platform.runLater(() -> infoLabel.setText("Capture " + captureCount + "/" + REQUIRED_CAPTURES));
                } else {
                    Platform.runLater(() -> infoLabel.setText("Aucun visage detecte. Regardez la camera."));
                }

                Image image = matToImage(frame);
                if (image != null) {
                    Platform.runLater(() -> cameraView.setImage(image));
                }

                Thread.sleep(120);
            }

            stopCapture();
            Platform.runLater(() -> infoLabel.setText("Entrainement du modele Face ID..."));
            faceService.trainModel();
            Platform.runLater(() -> {
                infoLabel.setText("Face ID configure avec succes.");
                stage.close();
            });
        } catch (Exception e) {
            stopCapture();
            Platform.runLater(() -> infoLabel.setText("Erreur Face ID: " + e.getMessage()));
        }
    }

    private Image matToImage(Mat mat) {
        try {
            BytePointer bytePointer = new BytePointer();
            imencode(".png", mat, bytePointer);
            byte[] bytes = bytePointer.getStringBytes();
            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    private void stopCapture() {
        try {
            if (capture != null) {
                capture.release();
                capture = null;
            }
        } catch (Exception ignored) {
        }
    }
}
