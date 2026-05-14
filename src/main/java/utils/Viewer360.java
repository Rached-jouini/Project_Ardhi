package utils;

import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

public class Viewer360 {

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    public void display(String imagePath) {
        Stage stage = new Stage();
        stage.setTitle("Vue 360° du Terrain");

        // Création de la sphère
        Sphere sphere = new Sphere(500);
        
        // Inversion des normales (en utilisant un scale négatif sur l'axe Z)
        // Cela permet de voir la texture depuis l'intérieur
        sphere.setScaleX(-1);

        PhongMaterial material = new PhongMaterial();
        try {
            String finalPath = imagePath;
            // Si c'est un chemin local (ne commence pas par http ou file), on ajoute file:
            if (!finalPath.startsWith("http") && !finalPath.startsWith("file:")) {
                finalPath = "file:" + finalPath;
            }
            Image img = new Image(finalPath);
            material.setDiffuseMap(img);
            material.setSelfIlluminationMap(img);
        } catch (Exception e) {
            System.err.println("Erreur chargement texture 360 : " + e.getMessage());
        }
        sphere.setMaterial(material);
        
        // On s'assure de voir l'intérieur
        sphere.setCullFace(CullFace.NONE);

        // Groupe pour contenir la sphère et la lumière
        Group root = new Group(sphere);
        
        // Ajout d'une lumière ambiante pour être sûr de voir quelque chose
        AmbientLight ambientLight = new AmbientLight(javafx.scene.paint.Color.WHITE);
        root.getChildren().add(ambientLight);
        
        // Caméra au centre de la sphère
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(0); // Centre
        
        Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        // Contrôles de rotation
        root.getTransforms().addAll(rotateX, rotateY);

        scene.setOnMousePressed((MouseEvent event) -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()));
        });

        stage.setScene(scene);
        stage.show();
    }
}
