package application.controller;

import application.dao.UtilisateurDAO;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;   // ← nouveau
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;
    @FXML private Label         togglePasswordLabel;
    @FXML private Button        loginButton;
    @FXML private VBox          loginCard;

    @FXML private Region        errorSpacer;
    @FXML private Region        successSpacer;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    // ── Initialisation ────────────────────────────────────────
    @FXML
    public void initialize() {
        hideError();
        hideSuccess();

        // Connexion via touche Entrée
        passwordField.setOnAction(e -> handleLogin());
        passwordVisible.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());

        // Animation d'apparition de la carte
        loginCard.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), loginCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        // Cacher erreur quand l'user retape
        emailField.textProperty().addListener((o, ov, nv) -> hideError());
        passwordField.textProperty().addListener((o, ov, nv) -> hideError());
        passwordVisible.textProperty().addListener((o, ov, nv) -> hideError());
    }

    // ── Connexion ─────────────────────────────────────────────
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();

        // Lire le bon champ selon lequel est visible
        String mdp = passwordField.isVisible()
                ? passwordField.getText().trim()
                : passwordVisible.getText().trim();

        if (email.isEmpty() || mdp.isEmpty()) {
            showError("⚠  Veuillez remplir tous les champs.");
            return;
        }

        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("⚠  Format d'email invalide.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Connexion...");
        hideError();
        hideSuccess();

        new Thread(() -> {
            try {
                Utilisateur u = dao.authentifier(email, mdp);
                Platform.runLater(() -> {
                    if (u != null) {
                        System.out.println("✅ Redirection vers accueil...");
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        new ViewManager(stage).showAccueil(u);
                    } else {
                        showError("❌  Email ou mot de passe incorrect.");
                        shakeNode(loginCard);
                        // Vider les deux champs
                        passwordField.clear();
                        passwordVisible.clear();
                        loginButton.setDisable(false);
                        loginButton.setText("Se connecter");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("❌  Erreur de connexion : " + e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");
                });
            }
        }).start();
    }

    // ── Appelée par ViewManager après inscription réussie ─────
    public void afficherMessageSucces(String message) {
        showSuccess(message);
        PauseTransition pause = new PauseTransition(Duration.seconds(6));
        pause.setOnFinished(e -> hideSuccessAnime());
        pause.play();
    }

    // ── Toggle mot de passe ───────────────────────────────────
    @FXML
    private void handleTogglePassword() {
        boolean showing = passwordVisible.isVisible();

        if (showing) {
            // Masquer → repasser en PasswordField
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            togglePasswordLabel.setText("👁 Afficher");
        } else {
            // Afficher → passer en TextField
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordLabel.setText("🙈 Masquer");
        }
    }

    // ── Naviguer vers Register ────────────────────────────────
    @FXML
    private void handleGoToRegister() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        new ViewManager(stage).showRegister();
    }

    // ══ Helpers affichage labels ══════════════════════════════

    private void showError(String msg) {
        hideSuccess();
        errorLabel.setText(msg);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorSpacer.setManaged(true);
        errorSpacer.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorSpacer.setVisible(false);
        errorSpacer.setManaged(false);
    }

    private void showSuccess(String msg) {
        hideError();
        successLabel.setText(msg);
        successLabel.setManaged(true);
        successLabel.setVisible(true);
        successSpacer.setManaged(true);
        successSpacer.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(400), successLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideSuccess() {
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        successSpacer.setVisible(false);
        successSpacer.setManaged(false);
    }

    private void hideSuccessAnime() {
        FadeTransition ft = new FadeTransition(Duration.millis(500), successLabel);
        ft.setFromValue(1);
        ft.setOnFinished(e -> hideSuccess());
        ft.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt =
            new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0);
        tt.setByX(9);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}