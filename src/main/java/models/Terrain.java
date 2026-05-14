package models;

public class Terrain {
    private int id;
    private float superficie;
    private String region;
    private String type_sol;
    private String statut;
    private String gps_coordinates;
    private float prix_location;
    private String coordonnees_gps;

    public Terrain(int id, float superficie, String region, String type_sol, String statut, String gps_coordinates, float prix_location, String coordonnees_gps) {
        this.id = id;
        this.superficie = superficie;
        this.region = region;
        this.type_sol = type_sol;
        this.statut = statut;
        this.gps_coordinates = gps_coordinates;
        this.prix_location = prix_location;
        this.coordonnees_gps = coordonnees_gps;
    }

    public Terrain(float superficie, String region, String type_sol, String statut, String gps_coordinates, float prix_location, String coordonnees_gps) {
        this.superficie = superficie;
        this.region = region;
        this.type_sol = type_sol;
        this.statut = statut;
        this.gps_coordinates = gps_coordinates;
        this.prix_location = prix_location;
        this.coordonnees_gps = coordonnees_gps;
    }

    // Nouveau constructeur pour compatibilité ascendante (5 arguments)
    public Terrain(float superficie, String region, String type_sol, String statut, float prix_location) {
        this(superficie, region, type_sol, statut, null, prix_location, null);
    }

    // Nouveau constructeur pour compatibilité (6 arguments sans ID)
    public Terrain(float superficie, String region, String type_sol, String statut, float prix_location, String coordonnees_gps) {
        this(superficie, region, type_sol, statut, null, prix_location, coordonnees_gps);
    }

    // Nouveau constructeur pour compatibilité ascendante (6 arguments avec ID)
    public Terrain(int id, float superficie, String region, String type_sol, String statut, float prix_location) {
        this(id, superficie, region, type_sol, statut, null, prix_location, null);
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

    public String getGps_coordinates() {
        return gps_coordinates;
    }

    public void setGps_coordinates(String gps_coordinates) {
        this.gps_coordinates = gps_coordinates;
    }

    public float getPrix_location() {
        return prix_location;
    }

    public void setPrix_location(float prix_location) {
        this.prix_location = prix_location;
    }

    public String getCoordonnees_gps() {
        return coordonnees_gps;
    }

    public void setCoordonnees_gps(String coordonnees_gps) {
        this.coordonnees_gps = coordonnees_gps;
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
                ", gps_coordinates='" + gps_coordinates + '\'' +
                ", prix_location=" + prix_location +
                ", coordonnees_gps='" + coordonnees_gps + '\'' +
                '}';
    }
}
