package services;

import models.Equipement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementService implements ardhi<Equipement> {
    Connection connection;

    public EquipementService() {
        connection = MyDataBase.getInstance().getConnection();
        if (connection != null) {
            try (Statement st = connection.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS equipement_photo (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "id_equipement INT NOT NULL, " +
                        "chemin_photo VARCHAR(255) NOT NULL, " +
                        "FOREIGN KEY (id_equipement) REFERENCES equipement(id) ON DELETE CASCADE" +
                        ")");
            } catch (SQLException e) {
                System.err.println("⚠️ ALERTE SQL : La table equipement_photo n'a pas pu être créée automatiquement.");
                System.err.println("Raison : " + e.getMessage());
            }
        }
    }

    @Override
    public void add(Equipement equipement) throws SQLException {
        String sql = "insert into equipement (nom, type, quantite_totale, quantite_dispo, etat, photo, prix_location_jour, date_mise_en_service, duree_vie_annees) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, equipement.getNom());
        ps.setString(2, equipement.getType());
        ps.setInt(3, equipement.getQuantite_totale());
        ps.setInt(4, equipement.getQuantite_dispo());
        ps.setString(5, equipement.getEtat());
        ps.setString(6, equipement.getPhoto());
        ps.setDouble(7, equipement.getPrix_location_jour());
        ps.setDate(8, equipement.getDate_mise_en_service() != null ? java.sql.Date.valueOf(equipement.getDate_mise_en_service()) : null);
        ps.setInt(9, equipement.getDuree_vie_annees());
        ps.executeUpdate();

        // Récupérer l'ID généré pour insérer les photos
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            int idEq = generatedKeys.getInt(1);
            savePhotos(idEq, equipement.getPhotos());
        }
    }

    private void savePhotos(int idEq, List<String> photos) throws SQLException {
        if (photos == null || photos.isEmpty()) return;
        String sql = "insert into equipement_photo (id_equipement, chemin_photo) values (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (String path : photos) {
            ps.setInt(1, idEq);
            ps.setString(2, path);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    @Override
    public void update(Equipement equipement) throws SQLException {
        String sql = "update equipement set nom=?, type=?, quantite_totale=?, quantite_dispo=?, etat=?, photo=?, prix_location_jour=?, date_mise_en_service=?, duree_vie_annees=? where id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, equipement.getNom());
        ps.setString(2, equipement.getType());
        ps.setInt(3, equipement.getQuantite_totale());
        ps.setInt(4, equipement.getQuantite_dispo());
        ps.setString(5, equipement.getEtat());
        ps.setString(6, equipement.getPhoto());
        ps.setDouble(7, equipement.getPrix_location_jour());
        ps.setDate(8, equipement.getDate_mise_en_service() != null ? java.sql.Date.valueOf(equipement.getDate_mise_en_service()) : null);
        ps.setInt(9, equipement.getDuree_vie_annees());
        ps.setInt(10, equipement.getId());
        ps.executeUpdate();

        // Mettre à jour les photos (on vide et on remet tout)
        String deleteSql = "delete from equipement_photo where id_equipement = ?";
        PreparedStatement deletePs = connection.prepareStatement(deleteSql);
        deletePs.setInt(1, equipement.getId());
        deletePs.executeUpdate();

        savePhotos(equipement.getId(), equipement.getPhotos());
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "delete from equipement where id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Equipement> select() throws SQLException {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "select * from equipement";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            Equipement equipement = new Equipement();
            equipement.setId(resultSet.getInt("id"));
            equipement.setNom(resultSet.getString("nom"));
            equipement.setType(resultSet.getString("type"));
            equipement.setQuantite_totale(resultSet.getInt("quantite_totale"));
            equipement.setQuantite_dispo(resultSet.getInt("quantite_dispo"));
            equipement.setEtat(resultSet.getString("etat"));
            equipement.setPhoto(resultSet.getString("photo"));
            equipement.setPrix_location_jour(resultSet.getDouble("prix_location_jour"));
            // Cycle de vie
            java.sql.Date dms = resultSet.getDate("date_mise_en_service");
            if (dms != null) equipement.setDate_mise_en_service(dms.toLocalDate());
            equipement.setDuree_vie_annees(resultSet.getInt("duree_vie_annees"));
            
            // Charger les photos multiples
            equipement.setPhotos(loadPhotos(equipement.getId()));
            
            equipements.add(equipement);
        }
        return equipements;
    }

    private List<String> loadPhotos(int idEq) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "select chemin_photo from equipement_photo where id_equipement = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idEq);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(rs.getString("chemin_photo"));
        }
        return list;
    }
    public void updateStock(int id, int quantityToReduce) throws SQLException {
        String sql = "UPDATE equipement SET quantite_dispo = quantite_dispo - ? WHERE id = ? AND quantite_dispo >= ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, quantityToReduce);
        preparedStatement.setInt(2, id);
        preparedStatement.setInt(3, quantityToReduce);
        preparedStatement.executeUpdate();
    }
}
