package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Button btnFinance, btnTransactions, btnWallets, btnEquipement, btnSettings, btnAdminEquip;
    
    // Instance statique pour permettre la navigation depuis les autres contrôleurs
    private static MainLayoutController instance;

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        // Charger le dashboard finance au démarrage
        loadPage("/FinanceDashboard.fxml");
    }

    @FXML
    void showFinance(ActionEvent event) {
        loadPage("/FinanceDashboard.fxml");
        updateButtonStyle(btnFinance);
    }

    @FXML
    void showTransactions(ActionEvent event) {
        loadPage("/TransactionsView.fxml");
        updateButtonStyle(btnTransactions);
    }

    @FXML
    void showWallets(ActionEvent event) {
        loadPage("/GestionSources.fxml"); // "Mes Comptes & Cartes"
        updateButtonStyle(btnWallets);
    }



    @FXML
    void showEquipement(ActionEvent event) {
        loadPage("/LocationEquipement.fxml");
        updateButtonStyle(btnEquipement);
    }

    @FXML
    void showAdmin(ActionEvent event) {
        loadPage("/FinanceAdminPanel.fxml"); // Redirection vers le Hub Admin
        updateButtonStyle(btnSettings);
    }

    @FXML
    void showAdminEquip(ActionEvent event) {
        loadPage("/GestionEquipement.fxml"); // Le CRUD équipements de ta capture
        updateButtonStyle(btnAdminEquip);
    }

    private void updateButtonStyle(Button activeBtn) {
        Button[] buttons = {btnFinance, btnTransactions, btnWallets, btnEquipement, btnSettings, btnAdminEquip};
        for (Button btn : buttons) {
            if (btn == null) continue;
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #085041; -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: #10B981; -fx-border-width: 0 0 0 4;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-background-radius: 10; -fx-border-width: 0;");
            }
        }
    }

    // Methode pour changer de page
    public void loadPage(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setContent(Parent root) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(root);
    }
}
