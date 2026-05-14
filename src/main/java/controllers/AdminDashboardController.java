package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.SessionManager;
import utils.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label adminUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label bannedUsersLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextField idField;
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
    private ComboBox<String> statutBox;
    @FXML
    private ComboBox<Integer> roleBox;
    @FXML
    private Spinner<Integer> banDurationSpinner;
    @FXML
    private ComboBox<String> banUnitBox;
    @FXML
    private Label selectionStatusLabel;

    @FXML
    private TableView<Utilisateur> userTable;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colPhoto;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colTelephone;
    @FXML
    private TableColumn<Utilisateur, String> colStatut;
    @FXML
    private TableColumn<Utilisateur, String> colRole;
    @FXML
    private TableColumn<Utilisateur, String> colBanUntil;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final DateTimeFormatter banFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        statutBox.setItems(FXCollections.observableArrayList("actif", "inactif"));
        roleBox.setItems(FXCollections.observableArrayList(1, 2));
        banUnitBox.setItems(FXCollections.observableArrayList("jours", "semaines", "mois"));
        banUnitBox.setValue("jours");
        banDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 2));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPhoto.setCellValueFactory(new PropertyValueFactory<>("photo"));
        colPhoto.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(34);
                imageView.setFitHeight(34);
                imageView.setPreserveRatio(false);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Image image = resolvePhoto(item);
                if (image != null) {
                    imageView.setImage(image);
                    setGraphic(imageView);
                } else {
                    setGraphic(null);
                }
            }
        });
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTelephone() != null ? data.getValue().getTelephone() : "-"
        ));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isBanned() ? "banni" : data.getValue().getStatut()
        ));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getIdRole() == 1 ? "Admin" : "Utilisateur"
        ));
        colBanUntil.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBanUntil() != null ? data.getValue().getBanUntil().format(banFormatter) : "-"
        ));

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateFormFromSelection(newValue));
        refreshAll();
    }

    @FXML
    public void handleSearch() {
        try {
            String keyword = searchField.getText().trim();
            List<Utilisateur> users = keyword.isEmpty() ? utilisateurService.select() : utilisateurService.search(keyword);
            populateTable(users);
            updateStats(utilisateurService.select());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleUpdate() {
        try {
            Utilisateur selected = requireSelection();
            Utilisateur user = buildUserFromForm(selected);
            if (utilisateurService.emailExistsForAnotherUser(user.getEmail(), user.getId())) {
                showWarning("Email deja utilise par un autre utilisateur.");
                return;
            }

            utilisateurService.update(user);
            selectionStatusLabel.setText("Utilisateur mis a jour.");
            refreshAll();
            restoreSelection(user.getId());
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        try {
            Utilisateur selected = requireSelection();
            utilisateurService.delete(selected.getId());
            selectionStatusLabel.setText("Utilisateur supprime.");
            refreshAll();
            clearForm();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleBan() {
        try {
            Utilisateur selected = requireSelection();
            int amount = banDurationSpinner.getValue();
            String unit = banUnitBox.getValue();
            LocalDateTime banUntil = LocalDateTime.now();

            if ("jours".equals(unit)) {
                banUntil = banUntil.plusDays(amount);
            } else if ("semaines".equals(unit)) {
                banUntil = banUntil.plusWeeks(amount);
            } else if ("mois".equals(unit)) {
                banUntil = banUntil.plusMonths(amount);
            }

            selected.setBanUntil(banUntil);
            utilisateurService.update(selected);
            selectionStatusLabel.setText("Utilisateur banni jusqu'au " + banUntil.format(banFormatter) + ".");
            refreshAll();
            restoreSelection(selected.getId());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleUnban() {
        try {
            Utilisateur selected = requireSelection();
            selected.setBanUntil(null);
            utilisateurService.update(selected);
            selectionStatusLabel.setText("Utilisateur debanni.");
            refreshAll();
            restoreSelection(selected.getId());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        clearForm();
    }

    @FXML
    public void handleOpenMainMenu(ActionEvent event) {
        try {
            loadScene(event, "/user-home.fxml", "Ardhi - Accueil");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleOpenTerrain(ActionEvent event) {
        try {
            loadScene(event, "/AfficherTerrains.fxml", "Ardhi - Gestion Terrains");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleOpenPlante(ActionEvent event) {
        try {
            loadScene(event, "/AfficherPlantes.fxml", "Ardhi - Gestion Plantes");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            SessionManager.clear();
            loadScene(event, "/signin.fxml", "Ardhi - Authentification");
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    private void refreshAll() {
        try {
            List<Utilisateur> users = utilisateurService.select();
            populateTable(users);
            updateStats(users);
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void populateTable(List<Utilisateur> users) {
        ObservableList<Utilisateur> data = FXCollections.observableArrayList(users);
        userTable.setItems(data);
    }

    private void updateStats(List<Utilisateur> users) {
        long total = users.size();
        long admins = users.stream().filter(user -> user.getIdRole() == 1).count();
        long banned = users.stream().filter(Utilisateur::isBanned).count();
        long active = users.stream().filter(user -> "actif".equalsIgnoreCase(user.getStatut()) && !user.isBanned()).count();

        totalUsersLabel.setText(String.valueOf(total));
        adminUsersLabel.setText(String.valueOf(admins));
        activeUsersLabel.setText(String.valueOf(active));
        bannedUsersLabel.setText(String.valueOf(banned));
    }

    private void populateFormFromSelection(Utilisateur selected) {
        if (selected == null) {
            return;
        }

        idField.setText(String.valueOf(selected.getId()));
        nomField.setText(selected.getNom());
        prenomField.setText(selected.getPrenom());
        emailField.setText(selected.getEmail());
        passwordField.setText(selected.getMotDePasse());
        telephoneField.setText(selected.getTelephone() != null ? selected.getTelephone() : "");
        statutBox.setValue(selected.getStatut());
        roleBox.setValue(selected.getIdRole());
        selectionStatusLabel.setText(selected.isBanned()
                ? "Banni jusqu'au " + selected.getBanUntil().format(banFormatter)
                : "Utilisateur selectionne.");
    }

    private Utilisateur buildUserFromForm(Utilisateur selected) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String statut = statutBox.getValue();
        Integer role = roleBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || statut == null || role == null) {
            throw new IllegalArgumentException("Tous les champs principaux sont obligatoires.");
        }
        if (!nom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$") || !prenom.matches("^[A-Za-zÀ-ÿ\\s-]{2,}$")) {
            throw new IllegalArgumentException("Nom/Prenom invalides.");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Email invalide. Utilisez le format nom@domaine.tld.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mot de passe trop court.");
        }
        if (!telephone.isEmpty() && !telephone.matches("^[0-9]{8,15}$")) {
            throw new IllegalArgumentException("Telephone invalide.");
        }

        Utilisateur user = new Utilisateur();
        user.setId(selected.getId());
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMotDePasse(password);
        user.setTelephone(telephone.isBlank() ? null : telephone);
        user.setStatut(statut);
        user.setIdRole(role);
        user.setDateInscription(selected.getDateInscription());
        user.setPhoto(selected.getPhoto());
        user.setBanUntil(selected.getBanUntil());
        return user;
    }

    private Utilisateur requireSelection() {
        Utilisateur selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Selectionnez un utilisateur.");
        }
        return selected;
    }

    private void restoreSelection(int userId) {
        for (Utilisateur user : userTable.getItems()) {
            if (user.getId() == userId) {
                userTable.getSelectionModel().select(user);
                populateFormFromSelection(user);
                return;
            }
        }
    }

    private Image resolvePhoto(String path) {
        try {
            if (path == null || path.isBlank()) {
                return null;
            }
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")) {
                return new Image(path, true);
            }

            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void clearForm() {
        idField.clear();
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telephoneField.clear();
        statutBox.setValue(null);
        roleBox.setValue(null);
        userTable.getSelectionModel().clearSelection();
        selectionStatusLabel.setText("");
    }

    private void loadScene(ActionEvent event, String resource, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resource));
        Scene scene = new Scene(root, 1150, 700);
        scene.getStylesheets().add(getClass().getResource("/ardhi.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
