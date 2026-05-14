package services;

import models.SourceFinancement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SourceFinancementService {

    private Connection connection;

    public SourceFinancementService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void add(SourceFinancement sf) throws SQLException {
        String sql = "INSERT INTO source_financement (nom, type, montant, description) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, sf.getNom());
        ps.setString(2, sf.getType());
        ps.setDouble(3, sf.getMontant());
        ps.setString(4, sf.getDescription());
        ps.executeUpdate();
    }

    public void update(SourceFinancement sf) throws SQLException {
        String sql = "UPDATE source_financement SET nom=?, type=?, montant=?, description=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, sf.getNom());
        ps.setString(2, sf.getType());
        ps.setDouble(3, sf.getMontant());
        ps.setString(4, sf.getDescription());
        ps.setInt(5, sf.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM source_financement WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<SourceFinancement> select() throws SQLException {
        List<SourceFinancement> list = new ArrayList<>();
        String sql = "SELECT * FROM source_financement";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new SourceFinancement(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getDouble("montant"),
                    rs.getString("description")
            ));
        }
        return list;
    }

    // METIER AVANCE : Mise à jour du solde d'une source (Carte/Banque)
    public void updateMontant(int id, double nouveauMontant) throws SQLException {
        String sql = "UPDATE source_financement SET montant = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDouble(1, nouveauMontant);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    // METIER AVANCE : Calcul automatique du capital total
    public double calculerCapitalTotal() throws SQLException {
        String sql = "SELECT SUM(montant) FROM source_financement";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0;
    }
}
