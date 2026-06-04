package application.controller;

import application.dao.RendezVousDAO;
import application.model.RendezVous;
import application.model.Utilisateur;
import application.service.GmailService;
import application.view.ViewManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientRendezVousController implements PatientSubController {

    @FXML private TableView<RendezVous>           tableRdv;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colMedecin;
    @FXML private TableColumn<RendezVous, String> colSpecialite;
    @FXML private TableColumn<RendezVous, String> colMotif;
    @FXML private TableColumn<RendezVous, String> colStatut;

    @FXML private VBox             formNouveauRdv;
    @FXML private ComboBox<String> medecinCombo;
    @FXML private DatePicker       datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private TextField        motifField;
    @FXML private Label            formErrorLabel;
    @FXML private Label            formSuccessLabel;
    @FXML private Button           btnNouveauRdv;

    private Utilisateur       utilisateur;
    private ViewManager       viewManager;
    private AccueilController parent;
    private final RendezVousDAO dao = new RendezVousDAO();
    private List<String[]> medecins;

    @Override
    public void initPatient(Utilisateur u, ViewManager vm, AccueilController p) {
        this.utilisateur = u;
        this.viewManager = vm;
        this.parent      = p;
        setupTable();
        setupForm();
        chargerRendezVous();
    }

    private void setupTable() {
        DateTimeFormatter dateFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");

        colDate.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDate() != null ? c.getValue().getDate().format(dateFmt) : ""));
        colHeure.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().getHeure() != null ? c.getValue().getHeure().format(heureFmt) : ""));
        colMedecin.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getNomMedecin()));
        colSpecialite.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialite()));
        colMotif.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getMotif()));
        colStatut.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getStatutLabel()));

        tableRdv.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RendezVous rv, boolean empty) {
                super.updateItem(rv, empty);
                if (empty || rv == null) { setStyle(""); return; }
                setStyle(switch (rv.getStatut()) {
                    case CONFIRME   -> "-fx-background-color:#e8f8f5;";
                    case EN_ATTENTE -> "-fx-background-color:#fef9e7;";
                    case ANNULE     -> "-fx-background-color:#fdedec;";
                    case TERMINE    -> "-fx-background-color:#f2f3f4;";
                    default -> throw new IllegalArgumentException("Unexpected: " + rv.getStatut());
                });
            }
        });
    }

    private void setupForm() {
        heureCombo.getItems().addAll(
            "08:00","08:30","09:00","09:30","10:00","10:30",
            "11:00","11:30","14:00","14:30","15:00","15:30",
            "16:00","16:30","17:00","17:30");
        heureCombo.setValue("09:00");

        datePicker.setValue(LocalDate.now().plusDays(1));
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        new Thread(() -> {
            medecins = dao.getMedecins();
            Platform.runLater(() -> {
                for (String[] med : medecins)
                    medecinCombo.getItems().add("Dr. " + med[1] + " — " + med[2]);
                if (!medecinCombo.getItems().isEmpty())
                    medecinCombo.getSelectionModel().selectFirst();
            });
        }).start();
    }

    private void chargerRendezVous() {
        new Thread(() -> {
            List<RendezVous> liste = dao.getRendezVousPatient(utilisateur.getId());
            Platform.runLater(() -> tableRdv.setItems(FXCollections.observableArrayList(liste)));
        }).start();
    }

    // ── Prendre un nouveau RDV ────────────────────────────────
    @FXML
    private void handleNouveauRdv() {
        int idx     = medecinCombo.getSelectionModel().getSelectedIndex();
        LocalDate date  = datePicker.getValue();
        String    heure = heureCombo.getValue();
        String    motif = motifField.getText().trim();

        if (idx < 0 || date == null || heure == null) {
            formErrorLabel.setText("⚠  Veuillez remplir tous les champs.");
            formErrorLabel.setVisible(true);
            formSuccessLabel.setVisible(false);
            return;
        }

        int medecinId       = Integer.parseInt(medecins.get(idx)[0]);
        LocalTime localTime = LocalTime.parse(heure);
        String motifFinal   = motif.isEmpty() ? "Consultation générale" : motif;

        // Snapshot des données pour le thread
        String patientNom = utilisateur.getPrenom() + " " + utilisateur.getNom();

        btnNouveauRdv.setDisable(true);
        btnNouveauRdv.setText("Envoi en cours...");
        formErrorLabel.setVisible(false);

        new Thread(() -> {
            // 1. Créer le RDV en base
            boolean ok = dao.creerRendezVous(
                utilisateur.getId(), medecinId, date, localTime, motifFinal);

            if (ok) {
                // 2. Récupérer email + nom du médecin
                String[] medecinInfo = dao.getEmailMedecin(medecinId);
                if (medecinInfo != null) {
                    // 3. ✅ Notifier le médecin par e-mail
                    boolean emailOk = GmailService.envoyerNouveauRdvMedecin(
                        medecinInfo[0],   // email médecin
                        medecinInfo[1],   // nom médecin
                        patientNom,
                        date,
                        localTime,
                        motifFinal
                    );
                    if (!emailOk)
                        System.err.println("⚠ RDV créé mais e-mail non envoyé.");
                }
            }

            Platform.runLater(() -> {
                if (ok) {
                    formSuccessLabel.setText("✅  Rendez-vous demandé avec succès !");
                    formSuccessLabel.setVisible(true);
                    formErrorLabel.setVisible(false);
                    motifField.clear();
                    chargerRendezVous();
                } else {
                    formErrorLabel.setText("❌  Erreur lors de la demande. Réessayez.");
                    formErrorLabel.setVisible(true);
                    formSuccessLabel.setVisible(false);
                }
                btnNouveauRdv.setDisable(false);
                btnNouveauRdv.setText("Demander ce rendez-vous");
            });
        }).start();
    }

    // ── Annuler le RDV sélectionné ────────────────────────────
    @FXML
    private void handleAnnulerRdv() {
        RendezVous selected = tableRdv.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                "Sélectionnez un rendez-vous à annuler.", ButtonType.OK).showAndWait();
            return;
        }
        if (selected.getStatut() == RendezVous.Statut.ANNULE ||
            selected.getStatut() == RendezVous.Statut.TERMINE) {
            new Alert(Alert.AlertType.INFORMATION,
                "Ce rendez-vous ne peut pas être annulé.", ButtonType.OK).showAndWait();
            return;
        }

        DateTimeFormatter dateFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Annuler le rendez-vous du " + selected.getDate().format(dateFmt)
            + " à " + selected.getHeure().format(heureFmt) + " ?",
            ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;

            int       rdvId      = selected.getId();
            int       medecinId  = selected.getMedecinId();
            LocalDate dateRdv    = selected.getDate();
            LocalTime heureRdv   = selected.getHeure();
            String    motif      = selected.getMotif();
            String    patientNom = utilisateur.getPrenom() + " " + utilisateur.getNom();

            new Thread(() -> {
                // 1. Annuler en base
                boolean ok = dao.annulerRendezVous(rdvId);

                if (ok) {
                    // 2. Récupérer email + nom du médecin
                    String[] medecinInfo = dao.getEmailMedecin(medecinId);
                    if (medecinInfo != null) {
                        // 3. ✅ Notifier le médecin par e-mail
                        boolean emailOk = GmailService.envoyerAnnulationRdvMedecin(
                            medecinInfo[0], medecinInfo[1],
                            patientNom, dateRdv, heureRdv, motif);
                        if (!emailOk)
                            System.err.println("⚠ RDV annulé mais e-mail non envoyé.");
                    }
                }

                Platform.runLater(() -> {
                    if (ok) chargerRendezVous();
                    else new Alert(Alert.AlertType.ERROR,
                            "Erreur lors de l'annulation. Réessayez.",
                            ButtonType.OK).showAndWait();
                });
            }).start();
        });
    }
}