
package test;

import models.Equipement;
import services.EquipementService;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        EquipementService equipementService = new EquipementService();

        try {
            // Ajouter un équipement
            equipementService.add(new Equipement("Perceuse", "Outillage", 5, 3, "bon", "perceuse.jpg", 12.50));

            // Vérifier que l'équipement est bien ajouté
            List<Equipement> equipements = equipementService.select();
            System.out.println("Après ajout : " + equipements);

            // Mettre à jour le premier équipement
            if (!equipements.isEmpty()) {
                Equipement e = equipements.get(0);
                e.setNom("Perceuse électrique");
                equipementService.update(e);
            }

            // Vérifier la mise à jour
            equipements = equipementService.select();
            System.out.println("Après update : " + equipements);

            // Supprimer le premier équipement
            if (!equipements.isEmpty()) {
                equipementService.delete(equipements.get(0).getId());
            }

            //Vérifier la suppression
            equipements = equipementService.select();
            System.out.println("Après delete : " + equipements);

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }

}