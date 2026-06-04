package application.controller;

import application.dao.AdminDAO;
import application.model.Admin;
import application.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.Map;

public class AdminRapportsController implements AdminSubController {

    // ── Labels statistiques ───────────────────────────────────
    @FXML private Label adminLabel;
    @FXML private Label totalLabel;
    @FXML private Label medecinLabel;
    @FXML private Label patientLabel;
    @FXML private Label pharmacienLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label payeLabel;
    @FXML private Label attenteLabel;

    // ── Graphes ───────────────────────────────────────────────
    /** PieChart : répartition des utilisateurs par rôle */
    @FXML private PieChart pieRoles;

    /** BarChart : nombre de RDV par statut */
    @FXML private BarChart<String, Number> barRdv;
    @FXML private CategoryAxis xAxisRdv;
    @FXML private NumberAxis   yAxisRdv;

    /** BarChart : CA par médecin (factures payées) */
    @FXML private BarChart<String, Number> barCa;
    @FXML private CategoryAxis xAxisCa;
    @FXML private NumberAxis   yAxisCa;

    /** BarChart : Top 5 médicaments dispensés */
    @FXML private BarChart<String, Number> barMeds;
    @FXML private CategoryAxis xAxisMeds;
    @FXML private NumberAxis   yAxisMeds;

    /** PieChart : statut des factures */
    @FXML private PieChart pieFactures;

    private final AdminDAO dao = new AdminDAO();

    @Override
    public void initAdmin(Admin admin, ViewManager viewManager,
                          AccueilController accueil) {
        adminLabel.setText("Rapport — " + admin.getNomComplet());
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        chargerCartes();
        chargerPieRoles();
        chargerBarRdv();
        chargerBarCa();
        chargerBarMeds();
        chargerPieFactures();
        System.out.println("✅ Statistiques et graphes chargés.");
    }

    // ── Cartes chiffres clés ──────────────────────────────────
    private void chargerCartes() {
        totalLabel.setText(String.valueOf(dao.compterTotal()));
        medecinLabel.setText(String.valueOf(dao.compterParRole("MEDECIN")));
        patientLabel.setText(String.valueOf(dao.compterParRole("PATIENT")));
        pharmacienLabel.setText(String.valueOf(dao.compterParRole("PHARMACIEN")));
        adminCountLabel.setText(String.valueOf(dao.compterParRole("ADMIN")));

        double[] totaux = dao.getTotauxFactures();
        payeLabel.setText(String.format("%.0f MAD", totaux[0]));
        attenteLabel.setText(String.format("%.0f MAD", totaux[1]));
    }

    // ── PieChart : utilisateurs par rôle ─────────────────────
    private void chargerPieRoles() {
        pieRoles.getData().clear();
        Map<String, Integer> data = dao.getRepartitionParRole();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = traduireRole(entry.getKey())
                         + " (" + entry.getValue() + ")";
            pieRoles.getData().add(
                new PieChart.Data(label, entry.getValue()));
        }
        pieRoles.setLegendVisible(true);
        pieRoles.setLabelsVisible(true);
    }

    // ── BarChart : RDV par statut ─────────────────────────────
    private void chargerBarRdv() {
        barRdv.getData().clear();
        Map<String, Integer> data = dao.getRdvParStatut();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rendez-vous");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(
                new XYChart.Data<>(traduireStatutRdv(entry.getKey()),
                                   entry.getValue()));
        }
        barRdv.getData().add(series);
        barRdv.setLegendVisible(false);
    }

    // ── BarChart : CA par médecin ─────────────────────────────
    private void chargerBarCa() {
        barCa.getData().clear();
        Map<String, Double> data = dao.getCaParMedecin();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CA (MAD)");

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(
                new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barCa.getData().add(series);
        barCa.setLegendVisible(false);
    }

    // ── BarChart : Top 5 médicaments ──────────────────────────
    private void chargerBarMeds() {
        barMeds.getData().clear();
        Map<String, Integer> data = dao.getTopMedicaments();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantité");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            // Tronquer les noms trop longs pour l'axe
            String nom = entry.getKey().length() > 18
                ? entry.getKey().substring(0, 16) + "…"
                : entry.getKey();
            series.getData().add(new XYChart.Data<>(nom, entry.getValue()));
        }
        barMeds.getData().add(series);
        barMeds.setLegendVisible(false);
    }

    // ── PieChart : statut des factures ────────────────────────
    private void chargerPieFactures() {
        pieFactures.getData().clear();
        Map<String, Integer> data = dao.getFacturesParStatut();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = traduireStatutFacture(entry.getKey())
                         + " (" + entry.getValue() + ")";
            pieFactures.getData().add(
                new PieChart.Data(label, entry.getValue()));
        }
        pieFactures.setLegendVisible(true);
        pieFactures.setLabelsVisible(true);
    }

    @FXML
    private void handleActualiser() {
        chargerStatistiques();
    }

    // ── Helpers traduction ────────────────────────────────────
    private String traduireRole(String role) {
        return switch (role) {
            case "MEDECIN"    -> "Médecins";
            case "PATIENT"    -> "Patients";
            case "PHARMACIEN" -> "Pharmaciens";
            case "ADMIN"      -> "Admins";
            default           -> role;
        };
    }

    private String traduireStatutRdv(String statut) {
        return switch (statut) {
            case "CONFIRME"   -> "Confirmé";
            case "EN_ATTENTE" -> "En attente";
            case "REFUSE"     -> "Refusé";
            case "ANNULE"     -> "Annulé";
            case "TERMINE"    -> "Terminé";
            default           -> statut;
        };
    }

    private String traduireStatutFacture(String statut) {
        return switch (statut) {
            case "PAYEE"      -> "Payées";
            case "EN_ATTENTE" -> "En attente";
            case "ANNULEE"    -> "Annulées";
            default           -> statut;
        };
    }
}