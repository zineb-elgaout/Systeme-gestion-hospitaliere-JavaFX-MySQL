package application.controller;

import application.dao.PharmacienDAO;
import application.model.Medicament;
import application.model.Pharmacien;
import application.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PharmacienDispensationController implements PharmacienSubController {

    @FXML private Label                        pharmacienLabel;
    @FXML private ComboBox<Medicament>         medicamentCombo;
    @FXML private TextField                    quantiteField;
    @FXML private TextField                    patientField;
    @FXML private TextField                    ordonnanceField;
    @FXML private Label                        stockDispoLabel;
    @FXML private Label                        prixTotalLabel;
    @FXML private Label                        messageLabel;

    @FXML private TableView<String[]>          historiqueTable;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private TableColumn<String[], String> colPatient;
    @FXML private TableColumn<String[], String> colMedicament;
    @FXML private TableColumn<String[], String> colQte;
    @FXML private TableColumn<String[], String> colMontant;

    private final PharmacienDAO              dao = new PharmacienDAO();
    private Pharmacien                        pharmacien;
    private ViewManager                       viewManager;
    private AccueilController                 accueil;

    // ← La liste est maintenant alimentée depuis la BD + nouvelles dispensations
    private final ObservableList<String[]>    historique =
        FXCollections.observableArrayList();

    @Override
    public void initPharmacien(Pharmacien pharmacien, ViewManager viewManager,
                                AccueilController accueil) {
        this.pharmacien  = pharmacien;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        pharmacienLabel.setText("Pharmacien : " + pharmacien.getNomComplet());

        // ── Remplir le combo médicaments depuis la BD ─────────
        ObservableList<Medicament> meds = dao.getTousMedicaments();
        medicamentCombo.setItems(meds);
        medicamentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Medicament m) {
                return m == null ? "" : m.getNom();
            }
            @Override public Medicament fromString(String s) { return null; }
        });

        // Mise à jour stock dispo et prix à chaque sélection
        medicamentCombo.valueProperty().addListener((obs, old, m) -> {
            if (m != null) {
                stockDispoLabel.setText("Stock disponible : " + m.getQuantiteStock());
                calculerPrix();
            }
        });

        quantiteField.textProperty().addListener((o, ov, nv) -> calculerPrix());

        // ── Colonnes historique ───────────────────────────────
        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[0]));
        colPatient.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[1]));
        colMedicament.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[2]));
        colQte.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[3]));
        colMontant.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[4]));

        historiqueTable.setItems(historique);

        // ── Charger l'historique depuis la BD ─────────────────
        chargerHistorique();
    }

    /**
     * Charge l'historique des dispensations depuis la base de données
     * (ordonnances DELIVREE / VALIDEE liées à ce pharmacien).
     */
    private void chargerHistorique() {
        historique.clear();
        ObservableList<String[]> lignesBD =
            dao.getHistoriqueDispensation(pharmacien.getId());
        historique.addAll(lignesBD);

        if (historique.isEmpty()) {
            afficherMessage("ℹ Aucune dispensation enregistrée.", false);
        } else {
            afficherMessage("✅ " + historique.size()
                + " dispensation(s) chargée(s).", false);
        }
    }

    private void calculerPrix() {
        Medicament m = medicamentCombo.getValue();
        if (m == null || quantiteField.getText().isBlank()) {
            prixTotalLabel.setText("—");
            return;
        }
        try {
            int qte = Integer.parseInt(quantiteField.getText().trim());
            double total = qte * m.getPrixUnitaire();
            prixTotalLabel.setText(String.format("%.2f MAD", total));
        } catch (NumberFormatException e) {
            prixTotalLabel.setText("—");
        }
    }

    @FXML
    private void handleDispenser() {
        Medicament m = medicamentCombo.getValue();

        if (m == null) {
            afficherMessage("⚠ Sélectionnez un médicament.", true);
            return;
        }
        if (patientField.getText().isBlank()) {
            afficherMessage("⚠ Nom du patient obligatoire.", true);
            return;
        }
        if (quantiteField.getText().isBlank()) {
            afficherMessage("⚠ Quantité obligatoire.", true);
            return;
        }

        int qte;
        try {
            qte = Integer.parseInt(quantiteField.getText().trim());
            if (qte <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherMessage("⚠ Quantité invalide (entier > 0).", true);
            return;
        }

        if (qte > m.getQuantiteStock()) {
            afficherMessage("❌ Stock insuffisant ("
                          + m.getQuantiteStock() + " disponibles).", true);
            return;
        }

        // ── Mettre à jour le stock en BD ──────────────────────
        int nouveauStock = m.getQuantiteStock() - qte;
        if (dao.mettreAJourStock(m.getId(), nouveauStock)) {
            m.setQuantiteStock(nouveauStock);
            stockDispoLabel.setText("Stock disponible : " + nouveauStock);

            // ── Ajouter en tête de l'historique (session courante) ─
            String date = java.time.LocalDate.now().toString();
            double montant = qte * m.getPrixUnitaire();

            historique.add(0, new String[]{
                date,
                patientField.getText().trim(),
                m.getNom(),
                String.valueOf(qte),
                String.format("%.2f MAD", montant)
            });

            String msg = "✅ Dispensation effectuée avec succès.";
            // ── Alerte rupture ────────────────────────────────
            if (m.estEnRupture()) {
                msg += "  ⚠ Stock bas : " + nouveauStock + " restants !";
            }
            afficherMessage(msg, false);
            viderFormulaire();

        } else {
            afficherMessage("❌ Erreur mise à jour stock.", true);
        }
    }

    @FXML
    private void handleEffacer() { viderFormulaire(); }

    private void viderFormulaire() {
        medicamentCombo.setValue(null);
        quantiteField.clear();
        patientField.clear();
        ordonnanceField.clear();
        prixTotalLabel.setText("—");
        stockDispoLabel.setText("Stock disponible : —");
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg);
        messageLabel.setStyle(erreur
            ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
            : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
}