package application.dao;

import application.model.Admin;
import application.model.Medecin;
import application.model.Patient;
import application.model.Pharmacien;
import application.model.Utilisateur;

import java.sql.*;

public class UtilisateurDAO {

    public Utilisateur authentifier(String email, String motDePasse) {
    String sql =
        "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, u.role, " +
        "       m.specialite " +
        "FROM Utilisateur u " +
        "LEFT JOIN Medecin m ON u.id = m.id " +
        "WHERE u.email = ? AND u.mot_de_passe = SHA2(?, 256)";

    try (Connection conn = DatabaseConnection.getInstance();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, email);
        ps.setString(2, motDePasse);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String role = rs.getString("role");

            switch (role) {
                case "MEDECIN" -> {
                    Medecin m = new Medecin();
                    m.setId(rs.getInt("id"));
                    m.setNom(rs.getString("nom"));
                    m.setPrenom(rs.getString("prenom"));
                    m.setEmail(rs.getString("email"));
                    m.setTelephone(rs.getString("telephone"));
                    return m;
                }
                case "PATIENT" -> {
                    Patient p = new Patient();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setEmail(rs.getString("email"));
                    p.setTelephone(rs.getString("telephone"));
                    return p;
                }
                case "ADMIN" -> {
                    Admin a = new Admin();
                    a.setId(rs.getInt("id"));
                    a.setNom(rs.getString("nom"));
                    a.setPrenom(rs.getString("prenom"));
                    a.setEmail(rs.getString("email"));
                    a.setTelephone(rs.getString("telephone"));
                    return a;
                }
                case "PHARMACIEN" -> {
                    Pharmacien p = new Pharmacien();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setEmail(rs.getString("email"));
                    p.setTelephone(rs.getString("telephone"));
                    return p;
                }
                default -> {
                    Utilisateur u = new Utilisateur();
                    u.setId(rs.getInt("id"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email"));
                    u.setTelephone(rs.getString("telephone"));
                    u.setRole(Utilisateur.Role.valueOf(role));
                    return u;
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Erreur authentification : " + e.getMessage());
    }
    return null;
}
 
    // ── Vérifier si email existe déjà ────────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT id FROM Utilisateur WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            boolean existe = rs.next();

            if (existe) {
                System.err.println("❌ Email déjà utilisé : " + email);
            } else {
                System.out.println("✅ Email disponible : " + email);
            }
            return existe;

        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification email : " + e.getMessage());
            return false;
        }
    }

    // ── Inscription Patient (sans activation, accès direct) ───
    public boolean inscrirePatient(String nom, String prenom, String email,
                                   String motDePasse, String telephone,
                                   String dateNaissance, String adresse,
                                   String groupeSanguin, String allergies) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance();
            conn.setAutoCommit(false);

            // ── Étape 1 : Insérer dans Utilisateur ─────────────
            String sqlUser =
                "INSERT INTO Utilisateur " +
                "(nom, prenom, email, mot_de_passe, telephone, role) " +
                "VALUES (?, ?, ?, SHA2(?, 256), ?, 'PATIENT')";

            PreparedStatement psUser = conn.prepareStatement(
                sqlUser, Statement.RETURN_GENERATED_KEYS
            );
            psUser.setString(1, nom);
            psUser.setString(2, prenom);
            psUser.setString(3, email);
            psUser.setString(4, motDePasse);
            psUser.setString(5, telephone);
            psUser.executeUpdate();

            // Récupérer l'ID généré
            ResultSet keys = psUser.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                System.err.println("❌ Impossible de récupérer l'ID généré.");
                return false;
            }
            int newUserId = keys.getInt(1);
            System.out.println("✅ Utilisateur créé avec ID : " + newUserId);

            // ── Étape 2 : Insérer dans Patient ─────────────────
            String sqlPatient =
                "INSERT INTO Patient " +
                "(id, date_naissance, adresse, groupe_sanguin, allergies) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement psPatient = conn.prepareStatement(sqlPatient);
            psPatient.setInt(1, newUserId);

            if (dateNaissance != null && !dateNaissance.isEmpty()) {
                psPatient.setDate(2, java.sql.Date.valueOf(dateNaissance));
            } else {
                psPatient.setNull(2, java.sql.Types.DATE);
            }

            psPatient.setString(3, adresse.isEmpty()      ? null : adresse);
            psPatient.setString(4, groupeSanguin);
            psPatient.setString(5, allergies.isEmpty()    ? "Aucune" : allergies);
            psPatient.executeUpdate();
            System.out.println("✅ Profil Patient créé.");

            // ── Étape 3 : Créer DossierMedical automatiquement ─
            String sqlDossier =
                "INSERT INTO DossierMedical (patient_id, date_creation) " +
                "VALUES (?, CURDATE())";

            PreparedStatement psDossier = conn.prepareStatement(sqlDossier);
            psDossier.setInt(1, newUserId);
            psDossier.executeUpdate();
            System.out.println("✅ Dossier médical créé.");

            // ── Valider la transaction ──────────────────────────
            conn.commit();
            System.out.println("✅ Inscription complète pour : " + email);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur inscription : " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
                System.out.println("↩  Rollback effectué.");
            } catch (SQLException ex) {
                System.err.println("❌ Erreur rollback : " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("❌ Erreur reset autoCommit : " + e.getMessage());
            }
        }
    }
}