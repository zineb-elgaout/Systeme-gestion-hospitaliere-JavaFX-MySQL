package application.controller;

import application.dao.PharmacienDAO;
import application.model.Medicament;
import application.model.Pharmacien;
import application.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PharmacienStockController implements PharmacienSubController {

    // ── Tableau ───────────────────────────────────────────────
    @FXML private TableView<Medicament>           table;
    @FXML private TableColumn<Medicament, String> colId;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, String> colDescription;
    @FXML private TableColumn<Medicament, String> colStock;
    @FXML private TableColumn<Medicament, String> colSeuil;
    @FXML private TableColumn<Medicament, String> colPrix;
    @FXML private TableColumn<Medicament, String> colEtat;

    // ── Filtres ───────────────────────────────────────────────
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filtreEtat;

    // ── Formulaire ────────────────────────────────────────────
    @FXML private TextField  nomField;
    @FXML private TextArea   descriptionArea;
    @FXML private TextField  stockField;
    @FXML private TextField  seuilField;
    @FXML private TextField  prixField;
    @FXML private Label      messageLabel;
    @FXML private Label      pharmacienLabel;
    @FXML private Label      totalLabel;
    @FXML private Label      ruptureLabel;
    @FXML private Button     btnModifier;
    @FXML private Button     btnSupprimer;

    private final PharmacienDAO dao = new PharmacienDAO();
    private Pharmacien           pharmacien;
    private ViewManager          viewManager;
    private AccueilController    accueil;
    private Medicament           selection;

    @Override
    public void initPharmacien(Pharmacien pharmacien, ViewManager viewManager,
                                AccueilController accueil) {
        this.pharmacien  = pharmacien;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        pharmacienLabel.setText("Pharmacien : " + pharmacien.getNomComplet());

        filtreEtat.getItems().addAll("Tous", "En stock", "En rupture");
        filtreEtat.setValue("Tous");

        // ── Colonnes ──────────────────────────────────────────
        colId.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNom.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getNom()));
        colDescription.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDescription()));
        colStock.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.valueOf(c.getValue().getQuantiteStock())));
        colSeuil.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.valueOf(c.getValue().getSeuilAlerte())));
        colPrix.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f €", c.getValue().getPrixUnitaire())));
        colEtat.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().estEnRupture() ? "⚠ Rupture" : "✅ OK"));

        // Couleur état
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(item.contains("Rupture")
                        ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
                        : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
                }
            }
        });

        // Sélection → remplir formulaire
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nouveau) -> {
                if (nouveau != null) remplirFormulaire(nouveau);
            });

        chargerStock();
    }

    private void chargerStock() {
        ObservableList<Medicament> liste = dao.getTousMedicaments();
        table.setItems(liste);
        totalLabel.setText("Total : " + dao.compterTotal());
        ruptureLabel.setText("Ruptures : " + dao.compterRuptures());
        afficherMessage("✅ " + liste.size() + " médicaments chargés.", false);
    }

    private void remplirFormulaire(Medicament m) {
        this.selection = m;
        nomField.setText(m.getNom());
        descriptionArea.setText(m.getDescription());
        stockField.setText(String.valueOf(m.getQuantiteStock()));
        seuilField.setText(String.valueOf(m.getSeuilAlerte()));
        prixField.setText(String.valueOf(m.getPrixUnitaire()));
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    @FXML
    private void handleAjouter() {
        if (!valider()) return;
        Medicament m = buildMedicament();
        if (dao.ajouterMedicament(m)) {
            afficherMessage("✅ Médicament ajouté.", false);
            chargerStock();
            viderFormulaire();
        } else {
            afficherMessage("❌ Erreur lors de l'ajout.", true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selection == null) {
            afficherMessage("⚠ Sélectionnez un médicament.", true);
            return;
        }
        if (!valider()) return;
        Medicament m = buildMedicament();
        m.setId(selection.getId());
        if (dao.modifierMedicament(m)) {
            afficherMessage("✅ Médicament modifié.", false);
            chargerStock();
            viderFormulaire();
        } else {
            afficherMessage("❌ Erreur modification.", true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selection == null) {
            afficherMessage("⚠ Sélectionnez un médicament.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + selection.getNom() + " ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (dao.supprimerMedicament(selection.getId())) {
                    afficherMessage("✅ Médicament supprimé.", false);
                    chargerStock();
                    viderFormulaire();
                } else {
                    afficherMessage("❌ Erreur suppression.", true);
                }
            }
        });
    }

    @FXML
    private void handleRechercher() {
        String terme = searchField.getText().trim();
        String etat  = filtreEtat.getValue();

        if (terme.isEmpty() && (etat == null || etat.equals("Tous"))) {
            chargerStock();
        } else if (!terme.isEmpty()) {
            table.setItems(dao.rechercherMedicament(terme));
        } else if ("En rupture".equals(etat)) {
            table.setItems(dao.getMedicamentsEnRupture());
        } else {
            chargerStock();
        }
    }

    @FXML
    private void handleVider() { viderFormulaire(); }

    private Medicament buildMedicament() {
        Medicament m = new Medicament();
        m.setNom(nomField.getText().trim());
        m.setDescription(descriptionArea.getText().trim());
        m.setQuantiteStock(Integer.parseInt(stockField.getText().trim()));
        m.setSeuilAlerte(Integer.parseInt(seuilField.getText().trim()));
        m.setPrixUnitaire(Double.parseDouble(prixField.getText().trim()));
        return m;
    }

    private boolean valider() {
        if (nomField.getText().isBlank() || stockField.getText().isBlank()
                || prixField.getText().isBlank()) {
            afficherMessage("⚠ Nom, stock et prix sont obligatoires.", true);
            return false;
        }
        try {
            Integer.parseInt(stockField.getText().trim());
            Integer.parseInt(seuilField.getText().trim());
            Double.parseDouble(prixField.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("⚠ Stock, seuil et prix doivent être numériques.", true);
            return false;
        }
        return true;
    }

    private void viderFormulaire() {
        nomField.clear();
        descriptionArea.clear();
        stockField.clear();
        seuilField.clear();
        prixField.clear();
        selection = null;
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        table.getSelectionModel().clearSelection();
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg);
        messageLabel.setStyle(erreur
            ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
            : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
    @FXML
    private void handleActualiser() {

        chargerStock();

        table.refresh();
        
        searchField.clear();

        afficherMessage("🔄 Stock actualisé avec succès.", false);
    }
    
}