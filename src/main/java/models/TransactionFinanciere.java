package models;

import java.time.LocalDate;

public class TransactionFinanciere {
    private int id;
    private String type; // 'revenu' ou 'depense'
    private double montant;
    private LocalDate date_operation;
    private int source_type_id;
    private int source_id;
    private String description;
    private Integer wallet_id;
    private String modePaiement;

    public TransactionFinanciere() {
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public Integer getWalletId() {
        return wallet_id;
    }

    public void setWalletId(Integer wallet_id) {
        this.wallet_id = wallet_id;
    }

    public TransactionFinanciere(LocalDate date_operation, int id, String type, double montant, int source_type_id, int source_id, String description) {
        this.date_operation = date_operation;
        this.id = id;
        this.type = type;
        this.montant = montant;
        this.source_type_id = source_type_id;
        this.source_id = source_id;
        this.description = description;
    }

    public TransactionFinanciere(String type, double montant, LocalDate date_operation, int source_type_id, String description, int source_id) {
        this.type = type;
        this.montant = montant;
        this.date_operation = date_operation;
        this.source_type_id = source_type_id;
        this.description = description;
        this.source_id = source_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDate getDate_operation() {
        return date_operation;
    }

    public void setDate_operation(LocalDate date_operation) {
        this.date_operation = date_operation;
    }

    public int getSource_type_id() {
        return source_type_id;
    }

    public void setSource_type_id(int source_type_id) {
        this.source_type_id = source_type_id;
    }

    public int getSource_id() {
        return source_id;
    }

    public void setSource_id(int source_id) {
        this.source_id = source_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Capitale{" + "id=" + id
                + ", type  =" + type
                + ", montant=" + montant
                + ", date_operation=" + date_operation
                + ", source_type_id=" + source_type_id
                + ", source_id=" + source_id
                + ", description=" + description + '}';
    }
}
