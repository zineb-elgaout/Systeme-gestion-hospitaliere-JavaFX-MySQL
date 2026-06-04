package application.dao;

import application.model.Medicament;
import application.model.Pharmacien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class PharmacienDAO {

    // ── Tous les médicaments ──────────────────────────────────
    public ObservableList<Medicament> getTousMedicaments() {
        ObservableList<Medicament> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, description, quantite_stock, " +
            "       seuil_alerte, prix_unitaire " +
            "FROM medicament ORDER BY nom";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) liste.add(mapMedicament(rs));

        } catch (SQLException e) {
            System.err.println("❌ getTousMedicaments : " + e.getMessage());
        }
        return liste;
    }

    // ── Médicaments en rupture ────────────────────────────────
    public ObservableList<Medicament> getMedicamentsEnRupture() {
        ObservableList<Medicament> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, description, quantite_stock, " +
            "       seuil_alerte, prix_unitaire " +
            "FROM medicament " +
            "WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) liste.add(mapMedicament(rs));

        } catch (SQLException e) {
            System.err.println("❌ getMedicamentsEnRupture : " + e.getMessage());
        }
        return liste;
    }

    // ── Rechercher médicament ─────────────────────────────────
    public ObservableList<Medicament> rechercherMedicament(String terme) {
        ObservableList<Medicament> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, description, quantite_stock, " +
            "       seuil_alerte, prix_unitaire " +
            "FROM medicament " +
            "WHERE LOWER(nom) LIKE ? OR LOWER(description) LIKE ? " +
            "ORDER BY nom";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String m = "%" + terme.toLowerCase() + "%";
            ps.setString(1, m);
            ps.setString(2, m);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapMedicament(rs));

        } catch (SQLException e) {
            System.err.println("❌ rechercherMedicament : " + e.getMessage());
        }
        return liste;
    }

    // ── Ajouter médicament ────────────────────────────────────
    public boolean ajouterMedicament(Medicament med) {
        String sql =
            "INSERT INTO medicament" +
            "(nom, description, quantite_stock, seuil_alerte, prix_unitaire) " +
            "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, med.getNom());
            ps.setString(2, med.getDescription());
            ps.setInt(3, med.getQuantiteStock());
            ps.setInt(4, med.getSeuilAlerte());
            ps.setDouble(5, med.getPrixUnitaire());

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) med.setId(keys.getInt(1));
                System.out.println("✅ Médicament ajouté : " + med.getNom());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ ajouterMedicament : " + e.getMessage());
        }
        return false;
    }

    // ── Modifier médicament ───────────────────────────────────
    public boolean modifierMedicament(Medicament med) {
        String sql =
            "UPDATE medicament " +
            "SET nom=?, description=?, quantite_stock=?, " +
            "    seuil_alerte=?, prix_unitaire=? " +
            "WHERE id=?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, med.getNom());
            ps.setString(2, med.getDescription());
            ps.setInt(3, med.getQuantiteStock());
            ps.setInt(4, med.getSeuilAlerte());
            ps.setDouble(5, med.getPrixUnitaire());
            ps.setInt(6, med.getId());

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Médicament modifié : " + med.getNom());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ modifierMedicament : " + e.getMessage());
        }
        return false;
    }

    // ── Supprimer médicament ──────────────────────────────────
    public boolean supprimerMedicament(int id) {
        String sql = "DELETE FROM medicament WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Médicament supprimé ID : " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ supprimerMedicament : " + e.getMessage());
        }
        return false;
    }

    // ── Mettre à jour le stock ────────────────────────────────
    public boolean mettreAJourStock(int id, int nouvelleQuantite) {
        String sql = "UPDATE medicament SET quantite_stock=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nouvelleQuantite);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ mettreAJourStock : " + e.getMessage());
        }
        return false;
    }

    // ── Compter ruptures ──────────────────────────────────────
    public int compterRuptures() {
        String sql =
            "SELECT COUNT(*) FROM medicament " +
            "WHERE quantite_stock <= seuil_alerte";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ compterRuptures : " + e.getMessage());
        }
        return 0;
    }

    // ── Compter total ─────────────────────────────────────────
    public int compterTotal() {
        String sql = "SELECT COUNT(*) FROM medicament";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ compterTotal : " + e.getMessage());
        }
        return 0;
    }

    // ── Tous les pharmaciens ──────────────────────────────────
    public ObservableList<Pharmacien> getTousPharmaciens() {
        ObservableList<Pharmacien> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT id, nom, prenom, email, telephone " +
            "FROM utilisateur WHERE role='PHARMACIEN' ORDER BY nom";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pharmacien p = new Pharmacien();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setEmail(rs.getString("email"));
                p.setTelephone(rs.getString("telephone"));
                liste.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ getTousPharmaciens : " + e.getMessage());
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════
    //  NOUVELLES MÉTHODES — ORDONNANCES
    // ════════════════════════════════════════════════════════════

    /**
     * Charge toutes les ordonnances avec les infos patient, médecin et statut.
     * Retourne un tableau de String[] par ligne :
     *   [0] id ordonnance
     *   [1] patient (prénom + nom)
     *   [2] médecin (prénom + nom)
     *   [3] date émission
     *   [4] statut (libellé français)
     *   [5] instructions
     *   [6] id ordonnance (pour les opérations)
     */
    public ObservableList<String[]> getToutesOrdonnances() {
        ObservableList<String[]> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT o.id, " +
            "       CONCAT(up.prenom, ' ', up.nom)  AS patient, " +
            "       CONCAT(um.prenom, ' ', um.nom)  AS medecin, " +
            "       o.date_emission, " +
            "       o.statut, " +
            "       COALESCE(o.instructions, '') AS instructions " +
            "FROM ordonnance o " +
            "JOIN consultation c  ON c.id  = o.consultation_id " +
            "JOIN dossiermedical dm ON dm.id = c.dossier_id " +
            "JOIN patient pat     ON pat.id = dm.patient_id " +
            "JOIN utilisateur up  ON up.id  = pat.id " +
            "JOIN medecin med     ON med.id = c.medecin_id " +
            "JOIN utilisateur um  ON um.id  = med.id " +
            "ORDER BY o.date_emission DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String[] row = new String[7];
                row[0] = String.valueOf(rs.getInt("id"));
                row[1] = rs.getString("patient");
                row[2] = rs.getString("medecin");
                row[3] = rs.getString("date_emission");
                row[4] = traduireStatutOrdonnance(rs.getString("statut"));
                row[5] = rs.getString("instructions");
                row[6] = row[0]; // id dupliqué pour usage interne
                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ getToutesOrdonnances : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Recherche les ordonnances par nom de patient ou de médecin.
     */
    public ObservableList<String[]> rechercherOrdonnances(String terme) {
        ObservableList<String[]> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT o.id, " +
            "       CONCAT(up.prenom, ' ', up.nom)  AS patient, " +
            "       CONCAT(um.prenom, ' ', um.nom)  AS medecin, " +
            "       o.date_emission, " +
            "       o.statut, " +
            "       COALESCE(o.instructions, '') AS instructions " +
            "FROM ordonnance o " +
            "JOIN consultation c  ON c.id  = o.consultation_id " +
            "JOIN dossiermedical dm ON dm.id = c.dossier_id " +
            "JOIN patient pat     ON pat.id = dm.patient_id " +
            "JOIN utilisateur up  ON up.id  = pat.id " +
            "JOIN medecin med     ON med.id = c.medecin_id " +
            "JOIN utilisateur um  ON um.id  = med.id " +
            "WHERE LOWER(CONCAT(up.prenom,' ',up.nom)) LIKE ? " +
            "   OR LOWER(CONCAT(um.prenom,' ',um.nom)) LIKE ? " +
            "ORDER BY o.date_emission DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String m = "%" + terme.toLowerCase() + "%";
            ps.setString(1, m);
            ps.setString(2, m);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String[] row = new String[7];
                row[0] = String.valueOf(rs.getInt("id"));
                row[1] = rs.getString("patient");
                row[2] = rs.getString("medecin");
                row[3] = rs.getString("date_emission");
                row[4] = traduireStatutOrdonnance(rs.getString("statut"));
                row[5] = rs.getString("instructions");
                row[6] = row[0];
                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ rechercherOrdonnances : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Met à jour le statut d'une ordonnance et enregistre le pharmacien
     * qui la valide/délivre.
     *
     * @param ordonnanceId  id de l'ordonnance
     * @param statutFr      statut en français ("En attente", "Dispensée", "Expirée")
     * @param pharmacienId  id du pharmacien connecté
     */
    public boolean mettreAJourStatutOrdonnance(int ordonnanceId,
                                               String statutFr,
                                               int pharmacienId) {
        String statutBd = traduireStatutVersDB(statutFr);
        String sql =
            "UPDATE ordonnance " +
            "SET statut=?, pharmacien_id=? " +
            "WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statutBd);
            ps.setInt(2, pharmacienId);
            ps.setInt(3, ordonnanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ mettreAJourStatutOrdonnance : " + e.getMessage());
        }
        return false;
    }

    /**
     * Charge les lignes de médicaments d'une ordonnance pour l'affichage
     * dans le détail.
     * Retourne une liste de String formatées "nom — quantité × posologie (X jours)"
     */
    public ObservableList<String> getLignesOrdonnance(int ordonnanceId) {
        ObservableList<String> lignes = FXCollections.observableArrayList();
        String sql =
            "SELECT med.nom, lo.quantite, lo.posologie, lo.duree_jours " +
            "FROM ligneordonnance lo " +
            "JOIN medicament med ON med.id = lo.medicament_id " +
            "WHERE lo.ordonnance_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ordonnanceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ligne = rs.getString("nom")
                    + " — " + rs.getInt("quantite") + " unité(s)"
                    + "\n  " + rs.getString("posologie")
                    + " (" + rs.getInt("duree_jours") + " jours)";
                lignes.add(ligne);
            }
        } catch (SQLException e) {
            System.err.println("❌ getLignesOrdonnance : " + e.getMessage());
        }
        return lignes;
    }

    // ════════════════════════════════════════════════════════════
    //  NOUVELLES MÉTHODES — HISTORIQUE DISPENSATION
    //  (construit depuis ligneordonnance + ordonnance délivrées)
    // ════════════════════════════════════════════════════════════

    /**
     * Retourne l'historique des dispensations réalisées par un pharmacien.
     * Chaque ligne : [date_emission, patient, médicament, quantité, montant]
     *
     * @param pharmacienId  id du pharmacien connecté (filtre optionnel)
     */
    public ObservableList<String[]> getHistoriqueDispensation(int pharmacienId) {
        ObservableList<String[]> liste = FXCollections.observableArrayList();
        String sql =
            "SELECT o.date_emission, " +
            "       CONCAT(up.prenom, ' ', up.nom) AS patient, " +
            "       med.nom                         AS medicament, " +
            "       lo.quantite, " +
            "       (lo.quantite * med.prix_unitaire) AS montant " +
            "FROM ordonnance o " +
            "JOIN ligneordonnance lo  ON lo.ordonnance_id = o.id " +
            "JOIN medicament med      ON med.id = lo.medicament_id " +
            "JOIN consultation c      ON c.id  = o.consultation_id " +
            "JOIN dossiermedical dm   ON dm.id = c.dossier_id " +
            "JOIN patient pat         ON pat.id = dm.patient_id " +
            "JOIN utilisateur up      ON up.id  = pat.id " +
            "WHERE o.statut IN ('DELIVREE','VALIDEE') " +
            "  AND (o.pharmacien_id = ? OR o.pharmacien_id IS NOT NULL) " +
            "ORDER BY o.date_emission DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pharmacienId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String[] row = new String[5];
                row[0] = rs.getString("date_emission");
                row[1] = rs.getString("patient");
                row[2] = rs.getString("medicament");
                row[3] = String.valueOf(rs.getInt("quantite"));
                row[4] = String.format("%.2f MAD", rs.getDouble("montant"));
                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ getHistoriqueDispensation : " + e.getMessage());
        }
        return liste;
    }

    // ── Helpers privés ────────────────────────────────────────

    /** BD → libellé français */
    private String traduireStatutOrdonnance(String statut) {
        if (statut == null) return "En attente";
        return switch (statut) {
            case "EN_ATTENTE" -> "En attente";
            case "VALIDEE"    -> "Validée";
            case "DELIVREE"   -> "Dispensée";
            case "EXPIREE"    -> "Expirée";
            default           -> statut;
        };
    }

    /** Libellé français → valeur ENUM de la BD */
    private String traduireStatutVersDB(String statutFr) {
        return switch (statutFr) {
            case "En attente" -> "EN_ATTENTE";
            case "Validée"    -> "VALIDEE";
            case "Dispensée"  -> "DELIVREE";
            case "Expirée"    -> "EXPIREE";
            default           -> "EN_ATTENTE";
        };
    }

    private Medicament mapMedicament(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id"));
        m.setNom(rs.getString("nom"));
        m.setDescription(rs.getString("description"));
        m.setQuantiteStock(rs.getInt("quantite_stock"));
        m.setSeuilAlerte(rs.getInt("seuil_alerte"));
        m.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        return m;
    }
}