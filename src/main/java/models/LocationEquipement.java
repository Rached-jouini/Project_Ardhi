package models;

import java.time.LocalDate;

public class LocationEquipement {
    private int id;
    private int id_equipement;
    private int id_utilisateur;
    private LocalDate date_location;
    private LocalDate date_retour_prevue;
    private LocalDate date_retour_reelle;
    private String statut;
    private String etat_retour;
    private double cout_total;

    public LocationEquipement() {
    }

    public LocationEquipement(int id, int id_equipement, int id_utilisateur,
                              LocalDate date_location, LocalDate date_retour_prevue,
                              LocalDate date_retour_reelle, String statut,
                              String etat_retour, double cout_total) {
        this.id = id;
        this.id_equipement = id_equipement;
        this.id_utilisateur = id_utilisateur;
        this.date_location = date_location;
        this.date_retour_prevue = date_retour_prevue;
        this.date_retour_reelle = date_retour_reelle;
        this.statut = statut;
        this.etat_retour = etat_retour;
        this.cout_total = cout_total;
    }

    public LocationEquipement(int id_equipement, int id_utilisateur,
                              LocalDate date_location, LocalDate date_retour_prevue,
                              LocalDate date_retour_reelle, String statut,
                              String etat_retour, double cout_total) {
        this.id_equipement = id_equipement;
        this.id_utilisateur = id_utilisateur;
        this.date_location = date_location;
        this.date_retour_prevue = date_retour_prevue;
        this.date_retour_reelle = date_retour_reelle;
        this.statut = statut;
        this.etat_retour = etat_retour;
        this.cout_total = cout_total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_equipement() {
        return id_equipement;
    }

    public void setId_equipement(int id_equipement) {
        this.id_equipement = id_equipement;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public LocalDate getDate_location() {
        return date_location;
    }

    public void setDate_location(LocalDate date_location) {
        this.date_location = date_location;
    }

    public LocalDate getDate_retour_prevue() {
        return date_retour_prevue;
    }

    public void setDate_retour_prevue(LocalDate date_retour_prevue) {
        this.date_retour_prevue = date_retour_prevue;
    }

    public LocalDate getDate_retour_reelle() {
        return date_retour_reelle;
    }

    public void setDate_retour_reelle(LocalDate date_retour_reelle) {
        this.date_retour_reelle = date_retour_reelle;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getEtat_retour() {
        return etat_retour;
    }

    public void setEtat_retour(String etat_retour) {
        this.etat_retour = etat_retour;
    }

    public double getCout_total() {
        return cout_total;
    }

    public void setCout_total(double cout_total) {
        this.cout_total = cout_total;
    }

    @Override
    public String toString() {
        return "LocationEquipement{" +
                "id=" + id +
                ", id_equipement=" + id_equipement +
                ", id_utilisateur=" + id_utilisateur +
                ", date_location=" + date_location +
                ", date_retour_prevue=" + date_retour_prevue +
                ", date_retour_reelle=" + date_retour_reelle +
                ", statut='" + statut + '\'' +
                ", etat_retour='" + etat_retour + '\'' +
                ", cout_total=" + cout_total +
                '}';
    }
}
