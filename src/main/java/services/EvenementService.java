package services;

import models.Evenement;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements ardhi<Evenement> {

    private Connection connection;

    public EvenementService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Evenement ev) throws SQLException {
        String req = "INSERT INTO evenement (nom, description, date, lieu ,type, culture_concernee, nombre_places, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, ev.getNom());
        ps.setString(2, ev.getDescription());
        ps.setDate(3, java.sql.Date.valueOf(ev.getDate()));
        ps.setString(4, ev.getLieu());
        ps.setString(5, ev.getType());
        ps.setString(6, ev.getCulture_concernee());
        ps.setInt(7, ev.getNombre_places());
        ps.setString(8, ev.getStatut());

        ps.executeUpdate();
    }

    @Override
    public void update(Evenement ev) throws SQLException {
        String req = "UPDATE evenement SET nom=?, description=?, date=?, lieu=?, type=?, culture_concernee=?, nombre_places=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, ev.getNom());
        ps.setString(2, ev.getDescription());
        ps.setDate(3, java.sql.Date.valueOf(ev.getDate()));
        ps.setString(4, ev.getLieu());
        ps.setString(5, ev.getType());
        ps.setString(6, ev.getCulture_concernee());
        ps.setInt(7, ev.getNombre_places());
        ps.setString(8, ev.getStatut());
        ps.setInt(9, ev.getId());

        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM evenement WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Evenement> select() throws SQLException {
        List<Evenement> evenements = new ArrayList<>();
        String req = "SELECT * FROM evenement";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Evenement ev = new Evenement(); // Utilise le constructeur vide
            ev.setId(rs.getInt("id"));
            ev.setNom(rs.getString("nom"));
            ev.setDescription(rs.getString("description"));
            ev.setDate(rs.getDate("date").toLocalDate());
            ev.setLieu(rs.getString("lieu")); // C'est ici qu'on récupère bien le lieu
            ev.setType(rs.getString("type"));
            ev.setCulture_concernee(rs.getString("culture_concernee"));
            ev.setNombre_places(rs.getInt("nombre_places"));
            ev.setStatut(rs.getString("statut"));

            evenements.add(ev);
        }
        return evenements;
    }
}