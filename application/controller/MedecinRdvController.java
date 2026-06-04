package application.controller;

import application.dao.PatientDAO;
import application.dao.RendezVousDAO;
import application.model.Medecin;
import application.model.RendezVous;
import application.model.Utilisateur;
import application.service.GmailService;
import application.view.ViewManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MedecinRdvController implements MedecinSubController {

    @FXML private Label                           medecinLabel;
    @FXML private TableView<RendezVous>           rdvTable;
    @FXML private TableColumn<RendezVous, String> colPatient;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colMotif;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private ComboBox<String>                filtreStatut;
    @FXML private DatePicker                      filtreDate;
    // ✅ Nouveau combo pour modifier le statut
    @FXML private ComboBox<String>                nouveauStatut;

    private final RendezVousDAO rdvDAO     = new RendezVousDAO();
    private final PatientDAO    patientDAO = new PatientDAO();
    private Medecin             medecin;
    private ViewManager         viewManager;
    private AccueilController   accueil;
    private ObservableList<RendezVous> tousLesRdv;

    @Override
    public void initMedecin(Medecin medecin, ViewManager viewManager,
                             AccueilController accueil) {
        this.medecin     = medecin;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        medecinLabel.setText("Dr. " + medecin.getNomComplet());

        // Filtre statut
        filtreStatut.getItems().addAll(
            "Tous", "En attente", "Confirmé", "Refusé", "Annulé", "Terminé");
        filtreStatut.setValue("Tous");

        // ✅ Combo modification statut
        nouveauStatut.getItems().addAll(
            "En attente", "Confirmé", "Refusé", "Annulé", "Terminé");
        nouveauStatut.setPromptText("Choisir nouveau statut");

        // ✅ Quand on sélectionne un RDV → pré-remplir le combo
        rdvTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    nouveauStatut.setValue(newVal.getStatutLabel());
                }
            });

        colPatient.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getNomMedecin()));
        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getDate() != null
                    ? c.getValue().getDate().toString() : "—"));
        colHeure.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getHeure() != null
                    ? c.getValue().getHeure().toString() : "—"));
        colMotif.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getMotif() != null
                    ? c.getValue().getMotif() : "—"));
        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatutLabel()));

        chargerRdv();
    }

    private void chargerRdv() {
        tousLesRdv = FXCollections.observableArrayList(
            rdvDAO.getRendezVousMedecin(medecin.getId())
        );
        rdvTable.setItems(tousLesRdv);
    }

    // ✅ Modifier le statut du RDV sélectionné ────────────────
    @FXML
    private void handleModifierStatut() {
        RendezVous selected = rdvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                "Sélectionnez un rendez-vous.", ButtonType.OK).showAndWait();
            return;
        }

        String statutChoisi = nouveauStatut.getValue();
        if (statutChoisi == null) {
            new Alert(Alert.AlertType.WARNING,
                "Choisissez un statut.", ButtonType.OK).showAndWait();
            return;
        }

        // Convertir le label lisible → enum Statut
        RendezVous.Statut nouveauStatutEnum = switch (statutChoisi) {
            case "Confirmé"    -> RendezVous.Statut.CONFIRME;
            case "Annulé"      -> RendezVous.Statut.ANNULE;
            case "Refusé"      -> RendezVous.Statut.ANNULE;  // ou REFUSE si vous avez cet enum
            case "Terminé"     -> RendezVous.Statut.TERMINE;
            case "En attente"  -> RendezVous.Statut.EN_ATTENTE;
            default            -> null;
        };

        if (nouveauStatutEnum == null) {
            new Alert(Alert.AlertType.ERROR, "Statut invalide.", ButtonType.OK).showAndWait();
            return;
        }

        // Snapshot avant le thread
        int       rdvId      = selected.getId();
        LocalDate dateRdv    = selected.getDate();
        LocalTime heureRdv   = selected.getHeure();
        String    motif      = selected.getMotif();
        String    nomMedecin = medecin.getNomComplet();
        RendezVous.Statut statutFinal = nouveauStatutEnum;

        new Thread(() -> {
            // 1. Mettre à jour le statut en base (méthode correcte : updateStatut)
            boolean ok = rdvDAO.updateStatut(rdvId, statutFinal);

            if (ok) {
                // 2. Récupérer email + nom du patient via l'id du RDV
                String[] patientInfo = rdvDAO.getEmailPatient(rdvId);

                if (patientInfo != null) {
                    String  emailPatient = patientInfo[0];
                    String  nomPatient   = patientInfo[1];
                    boolean emailOk      = true;

                    // 3. Envoyer le mail selon le statut choisi
                    if (statutChoisi.equals("Confirmé")) {
                        emailOk = GmailService.envoyerConfirmationRdvPatient(
                            emailPatient, nomPatient, nomMedecin,
                            dateRdv, heureRdv, motif);

                    } else if (statutChoisi.equals("Annulé")
                            || statutChoisi.equals("Refusé")) {
                        emailOk = GmailService.envoyerRefusRdvPatient(
                            emailPatient, nomPatient, nomMedecin,
                            dateRdv, heureRdv, motif);
                    }
                    // EN_ATTENTE / TERMINE → pas de mail

                    if (!emailOk)
                        System.err.println("⚠ Statut modifié mais e-mail non envoyé.");
                }
            }

            javafx.application.Platform.runLater(() -> {
                if (ok) {
                    handleActualiser();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                        "Erreur lors de la modification. Réessayez.",
                        ButtonType.OK).showAndWait();
                }
            });
        }).start();
    }

    // ── Dialog Nouveau RDV ────────────────────────────────────
    @FXML
    private void handleNouveauRdv() {
        Stage ownerStage = (Stage) rdvTable.getScene().getWindow();

        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox card = new VBox(0);
        card.getStyleClass().add("modal-card");
        card.setMaxHeight(Region.USE_PREF_SIZE);

        HBox header = new HBox(10);
        header.getStyleClass().add("modal-header");

        Label iconLbl = new Label("📅");
        iconLbl.getStyleClass().add("modal-icon");

        VBox headerText = new VBox(2);
        Label titre = new Label("Nouveau rendez-vous");
        titre.getStyleClass().add("modal-header-title");
        Label sous = new Label("Dr. " + medecin.getNomComplet());
        sous.getStyleClass().add("modal-header-subtitle");
        headerText.getChildren().addAll(titre, sous);

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("modal-close-btn");
        btnClose.setOnAction(e -> dialog.close());

        header.getChildren().addAll(iconLbl, headerText, spacerH, btnClose);

        VBox body = new VBox(10);
        body.getStyleClass().add("modal-body");

        List<Utilisateur> patients =
            patientDAO.getPatientsduMedecin(medecin.getId());

        ComboBox<String> patientCombo = new ComboBox<>();
        patients.forEach(p -> patientCombo.getItems().add(p.getNomComplet()));
        patientCombo.setPromptText("Sélectionner un patient");
        patientCombo.setMaxWidth(Double.MAX_VALUE);
        patientCombo.getStyleClass().add("modal-input");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.getStyleClass().add("modal-input");

        ComboBox<String> heureCombo = new ComboBox<>();
        heureCombo.getItems().addAll(
            "08:00","09:00","10:00","11:00","12:00",
            "13:00","14:00","15:00","16:00","17:00","18:00");
        heureCombo.setPromptText("Choisir une heure");
        heureCombo.setMaxWidth(Double.MAX_VALUE);
        heureCombo.getStyleClass().add("modal-input");

        TextField motifField = new TextField();
        motifField.setPromptText("Ex: Consultation générale, suivi...");
        motifField.getStyleClass().add("modal-input");
        motifField.setMaxWidth(Double.MAX_VALUE);

        HBox dateHeure = new HBox(12);
        VBox dateBox  = buildFieldBox("📆  Date",  datePicker);
        VBox heureBox = buildFieldBox("🕐  Heure", heureCombo);
        HBox.setHgrow(dateBox,  Priority.ALWAYS);
        HBox.setHgrow(heureBox, Priority.ALWAYS);
        dateHeure.getChildren().addAll(dateBox, heureBox);

        Label erreur = new Label();
        erreur.getStyleClass().add("modal-error-label");
        erreur.setVisible(false);
        erreur.setManaged(false);
        erreur.setWrapText(true);

        body.getChildren().addAll(
            buildFieldBox("👤  Patient", patientCombo),
            dateHeure,
            buildFieldBox("📝  Motif", motifField),
            erreur
        );

        HBox footer = new HBox(10);
        footer.getStyleClass().add("modal-footer");

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.getStyleClass().add("modal-btn-cancel");
        btnAnnuler.setOnAction(e -> dialog.close());

        Button btnConfirmer = new Button("✔  Confirmer le RDV");
        btnConfirmer.getStyleClass().add("modal-btn-confirm");
        btnConfirmer.setOnAction(e -> {
            if (patientCombo.getValue() == null) {
                showInlineError(erreur, "⚠ Veuillez sélectionner un patient.");
                return;
            }
            if (heureCombo.getValue() == null) {
                showInlineError(erreur, "⚠ Veuillez choisir une heure.");
                return;
            }
            if (datePicker.getValue() == null) {
                showInlineError(erreur, "⚠ Veuillez choisir une date.");
                return;
            }

            int index = patientCombo.getItems()
                .indexOf(patientCombo.getValue());
            Utilisateur patient = patients.get(index);
            LocalDate   date    = datePicker.getValue();
            LocalTime   heure   = LocalTime.parse(heureCombo.getValue());
            String      motif   = motifField.getText().trim();

            boolean ok = rdvDAO.creerRendezVous(
                patient.getId(), medecin.getId(), date, heure, motif);

            if (ok) {
                dialog.close();
                chargerRdv();
            } else {
                showInlineError(erreur,
                    "❌ Erreur lors de la création du rendez-vous.");
            }
        });

        footer.getChildren().addAll(btnAnnuler, btnConfirmer);
        card.getChildren().addAll(header, body, footer);

        StackPane root = new StackPane(card);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("modal-overlay");
        root.setMinWidth(ownerStage.getWidth());
        root.setMinHeight(ownerStage.getHeight());
        root.setMaxWidth(ownerStage.getWidth());
        root.setMaxHeight(ownerStage.getHeight());

        Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().addAll(
            ownerStage.getScene().getStylesheets());

        dialog.setScene(scene);

        card.setOpacity(0);
        card.setTranslateY(20);
        dialog.show();

        FadeTransition fade =
            new FadeTransition(Duration.millis(220), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide =
            new TranslateTransition(Duration.millis(220), card);
        slide.setFromY(20);
        slide.setToY(0);

        fade.play();
        slide.play();
    }

    private VBox buildFieldBox(String labelText,
                                javafx.scene.Node field) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("modal-field-label");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void showInlineError(Label erreur, String msg) {
        erreur.setText(msg);
        erreur.setManaged(true);
        erreur.setVisible(true);
        FadeTransition ft =
            new FadeTransition(Duration.millis(220), erreur);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    private void handleFiltrer() {
        String    statut = filtreStatut.getValue();
        LocalDate date   = filtreDate.getValue();
        ObservableList<RendezVous> filtres = tousLesRdv.filtered(rv -> {
            boolean statutOk = (statut == null || statut.equals("Tous"))
                || rv.getStatutLabel().equals(statut);
            boolean dateOk = (date == null)
                || (rv.getDate() != null && rv.getDate().equals(date));
            return statutOk && dateOk;
        });
        rdvTable.setItems(filtres);
    }

    @FXML
    private void handleActualiser() {
        filtreStatut.setValue("Tous");
        filtreDate.setValue(null);
        chargerRdv();
    }
}