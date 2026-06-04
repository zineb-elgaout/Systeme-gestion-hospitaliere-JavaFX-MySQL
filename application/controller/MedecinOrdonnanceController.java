package application.controller;

import application.dao.OrdonnanceDAO;
import application.dao.PatientDAO;
import application.model.Medecin;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class MedecinOrdonnanceController implements MedecinSubController {

    @FXML private ComboBox<String> patientCombo;
    @FXML private TextArea         prescriptionArea;
    @FXML private TextField        dureeField;
    @FXML private Label            medecinSignatureLabel;
    @FXML private DatePicker       dateOrdonnance;

    // ✅ Labels aperçu
    @FXML private Label apercuPatientLabel;
    @FXML private Label apercuDateLabel;
    @FXML private Label apercuPrescriptionLabel;
    @FXML private Label apercuDureeLabel;

    private final OrdonnanceDAO ordonnanceDAO = new OrdonnanceDAO();
    private final PatientDAO    patientDAO    = new PatientDAO();

    private Medecin           medecin;
    private ViewManager       viewManager;
    private AccueilController accueil;
    private List<Utilisateur> patients;

    @Override
    public void initMedecin(Medecin medecin, ViewManager viewManager,
                             AccueilController accueil) {
        this.medecin     = medecin;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        medecinSignatureLabel.setText(
            "Dr. " + medecin.getNomComplet() +
            (medecin.getSpecialite() != null
                ? "\n" + medecin.getSpecialite() : "")
        );

        // ✅ Charger les patients du médecin
        patients = patientDAO.getPatientsduMedecin(medecin.getId());
        patients.forEach(p -> patientCombo.getItems().add(p.getNomComplet()));

        // ✅ Listeners aperçu en temps réel
        patientCombo.valueProperty().addListener(
            (o, ov, nv) -> mettreAJourApercu());
        dateOrdonnance.valueProperty().addListener(
            (o, ov, nv) -> mettreAJourApercu());
        prescriptionArea.textProperty().addListener(
            (o, ov, nv) -> mettreAJourApercu());
        dureeField.textProperty().addListener(
            (o, ov, nv) -> mettreAJourApercu());
    }

    // ── Mise à jour aperçu en temps réel ─────────────────────
    private void mettreAJourApercu() {
        apercuPatientLabel.setText(
            patientCombo.getValue() != null
                ? "Patient : " + patientCombo.getValue()
                : "Patient : —");
        apercuDateLabel.setText(
            dateOrdonnance.getValue() != null
                ? "Date : " + dateOrdonnance.getValue()
                : "Date : —");
        apercuPrescriptionLabel.setText(
            !prescriptionArea.getText().isBlank()
                ? prescriptionArea.getText()
                : "—");
        apercuDureeLabel.setText(
            !dureeField.getText().isBlank()
                ? "Durée : " + dureeField.getText()
                : "");
    }

    // ── Générer l'ordonnance ──────────────────────────────────
    @FXML
    private void handleGenererOrdonnance() {
        // ── Validation ────────────────────────────────────────
        if (patientCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant",
                "Veuillez sélectionner un patient.");
            return;
        }
        if (prescriptionArea.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant",
                "Veuillez saisir la prescription.");
            return;
        }
        if (dateOrdonnance.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant",
                "Veuillez sélectionner une date.");
            return;
        }

        // ── Récupérer le patient sélectionné ─────────────────
        int index = patientCombo.getItems().indexOf(patientCombo.getValue());
        Utilisateur patient = patients.get(index);

        // ── Instructions complètes ────────────────────────────
        String base = prescriptionArea.getText().trim();
        final String instructionsFinales = dureeField.getText().isBlank()
            ? base
            : base + "\nDurée : " + dureeField.getText().trim();

        // ── Confirmation ──────────────────────────────────────
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'ordonnance");
        confirm.setHeaderText("Générer l'ordonnance pour "
            + patient.getNomComplet() + " ?");
        confirm.setContentText(
            "Date : " + dateOrdonnance.getValue() +
            "\n\nPrescription :\n" + prescriptionArea.getText()
        );

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean ok = ordonnanceDAO.creerOrdonnance(
                    patient.getId(),
                    medecin.getId(),
                    dateOrdonnance.getValue(),
                    instructionsFinales
                );

                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Ordonnance générée pour "
                            + patient.getNomComplet());
                    handleEffacer();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "❌ Impossible de générer l'ordonnance.");
                }
            }
        });
    }

    // ── Effacer le formulaire ─────────────────────────────────
    @FXML
    private void handleEffacer() {
        patientCombo.setValue(null);
        prescriptionArea.clear();
        dureeField.clear();
        dateOrdonnance.setValue(null);
        // ✅ Réinitialiser l'aperçu
        apercuPatientLabel.setText("Patient : —");
        apercuDateLabel.setText("Date : —");
        apercuPrescriptionLabel.setText("—");
        apercuDureeLabel.setText("");
    }

    // ── Utilitaire alert ──────────────────────────────────────
    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}