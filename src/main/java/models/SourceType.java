package models;

public class SourceType {
    private int id;
    private String nom;

    public SourceType() {
    }

    public SourceType(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public SourceType(String nom) {
        this.nom = nom;
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
    @Override
    public String toString() {
        return "SourceType{" + "id=" + id + ", nom=" + nom + '}';
    }
}
