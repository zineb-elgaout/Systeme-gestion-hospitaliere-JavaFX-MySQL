package application.controller;

import application.dao.RendezVousDAO;
import application.model.Medecin;
import application.model.RendezVous;
import application.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedecinPlanningController implements MedecinSubController {

    @FXML private Label     medecinLabel;
    @FXML private Label     semaineCourante;
    @FXML private GridPane  calendarGrid;

    // ✅ RendezVousDAO au lieu de MedecinDAO
    private final RendezVousDAO rdvDAO = new RendezVousDAO();
    private Medecin              medecin;
    private ViewManager          viewManager;
    private AccueilController    accueil;
    private LocalDate            lundiCourant;

    @Override
    public void initMedecin(Medecin medecin, ViewManager viewManager,
                             AccueilController accueil) {
        this.medecin     = medecin;
        this.viewManager = viewManager;
        this.accueil     = accueil;
        medecinLabel.setText("Dr. " + medecin.getNomComplet());
        lundiCourant = LocalDate.now().with(DayOfWeek.MONDAY);
        afficherSemaine();
    }

    private void afficherSemaine() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dimanche = lundiCourant.plusDays(6);

        semaineCourante.setText(
            "Semaine du " + lundiCourant.format(fmt) +
            " au "        + dimanche.format(fmt)
        );

        calendarGrid.getChildren().clear();

        // ── En-têtes jours ────────────────────────────────────
        String[] jours = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        for (int i = 0; i < 7; i++) {
            LocalDate jour = lundiCourant.plusDays(i);
            Label header = new Label(jours[i] + "\n" + jour.getDayOfMonth());
            header.setStyle(
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-padding:8px;" +
                "-fx-background-color:#1a5276;" +
                "-fx-text-fill:white;" +
                "-fx-min-width:100px;"
            );
            calendarGrid.add(header, i + 1, 0);
        }

        // ── Créneaux horaires vides ───────────────────────────
        String[] heures = {
            "08:00","09:00","10:00","11:00",
            "12:00","13:00","14:00","15:00",
            "16:00","17:00","18:00"
        };
        for (int h = 0; h < heures.length; h++) {
            Label heureLabel = new Label(heures[h]);
            heureLabel.setStyle(
                "-fx-padding:8px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#1a5276;" +
                "-fx-min-width:60px;"
            );
            calendarGrid.add(heureLabel, 0, h + 1);

            for (int j = 0; j < 7; j++) {
                Label creneau = new Label("");
                creneau.setStyle(
                    "-fx-border-color:#d5e8f5;" +
                    "-fx-border-width:0.5px;" +
                    "-fx-min-width:100px;" +
                    "-fx-min-height:40px;" +
                    "-fx-padding:4px;" +
                    "-fx-background-color:#f8fbff;"
                );
                calendarGrid.add(creneau, j + 1, h + 1);
            }
        }

        // ── Charger et afficher les RDV ───────────────────────
        List<RendezVous> rdvList = rdvDAO.getRendezVousMedecin(medecin.getId());

        // 🔍 DEBUG
        System.out.println("🗓 Semaine : " + lundiCourant + " → " + dimanche);
        System.out.println("📋 Total RDV chargés : " + rdvList.size());
        for (RendezVous rv : rdvList) {
            System.out.println("  RDV → date=" + rv.getDate()
                + " heure=" + rv.getHeure()
                + " patient=" + rv.getNomMedecin()
                + " statut=" + rv.getStatut());
        }

        for (RendezVous rv : rdvList) {
            LocalDate dateRdv = rv.getDate();
            if (dateRdv == null) continue;

            // Garder seulement les RDV de cette semaine
            if (dateRdv.isBefore(lundiCourant) || dateRdv.isAfter(dimanche)) continue;

            int colonne = dateRdv.getDayOfWeek().getValue(); // 1=Lun...7=Dim
            if (rv.getHeure() == null) continue;
            int heureRdv = rv.getHeure().getHour();
            int ligne = heureRdv - 8 + 1; // 08h=1, 09h=2...
            if (ligne < 1 || ligne > 11) continue;

            String couleur = switch (rv.getStatut()) {
                case CONFIRME   -> "#d5f5e3";
                case EN_ATTENTE -> "#fef9e7";
                case ANNULE     -> "#fadbd8";
                case REFUSE     -> "#f2d7d5";
                case TERMINE    -> "#d6eaf8";
            };

            Label rdvLabel = new Label(
                rv.getNomMedecin() + "\n" + rv.getStatutLabel()
            );
            rdvLabel.setStyle(
                "-fx-border-color:#aed6f1;" +
                "-fx-border-width:1px;" +
                "-fx-min-width:100px;" +
                "-fx-min-height:40px;" +
                "-fx-padding:4px;" +
                "-fx-font-size:11px;" +
                "-fx-background-color:" + couleur + ";"
            );
            calendarGrid.add(rdvLabel, colonne, ligne);
        }

        System.out.println("✅ Planning affiché pour Dr. " + medecin.getNomComplet());
    }

    @FXML private void handleSemainePrecedente() {
        lundiCourant = lundiCourant.minusWeeks(1);
        afficherSemaine();
    }

    @FXML private void handleSemaineSuivante() {
        lundiCourant = lundiCourant.plusWeeks(1);
        afficherSemaine();
    }

    @FXML private void handleAujordhui() {
        lundiCourant = LocalDate.now().with(DayOfWeek.MONDAY);
        afficherSemaine();
    }
}