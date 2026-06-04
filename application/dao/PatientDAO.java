package application.dao;

import application.model.Utilisateur;
import javafx.collections.ObservableList;

import java.sql.*;

public class PatientDAO {

    // ── Récupérer les infos complètes d'un patient ────────────
    public Utilisateur getPatientComplet(int userId) {
        String sql =
            "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, " +
            "       p.date_naissance, p.adresse, p.groupe_sanguin, p.allergies " +
            "FROM Utilisateur u " +
            "JOIN Patient p ON u.id = p.id " +
            "WHERE u.id = ?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setRole(Utilisateur.Role.PATIENT);
                // Stocker les champs patient dans des champs supplémentaires
                // (voir note ci-dessous)
                return u;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getPatientComplet : " + e.getMessage());
        }
        return null;
    }
 // ── Patients ayant eu un RDV avec ce médecin ──────────────
    public ObservableList<Utilisateur> getPatientsduMedecin(int medecinId) {
    ObservableList<Utilisateur> liste =
        javafx.collections.FXCollections.observableArrayList();

    String sql =
        "SELECT DISTINCT u.id, u.nom, u.prenom, u.email, u.telephone " +
        "FROM utilisateur u " +
        "JOIN rendezvous rv ON rv.patient_id = u.id " +
        "WHERE rv.medecin_id = ? " +
        "ORDER BY u.nom";

    try (Connection conn = DatabaseConnection.getInstance();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, medecinId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Utilisateur u = new Utilisateur();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            u.setTelephone(rs.getString("telephone"));
            u.setRole(Utilisateur.Role.PATIENT);
            liste.add(u);
        }
    } catch (SQLException e) {
        System.err.println("❌ Erreur getPatientsduMedecin : " + e.getMessage());
    }
    return liste;
}
 // ── Récupérer tous les patients (pour les combos médecin) ─
    public ObservableList<Utilisateur> getTousPatients() {
        ObservableList<Utilisateur> liste = 
            javafx.collections.FXCollections.observableArrayList();
        
        String sql =
            "SELECT u.id, u.nom, u.prenom " +
            "FROM Utilisateur u " +
            "WHERE u.role = 'PATIENT' " +
            "ORDER BY u.nom";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setRole(Utilisateur.Role.PATIENT);
                liste.add(u);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getTousPatients : " + e.getMessage());
        }
        return liste;
    }

    // ── Mettre à jour le profil patient ───────────────────────
    public boolean mettreAJourProfil(int userId, String telephone,
                                     String adresse, String allergies) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance();
            conn.setAutoCommit(false);

            // Mettre à jour Utilisateur
            String sqlUser = "UPDATE Utilisateur SET telephone = ? WHERE id = ?";
            PreparedStatement psUser = conn.prepareStatement(sqlUser);
            psUser.setString(1, telephone);
            psUser.setInt(2, userId);
            psUser.executeUpdate();

            // Mettre à jour Patient
            String sqlPatient =
                "UPDATE Patient SET adresse = ?, allergies = ? WHERE id = ?";
            PreparedStatement psPatient = conn.prepareStatement(sqlPatient);
            psPatient.setString(1, adresse);
            psPatient.setString(2, allergies);
            psPatient.setInt(3, userId);
            psPatient.executeUpdate();

            conn.commit();
            System.out.println("✅ Profil mis à jour pour userId=" + userId);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur mettreAJourProfil : " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // ── Récupérer les infos patient (tableau de résultat brut) ─
    public String[] getInfosPatient(int userId) {
        String sql =
            "SELECT p.date_naissance, p.adresse, p.groupe_sanguin, p.allergies " +
            "FROM Patient p WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[]{
                    rs.getString("date_naissance") != null
                        ? rs.getString("date_naissance") : "",
                    rs.getString("adresse")       != null
                        ? rs.getString("adresse") : "",
                    rs.getString("groupe_sanguin") != null
                        ? rs.getString("groupe_sanguin") : "",
                    rs.getString("allergies")     != null
                        ? rs.getString("allergies") : "Aucune"
                };
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getInfosPatient : " + e.getMessage());
        }
        return new String[]{"", "", "A+", "Aucune"};
    }
    
    
}