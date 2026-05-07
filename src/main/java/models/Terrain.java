package models;

public class Terrain {
    int id;
    float superficie;
    String region;
    String type_sol;
    String statut;
    float prix_location;

    public Terrain(int id, float superficie, String region, String type_sol, String statut, float prix_location) {
        this.id = id;
        this.superficie = superficie;
        this.region = region;
        this.type_sol = type_sol;
        this.statut = statut;
        this.prix_location = prix_location;
    }

    public Terrain(float superficie, String region, String type_sol, String statut, float prix_location) {
        this.superficie = superficie;
        this.region = region;
        this.type_sol = type_sol;
        this.statut = statut;
        this.prix_location = prix_location;
    }

    public Terrain() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getSuperficie() {
        return superficie;
    }

    public void setSuperficie(float superficie) {
        this.superficie = superficie;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getType_sol() {
        return type_sol;
    }

    public void setType_sol(String type_sol) {
        this.type_sol = type_sol;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public float getPrix_location() {
        return prix_location;
    }

    public void setPrix_location(float prix_location) {
        this.prix_location = prix_location;
    }

    @Override
    public String
    toString() {
        return "Terrain{" +
                "id=" + id +
                ", superficie=" + superficie +
                ", region='" + region + '\'' +
                ", type_sol='" + type_sol + '\'' +
                ", statut='" + statut + '\'' +
                ", prix_location=" + prix_location +
                '}';
    }
}
