package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private LocalDate dateInscription;
    private String statut;
    private int idRole;
    private String photo;
    private LocalDateTime banUntil;

    public Utilisateur() {
    }

    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String telephone,
                       LocalDate dateInscription, String statut, int idRole) {
        this(id, nom, prenom, email, motDePasse, telephone, dateInscription, statut, idRole, null, null);
    }

    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String telephone,
                       LocalDate dateInscription, String statut, int idRole, String photo, LocalDateTime banUntil) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.idRole = idRole;
        this.photo = photo;
        this.banUntil = banUntil;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String telephone,
                       LocalDate dateInscription, String statut, int idRole) {
        this(nom, prenom, email, motDePasse, telephone, dateInscription, statut, idRole, null, null);
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String telephone,
                       LocalDate dateInscription, String statut, int idRole, String photo, LocalDateTime banUntil) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.idRole = idRole;
        this.photo = photo;
        this.banUntil = banUntil;
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdRole() {
        return idRole;
    }

    public void setIdRole(int idRole) {
        this.idRole = idRole;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public LocalDateTime getBanUntil() {
        return banUntil;
    }

    public void setBanUntil(LocalDateTime banUntil) {
        this.banUntil = banUntil;
    }

    public boolean isBanned() {
        return banUntil != null && banUntil.isAfter(LocalDateTime.now());
    }
}
