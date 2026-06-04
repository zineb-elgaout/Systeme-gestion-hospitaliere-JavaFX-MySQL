package application.dao;

import application.model.RendezVous;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    // ── Récupérer les RDV d'un patient ────────────────────────
    public List<RendezVous> getRendezVousPatient(int patientId) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id, rv.patient_id, rv.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_medecin, " +
            "       m.specialite, rv.date_rdv, rv.heure_rdv, " +
            "       rv.motif, rv.statut " +
            "FROM rendezvous rv " +
            "JOIN utilisateur u ON rv.medecin_id = u.id " +
            "JOIN medecin m     ON rv.medecin_id = m.id " +
            "WHERE rv.patient_id = ? " +
            "ORDER BY rv.date_rdv DESC, rv.heure_rdv DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RendezVous rv = new RendezVous();
                rv.setId(rs.getInt("id"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                rv.setNomMedecin(rs.getString("nom_medecin"));
                rv.setSpecialite(rs.getString("specialite"));
                Date sqlDate = rs.getDate("date_rdv");
                if (sqlDate != null) rv.setDate(sqlDate.toLocalDate());
                Time sqlTime = rs.getTime("heure_rdv");
                if (sqlTime != null) rv.setHeure(sqlTime.toLocalTime());
                rv.setMotif(rs.getString("motif"));
                rv.setStatut(RendezVous.Statut.valueOf(rs.getString("statut")));
                liste.add(rv);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRendezVousPatient : " + e.getMessage());
        }
        return liste;
    }

    // ── Récupérer les RDV d'un médecin ────────────────────────
    public List<RendezVous> getRendezVousMedecin(int medecinId) {
        List<RendezVous> liste = new ArrayList<>();
        String sql =
            "SELECT rv.id, rv.patient_id, rv.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_patient, " +
            "       rv.date_rdv, rv.heure_rdv, rv.motif, rv.statut " +
            "FROM rendezvous rv " +
            "JOIN utilisateur u ON rv.patient_id = u.id " +
            "WHERE rv.medecin_id = ? " +
            "ORDER BY rv.date_rdv DESC, rv.heure_rdv DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RendezVous rv = new RendezVous();
                rv.setId(rs.getInt("id"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                rv.setNomMedecin(rs.getString("nom_patient"));
                Date sqlDate = rs.getDate("date_rdv");
                if (sqlDate != null) rv.setDate(sqlDate.toLocalDate());
                Time sqlTime = rs.getTime("heure_rdv");
                if (sqlTime != null) rv.setHeure(sqlTime.toLocalTime());
                rv.setMotif(rs.getString("motif"));
                rv.setStatut(RendezVous.Statut.valueOf(rs.getString("statut")));
                liste.add(rv);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRendezVousMedecin : " + e.getMessage());
        }
        return liste;
    }

    // ── Créer un nouveau RDV ──────────────────────────────────
    public boolean creerRendezVous(int patientId, int medecinId,
                                   LocalDate date, LocalTime heure,
                                   String motif) {
        String sql =
            "INSERT INTO rendezvous " +
            "(patient_id, medecin_id, date_rdv, heure_rdv, motif, statut) " +
            "VALUES (?, ?, ?, ?, ?, 'EN_ATTENTE')";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, medecinId);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(heure));
            ps.setString(5, motif);
            ps.executeUpdate();
            System.out.println("✅ RDV créé pour patient=" + patientId);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Erreur creerRendezVous : " + e.getMessage());
            return false;
        }
    }

    // ── Annuler un RDV ────────────────────────────────────────
    public boolean annulerRendezVous(int rdvId) {
        String sql = "UPDATE rendezvous SET statut = 'ANNULE' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rdvId);
            ps.executeUpdate();
            System.out.println("✅ RDV annulé id=" + rdvId);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Erreur annulerRendezVous : " + e.getMessage());
            return false;
        }
    }

    // ✅ Modifier le statut d'un RDV ───────────────────────────
    public boolean updateStatut(int rdvId, RendezVous.Statut statut) {
        String sql = "UPDATE rendezvous SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, rdvId);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ Statut RDV id=" + rdvId
                    + " → " + statut.name());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur updateStatut : " + e.getMessage());
        }
        return false;
    }

    // ── Récupérer les médecins ────────────────────────────────
    public List<String[]> getMedecins() {
        List<String[]> liste = new ArrayList<>();
        String sql =
            "SELECT u.id, CONCAT(u.prenom, ' ', u.nom) AS nom, m.specialite " +
            "FROM utilisateur u " +
            "JOIN medecin m ON u.id = m.id " +
            "ORDER BY u.nom";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nom"),
                    rs.getString("specialite")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getMedecins : " + e.getMessage());
        }
        return liste;
    }

    // ── ✅ NOUVEAU : Récupérer l'e-mail du médecin par son id ─
    /**
     * Retourne un tableau { email, nomComplet } du médecin,
     * ou null si l'id est introuvable.
     */
    public String[] getEmailMedecin(int medecinId) {
        String sql =
            "SELECT email, CONCAT(prenom, ' ', nom) AS nom_complet " +
            "FROM utilisateur " +
            "WHERE id = ? AND role = 'MEDECIN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("email"),
                    rs.getString("nom_complet")
                };
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getEmailMedecin : " + e.getMessage());
        }
        return null;
    }
    /**
     * Retourne { email, prénom + nom } du patient pour un RDV donné.
     * @return String[]{email, nomComplet} ou null si introuvable
     */
 // ── ✅ Récupérer l'e-mail du patient par l'id du RDV ──────
    public String[] getEmailPatient(int rdvId) {
        String sql =
            "SELECT u.email, CONCAT(u.prenom, ' ', u.nom) AS nom_complet " +
            "FROM utilisateur u " +
            "JOIN rendezvous rv ON rv.patient_id = u.id " +
            "WHERE rv.id = ?";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rdvId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("email"),
                    rs.getString("nom_complet")
                };
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getEmailPatient : " + e.getMessage());
        }
        return null;
    }
}