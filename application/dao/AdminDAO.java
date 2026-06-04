package application.dao;

import application.model.Admin;
import application.model.Medecin;
import application.model.Patient;
import application.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDAO {

    // ══ UTILISATEURS ══════════════════════════════════════════

    public ObservableList<Utilisateur> getTousUtilisateurs() {
        ObservableList<Utilisateur> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, prenom, email, telephone, role " +
            "FROM utilisateur ORDER BY role, nom";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapUtilisateur(rs));
        } catch (SQLException e) {
            System.err.println("❌ getTousUtilisateurs : " + e.getMessage());
        }
        return liste;
    }

    public ObservableList<Utilisateur> getUtilisateursByRole(String role) {
        ObservableList<Utilisateur> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, prenom, email, telephone, role " +
            "FROM utilisateur WHERE role = ? ORDER BY nom";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapUtilisateur(rs));
        } catch (SQLException e) {
            System.err.println("❌ getUtilisateursByRole : " + e.getMessage());
        }
        return liste;
    }

    public ObservableList<Utilisateur> rechercherUtilisateur(String terme) {
        ObservableList<Utilisateur> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, prenom, email, telephone, role " +
            "FROM utilisateur " +
            "WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ? " +
            "ORDER BY nom";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String m = "%" + terme.toLowerCase() + "%";
            ps.setString(1, m); ps.setString(2, m); ps.setString(3, m);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapUtilisateur(rs));
        } catch (SQLException e) {
            System.err.println("❌ rechercherUtilisateur : " + e.getMessage());
        }
        return liste;
    }

    public boolean ajouterUtilisateur(Utilisateur u, String motDePasse) {
        String sql =
            "INSERT INTO utilisateur(nom, prenom, email, telephone, role, mot_de_passe) " +
            "VALUES(?, ?, ?, ?, ?, SHA2(?, 256))";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getRole().name());
            ps.setString(6, motDePasse);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) u.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ ajouterUtilisateur : " + e.getMessage());
        }
        return false;
    }

    public boolean modifierUtilisateur(Utilisateur u) {
        String sql =
            "UPDATE utilisateur SET nom=?, prenom=?, email=?, telephone=?, role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getRole().name());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ modifierUtilisateur : " + e.getMessage());
        }
        return false;
    }

    public boolean supprimerUtilisateur(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ supprimerUtilisateur : " + e.getMessage());
        }
        return false;
    }

    public boolean reinitialiserMotDePasse(int id, String nouveauMdp) {
        String sql = "UPDATE utilisateur SET mot_de_passe=SHA2(?,256) WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauMdp);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ reinitialiserMotDePasse : " + e.getMessage());
        }
        return false;
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("❌ emailExiste : " + e.getMessage());
        }
        return false;
    }

    // ══ STATISTIQUES SIMPLES ═══════════════════════════════════

    public int compterParRole(String role) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ compterParRole : " + e.getMessage());
        }
        return 0;
    }

    public int compterTotal() {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ compterTotal : " + e.getMessage());
        }
        return 0;
    }

    // ══ NOUVELLES MÉTHODES POUR GRAPHES ═══════════════════════

    /**
     * Répartition des utilisateurs par rôle.
     * Retourne : { "MEDECIN" -> 2, "PATIENT" -> 5, ... }
     */
    public Map<String, Integer> getRepartitionParRole() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql =
            "SELECT role, COUNT(*) AS nb " +
            "FROM utilisateur GROUP BY role ORDER BY nb DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("role"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.err.println("❌ getRepartitionParRole : " + e.getMessage());
        }
        return map;
    }

    /**
     * Nombre de rendez-vous par statut.
     * Retourne : { "CONFIRME" -> 3, "EN_ATTENTE" -> 2, ... }
     */
    public Map<String, Integer> getRdvParStatut() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql =
            "SELECT statut, COUNT(*) AS nb " +
            "FROM rendezvous GROUP BY statut ORDER BY nb DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("statut"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.err.println("❌ getRdvParStatut : " + e.getMessage());
        }
        return map;
    }

    /**
     * Chiffre d'affaires par médecin (factures PAYEE uniquement).
     * Retourne : { "Dr Benali" -> 650.0, "Dr Idrissi" -> 400.0 }
     */
    public Map<String, Double> getCaParMedecin() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql =
            "SELECT CONCAT(u.prenom, ' ', u.nom) AS medecin, " +
            "       SUM(f.montant_total) AS ca " +
            "FROM facture f " +
            "JOIN medecin m ON m.id = f.medecin_id " +
            "JOIN utilisateur u ON u.id = m.id " +
            "WHERE f.statut = 'PAYEE' " +
            "GROUP BY f.medecin_id ORDER BY ca DESC";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("medecin"), rs.getDouble("ca"));
            }
        } catch (SQLException e) {
            System.err.println("❌ getCaParMedecin : " + e.getMessage());
        }
        return map;
    }

    /**
     * Répartition des factures par statut (PAYEE / EN_ATTENTE / ANNULEE).
     */
    public Map<String, Integer> getFacturesParStatut() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql =
            "SELECT statut, COUNT(*) AS nb FROM facture GROUP BY statut";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("statut"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.err.println("❌ getFacturesParStatut : " + e.getMessage());
        }
        return map;
    }

    /**
     * Top 5 médicaments les plus dispensés (via ligneordonnance).
     * Retourne : { "Paracétamol 500mg" -> 7, "Amlodipine 5mg" -> 4, ... }
     */
    public Map<String, Integer> getTopMedicaments() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql =
            "SELECT med.nom, SUM(lo.quantite) AS total " +
            "FROM ligneordonnance lo " +
            "JOIN medicament med ON med.id = lo.medicament_id " +
            "GROUP BY lo.medicament_id ORDER BY total DESC LIMIT 5";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("nom"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("❌ getTopMedicaments : " + e.getMessage());
        }
        return map;
    }

    /**
     * Montant total encaissé vs en attente.
     * Retourne double[2] : [0] = payé, [1] = en attente
     */
    public double[] getTotauxFactures() {
        double[] totaux = {0.0, 0.0};
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN statut='PAYEE'      THEN montant_total ELSE 0 END) AS paye, " +
            "  SUM(CASE WHEN statut='EN_ATTENTE' THEN montant_total ELSE 0 END) AS attente " +
            "FROM facture";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totaux[0] = rs.getDouble("paye");
                totaux[1] = rs.getDouble("attente");
            }
        } catch (SQLException e) {
            System.err.println("❌ getTotauxFactures : " + e.getMessage());
        }
        return totaux;
    }

    // ══ HELPER ════════════════════════════════════════════════

    private Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        Utilisateur u = switch (role) {
            case "MEDECIN"    -> new Medecin();
            case "PATIENT"    -> new Patient();
            case "ADMIN"      -> new Admin();
            default           -> new Utilisateur();
        };
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setRole(Utilisateur.Role.valueOf(role));
        return u;
    }
}