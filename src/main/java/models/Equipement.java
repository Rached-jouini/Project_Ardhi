package models;

import java.util.ArrayList;
import java.util.List;

public class Equipement {
    private int id;
    private String nom;
    private String type;
    private int quantite_totale;
    private int quantite_dispo;
    private String etat;
    private String photo;
    private double prix_location_jour;
    private java.time.LocalDate date_mise_en_service;
    private int duree_vie_annees;
    private List<String> photos = new ArrayList<>();

    public Equipement() {
    }

    public Equipement(int id, String nom, String type, int quantite_totale,
                      int quantite_dispo, String etat, String photo, double prix_location_jour) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.quantite_totale = quantite_totale;
        this.quantite_dispo = quantite_dispo;
        this.etat = etat;
        this.photo = photo;
        this.prix_location_jour = prix_location_jour;
    }

    public Equipement(String nom, String type, int quantite_totale,
                      int quantite_dispo, String etat, String photo, double prix_location_jour) {
        this.nom = nom;
        this.type = type;
        this.quantite_totale = quantite_totale;
        this.quantite_dispo = quantite_dispo;
        this.etat = etat;
        this.photo = photo;
        this.prix_location_jour = prix_location_jour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantite_totale() {
        return quantite_totale;
    }

    public void setQuantite_totale(int quantite_totale) {
        this.quantite_totale = quantite_totale;
    }

    public int getQuantite_dispo() {
        return quantite_dispo;
    }

    public void setQuantite_dispo(int quantite_dispo) {
        this.quantite_dispo = quantite_dispo;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public double getPrix_location_jour() {
        return prix_location_jour;
    }

    public void setPrix_location_jour(double prix_location_jour) {
        this.prix_location_jour = prix_location_jour;
    }

    public java.time.LocalDate getDate_mise_en_service() { return date_mise_en_service; }
    public void setDate_mise_en_service(java.time.LocalDate d) { this.date_mise_en_service = d; }

    public int getDuree_vie_annees() { return duree_vie_annees; }
    public void setDuree_vie_annees(int d) { this.duree_vie_annees = d; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    /**
     * Calcule l'age en années depuis la mise en service.
     * Retourne -1 si la date n'est pas définie.
     */
    public int getAge() {
        if (date_mise_en_service == null) return -1;
        return (int) java.time.temporal.ChronoUnit.YEARS.between(date_mise_en_service, java.time.LocalDate.now());
    }

    /**
     * Retourne true si l'équipement a dépassé sa durée de vie.
     */
    public boolean doitEtreRemplace() {
        if (date_mise_en_service == null || duree_vie_annees <= 0) return false;
        return getAge() >= duree_vie_annees;
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", quantite_totale=" + quantite_totale +
                ", quantite_dispo=" + quantite_dispo +
                ", etat='" + etat + '\'' +
                ", photo='" + photo + '\'' +
                ", prix_location_jour=" + prix_location_jour +
                '}';
    }
}