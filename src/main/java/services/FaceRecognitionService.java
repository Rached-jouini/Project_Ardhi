package services;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.IntBuffer;
import java.util.stream.Stream;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;

public class FaceRecognitionService {

    private final FaceRecognizer faceRecognizer;
    private final CascadeClassifier faceDetector;
    private final Path trainingDir;
    private final Path modelPath;

    public FaceRecognitionService() {
        try {
            this.trainingDir = Path.of("uploads", "face_id");
            this.modelPath = trainingDir.resolve("face_model.yml");
            Files.createDirectories(trainingDir);

            Path classifierPath = ensureClassifierFile();
            this.faceDetector = new CascadeClassifier(classifierPath.toAbsolutePath().toString());
            if (faceDetector.empty()) {
                throw new IllegalStateException("Impossible de charger le classifieur Face ID.");
            }

            this.faceRecognizer = LBPHFaceRecognizer.create();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'initialiser Face ID: " + e.getMessage(), e);
        }
    }

    private Path ensureClassifierFile() throws IOException {
        Path mlDir = Path.of("uploads", "ml");
        Files.createDirectories(mlDir);
        Path classifierFile = mlDir.resolve("haarcascade_frontalface_default.xml");
        if (Files.exists(classifierFile)) {
            return classifierFile;
        }

        try (InputStream in = getClass().getResourceAsStream("/ml/haarcascade_frontalface_default.xml")) {
            if (in == null) {
                throw new IllegalStateException("Le fichier haarcascade_frontalface_default.xml est introuvable.");
            }
            Files.copy(in, classifierFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return classifierFile;
    }

    public void trainModel() {
        try (Stream<Path> paths = Files.list(trainingDir)) {
            Path[] imageFiles = paths
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".png");
                    })
                    .toArray(Path[]::new);

            if (imageFiles.length == 0) {
                throw new IllegalStateException("Aucune image Face ID d'entrainement n'a ete trouvee.");
            }

            MatVector images = new MatVector(imageFiles.length);
            Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
            IntBuffer labelsBuf = labels.createBuffer();

            int counter = 0;
            for (Path imagePath : imageFiles) {
                Mat img = imread(imagePath.toAbsolutePath().toString(), IMREAD_GRAYSCALE);
                if (img.empty()) {
                    continue;
                }

                equalizeHist(img, img);
                int label = Integer.parseInt(imagePath.getFileName().toString().split("_")[0]);
                images.put(counter, img);
                labelsBuf.put(counter, label);
                counter++;
            }

            if (counter == 0) {
                throw new IllegalStateException("Les images Face ID sont invalides.");
            }

            faceRecognizer.train(images, labels);
            faceRecognizer.save(modelPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new IllegalStateException("Erreur d'entrainement Face ID: " + e.getMessage(), e);
        }
    }

    public void loadModel() {
        if (Files.exists(modelPath)) {
            faceRecognizer.read(modelPath.toAbsolutePath().toString());
        } else {
            throw new IllegalStateException("Aucun modele Face ID trouve. Configurez d'abord Face ID depuis le profil.");
        }
    }

    public int recognize(Mat grayImage) {
        int[] label = new int[1];
        double[] confidence = new double[1];
        Mat equalized = new Mat();
        equalizeHist(grayImage, equalized);
        faceRecognizer.predict(equalized, label, confidence);

        if (confidence[0] < 35.0d) {
            return label[0];
        }
        return -1;
    }

    public Rect[] detectFaces(Mat frame) {
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(frame, faces, 1.1, 3, 0, new Size(100, 100), new Size(500, 500));
        Rect[] result = new Rect[(int) faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            result[i] = faces.get(i);
        }
        return result;
    }
}
