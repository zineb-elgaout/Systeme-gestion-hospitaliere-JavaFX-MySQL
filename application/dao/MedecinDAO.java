package application.dao;

import application.model.Medecin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class MedecinDAO {

    // ── Récupérer tous les médecins ───────────────────────────
    public ObservableList<Medecin> getTousMedecins() {
        ObservableList<Medecin> liste = FXCollections.observableArrayList();

        String sql =
        	    "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, m.specialite " +
        	    "FROM Utilisateur u " +
        	    "JOIN Medecin m ON u.id = m.id " +
        	    "WHERE u.role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medecin m = new Medecin();
                m.setId(rs.getInt("id"));
                m.setNom(rs.getString("nom"));
                m.setPrenom(rs.getString("prenom"));
                m.setEmail(rs.getString("email"));
                m.setTelephone(rs.getString("telephone"));
                m.setSpecialite(rs.getString("specialite"));
                liste.add(m);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getTousMedecins : " + e.getMessage());
        }
        return liste;
    }

    // ── Récupérer un médecin par son ID ──────────────────────
    public Medecin getMedecinById(int id) {
        String sql =
            "SELECT id, nom, prenom, email, telephone " +
            "FROM Utilisateur " +
            "WHERE id = ? AND role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Medecin m = new Medecin();
                m.setId(rs.getInt("id"));
                m.setNom(rs.getString("nom"));
                m.setPrenom(rs.getString("prenom"));
                m.setEmail(rs.getString("email"));
                m.setTelephone(rs.getString("telephone"));
                return m;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getMedecinById : " + e.getMessage());
        }
        return null;
    }

    // ── Ajouter un médecin ────────────────────────────────────
    public boolean ajouterMedecin(Medecin m, String motDePasse) {
        String sql =
            "INSERT INTO Utilisateur (nom, prenom, email, telephone, role, mot_de_passe) " +
            "VALUES (?, ?, ?, ?, 'MEDECIN', SHA2(?, 256))";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getTelephone());
            ps.setString(5, motDePasse);

            int lignes = ps.executeUpdate();

            if (lignes > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) m.setId(keys.getInt(1));
                System.out.println("✅ Médecin ajouté : " + m.getNomComplet());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur ajouterMedecin : " + e.getMessage());
        }
        return false;
    }

    // ── Modifier un médecin ───────────────────────────────────
    public boolean modifierMedecin(Medecin m) {
        String sql =
            "UPDATE Utilisateur " +
            "SET nom = ?, prenom = ?, email = ?, telephone = ? " +
            "WHERE id = ? AND role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getTelephone());
            ps.setInt(5, m.getId());

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Médecin modifié : " + m.getNomComplet());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur modifierMedecin : " + e.getMessage());
        }
        return false;
    }

    // ── Modifier le mot de passe ──────────────────────────────
    public boolean modifierMotDePasse(int id, String nouveauMotDePasse) {
        String sql =
            "UPDATE Utilisateur " +
            "SET mot_de_passe = SHA2(?, 256) " +
            "WHERE id = ? AND role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nouveauMotDePasse);
            ps.setInt(2, id);

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Mot de passe modifié pour ID : " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur modifierMotDePasse : " + e.getMessage());
        }
        return false;
    }

    // ── Supprimer un médecin ──────────────────────────────────
    public boolean supprimerMedecin(int id) {
        String sql =
            "DELETE FROM Utilisateur " +
            "WHERE id = ? AND role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Médecin supprimé, ID : " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur supprimerMedecin : " + e.getMessage());
        }
        return false;
    }

    // ── Rechercher par nom ou prénom ──────────────────────────
    public ObservableList<Medecin> rechercherMedecin(String terme) {
        ObservableList<Medecin> liste = FXCollections.observableArrayList();

        String sql =
            "SELECT id, nom, prenom, email, telephone " +
            "FROM Utilisateur " +
            "WHERE role = 'MEDECIN' " +
            "AND (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?)";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String motif = "%" + terme.toLowerCase() + "%";
            ps.setString(1, motif);
            ps.setString(2, motif);
            ps.setString(3, motif);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medecin m = new Medecin();
                m.setId(rs.getInt("id"));
                m.setNom(rs.getString("nom"));
                m.setPrenom(rs.getString("prenom"));
                m.setEmail(rs.getString("email"));
                m.setTelephone(rs.getString("telephone"));
                liste.add(m);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur rechercherMedecin : " + e.getMessage());
        }
        return liste;
    }

    // ── Vérifier si un email existe déjà ─────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur emailExiste : " + e.getMessage());
        }
        return false;
    }
}