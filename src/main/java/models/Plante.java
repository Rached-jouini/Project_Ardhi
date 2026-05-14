package models;

public class Plante {
    private int id;
    private String nom;
    private String type;
    private String date_debut_plantation;
    private String date_fin_plantation;
    private String besoin_eau;
    private String description;

    public Plante() {
    }

    public Plante(String nom, String type, String date_debut_plantation, String date_fin_plantation, String besoin_eau, String description) {
        this.nom = nom;
        this.type = type;
        this.date_debut_plantation = date_debut_plantation;
        this.date_fin_plantation = date_fin_plantation;
        this.besoin_eau = besoin_eau;
        this.description = description;
    }

    public Plante(int id, String nom, String type, String date_debut_plantation, String date_fin_plantation, String besoin_eau, String description) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.date_debut_plantation = date_debut_plantation;
        this.date_fin_plantation = date_fin_plantation;
        this.besoin_eau = besoin_eau;
        this.description = description;
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

    public String getDate_debut_plantation() {
        return date_debut_plantation;
    }

    public void setDate_debut_plantation(String date_debut_plantation) {
        this.date_debut_plantation = date_debut_plantation;
    }

    public String getDate_fin_plantation() {
        return date_fin_plantation;
    }

    public void setDate_fin_plantation(String date_fin_plantation) {
        this.date_fin_plantation = date_fin_plantation;
    }

    public String getBesoin_eau() {
        return besoin_eau;
    }

    public void setBesoin_eau(String besoin_eau) {
        this.besoin_eau = besoin_eau;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Plante{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", date_debut_plantation='" + date_debut_plantation + '\'' +
                ", date_fin_plantation='" + date_fin_plantation + '\'' +
                ", besoin_eau='" + besoin_eau + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
