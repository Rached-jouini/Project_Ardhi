package services;

import models.Plante;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanteService implements ardhi<Plante> {

    private Connection conn;

    public PlanteService() {
        this.conn = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Plante p) throws SQLException {
        String SQL = "INSERT INTO plante (nom, type, date_debut_plantation, date_fin_plantation, besoin_eau, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, p.getNom());
            pstmt.setString(2, p.getType());
            pstmt.setString(3, p.getDate_debut_plantation());
            pstmt.setString(4, p.getDate_fin_plantation());
            pstmt.setString(5, p.getBesoin_eau());
            pstmt.setString(6, p.getDescription());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Plante p) throws SQLException {
        String SQL = "UPDATE plante SET nom=?, type=?, date_debut_plantation=?, date_fin_plantation=?, besoin_eau=?, description=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, p.getNom());
            pstmt.setString(2, p.getType());
            pstmt.setString(3, p.getDate_debut_plantation());
            pstmt.setString(4, p.getDate_fin_plantation());
            pstmt.setString(5, p.getBesoin_eau());
            pstmt.setString(6, p.getDescription());
            pstmt.setInt(7, p.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String SQL = "DELETE FROM plante WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Plante> select() throws SQLException {
        List<Plante> plantes = new ArrayList<>();
        String SQL = "SELECT * FROM plante";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) {
                Plante p = new Plante();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setType(rs.getString("type"));
                p.setDate_debut_plantation(rs.getString("date_debut_plantation"));
                p.setDate_fin_plantation(rs.getString("date_fin_plantation"));
                p.setBesoin_eau(rs.getString("besoin_eau"));
                p.setDescription(rs.getString("description"));
                plantes.add(p);
            }
        }
        return plantes;
    }

    /**
     * Récupère les plantes dont la saison de plantation chevauche la période demandée.
     * Les dates de plantation en base doivent être au format "MM-DD" (ex: "03-01" pour le 1er Mars).
     */
    public List<Plante> getPlantesPourSaisonSaisonnier(String dateDebutLoc, String dateFinLoc) throws SQLException {
        // On calcule la durée en jours pour savoir si on couvre toute l'année
        java.time.LocalDate d1 = java.time.LocalDate.parse(dateDebutLoc);
        java.time.LocalDate d2 = java.time.LocalDate.parse(dateFinLoc);
        long days = java.time.temporal.ChronoUnit.DAYS.between(d1, d2);

        // Si la location dure 1 an (365 jours) ou plus, on retourne toutes les plantes
        if (days >= 365) {
            return select();
        }

        List<Plante> toutesLesPlantes = select();
        List<Plante> result = new ArrayList<>();

        String mmDdDebut = dateDebutLoc.substring(5, 10); 
        String mmDdFin = dateFinLoc.substring(5, 10);

        for (Plante p : toutesLesPlantes) {
            if (isPlanteDansSaison(p, mmDdDebut, mmDdFin)) {
                result.add(p);
            }
        }
        return result;
    }

    private boolean isPlanteDansSaison(Plante p, String debutLoc, String finLoc) {
        String pDebut = p.getDate_debut_plantation(); // "MM-DD"
        String pFin = p.getDate_fin_plantation();

        if (pDebut == null || pFin == null) return false;

        // Cas simple : la saison ne traverse pas la fin d'année (ex: 03-01 à 06-01)
        if (pDebut.compareTo(pFin) <= 0) {
            return (debutLoc.compareTo(pFin) <= 0 && finLoc.compareTo(pDebut) >= 0);
        } else {
            // Cas complexe : la saison traverse le nouvel an (ex: 11-01 à 02-01)
            // La plante est disponible si la location touche soit la fin d'année, soit le début d'année
            return (debutLoc.compareTo(pFin) <= 0 || finLoc.compareTo(pDebut) >= 0 || debutLoc.compareTo(pDebut) >= 0 || finLoc.compareTo(pFin) <= 0);
        }
    }
}
