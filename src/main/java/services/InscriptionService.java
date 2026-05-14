package services;

import models.Inscription;
import utils.MyDataBase;
import java.sql.*;

public class InscriptionService {
    private Connection connection;

    public InscriptionService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public int add(Inscription ins) throws SQLException {
        // Étape 1 : Créer les colonnes manquantes si elles n'existent pas
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE inscription ADD COLUMN IF NOT EXISTS nom VARCHAR(255)");
            st.executeUpdate("ALTER TABLE inscription ADD COLUMN IF NOT EXISTS email VARCHAR(255)");
            st.executeUpdate("ALTER TABLE inscription ADD COLUMN IF NOT EXISTS statut_participation VARCHAR(50) DEFAULT 'En attente'");
        } catch (Exception e) {}

        // S'assurer que date_inscription est bien de type DATETIME (et non DATE)
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE inscription MODIFY COLUMN date_inscription DATETIME");
        } catch (Exception e) {}

        // Étape 2 : Inscription avec NOW() pour garantir l'heure exacte
        String req = "INSERT INTO inscription (id_evenement, nom, email, date_inscription, statut_participation) VALUES (?, ?, ?, NOW(), 'En attente')";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, ins.getId_evenement());
        ps.setString(2, ins.getNom());
        ps.setString(3, ins.getEmail());
        ps.executeUpdate();
        
        int generatedId = -1;
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
        }
        
        // Mettre à jour le nombre de places
        String updateReq = "UPDATE evenement SET nombre_places = nombre_places - 1 WHERE id = ?";
        PreparedStatement psUpdate = connection.prepareStatement(updateReq);
        psUpdate.setInt(1, ins.getId_evenement());
        psUpdate.executeUpdate();

        // ✅ Insérer dans participantsevenement avec une connexion fraîche et indépendante
        try {
            Connection freshConn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/ardhi?useUnicode=true&characterEncoding=UTF-8",
                "root", ""
            );
            // On laisse MySQL gérer la date automatiquement (DEFAULT CURRENT_TIMESTAMP)
            String insertParticipant = "INSERT INTO participantsevenement (nom_client, email, statut) VALUES (?, ?, 'En attente')";
            PreparedStatement psParticipant = freshConn.prepareStatement(insertParticipant);
            psParticipant.setString(1, ins.getNom());
            psParticipant.setString(2, ins.getEmail());
            int rows = psParticipant.executeUpdate();
            System.out.println("[INSCRIPTION] participantsevenement : " + rows + " ligne(s) insérée(s).");
            freshConn.close();
        } catch (Exception e) {
            System.err.println("[INSCRIPTION] ERREUR participantsevenement : " + e.getMessage());
            e.printStackTrace();
        }

        return generatedId;
    }

    public java.util.List<Inscription> getAll() throws SQLException {
        java.util.List<Inscription> list = new java.util.ArrayList<>();
        String req = "SELECT * FROM inscription ORDER BY id DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Inscription ins = new Inscription(
                rs.getInt("id"),
                rs.getInt("id_evenement"),
                rs.getString("nom"),
                rs.getString("email"),
                rs.getTimestamp("date_inscription").toLocalDateTime(),
                rs.getString("statut_participation")
            );
            list.add(ins);
        }
        return list;
    }

    public void updateStatus(int id, String status) throws SQLException {
        String req = "UPDATE inscription SET statut_participation = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }
}