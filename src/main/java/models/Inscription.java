package models;

import java.time.LocalDateTime;

public class Inscription {
    private int id;
    private int id_evenement;
    private String nom;
    private String email;
    private LocalDateTime date_inscription;
    private String statut_participation; // En attente, Présent, Absent

    public Inscription() {}

    public Inscription(int id_evenement, String nom, String email) {
        this.id_evenement = id_evenement;
        this.nom = nom;
        this.email = email;
        this.date_inscription = LocalDateTime.now();
        this.statut_participation = "En attente";
    }

    public Inscription(int id, int id_evenement, String nom, String email, LocalDateTime date_inscription, String statut_participation) {
        this.id = id;
        this.id_evenement = id_evenement;
        this.nom = nom;
        this.email = email;
        this.date_inscription = date_inscription;
        this.statut_participation = statut_participation;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getId_evenement() { return id_evenement; }
    public void setId_evenement(int id_evenement) { this.id_evenement = id_evenement; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDate_inscription() { return date_inscription; }
    public void setDate_inscription(LocalDateTime date_inscription) { this.date_inscription = date_inscription; }

    public String getStatut_participation() { return statut_participation; }
    public void setStatut_participation(String statut_participation) { this.statut_participation = statut_participation; }
}
