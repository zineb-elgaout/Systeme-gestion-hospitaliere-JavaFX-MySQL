package application.dao;

import application.model.Facture;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class FactureDAO {

    // ── Factures d'un patient ─────────────────────────────────
    public List<Facture> getFacturesPatient(int patientId) {
        List<Facture> liste = new ArrayList<>();

        String sql =
            "SELECT f.id, f.patient_id, f.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_medecin, " +
            "       f.date_facture, f.montant_total, f.statut, " +
            "       COALESCE(SUM(p.montant), 0) AS montant_paye " +
            "FROM facture f " +
            "JOIN utilisateur u ON f.medecin_id = u.id " +
            "LEFT JOIN paiement p ON p.facture_id = f.id " +
            "WHERE f.patient_id = ? " +
            "GROUP BY f.id, f.patient_id, f.medecin_id, u.prenom, u.nom, " +
            "         f.date_facture, f.montant_total, f.statut " +
            "ORDER BY f.date_facture DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Facture f = new Facture();
                f.setId(rs.getInt("id"));
                f.setPatientId(rs.getInt("patient_id"));
                f.setMedecinId(rs.getInt("medecin_id"));
                f.setNomMedecin(rs.getString("nom_medecin"));

                Date sqlDate = rs.getDate("date_facture");
                if (sqlDate != null)
                    f.setDateFacture(sqlDate.toLocalDate());

                f.setMontantTotal(rs.getDouble("montant_total"));
                f.setMontantPaye(rs.getDouble("montant_paye"));
                f.setStatut(Facture.Statut.valueOf(rs.getString("statut")));
                liste.add(f);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getFacturesPatient : " + e.getMessage());
        }
        return liste;
    }

    // ── Totaux financiers ─────────────────────────────────────
    public double[] getTotauxPatient(int patientId) {
        String sql =
            "SELECT " +
            "  COALESCE(SUM(f.montant_total), 0) AS total_du, " +
            "  COALESCE(SUM(p.montant_paye), 0)  AS total_paye " +
            "FROM facture f " +
            "LEFT JOIN ( " +
            "    SELECT facture_id, SUM(montant) AS montant_paye " +
            "    FROM paiement " +
            "    GROUP BY facture_id " +
            ") p ON p.facture_id = f.id " +
            "WHERE f.patient_id = ? AND f.statut != 'ANNULEE'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double totalDu   = rs.getDouble("total_du");
                double totalPaye = rs.getDouble("total_paye");
                return new double[]{totalDu, totalPaye, totalDu - totalPaye};
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getTotauxPatient : " + e.getMessage());
        }
        return new double[]{0, 0, 0};
    }

    // ── Créer une facture avec consultation liée ──────────────
    /**
     * Crée une facture en BDD.
     * 1. Crée une consultation pour lier la facture
     * 2. Crée la facture avec consultation_id
     */
    // ── Dans FactureDAO.creerFacture() ────────────────────────
