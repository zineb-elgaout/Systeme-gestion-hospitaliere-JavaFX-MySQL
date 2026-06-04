package application.controller;

import application.dao.PatientDAO;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PatientProfilController implements PatientSubController {

    @FXML private Label     nomCompletLabel;
    @FXML private Label     emailLabel;
    @FXML private Label     roleLabel;
    @FXML private Label     avatarLabel;
    @FXML private TextField telephoneField;
    @FXML private Label     dateNaissanceLabel;
    @FXML private TextField adresseField;
    @FXML private Label     groupeSanguinLabel;
    @FXML private TextField allergiesField;
    @FXML private Label     errorLabel;
    @FXML private Label     successLabel;

    private Utilisateur      utilisateur;
    private final PatientDAO dao = new PatientDAO();

    @Override
    public void initPatient(Utilisateur u, ViewManager vm, AccueilController p) {
        this.utilisateur = u;
        remplirProfil();
    }

    private void remplirProfil() {
        nomCompletLabel.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
        emailLabel.setText(utilisateur.getEmail());
        roleLabel.setText("Patient");
        telephoneField.setText(
            utilisateur.getTelephone() != null ? utilisateur.getTelephone() : ""
        );

        // Avatar avec initiales
        String initiales =
            String.valueOf(utilisateur.getPrenom().charAt(0)).toUpperCase() +
            String.valueOf(utilisateur.getNom().charAt(0)).toUpperCase();
        avatarLabel.setText(initiales);

        // Charger les infos patient depuis la base
        new Thread(() -> {
            String[] infos = dao.getInfosPatient(utilisateur.getId());
            Platform.runLater(() -> {
                dateNaissanceLabel.setText(
                    infos[0].isEmpty() ? "Non renseignée" : infos[0]
                );
                adresseField.setText(infos[1]);
                groupeSanguinLabel.setText(infos[2]);
                allergiesField.setText(infos[3]);
            });
        }).start();
    }

    @FXML
    private void handleSauvegarder() {
        String telephone = telephoneField.getText().trim();
        String adresse   = adresseField.getText().trim();
        String allergies = allergiesField.getText().trim();

        errorLabel.setVisible(false);
        new Thread(() -> {
            boolean ok = dao.mettreAJourProfil(
                utilisateur.getId(), telephone, adresse,
                allergies.isEmpty() ? "Aucune" : allergies
            );
            Platform.runLater(() -> {
                if (ok) {
                    successLabel.setText("✅  Profil mis à jour avec succès.");
                    successLabel.setVisible(true);
                    errorLabel.setVisible(false);
                    // Mettre à jour l'objet en mémoire
                    utilisateur.setTelephone(telephone);
                } else {
                    errorLabel.setText("❌  Erreur lors de la mise à jour.");
                    errorLabel.setVisible(true);
                    successLabel.setVisible(false);
                }
            });
        }).start();
    }
}