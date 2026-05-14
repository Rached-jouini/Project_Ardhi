package services;

import models.Wallet;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletService {

    private Connection connection;

    public WalletService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO wallet (nom, budget_initial, budget_actuel, description, couleur, limite_decouvert) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, wallet.getNom());
        ps.setDouble(2, wallet.getBudgetInitial());
        ps.setDouble(3, wallet.getBudgetActuel());
        ps.setString(4, wallet.getDescription());
        ps.setString(5, wallet.getCouleur());
        ps.setDouble(6, wallet.getLimiteDecouvert());
        ps.executeUpdate();
    }

    public List<Wallet> select() throws SQLException {
        List<Wallet> list = new ArrayList<>();
        String sql = "SELECT * FROM wallet";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Wallet w = new Wallet(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getDouble("budget_initial"),
                rs.getDouble("budget_actuel"),
                rs.getString("description"),
                rs.getString("couleur")
            );
            w.setLimiteDecouvert(rs.getDouble("limite_decouvert"));
            list.add(w);
        }
        return list;
    }

    public void update(Wallet wallet) throws SQLException {
        String sql = "UPDATE wallet SET nom=?, budget_initial=?, budget_actuel=?, description=?, couleur=?, limite_decouvert=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, wallet.getNom());
        ps.setDouble(2, wallet.getBudgetInitial());
        ps.setDouble(3, wallet.getBudgetActuel());
        ps.setString(4, wallet.getDescription());
        ps.setString(5, wallet.getCouleur());
        ps.setDouble(6, wallet.getLimiteDecouvert());
        ps.setInt(7, wallet.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM wallet WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // METIER AVANCE : Deduire une depense d'un wallet
    public void deduireDepense(int walletId, double montant) throws SQLException {
        String sql = "UPDATE wallet SET budget_actuel = budget_actuel - ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDouble(1, montant);
        ps.setInt(2, walletId);
        ps.executeUpdate();
    }
}
