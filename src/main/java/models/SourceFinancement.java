package models;

public class SourceFinancement {
    private int id;
    private String nom;
    private String type; // Interne, Externe, Emprunt
    private double montant;
    private String description;

    public SourceFinancement() {
    }

    public SourceFinancement(int id, String nom, String type, double montant, String description) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.montant = montant;
        this.description = description;
    }

    public SourceFinancement(String nom, String type, double montant, String description) {
        this.nom = nom;
        this.type = type;
        this.montant = montant;
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

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SourceFinancement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", montant=" + montant +
                '}';
    }
}
