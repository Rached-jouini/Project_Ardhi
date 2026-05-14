package models;

public class Wallet {
    private int id;
    private String nom;
    private double budgetInitial;
    private double budgetActuel;
    private String description;
    private String couleur;
    private double limiteDecouvert;

    public Wallet() {}

    public Wallet(int id, String nom, double budgetInitial, double budgetActuel, String description, String couleur) {
        this.id = id;
        this.nom = nom;
        this.budgetInitial = budgetInitial;
        this.budgetActuel = budgetActuel;
        this.description = description;
        this.couleur = couleur;
    }

    public Wallet(String nom, double budgetInitial, double budgetActuel, String description, String couleur) {
        this.nom = nom;
        this.budgetInitial = budgetInitial;
        this.budgetActuel = budgetActuel;
        this.description = description;
        this.couleur = couleur;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getBudgetInitial() { return budgetInitial; }
    public void setBudgetInitial(double budgetInitial) { this.budgetInitial = budgetInitial; }

    public double getBudgetActuel() { return budgetActuel; }
    public void setBudgetActuel(double budgetActuel) { this.budgetActuel = budgetActuel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public double getLimiteDecouvert() { return limiteDecouvert; }
    public void setLimiteDecouvert(double limiteDecouvert) { this.limiteDecouvert = limiteDecouvert; }

    @Override
    public String toString() {
        return nom + " (" + budgetActuel + " DT)";
    }
}
