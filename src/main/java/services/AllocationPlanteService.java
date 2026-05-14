package services;

import models.AllocationPlante;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllocationPlanteService implements ardhi<AllocationPlante> {

    private Connection conn;

    public AllocationPlanteService() {
        this.conn = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(AllocationPlante ap) throws SQLException {
        String SQL = "INSERT INTO allocation_plante (id_plante, id_terrain, date_allocation) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, ap.getId_plante());
            pstmt.setInt(2, ap.getId_terrain());
            pstmt.setString(3, ap.getDate_allocation());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(AllocationPlante ap) throws SQLException {
        String SQL = "UPDATE allocation_plante SET id_plante=?, id_terrain=?, date_allocation=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, ap.getId_plante());
            pstmt.setInt(2, ap.getId_terrain());
            pstmt.setString(3, ap.getDate_allocation());
            pstmt.setInt(4, ap.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM allocation_plante WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<AllocationPlante> select() throws SQLException {
        List<AllocationPlante> allocations = new ArrayList<>();
        String SQL = "SELECT * FROM allocation_plante";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                AllocationPlante ap = new AllocationPlante(
                    rs.getInt("id"),
                    rs.getInt("id_plante"),
                    rs.getInt("id_terrain"),
                    rs.getString("date_allocation")
                );
                allocations.add(ap);
            }
        }
        return allocations;
    }
}
