package application.controller;

import application.dao.UtilisateurDAO;
import application.view.ViewManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {

    @FXML private TextField        nomField;
    @FXML private TextField        prenomField;
    @FXML private TextField        emailField;
    @FXML private TextField        telephoneField;
    @FXML private TextField        dateNaissanceField;
    @FXML private TextField        adresseField;
    @FXML private ComboBox<String> groupeSanguinCombo;
    @FXML private TextField        allergiesField;
    @FXML private PasswordField    passwordField;
    @FXML private PasswordField    confirmPasswordField;
    @FXML private Label            errorLabel;
    @FXML private Label            successLabel;
    @FXML private Label            emailStatusLabel;
    @FXML private Button           registerButton;
    @FXML private VBox             registerCard;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @FXML
    public void initialize() {
        groupeSanguinCombo.getItems().addAll(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        groupeSanguinCombo.setValue("A+");

        // Animation d'apparition
        registerCard.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), registerCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        // Vérification email en temps réel
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) checkEmailDisponible();
        });

        // Cacher messages quand l'user retape
        nomField.textProperty().addListener((o, ov, nv)      -> hideMessages());
        passwordField.textProperty().addListener((o, ov, nv) -> hideMessages());
    }

    // ── Vérification email en temps réel ─────────────────────
    private void checkEmailDisponible() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            emailStatusLabel.setVisible(false);
            return;
        }

        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            emailStatusLabel.setText("⚠  Format d'email invalide.");
            emailStatusLabel.setStyle("-fx-text-fill:#e74c3c;");
            emailStatusLabel.setVisible(true);
            return;
        }

        new Thread(() -> {
            boolean existe = dao.emailExiste(email);
            Platform.runLater(() -> {
                emailStatusLabel.setVisible(true);
                if (existe) {
                    emailStatusLabel.setText("❌  Cet email est déjà utilisé.");
                    emailStatusLabel.setStyle("-fx-text-fill:#e74c3c;");
                } else {
                    emailStatusLabel.setText("✅  Email disponible.");
                    emailStatusLabel.setStyle("-fx-text-fill:#1e8449;");
                }
            });
        }).start();
    }

    // ── Inscription ───────────────────────────────────────────
    @FXML
    private void handleRegister() {
        String nom             = nomField.getText().trim();
        String prenom          = prenomField.getText().trim();
        String email           = emailField.getText().trim();
        String telephone       = telephoneField.getText().trim();
        String dateNaissance   = dateNaissanceField.getText().trim();
        String adresse         = adresseField.getText().trim();
        String groupeSanguin   = groupeSanguinCombo.getValue();
        String allergies       = allergiesField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // ── Validations ────────────────────────────────────────
        if (nom.isEmpty() || prenom.isEmpty() ||
            email.isEmpty() || password.isEmpty()) {
            showError("⚠  Nom, Prénom, Email et Mot de passe sont obligatoires.");
            return;
        }

        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("⚠  Format d'email invalide.");
            return;
        }

        if (password.length() < 6) {
            showError("⚠  Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("⚠  Les mots de passe ne correspondent pas.");
            return;
        }

        if (!dateNaissance.isEmpty() &&
            !dateNaissance.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("⚠  Format date invalide. Utilisez : AAAA-MM-JJ");
            return;
        }

        // ── Désactiver bouton pendant l'opération ──────────────
        registerButton.setDisable(true);
        registerButton.setText("Création en cours...");
        hideMessages();

        // ── Thread inscription ─────────────────────────────────
        new Thread(() -> {

            // Vérifier email une dernière fois avant insertion
            if (dao.emailExiste(email)) {
                Platform.runLater(() -> {
                    showError("❌  Cet email est déjà utilisé.");
                    registerButton.setDisable(false);
                    registerButton.setText("Créer mon compte");
                });
                return;
            }

            boolean succes = dao.inscrirePatient(
                nom, prenom, email, password, telephone,
                dateNaissance.isEmpty() ? null : dateNaissance,
                adresse, groupeSanguin,
                allergies.isEmpty() ? "Aucune" : allergies
            );

            Platform.runLater(() -> {
                if (succes) {
                    // ✅ Naviguer vers login avec message de succès
                    naviguerVersLoginAvecSucces(
                        "✅  Compte créé avec succès !\n" +
                        "Vous pouvez maintenant vous connecter."
                    );
                } else {
                    showError("❌  Erreur lors de la création. Réessayez.");
                    registerButton.setDisable(false);
                    registerButton.setText("Créer mon compte");
                }
            });
        }).start();
    }

    // ── Navigation vers Login + message succès ────────────────
    private void naviguerVersLoginAvecSucces(String message) {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        ViewManager vm = new ViewManager(stage);

        // Passer le message au LoginController via ViewManager
        vm.showLoginAvecSucces(message);
    }

    // ── Retour Login ──────────────────────────────────────────
    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        new ViewManager(stage).showLogin();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showSuccess(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        FadeTransition ft = new FadeTransition(Duration.millis(300), successLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        dateNaissanceField.clear();
        adresseField.clear();
        allergiesField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        groupeSanguinCombo.setValue("A+");
        emailStatusLabel.setVisible(false);
    }
}