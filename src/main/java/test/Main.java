package test;

import models.Evenement;
import services.EvenementService;
import utils.MyDataBase;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Connection connection = MyDataBase.getInstance().getConnection();
        Connection connection1 = MyDataBase.getInstance().getConnection();
        System.out.println(connection);
        System.out.println(connection1);

        EvenementService evenementService = new EvenementService();

        try {
            // -- AJOUTER un événement --
            evenementService.add(new Evenement("Festival du Blé", "Fête annuelle de la récolte", LocalDate.of(2025, 7, 15),"tunis", "Festival", "Céréales", 200, "disponible"));

            // -- MODIFIER un événement --
            // evenementService.update(new Evenement(1, "Nouveau Nom", "Nouvelle desc", LocalDate.of(2025, 8, 20), "Conférence", "Maraîchage", 100, "complet"));

            // -- SUPPRIMER un événement --
            // evenementService.delete(1);

            // -- AFFICHER tous les événements --
            System.out.println(evenementService.select());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}