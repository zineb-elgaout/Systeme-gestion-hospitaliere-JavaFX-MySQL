package application.controller;

import application.dao.FactureDAO;
import application.dao.PatientDAO;
import application.model.Medecin;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class MedecinFactureController implements MedecinSubController {

    @FXML private ComboBox<String> patientCombo;
    @FXML private ComboBox<String> acteCombo;
    @FXML private TextField        montantField;
    @FXML private TextField        mutuelleField;
    @FXML private Label            medecinLabel;
    @FXML private Label            totalLabel;
    @FXML private DatePicker       dateFacture;

    // ✅ Labels aperçu
    @FXML private Label apercuPatientLabel;
    @FXML private Label apercuActeLabel;
    @FXML private Label apercuDateLabel;
    @FXML private Label apercuMontantLabel;
    @FXML private Label apercuRestantLabel;

    private final PatientDAO patientDAO = new PatientDAO();
    private Medecin           medecin;
    private ViewManager       viewManager;
    private AccueilController accueil;

    // ✅ Garder la liste pour retrouver l'ID
    private List<Utilisateur> patients;

    @Override
    public void initMedecin(Medecin medecin, ViewManager viewManager,
                             AccueilController accueil) {
        this.medecin     = medecin;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        medecinLabel.setText("Dr. " + medecin.getNomComplet());

        acteCombo.getItems().addAll(
            "Consultation générale",
            "Consultation spécialisée",
            "Acte chirurgical",
            "Radio / Imagerie",
            "Analyses biologiques",
            "Autre"
        );

        // ✅ Charger les patients du médecin
        patients = patientDAO.getPatientsduMedecin(medecin.getId());
        patients.forEach(p -> patientCombo.getItems().add(p.getNomComplet()));

        // ✅ Listeners aperçu en temps réel
        patientCombo.valueProperty().addListener((o, ov, nv) -> mettreAJourApercu());
        acteCombo.valueProperty().addListener((o, ov, nv) -> mettreAJourApercu());
        montantField.textProperty().addListener((o, ov, nv) -> mettreAJourApercu());
        mutuelleField.textProperty().addListener((o, ov, nv) -> mettreAJourApercu());
        dateFacture.valueProperty().addListener((o, ov, nv) -> mettreAJourApercu());

        // ✅ Listener calcul reste à charge (barre du bas)
        montantField.textProperty().addListener((o, ov, nv) -> calculerTotal());
        mutuelleField.textProperty().addListener((o, ov, nv) -> calculerTotal());
    }

    // ── Calcul reste à charge (barre du bas) ─────────────────
    private void calculerTotal() {
        try {
            double montant  = Double.parseDouble(montantField.getText().trim());
            double mutuelle = mutuelleField.getText().isBlank() ? 0
                : Double.parseDouble(mutuelleField.getText().trim());
            totalLabel.setText(String.format("%.2f MAD", montant - mutuelle));
        } catch (NumberFormatException e) {
            totalLabel.setText("—");
        }
    }

    // ── Mise à jour aperçu en temps réel ─────────────────────
    private void mettreAJourApercu() {
        apercuPatientLabel.setText(
            patientCombo.getValue() != null
                ? "Patient : " + patientCombo.getValue() : "Patient : —");
        apercuActeLabel.setText(
            acteCombo.getValue() != null
                ? "Acte : " + acteCombo.getValue() : "Acte : —");
        apercuDateLabel.setText(
            dateFacture.getValue() != null
                ? "Date : " + dateFacture.getValue() : "Date : —");
        try {
            double montant  = Double.parseDouble(montantField.getText().trim());
            double mutuelle = mutuelleField.getText().isBlank() ? 0
                : Double.parseDouble(mutuelleField.getText().trim());
            apercuMontantLabel.setText(
                String.format("Montant : %.2f MAD", montant));
            apercuRestantLabel.setText(
                String.format("Reste à charge : %.2f MAD", montant - mutuelle));
        } catch (NumberFormatException e) {
            apercuMontantLabel.setText("Montant : —");
            apercuRestantLabel.setText("Reste à charge : —");
        }
    }

    // ── Générer la facture ────────────────────────────────────
   @FXML
private void handleGenererFacture() {
    if (patientCombo.getValue() == null
            || acteCombo.getValue() == null
            || montantField.getText().isBlank()
            || dateFacture.getValue() == null) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Champs manquants");
        alert.setContentText(
            "Veuillez remplir tous les champs obligatoires (*).");
        alert.showAndWait();
        return;
    }

    try {
        double montant = Double.parseDouble(montantField.getText().trim());
        int index = patientCombo.getItems().indexOf(patientCombo.getValue());
        Utilisateur patient = patients.get(index);

        // ✅ Utiliser FactureDAO.creerFacture() 
        // → crée consultation + facture avec consultation_id rempli
        FactureDAO factureDAO = new FactureDAO();
        boolean ok = factureDAO.creerFacture(
            patient.getId(),
            medecin.getId(),
            dateFacture.getValue(),
            montant,
            acteCombo.getValue()
        );

        if (ok) {
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setContentText("✅ Facture de " + montant
                + " MAD générée pour " + patient.getNomComplet());
            success.showAndWait();
            handleEffacer();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur");
            error.setContentText(
                "❌ Impossible de générer la facture.");
            error.showAndWait();
        }

    } catch (NumberFormatException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText("Montant invalide.");
        alert.showAndWait();
    }
}

    // ── Effacer le formulaire ─────────────────────────────────
    @FXML
    private void handleEffacer() {
        patientCombo.setValue(null);
        acteCombo.setValue(null);
        montantField.clear();
        mutuelleField.clear();
        totalLabel.setText("—");
        // ✅ Réinitialiser l'aperçu
        apercuPatientLabel.setText("Patient : —");
        apercuActeLabel.setText("Acte : —");
        apercuDateLabel.setText("Date : —");
        apercuMontantLabel.setText("Montant : —");
        apercuRestantLabel.setText("Reste à charge : —");
    }
}