public boolean creerFacture(int patientId, int medecinId,
                             LocalDate date, double montant,
                             String acte) {
    // ✅ Utiliser ConsultationDAO
    ConsultationDAO consultationDAO = new ConsultationDAO();
    int consultationId = consultationDAO.creerConsultation(
        patientId, medecinId, date, acte);

    if (consultationId == -1) {
        System.err.println("❌ Consultation introuvable.");
        return false;
    }

    // Créer la facture avec consultationId
    String sql =
        "INSERT INTO facture " +
        "(patient_id, medecin_id, consultation_id, " +
        " date_facture, montant_total, statut) " +
        "VALUES (?, ?, ?, ?, ?, 'EN_ATTENTE')";

    try (Connection conn = DatabaseConnection.getInstance();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, patientId);
        ps.setInt(2, medecinId);
        ps.setInt(3, consultationId);
        ps.setDate(4, Date.valueOf(date));
        ps.setDouble(5, montant);
        ps.executeUpdate();

        System.out.println("✅ Facture créée avec consultationId=" 
            + consultationId);
        return true;

    } catch (SQLException e) {
        System.err.println("❌ Erreur creerFacture : " 
            + e.getMessage());
        return false;
    }
}

    // ── Factures d'un médecin ─────────────────────────────────
    public List<Facture> getFacturesMedecin(int medecinId) {
        List<Facture> liste = new ArrayList<>();

        String sql =
            "SELECT f.id, f.patient_id, f.medecin_id, " +
            "       CONCAT(u.prenom, ' ', u.nom) AS nom_patient, " +
            "       f.date_facture, f.montant_total, f.statut, " +
            "       COALESCE(SUM(p.montant), 0) AS montant_paye " +
            "FROM facture f " +
            "JOIN utilisateur u ON f.patient_id = u.id " +
            "LEFT JOIN paiement p ON p.facture_id = f.id " +
            "WHERE f.medecin_id = ? " +
            "GROUP BY f.id, f.patient_id, f.medecin_id, u.prenom, u.nom, " +
            "         f.date_facture, f.montant_total, f.statut " +
            "ORDER BY f.date_facture DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Facture f = new Facture();
                f.setId(rs.getInt("id"));
                f.setPatientId(rs.getInt("patient_id"));
                f.setMedecinId(rs.getInt("medecin_id"));
                f.setNomMedecin(rs.getString("nom_patient"));

                Date sqlDate = rs.getDate("date_facture");
                if (sqlDate != null)
                    f.setDateFacture(sqlDate.toLocalDate());

                f.setMontantTotal(rs.getDouble("montant_total"));
                f.setMontantPaye(rs.getDouble("montant_paye"));
                f.setStatut(Facture.Statut.valueOf(rs.getString("statut")));
                liste.add(f);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getFacturesMedecin : " 
                + e.getMessage());
        }
        return liste;
    }
    public List<Facture> getToutesFactures() {

        List<Facture> liste = new ArrayList<>();

        String sql =
            "SELECT f.id, " +
            "       CONCAT(p.prenom, ' ', p.nom) AS nom_patient, " +
            "       CONCAT(m.prenom, ' ', m.nom) AS nom_medecin, " +
            "       f.date_facture, " +
            "       f.montant_total, " +
            "       f.statut, " +
            "       COALESCE(SUM(pa.montant), 0) AS montant_paye " +
            "FROM facture f " +
            "JOIN utilisateur p ON f.patient_id = p.id " +
            "JOIN utilisateur m ON f.medecin_id = m.id " +
            "LEFT JOIN paiement pa ON pa.facture_id = f.id " +
            "GROUP BY f.id, " +
            "         p.prenom, p.nom, " +
            "         m.prenom, m.nom, " +
            "         f.date_facture, " +
            "         f.montant_total, " +
            "         f.statut " +
            "ORDER BY f.date_facture DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Facture f = new Facture();

                f.setId(rs.getInt("id"));

                f.setNomPatient(
                    rs.getString("nom_patient")
                );

                f.setNomMedecin(
                    rs.getString("nom_medecin")
                );

                Date sqlDate = rs.getDate("date_facture");

                if (sqlDate != null) {
                    f.setDateFacture(
                        sqlDate.toLocalDate()
                    );
                }

                f.setMontantTotal(
                    rs.getDouble("montant_total")
                );

                f.setMontantPaye(
                    rs.getDouble("montant_paye")
                );

                f.setStatut(
                    Facture.Statut.valueOf(
                        rs.getString("statut")
                    )
                );

                liste.add(f);
            }

        } catch (SQLException e) {

            System.err.println(
                "❌ Erreur getToutesFactures : "
                + e.getMessage()
            );
        }

        return liste;
    }
    public List<Facture> rechercherFactures(String terme) {

        List<Facture> liste = new ArrayList<>();

        String sql =
            "SELECT f.id, " +
            "       CONCAT(p.prenom, ' ', p.nom) AS nom_patient, " +
            "       CONCAT(m.prenom, ' ', m.nom) AS nom_medecin, " +
            "       f.date_facture, " +
            "       f.montant_total, " +
            "       f.statut, " +
            "       COALESCE(SUM(pa.montant), 0) AS montant_paye " +
            "FROM facture f " +
            "JOIN utilisateur p ON f.patient_id = p.id " +
            "JOIN utilisateur m ON f.medecin_id = m.id " +
            "LEFT JOIN paiement pa ON pa.facture_id = f.id " +
            "WHERE CONCAT(p.prenom, ' ', p.nom) LIKE ? " +
            "   OR CONCAT(m.prenom, ' ', m.nom) LIKE ? " +
            "   OR f.statut LIKE ? " +
            "GROUP BY f.id, " +
            "         p.prenom, p.nom, " +
            "         m.prenom, m.nom, " +
            "         f.date_facture, " +
            "         f.montant_total, " +
            "         f.statut " +
            "ORDER BY f.date_facture DESC";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String recherche = "%" + terme + "%";

            ps.setString(1, recherche);
            ps.setString(2, recherche);
            ps.setString(3, recherche);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Facture f = new Facture();

                f.setId(rs.getInt("id"));

                f.setNomPatient(
                    rs.getString("nom_patient")
                );

                f.setNomMedecin(
                    rs.getString("nom_medecin")
                );

                Date sqlDate = rs.getDate("date_facture");

                if (sqlDate != null) {
                    f.setDateFacture(
                        sqlDate.toLocalDate()
                    );
                }

                f.setMontantTotal(
                    rs.getDouble("montant_total")
                );

                f.setMontantPaye(
                    rs.getDouble("montant_paye")
                );

                f.setStatut(
                    Facture.Statut.valueOf(
                        rs.getString("statut")
                    )
                );

                liste.add(f);
            }

        } catch (SQLException e) {

            System.err.println(
                "❌ Erreur rechercherFactures : "
                + e.getMessage()
            );
        }

        return liste;
    }
}