package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import java.io.IOException;

public class FinanceAdminPanelController {

    @FXML
    void ouvrirConfigCapital(MouseEvent event) throws IOException {
        MainLayoutController.getInstance().loadPage("/ConfigCapitale.fxml");
    }

    @FXML
    void ouvrirGestionWallets(MouseEvent event) throws IOException {
        MainLayoutController.getInstance().loadPage("/GestionWallets.fxml");
    }

    @FXML
    void retourDashboard(ActionEvent event) throws IOException {
        MainLayoutController.getInstance().loadPage("/FinanceDashboard.fxml");
    }
}
