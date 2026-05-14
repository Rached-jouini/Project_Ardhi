package models;

public class LocationTerrain {
    private int id;
    private int id_terrain;
    private int id_utilisateur;
    private String date_debut;
    private String date_fin;
    private float prix_mensuel;
    private float prix_total;

    public LocationTerrain() {
    }

    public LocationTerrain(int id_terrain, int id_utilisateur, String date_debut, String date_fin, float prix_mensuel, float prix_total) {
        this.id_terrain = id_terrain;
        this.id_utilisateur = id_utilisateur;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.prix_mensuel = prix_mensuel;
        this.prix_total = prix_total;
    }

    public LocationTerrain(int id, int id_terrain, int id_utilisateur, String date_debut, String date_fin, float prix_mensuel, float prix_total) {
        this.id = id;
        this.id_terrain = id_terrain;
        this.id_utilisateur = id_utilisateur;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.prix_mensuel = prix_mensuel;
        this.prix_total = prix_total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_terrain() {
        return id_terrain;
    }

    public void setId_terrain(int id_terrain) {
        this.id_terrain = id_terrain;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public String getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(String date_debut) {
        this.date_debut = date_debut;
    }

    public String getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(String date_fin) {
        this.date_fin = date_fin;
    }

    public float getPrix_mensuel() {
        return prix_mensuel;
    }

    public void setPrix_mensuel(float prix_mensuel) {
        this.prix_mensuel = prix_mensuel;
    }

    public float getPrix_total() {
        return prix_total;
    }

    public void setPrix_total(float prix_total) {
        this.prix_total = prix_total;
    }

    @Override
    public String toString() {
        return "LocationTerrain{" +
                "id=" + id +
                ", id_terrain=" + id_terrain +
                ", id_utilisateur=" + id_utilisateur +
                ", date_debut='" + date_debut + '\'' +
                ", date_fin='" + date_fin + '\'' +
                ", prix_mensuel=" + prix_mensuel +
                ", prix_total=" + prix_total +
                '}';
    }
}
