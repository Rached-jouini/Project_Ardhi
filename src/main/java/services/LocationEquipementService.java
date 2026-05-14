package services;

import models.LocationEquipement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationEquipementService implements ardhi<LocationEquipement> {
    Connection connection;

    public LocationEquipementService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(LocationEquipement location) throws SQLException {
        String sql = "insert into location_equipement (id_equipement, id_utilisateur, date_location, date_retour_prevue, date_retour_reelle, statut, etat_retour, cout_total) values (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, location.getId_equipement());
        preparedStatement.setInt(2, location.getId_utilisateur());
        preparedStatement.setDate(3, Date.valueOf(location.getDate_location()));
        preparedStatement.setDate(4, Date.valueOf(location.getDate_retour_prevue()));
        preparedStatement.setDate(5, location.getDate_retour_reelle() != null ? Date.valueOf(location.getDate_retour_reelle()) : null);
        preparedStatement.setString(6, location.getStatut());
        preparedStatement.setString(7, location.getEtat_retour());
        preparedStatement.setDouble(8, location.getCout_total());
        preparedStatement.executeUpdate();
    }

    @Override
    public void update(LocationEquipement location) throws SQLException {
        String sql = "update location_equipement set id_equipement=?, id_utilisateur=?, date_location=?, date_retour_prevue=?, date_retour_reelle=?, statut=?, etat_retour=?, cout_total=? where id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, location.getId_equipement());
        preparedStatement.setInt(2, location.getId_utilisateur());
        preparedStatement.setDate(3, Date.valueOf(location.getDate_location()));
        preparedStatement.setDate(4, Date.valueOf(location.getDate_retour_prevue()));
        preparedStatement.setDate(5, location.getDate_retour_reelle() != null ? Date.valueOf(location.getDate_retour_reelle()) : null);
        preparedStatement.setString(6, location.getStatut());
        preparedStatement.setString(7, location.getEtat_retour());
        preparedStatement.setDouble(8, location.getCout_total());
        preparedStatement.setInt(9, location.getId());
        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "delete from location_equipement where id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<LocationEquipement> select() throws SQLException {
        List<LocationEquipement> locations = new ArrayList<>();
        String sql = "select * from location_equipement";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            LocationEquipement location = new LocationEquipement();
            location.setId(resultSet.getInt("id"));
            location.setId_equipement(resultSet.getInt("id_equipement"));
            location.setId_utilisateur(resultSet.getInt("id_utilisateur"));

            Date dateLocation = resultSet.getDate("date_location");
            if (dateLocation != null) {
                location.setDate_location(dateLocation.toLocalDate());
            }

            Date dateRetourPrevue = resultSet.getDate("date_retour_prevue");
            if (dateRetourPrevue != null) {
                location.setDate_retour_prevue(dateRetourPrevue.toLocalDate());
            }

            Date dateRetourReelle = resultSet.getDate("date_retour_reelle");
            if (dateRetourReelle != null) {
                location.setDate_retour_reelle(dateRetourReelle.toLocalDate());
            }

            location.setStatut(resultSet.getString("statut"));
            location.setEtat_retour(resultSet.getString("etat_retour"));
            location.setCout_total(resultSet.getDouble("cout_total"));
            locations.add(location);
        }
        return locations;
    }

    public double getTotalRevenue(int idEquipement) throws SQLException {
        String sql = "SELECT SUM(cout_total) FROM location_equipement WHERE id_equipement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idEquipement);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }
}
