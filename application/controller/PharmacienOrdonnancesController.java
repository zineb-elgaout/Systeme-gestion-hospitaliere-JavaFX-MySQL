package application.controller;

import application.dao.PharmacienDAO;
import application.model.Pharmacien;
import application.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PharmacienOrdonnancesController implements PharmacienSubController {

    @FXML private Label                        pharmacienLabel;
    @FXML private TextField                    searchField;
    @FXML private TableView<String[]>          table;
    @FXML private TableColumn<String[], String> colNumero;
    @FXML private TableColumn<String[], String> colPatient;
    @FXML private TableColumn<String[], String> colMedecin;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private TableColumn<String[], String> colStatut;
    @FXML private Label                        messageLabel;

    // Détail ordonnance
    @FXML private Label           detailNumero;
    @FXML private Label           detailPatient;
    @FXML private Label           detailMedecin;
    @FXML private Label           detailDate;
    @FXML private TextArea        detailPrescription;
    @FXML private ComboBox<String> statutCombo;

    private final PharmacienDAO           dao = new PharmacienDAO();
    private Pharmacien                     pharmacien;
    private ViewManager                    viewManager;
    private AccueilController              accueil;

    // Cache complet pour filtrage sans retour BD
    private final ObservableList<String[]> toutesOrdonnances =
        FXCollections.observableArrayList();
    // Liste affichée dans la table
    private final ObservableList<String[]> ordonnancesAffichees =
        FXCollections.observableArrayList();

    @Override
    public void initPharmacien(Pharmacien pharmacien, ViewManager viewManager,
                                AccueilController accueil) {
        this.pharmacien  = pharmacien;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        pharmacienLabel.setText("Pharmacien : " + pharmacien.getNomComplet());

        // ── Configuration des colonnes ────────────────────────
        colNumero.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[0]));   // id ordonnance
        colPatient.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[1]));   // patient
        colMedecin.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[2]));   // médecin
        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[3]));   // date émission
        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue()[4]));   // statut (Fr)

        // ── Couleur selon statut ──────────────────────────────
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "Dispensée"  ->
                            "-fx-text-fill:#27ae60;-fx-font-weight:bold;";
                        case "Validée"    ->
                            "-fx-text-fill:#2980b9;-fx-font-weight:bold;";
                        case "En attente" ->
                            "-fx-text-fill:#e67e22;-fx-font-weight:bold;";
                        case "Expirée"    ->
                            "-fx-text-fill:#e74c3c;-fx-font-weight:bold;";
                        default -> "";
                    });
                }
            }
        });

        // ── ComboBox statuts disponibles ──────────────────────
        statutCombo.getItems().addAll(
            "En attente", "Validée", "Dispensée", "Expirée");

        // ── Sélection → afficher le détail ───────────────────
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nouveau) -> {
                if (nouveau != null) afficherDetail(nouveau);
            });

        table.setItems(ordonnancesAffichees);

        // ── Chargement initial depuis la BD ───────────────────
        chargerOrdonnances();
    }

    /**
     * Charge toutes les ordonnances depuis la BD et met à jour la table.
     */
    private void chargerOrdonnances() {
        toutesOrdonnances.clear();
        ordonnancesAffichees.clear();
        viderDetail();

        ObservableList<String[]> lignesBD = dao.getToutesOrdonnances();
        toutesOrdonnances.addAll(lignesBD);
        ordonnancesAffichees.addAll(lignesBD);

        if (ordonnancesAffichees.isEmpty()) {
            afficherMessage("ℹ Aucune ordonnance trouvée en base.", false);
        } else {
            afficherMessage("✅ " + ordonnancesAffichees.size()
                + " ordonnance(s) chargée(s).", false);
        }
    }

    /**
     * Affiche le détail d'une ordonnance sélectionnée dans le panneau droit.
     * Charge aussi les lignes de médicaments depuis la BD.
     */
    private void afficherDetail(String[] o) {
        detailNumero.setText("N° " + o[0]);
        detailPatient.setText(o[1]);
        detailMedecin.setText(o[2]);
        detailDate.setText(o[3]);
        statutCombo.setValue(o[4]);

        // ── Charger les médicaments prescrits depuis la BD ────
        int ordonnanceId = Integer.parseInt(o[6]);
        ObservableList<String> lignes = dao.getLignesOrdonnance(ordonnanceId);

        if (lignes.isEmpty()) {
            // Afficher les instructions si pas de lignes détaillées
            String instructions = o[5] != null && !o[5].isEmpty()
                ? o[5]
                : "Aucune prescription détaillée.";
            detailPrescription.setText(instructions);
        } else {
            // Construire le texte de prescription à partir des lignes
            StringBuilder sb = new StringBuilder();
            if (o[5] != null && !o[5].isEmpty()) {
                sb.append("Instructions : ").append(o[5]).append("\n\n");
            }
            sb.append("Médicaments prescrits :\n");
            sb.append("─────────────────────\n");
            for (String ligne : lignes) {
                sb.append("• ").append(ligne).append("\n\n");
            }
            detailPrescription.setText(sb.toString().trim());
        }
    }

    /** Recherche locale sans retour BD (rapide), avec fallback BD si nécessaire. */
    @FXML
    private void handleRechercher() {
        String terme = searchField.getText().trim();

        if (terme.isEmpty()) {
            // Restaurer la liste complète
            ordonnancesAffichees.clear();
            ordonnancesAffichees.addAll(toutesOrdonnances);
            afficherMessage("✅ " + ordonnancesAffichees.size()
                + " ordonnance(s).", false);
            return;
        }

        // ── Recherche en BD (inclut des critères étendus) ─────
        ObservableList<String[]> resultats = dao.rechercherOrdonnances(terme);
        ordonnancesAffichees.clear();
        ordonnancesAffichees.addAll(resultats);
        viderDetail();

        if (resultats.isEmpty()) {
            afficherMessage("ℹ Aucun résultat pour « " + terme + " ».", false);
        } else {
            afficherMessage("🔍 " + resultats.size()
                + " résultat(s) pour « " + terme + " ».", false);
        }
    }

    /**
     * Valide le nouveau statut en BD et rafraîchit la ligne dans la table.
     */
    @FXML
    private void handleValiderStatut() {
        String[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("⚠ Sélectionnez une ordonnance.", true);
            return;
        }

        String nouveauStatut = statutCombo.getValue();
        if (nouveauStatut == null) {
            afficherMessage("⚠ Choisissez un statut.", true);
            return;
        }

        int ordonnanceId = Integer.parseInt(sel[6]);
        boolean ok = dao.mettreAJourStatutOrdonnance(
            ordonnanceId, nouveauStatut, pharmacien.getId());

        if (ok) {
            // Mettre à jour en mémoire pour éviter un rechargement complet
            sel[4] = nouveauStatut;
            table.refresh();
            afficherMessage("✅ Statut mis à jour : " + nouveauStatut, false);
        } else {
            afficherMessage("❌ Erreur lors de la mise à jour du statut.", true);
        }
    }

    /** Vide le panneau de détail. */
    private void viderDetail() {
        detailNumero.setText("");
        detailPatient.setText("");
        detailMedecin.setText("");
        detailDate.setText("");
        detailPrescription.setText("");
        statutCombo.setValue(null);
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg);
        messageLabel.setStyle(erreur
            ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
            : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
    
    @FXML
    private void handleActualiser() {

        chargerOrdonnances();

        table.refresh();

        searchField.clear();

        afficherMessage(
            "🔄 Ordonnances actualisées avec succès.",
            false
        );
    }
}