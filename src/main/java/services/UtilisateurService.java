package services;

import models.GoogleUserInfo;
import models.Utilisateur;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements ardhi<Utilisateur> {

    private final Connection cnx;

    public UtilisateurService() {
        this.cnx = MyDataBase.getInstance().getConnection();
        try {
            ensureSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de preparer le schema utilisateur: " + e.getMessage(), e);
        }
    }

    private void ensureSchema() throws SQLException {
        addColumnIfMissing("photo", "VARCHAR(255) NULL");
        addColumnIfMissing("ban_until", "DATETIME NULL");
    }

    private void addColumnIfMissing(String columnName, String sqlDefinition) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'utilisateur' AND column_name = ?";
        try (PreparedStatement pst = cnx.prepareStatement(checkSql)) {
            pst.setString(1, columnName);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement st = cnx.createStatement()) {
                        st.executeUpdate("ALTER TABLE utilisateur ADD COLUMN " + columnName + " " + sqlDefinition);
                    }
                }
            }
        }
    }

    @Override
    public void add(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, date_inscription, statut, id_role, photo, ban_until) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getMotDePasse());
            pst.setString(5, u.getTelephone());
            pst.setDate(6, Date.valueOf(u.getDateInscription()));
            pst.setString(7, u.getStatut());
            pst.setInt(8, u.getIdRole());
            pst.setString(9, u.getPhoto());
            pst.setTimestamp(10, u.getBanUntil() != null ? Timestamp.valueOf(u.getBanUntil()) : null);
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, telephone=?, statut=?, id_role=?, photo=?, ban_until=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getMotDePasse());
            pst.setString(5, u.getTelephone());
            pst.setString(6, u.getStatut());
            pst.setInt(7, u.getIdRole());
            pst.setString(8, u.getPhoto());
            pst.setTimestamp(9, u.getBanUntil() != null ? Timestamp.valueOf(u.getBanUntil()) : null);
            pst.setInt(10, u.getId());
            pst.executeUpdate();
        }
    }

    public void updateProfile(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, telephone=?, photo=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, u.getNom());
            pst.setString(2, u.getPrenom());
            pst.setString(3, u.getEmail());
            pst.setString(4, u.getMotDePasse());
            pst.setString(5, u.getTelephone());
            pst.setString(6, u.getPhoto());
            pst.setInt(7, u.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> select() throws SQLException {
        String sql = "SELECT * FROM utilisateur ORDER BY id DESC";
        List<Utilisateur> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Utilisateur> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? ORDER BY id DESC";
        List<Utilisateur> list = new ArrayList<>();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            String token = "%" + keyword + "%";
            pst.setString(1, token);
            pst.setString(2, token);
            pst.setString(3, token);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Utilisateur authenticate(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setString(2, motDePasse);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM utilisateur WHERE email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExistsForAnotherUser(String email, int id) throws SQLException {
        String sql = "SELECT id FROM utilisateur WHERE email=? AND id<>?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setInt(2, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Utilisateur findById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updatePasswordByEmail(String email, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, newPassword);
            pst.setString(2, email);
            return pst.executeUpdate() > 0;
        }
    }

    public Utilisateur findOrCreateGoogleUser(GoogleUserInfo info) throws SQLException {
        if (info == null || info.email == null || info.email.isBlank()) {
            return null;
        }

        Utilisateur existing = findByEmail(info.email.trim());
        if (existing != null) {
            return existing;
        }

        String prenom = (info.given_name != null && !info.given_name.isBlank()) ? info.given_name.trim() : "Google";
        String nom = (info.family_name != null && !info.family_name.isBlank()) ? info.family_name.trim() : "User";

        Utilisateur googleUser = new Utilisateur(
                nom,
                prenom,
                info.email.trim(),
                "google-auth",
                "00000000",
                LocalDate.now(),
                "actif",
                2,
                info.picture,
                null
        );
        add(googleUser);
        return findByEmail(info.email.trim());
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Date date = rs.getDate("date_inscription");
        LocalDate localDate = date != null ? date.toLocalDate() : LocalDate.now();
        Timestamp banTs = rs.getTimestamp("ban_until");
        LocalDateTime banUntil = banTs != null ? banTs.toLocalDateTime() : null;

        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("telephone"),
                localDate,
                rs.getString("statut"),
                rs.getInt("id_role"),
                rs.getString("photo"),
                banUntil
        );
    }
}
