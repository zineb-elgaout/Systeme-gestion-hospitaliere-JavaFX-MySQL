package application.dao;

import java.sql.*;
import java.time.LocalDate;

public class OrdonnanceDAO {

    /**
     * Crée une ordonnance liée à une consultation.
     * Comme on n'a pas de consultation_id direct ici, on crée d'abord
     * une consultation puis l'ordonnance.
     */
   // ── Dans OrdonnanceDAO.creerOrdonnance() ──────────────────
public boolean creerOrdonnance(int patientId, int medecinId,
                                LocalDate date, String instructions) {
    // ✅ Utiliser ConsultationDAO
    ConsultationDAO consultationDAO = new ConsultationDAO();
    int consultationId = consultationDAO.creerConsultation(
        patientId, medecinId, date, "Ordonnance médicale");

    if (consultationId == -1) {
        System.err.println("❌ Consultation introuvable.");
        return false;
    }

    // Créer l'ordonnance avec consultationId
    String sql =
        "INSERT INTO ordonnance " +
        "(consultation_id, date_emission, instructions, statut) " +
        "VALUES (?, ?, ?, 'EN_ATTENTE')";

    try (Connection conn = DatabaseConnection.getInstance();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, consultationId);
        ps.setDate(2, Date.valueOf(date));
        ps.setString(3, instructions);
        ps.executeUpdate();

        System.out.println("✅ Ordonnance créée avec consultationId=" 
            + consultationId);
        return true;

    } catch (SQLException e) {
        System.err.println("❌ Erreur creerOrdonnance : " 
            + e.getMessage());
        return false;
    }
}
}