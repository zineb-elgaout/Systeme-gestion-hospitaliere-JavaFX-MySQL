package application.dao;
 
import application.model.Consultation;
import application.model.DossierMedical;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
public class DossierMedicalDAO {
 
    /**
     * Récupère le dossier médical du patient.
     * La table dossiermedical a : id, patient_id, date_creation, antecedents, allergies
     */
    public DossierMedical getDossier(int patientId) {
        String sql =
            "SELECT dm.id, dm.patient_id, dm.date_creation, " +
            "       dm.antecedents, dm.allergies, " +
            "       p.groupe_sanguin " +
            "FROM dossiermedical dm " +
            "JOIN patient p ON p.id = dm.patient_id " +
            "WHERE dm.patient_id = ?";
 
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                DossierMedical d = new DossierMedical();
                d.setId(rs.getInt("id"));
                d.setPatientId(rs.getInt("patient_id"));
 
                Date sqlDate = rs.getDate("date_creation");
                if (sqlDate != null)
                    d.setDateCreation(sqlDate.toLocalDate());
 
                d.setAntecedents(
                    rs.getString("antecedents") != null
                        ? rs.getString("antecedents") : "Aucun antécédent enregistré."
                );
                d.setAllergies(
                    rs.getString("allergies") != null
                        ? rs.getString("allergies") : "Aucune allergie connue."
                );
                d.setGroupeSanguin(
                    rs.getString("groupe_sanguin") != null
                        ? rs.getString("groupe_sanguin") : "—"
                );
                return d;
            }
 
        } catch (SQLException e) {
            System.err.println("❌ Erreur getDossier : " + e.getMessage());
        }
        return null;
    }
 
    /**
     * Historique des consultations d'un patient.
     *
     * La table consultation utilise dossier_id (pas patient_id directement),
     * donc on joint via dossiermedical pour retrouver le patient.
     * La colonne date s'appelle date_consult.
     * La table ordonnance est liée via consultation_id et a : statut, instructions.
     */
    public List<Consultation> getHistorique(int patientId) {
        List<Consultation> liste = new ArrayList<>();
 
        String sql =
            "SELECT c.id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_medecin, " +
            "       m.specialite, " +
            "       c.date_consult, " +
            "       c.diagnostic, " +
            "       c.notes, " +
            "       o.statut          AS ordo_statut, " +
            "       o.instructions    AS ordo_instructions " +
            "FROM consultation c " +
            "JOIN dossiermedical dm  ON dm.id       = c.dossier_id " +
            "JOIN utilisateur u      ON u.id        = c.medecin_id " +
            "JOIN medecin m          ON m.id        = c.medecin_id " +
            "LEFT JOIN ordonnance o  ON o.consultation_id = c.id " +
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
                        ? rs.getString("diagnostic") : "—"
                );
                c.setNotes(
                    rs.getString("notes") != null
                        ? rs.getString("notes") : ""
                );
 
                // Résumer l'ordonnance depuis statut + instructions
                String ordoStatut  = rs.getString("ordo_statut");
                String ordoInstr   = rs.getString("ordo_instructions");
                if (ordoStatut != null) {
                    String resume = "Statut : " + ordoStatut;
                    if (ordoInstr != null && !ordoInstr.isBlank())
                        resume += "\n" + ordoInstr;
                    c.setOrdonnance(resume);
                }
 
                liste.add(c);
            }
 
        } catch (SQLException e) {
            System.err.println("❌ Erreur getHistorique : " + e.getMessage());
        }
        return liste;
    }
}