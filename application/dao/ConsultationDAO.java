package application.dao;

import application.model.Consultation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    // ── Créer une consultation et retourner son ID ────────────
    public int creerConsultation(int patientId, int medecinId,
                                  LocalDate date, String notes) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance();
            conn.setAutoCommit(false);

            // ── Trouver le dossier médical du patient ──────────
            String sqlDossier =
                "SELECT id FROM dossiermedical WHERE patient_id = ?";
            PreparedStatement psDossier =
                conn.prepareStatement(sqlDossier);
            psDossier.setInt(1, patientId);
            ResultSet rsDossier = psDossier.executeQuery();

            if (!rsDossier.next()) {
                System.err.println("❌ Dossier médical introuvable.");
                conn.rollback();
                return -1;
            }
            int dossierId = rsDossier.getInt("id");

            // ── Créer la consultation ──────────────────────────
            String sqlConsult =
                "INSERT INTO consultation " +
                "(dossier_id, medecin_id, date_consult, notes) " +
                "VALUES (?, ?, ?, ?)";
            PreparedStatement psConsult = conn.prepareStatement(
                sqlConsult, Statement.RETURN_GENERATED_KEYS);
            psConsult.setInt(1, dossierId);
            psConsult.setInt(2, medecinId);
            psConsult.setDate(3, Date.valueOf(date));
            psConsult.setString(4, notes);
            psConsult.executeUpdate();

            ResultSet keys = psConsult.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return -1;
            }
            int consultationId = keys.getInt(1);

            conn.commit();
            System.out.println("✅ Consultation créée id=" 
                + consultationId);
            return consultationId;

        } catch (SQLException e) {
            System.err.println("❌ Erreur creerConsultation : " 
                + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            return -1;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {}
        }
    }

    // ── Récupérer les consultations d'un patient ──────────────
    public List<Consultation> getConsultationsPatient(int patientId) {
        List<Consultation> liste = new ArrayList<>();

        String sql =
            "SELECT c.id, c.dossier_id, c.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_medecin, " +
            "       m.specialite, " +
            "       c.date_consult, c.diagnostic, c.notes " +
            "FROM consultation c " +
            "JOIN dossiermedical dm ON dm.id = c.dossier_id " +
            "JOIN utilisateur u     ON u.id  = c.medecin_id " +
            "JOIN medecin m         ON m.id  = c.medecin_id " +
            "WHERE dm.patient_id = ? " +
            "ORDER BY c.date_consult DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Consultation c = new Consultation();
                c.setId(rs.getInt("id"));
                c.setPatientId(patientId);
                c.setNomMedecin(rs.getString("nom_medecin"));
                c.setSpecialite(rs.getString("specialite"));

                Timestamp ts = rs.getTimestamp("date_consult");
                if (ts != null)
                    c.setDate(ts.toLocalDateTime().toLocalDate());

                c.setDiagnostic(
                    rs.getString("diagnostic") != null
                        ? rs.getString("diagnostic") : "—");
                c.setNotes(
                    rs.getString("notes") != null
                        ? rs.getString("notes") : "");
                liste.add(c);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getConsultationsPatient : " 
                + e.getMessage());
        }
        return liste;
    }

    // ── Récupérer les consultations d'un médecin ──────────────
    public List<Consultation> getConsultationsMedecin(int medecinId) {
        List<Consultation> liste = new ArrayList<>();

        String sql =
            "SELECT c.id, c.dossier_id, c.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_patient, " +
            "       c.date_consult, c.diagnostic, c.notes " +
            "FROM consultation c " +
            "JOIN dossiermedical dm ON dm.id  = c.dossier_id " +
            "JOIN utilisateur u     ON u.id   = dm.patient_id " +
            "WHERE c.medecin_id = ? " +
            "ORDER BY c.date_consult DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Consultation c = new Consultation();
                c.setId(rs.getInt("id"));
                c.setNomMedecin(rs.getString("nom_patient"));

                Timestamp ts = rs.getTimestamp("date_consult");
                if (ts != null)
                    c.setDate(ts.toLocalDateTime().toLocalDate());

                c.setDiagnostic(
                    rs.getString("diagnostic") != null
                        ? rs.getString("diagnostic") : "—");
                c.setNotes(
                    rs.getString("notes") != null
                        ? rs.getString("notes") : "");
                liste.add(c);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getConsultationsMedecin : " 
                + e.getMessage());
        }
        return liste;
    }
}