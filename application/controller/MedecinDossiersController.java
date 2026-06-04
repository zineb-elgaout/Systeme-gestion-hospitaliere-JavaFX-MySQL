package application.controller;

import application.dao.DossierMedicalDAO;
import application.dao.PatientDAO;
import application.model.DossierMedical;
import application.model.Medecin;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;

public class MedecinDossiersController implements MedecinSubController {

    @FXML private Label                            medecinLabel;
    @FXML private TextField                        searchField;
    @FXML private TableView<Utilisateur>           patientsTable;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colTel;

    // ── Overlay modal (déclaré dans le FXML parent) ───────────
    @FXML private StackPane modalOverlay;

    // ── Champs du modal ───────────────────────────────────────
    @FXML private Label modalPatientNom;
    @FXML private Label modalDateCreation;
    @FXML private Label modalGroupeSanguin;
    @FXML private Label modalAntecedents;
    @FXML private Label modalAllergies;

    private final PatientDAO        patientDAO  = new PatientDAO();
    private final DossierMedicalDAO dossierDAO  = new DossierMedicalDAO();
    private Medecin           medecin;
    private ViewManager       viewManager;
    private AccueilController accueil;

    @Override
    public void initMedecin(Medecin medecin, ViewManager viewManager,
                             AccueilController accueil) {
        this.medecin     = medecin;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        medecinLabel.setText("Dr. " + medecin.getNomComplet());

        colNom.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEmail()));
        colTel.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getTelephone()));

        // Double-clic sur une ligne → ouvrir le dossier directement
        patientsTable.setRowFactory(tv -> {
            TableRow<Utilisateur> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    ouvrirDossierPatient(row.getItem());
                }
            });
            return row;
        });

        chargerDossiers();
    }

    // ── Chargement ─────────────────────────────────────────────
    private void chargerDossiers() {
        ObservableList<Utilisateur> liste =
            patientDAO.getPatientsduMedecin(medecin.getId());
        patientsTable.setItems(liste);
        System.out.println("✅ " + liste.size() + " patients chargés.");
    }

    @FXML
    private void handleRechercher() {
        String terme = searchField.getText().trim();
        if (terme.isEmpty()) {
            chargerDossiers();
        } else {
            ObservableList<Utilisateur> filtres =
                patientDAO.getPatientsduMedecin(medecin.getId())
                    .filtered(p ->
                        p.getNom().toLowerCase().contains(terme.toLowerCase()) ||
                        p.getPrenom().toLowerCase().contains(terme.toLowerCase())
                    );
            patientsTable.setItems(filtres);
        }
    }

    @FXML
    private void handleEffacerRecherche() {
        searchField.clear();
        chargerDossiers();
    }

    @FXML
    private void handleOuvrirDossier() {
        Utilisateur selection =
            patientsTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un patient dans le tableau.");
            return;
        }
        ouvrirDossierPatient(selection);
    }

    // ── Ouvrir le modal stylisé ────────────────────────────────
    private void ouvrirDossierPatient(Utilisateur patient) {
        DossierMedical dossier = dossierDAO.getDossier(patient.getId());
        if (dossier == null) {
            showWarning("Dossier introuvable",
                "Aucun dossier médical trouvé pour " + patient.getNomComplet() + ".");
            return;
        }

        // Remplir les labels du modal
        modalPatientNom.setText(patient.getNomComplet());
        modalDateCreation.setText(
            dossier.getDateCreation() != null ? dossier.getDateCreation().toString() : "–");
        modalGroupeSanguin.setText(
            dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "–");
        modalAntecedents.setText(
            dossier.getAntecedents() != null && !dossier.getAntecedents().isBlank()
                ? dossier.getAntecedents() : "Aucun antécédent renseigné");
        modalAllergies.setText(
            dossier.getAllergies() != null && !dossier.getAllergies().isBlank()
                ? dossier.getAllergies() : "Aucune allergie connue");

        // Afficher l'overlay avec animation
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        modalOverlay.setOpacity(0);

        // Trouver la carte modale (2ème enfant du StackPane)
        if (modalOverlay.getChildren().size() > 1) {
            javafx.scene.Node card = modalOverlay.getChildren().get(1);
            card.setScaleX(0.85);
            card.setScaleY(0.85);

            FadeTransition   ftOverlay = new FadeTransition(Duration.millis(220), modalOverlay);
            ftOverlay.setFromValue(0); ftOverlay.setToValue(1);

            ScaleTransition  stCard = new ScaleTransition(Duration.millis(280), card);
            stCard.setFromX(0.85); stCard.setToX(1.0);
            stCard.setFromY(0.85); stCard.setToY(1.0);

            new ParallelTransition(ftOverlay, stCard).play();
        } else {
            modalOverlay.setOpacity(1);
        }
    }

    @FXML
    private void handleFermerModal() {
        FadeTransition ft = new FadeTransition(Duration.millis(180), modalOverlay);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        });
        ft.play();
    }

    // ── Helper alerte simple ───────────────────────────────────
    private void showWarning(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}