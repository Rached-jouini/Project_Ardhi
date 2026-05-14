package models;



import java.time.LocalDate;

public class Evenement {
    private int id;
    private String nom;
    private String description;
    private LocalDate date;
    private String lieu;
    private String type;
    private String culture_concernee;
    private int nombre_places;
    private String statut;

    public Evenement(int id, String nom, String description, LocalDate date, String lieu, String type, String culture_concernee, int nombre_places, String statut) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.lieu= lieu;
        this.type = type;
        this.culture_concernee = culture_concernee;
        this.nombre_places = nombre_places;
        this.statut = statut;
    }


    public Evenement(String nom, String description, LocalDate date, String lieu, String type, String culture_concernee, int nombre_places, String statut) {
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
        this.type = type;
        this.culture_concernee = culture_concernee;
        this.nombre_places = nombre_places;
        this.statut = statut;
    }

    public Evenement() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLieu() {
        return lieu;
    }



    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCulture_concernee() {
        return culture_concernee;
    }

    public void setCulture_concernee(String culture_concernee) {
        this.culture_concernee = culture_concernee;
    }

    public int getNombre_places() {
        return nombre_places;
    }

    public void setNombre_places(int nombre_places) {
        this.nombre_places = nombre_places;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", lieu='" + lieu + '\'' +
                ", type='" + type + '\'' +
                ", culture_concernee='" + culture_concernee + '\'' +
                ", nombre_places=" + nombre_places +
                ", statut='" + statut + '\'' +
                '}';
    }
}
