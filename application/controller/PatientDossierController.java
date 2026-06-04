package application.controller;
 
import application.dao.DossierMedicalDAO;
import application.model.Consultation;
import application.model.DossierMedical;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
 
import java.time.format.DateTimeFormatter;
import java.util.List;
 
public class PatientDossierController implements PatientSubController {
 
    // ── Résumé du dossier ─────────────────────────────────────
    @FXML private Label dateCreationLabel;
    @FXML private Label groupeSanguinLabel;
    @FXML private Label allergiesLabel;
    @FXML private Label antecedentsLabel;
 
    // ── Statistiques ──────────────────────────────────────────
    @FXML private Label nbConsultationsLabel;
    @FXML private Label derniereVisiteLabel;
 
    // ── Tableau consultations ─────────────────────────────────
    @FXML private TableView<Consultation>           tableConsultations;
    @FXML private TableColumn<Consultation, String> colDate;
    @FXML private TableColumn<Consultation, String> colMedecin;
    @FXML private TableColumn<Consultation, String> colSpecialite;
    @FXML private TableColumn<Consultation, String> colDiagnostic;
    @FXML private TableColumn<Consultation, String> colOrdonnance;
 
    // ── Détail consultation ───────────────────────────────────
    @FXML private Label detailDateLabel;
    @FXML private Label detailMedecinLabel;
    @FXML private Label detailSpecialiteLabel;
    @FXML private Label detailDiagnosticLabel;
    @FXML private Label detailNotesLabel;
    @FXML private Label detailOrdonnanceLabel;
    @FXML private Label detailOrdonnanceTitre;
 
    private Utilisateur            utilisateur;
    private final DossierMedicalDAO dao = new DossierMedicalDAO();
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    @Override
    public void initPatient(Utilisateur u, ViewManager vm, AccueilController p) {
        this.utilisateur = u;
        setupTable();
        setupSelectionListener();
        chargerDossier();
    }
 
    // ── Configuration du tableau ──────────────────────────────
    private void setupTable() {
        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getDate() != null
                    ? c.getValue().getDate().format(FMT) : ""
            )
        );
        colMedecin.setCellValueFactory(c ->
            new SimpleStringProperty("Dr. " + c.getValue().getNomMedecin())
        );
        colSpecialite.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getSpecialite())
        );
        colDiagnostic.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDiagnostic())
        );
        colOrdonnance.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getOrdonnance() != null ? "📋 Oui" : "—"
            )
        );
 
        // Ligne verte légère si ordonnance présente
        tableConsultations.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Consultation c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setStyle("");
                } else if (c.getOrdonnance() != null) {
                    setStyle("-fx-background-color:#e8f8f5;");
                } else {
                    setStyle("");
                }
            }
        });
    }
 
    // ── Listener sélection ────────────────────────────────────
    private void setupSelectionListener() {
        tableConsultations.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (newVal != null) afficherDetailConsultation(newVal);
            });
    }
 
    // ── Afficher détail consultation ──────────────────────────
    private void afficherDetailConsultation(Consultation c) {
        detailDateLabel.setText(
            c.getDate() != null ? c.getDate().format(FMT) : "—"
        );
        detailMedecinLabel.setText("Dr. " + c.getNomMedecin());
        detailSpecialiteLabel.setText(c.getSpecialite());
        detailDiagnosticLabel.setText(c.getDiagnostic());
        detailNotesLabel.setText(
            c.getNotes() != null && !c.getNotes().isBlank()
                ? c.getNotes() : "Aucune note."
        );
 
        if (c.getOrdonnance() != null && !c.getOrdonnance().isBlank()) {
            detailOrdonnanceTitre.setVisible(true);
            detailOrdonnanceTitre.setManaged(true);
            detailOrdonnanceLabel.setText(c.getOrdonnance());
            detailOrdonnanceLabel.setVisible(true);
            detailOrdonnanceLabel.setManaged(true);
        } else {
            detailOrdonnanceTitre.setVisible(false);
            detailOrdonnanceTitre.setManaged(false);
            detailOrdonnanceLabel.setVisible(false);
            detailOrdonnanceLabel.setManaged(false);
        }
    }
 
    // ── Charger le dossier et l'historique ────────────────────
    private void chargerDossier() {
        new Thread(() -> {
            DossierMedical dossier  = dao.getDossier(utilisateur.getId());
            List<Consultation> hist = dao.getHistorique(utilisateur.getId());
 
            Platform.runLater(() -> {
                // Résumé du dossier
                if (dossier != null) {
                    dateCreationLabel.setText(
                        dossier.getDateCreation() != null
                            ? dossier.getDateCreation().format(FMT) : "—"
                    );
                    groupeSanguinLabel.setText(dossier.getGroupeSanguin());
                    allergiesLabel.setText(dossier.getAllergies());
                    antecedentsLabel.setText(dossier.getAntecedents());
                }
 
                // Statistiques
                nbConsultationsLabel.setText(String.valueOf(hist.size()));
                if (!hist.isEmpty() && hist.get(0).getDate() != null) {
                    derniereVisiteLabel.setText(
                        hist.get(0).getDate().format(FMT)
                    );
                } else {
                    derniereVisiteLabel.setText("—");
                }
 
                // Tableau
                tableConsultations.setItems(
                    FXCollections.observableArrayList(hist)
                );
                if (!hist.isEmpty()) {
                    tableConsultations.getSelectionModel().selectFirst();
                }
            });
        }).start();
    }
 
    @FXML
    private void handleRefresh() {
        chargerDossier();
    }
}