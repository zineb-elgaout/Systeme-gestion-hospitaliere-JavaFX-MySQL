package application.controller;
 
import application.dao.FactureDAO;
import application.model.Facture;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
 
import java.time.format.DateTimeFormatter;
import java.util.List;
 
public class PatientFacturesController implements PatientSubController {
 
    // ── Résumé financier ──────────────────────────────────────
    @FXML private Label totalDuLabel;
    @FXML private Label totalPayeLabel;
    @FXML private Label totalRestantLabel;
 
    // ── Tableau ───────────────────────────────────────────────
    @FXML private TableView<Facture>           tableFactures;
    @FXML private TableColumn<Facture, String> colDate;
    @FXML private TableColumn<Facture, String> colMedecin;
    @FXML private TableColumn<Facture, String> colMontantTotal;
    @FXML private TableColumn<Facture, String> colMontantPaye;
    @FXML private TableColumn<Facture, String> colRestant;
    @FXML private TableColumn<Facture, String> colStatut;
 
    // ── Filtre ────────────────────────────────────────────────
    @FXML private ComboBox<String> filtreStatutCombo;
 
    // ── Panneau de détail ─────────────────────────────────────
    @FXML private Label detailDateLabel;
    @FXML private Label detailMedecinLabel;
    @FXML private Label detailTotalLabel;
    @FXML private Label detailPayeLabel;
    @FXML private Label detailRestantLabel;
    @FXML private Label detailStatutLabel;
 
    private Utilisateur   utilisateur;
    private final FactureDAO dao = new FactureDAO();
    private List<Facture> toutesLesFactures;
 
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    @Override
    public void initPatient(Utilisateur u, ViewManager vm, AccueilController p) {
        this.utilisateur = u;
        setupFiltres();
        setupTable();
        setupSelectionListener();
        chargerFactures();
    }
 
    // ── Filtres ───────────────────────────────────────────────
    private void setupFiltres() {
        filtreStatutCombo.getItems().addAll(
            "Toutes", "En attente", "Payées", "Annulées"
        );
        filtreStatutCombo.setValue("Toutes");
        filtreStatutCombo.setOnAction(e -> appliquerFiltre());
    }
 
    // ── Configuration du tableau ──────────────────────────────
    private void setupTable() {
        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getDateFacture() != null
                    ? c.getValue().getDateFacture().format(FMT) : ""
            )
        );
        colMedecin.setCellValueFactory(c ->
            new SimpleStringProperty("Dr. " + c.getValue().getNomMedecin())
        );
        colMontantTotal.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f MAD", c.getValue().getMontantTotal())
            )
        );
        colMontantPaye.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f MAD", c.getValue().getMontantPaye())
            )
        );
        colRestant.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f MAD", c.getValue().getMontantRestant())
            )
        );
        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatutLabel())
        );
 
        // Coloration des lignes selon le statut
        tableFactures.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Facture f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setStyle("");
                } else {
                    setStyle(switch (f.getStatut()) {
                        case PAYEE      -> "-fx-background-color:#e8f8f5;";
                        case EN_ATTENTE -> "-fx-background-color:#fef9e7;";
                        case ANNULEE    -> "-fx-background-color:#f2f3f4;";
                    });
                }
            }
        });
    }
 
    // ── Listener de sélection ─────────────────────────────────
    private void setupSelectionListener() {
        tableFactures.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (newVal != null) afficherDetail(newVal);
            });
    }
 
    // ── Afficher détail d'une facture ─────────────────────────
    private void afficherDetail(Facture f) {
        detailDateLabel.setText(
            f.getDateFacture() != null
                ? f.getDateFacture().format(FMT) : "—"
        );
        detailMedecinLabel.setText("Dr. " + f.getNomMedecin());
        detailTotalLabel.setText(
            String.format("%.2f MAD", f.getMontantTotal())
        );
        detailPayeLabel.setText(
            String.format("%.2f MAD", f.getMontantPaye())
        );
        detailRestantLabel.setText(
            String.format("%.2f MAD", f.getMontantRestant())
        );
        detailStatutLabel.setText(f.getStatutLabel());
        detailStatutLabel.setStyle(switch (f.getStatut()) {
            case PAYEE      -> "-fx-text-fill:#1e8449;-fx-font-weight:bold;";
            case EN_ATTENTE -> "-fx-text-fill:#d68910;-fx-font-weight:bold;";
            case ANNULEE    -> "-fx-text-fill:#7f8c8d;-fx-font-weight:bold;";
        });
    }
 
    // ── Charger les factures ──────────────────────────────────
    private void chargerFactures() {
        new Thread(() -> {
            toutesLesFactures = dao.getFacturesPatient(utilisateur.getId());
            double[] totaux   = dao.getTotauxPatient(utilisateur.getId());
 
            Platform.runLater(() -> {
                tableFactures.setItems(
                    FXCollections.observableArrayList(toutesLesFactures)
                );
                totalDuLabel.setText(
                    String.format("%.2f MAD", totaux[0])
                );
                totalPayeLabel.setText(
                    String.format("%.2f MAD", totaux[1])
                );
                totalRestantLabel.setText(
                    String.format("%.2f MAD", totaux[2])
                );
                totalRestantLabel.setStyle(
                    totaux[2] > 0
                        ? "-fx-text-fill:#c0392b;-fx-font-weight:bold;"
                        : "-fx-text-fill:#1e8449;-fx-font-weight:bold;"
                );
 
                // Sélectionner la première ligne automatiquement
                if (!toutesLesFactures.isEmpty()) {
                    tableFactures.getSelectionModel().selectFirst();
                }
            });
        }).start();
    }
 
    // ── Filtrer ───────────────────────────────────────────────
    @FXML
    private void appliquerFiltre() {
        if (toutesLesFactures == null) return;
 
        String filtre = filtreStatutCombo.getValue();
        List<Facture> filtrees = switch (filtre) {
            case "En attente" -> toutesLesFactures.stream()
                .filter(f -> f.getStatut() == Facture.Statut.EN_ATTENTE).toList();
            case "Payées"     -> toutesLesFactures.stream()
                .filter(f -> f.getStatut() == Facture.Statut.PAYEE).toList();
            case "Annulées"   -> toutesLesFactures.stream()
                .filter(f -> f.getStatut() == Facture.Statut.ANNULEE).toList();
            default           -> toutesLesFactures;
        };
 
        tableFactures.setItems(FXCollections.observableArrayList(filtrees));
    }
 
    @FXML
    private void handleRefresh() {
        chargerFactures();
    }
}