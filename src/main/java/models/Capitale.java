package models;

import java.time.LocalDate;

public class Capitale {
    private int id;
    private double montant_initial;
    private double montant_actuel;
    private String devise;
    private LocalDate date_creation;
    private String description;

    public Capitale() {
    }

    public Capitale(int id, double montant_initial, double montant_actuel, String devise, LocalDate date_creation, String description) {
        this.id = id;
        this.montant_initial = montant_initial;
        this.montant_actuel = montant_actuel;
        this.devise = devise;
        this.date_creation = date_creation;
        this.description = description;
    }

    public Capitale(double montant_initial, double montant_actuel, String devise, LocalDate date_creation, String description) {
        this.montant_initial = montant_initial;
        this.montant_actuel = montant_actuel;
        this.devise = devise;
        this.date_creation = date_creation;
        this.description = description;
    }

    public int getId() {
        return id ;
    }
    public void setId(int id) {
        this.id = id;
    }

    public double getMontant_initial() {
        return montant_initial;
    }

    public void setMontant_initial(double montant_initial) {
        this.montant_initial = montant_initial;
    }

    public double getMontant_actuel() {
        return montant_actuel;
    }

    public void setMontant_actuel(double montant_actuel) {
        this.montant_actuel = montant_actuel;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public LocalDate getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDate date_creation) {
        this.date_creation = date_creation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Capitale{" + "id=" + id
                + ", montant_initial=" + montant_initial
                + ", montant_actuel=" + montant_actuel
                + ", devise=" + devise
                + ", date_creation=" + date_creation
                + ", description=" + description + '}';
    }
}
