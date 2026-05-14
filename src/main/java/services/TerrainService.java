package services;

import models.Terrain;
import utils.MyDataBase; // Importation de TA classe utilitaire
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerrainService implements ardhi<Terrain> {

    private Connection conn;

    public TerrainService() {
        // Ajusté pour utiliser MyDataBase.getInstance().getConnection()
        this.conn = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Terrain t) throws SQLException {
        String SQL = "INSERT INTO terrain (superficie, region, type_sol, statut, gps_coordinates, prix_location, coordonnees_gps) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setFloat(1, t.getSuperficie());
            pstmt.setString(2, t.getRegion());
            pstmt.setString(3, t.getType_sol());
            pstmt.setString(4, t.getStatut());
            pstmt.setString(5, t.getGps_coordinates());
            pstmt.setFloat(6, t.getPrix_location());
            pstmt.setString(7, t.getCoordonnees_gps());
            pstmt.executeUpdate();
            System.out.println("Terrain ajouté !");
        }
    }

    @Override
    public void update(Terrain t) throws SQLException {
        String SQL = "UPDATE terrain SET superficie=?, region=?, type_sol=?, statut=?, gps_coordinates=?, prix_location=?, coordonnees_gps=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setFloat(1, t.getSuperficie());
            pstmt.setString(2, t.getRegion());
            pstmt.setString(3, t.getType_sol());
            pstmt.setString(4, t.getStatut());
            pstmt.setString(5, t.getGps_coordinates());
            pstmt.setFloat(6, t.getPrix_location());
            pstmt.setString(7, t.getCoordonnees_gps());
            pstmt.setInt(8, t.getId());
            pstmt.executeUpdate();
            System.out.println("Terrain mis à jour !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM terrain WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Terrain supprimé !");
        }
    }

    @Override
    public List<Terrain> select() throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String SQL = "SELECT * FROM terrain";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                Terrain t = new Terrain();
                t.setId(rs.getInt("id"));
                t.setSuperficie(rs.getFloat("superficie"));
                t.setRegion(rs.getString("region"));
                t.setType_sol(rs.getString("type_sol"));
                t.setStatut(rs.getString("statut"));
                t.setGps_coordinates(rs.getString("gps_coordinates"));
                t.setPrix_location(rs.getFloat("prix_location"));
                t.setCoordonnees_gps(rs.getString("coordonnees_gps"));
                terrains.add(t);
            }
        }
        return terrains;
    }
}