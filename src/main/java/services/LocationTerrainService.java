package services;

import models.LocationTerrain;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationTerrainService implements ardhi<LocationTerrain> {

    private Connection conn;

    public LocationTerrainService() {
        this.conn = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(LocationTerrain lt) throws SQLException {
        String SQL = "INSERT INTO location_terrain (id_terrain, id_utilisateur, date_debut, date_fin, prix_mensuel, prix_total) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, lt.getId_terrain());
            pstmt.setInt(2, lt.getId_utilisateur());
            pstmt.setString(3, lt.getDate_debut());
            pstmt.setString(4, lt.getDate_fin());
            pstmt.setFloat(5, lt.getPrix_mensuel());
            pstmt.setFloat(6, lt.getPrix_total());
            pstmt.executeUpdate();
            
            // Mettre à jour le statut du terrain à "loue"
            updateStatutTerrain(lt.getId_terrain(), "loue");
        }
    }

    @Override
    public void update(LocationTerrain lt) throws SQLException {
        String SQL = "UPDATE location_terrain SET id_terrain=?, id_utilisateur=?, date_debut=?, date_fin=?, prix_mensuel=?, prix_total=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, lt.getId_terrain());
            pstmt.setInt(2, lt.getId_utilisateur());
            pstmt.setString(3, lt.getDate_debut());
            pstmt.setString(4, lt.getDate_fin());
            pstmt.setFloat(5, lt.getPrix_mensuel());
            pstmt.setFloat(6, lt.getPrix_total());
            pstmt.setInt(7, lt.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM location_terrain WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<LocationTerrain> select() throws SQLException {
        List<LocationTerrain> locations = new ArrayList<>();
        String SQL = "SELECT * FROM location_terrain";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                LocationTerrain lt = new LocationTerrain();
                lt.setId(rs.getInt("id"));
                lt.setId_terrain(rs.getInt("id_terrain"));
                lt.setId_utilisateur(rs.getInt("id_utilisateur"));
                lt.setDate_debut(rs.getString("date_debut"));
                lt.setDate_fin(rs.getString("date_fin"));
                lt.setPrix_mensuel(rs.getFloat("prix_mensuel"));
                lt.setPrix_total(rs.getFloat("prix_total"));
                locations.add(lt);
            }
        }
        return locations;
    }

    private void updateStatutTerrain(int idTerrain, String statut) throws SQLException {
        String SQL = "UPDATE terrain SET statut = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, statut);
            pstmt.setInt(2, idTerrain);
            pstmt.executeUpdate();
        }
    }
}
