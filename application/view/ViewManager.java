package application.view;

import application.controller.AccueilController;
import application.controller.LoginController;
import application.model.Utilisateur;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ViewManager {

    private final Stage stage;

    // ── Chemin de base des ressources ─────────────────────────
    //    src/ est la Sources Root, donc on part de /application/
    private static final String BASE = "/application/resources/";

    public ViewManager(Stage stage) {
        this.stage = stage;
        stage.setTitle("🏥 Système de Gestion Hospitalière");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setResizable(true);
    }

    // ── Login ─────────────────────────────────────────────────
 // ── Login normal ──────────────────────────────────────────
    public void showLogin() {
        loadScene("login.fxml", 920, 600, null);
    }

    // ── Login avec message de succès venant du Register ───────
    public void showLoginAvecSucces(String message) {
        try {
            String fullPath = BASE + "login.fxml";
            URL url = getClass().getResource(fullPath);

            if (url == null) {
                System.err.println("❌ login.fxml introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root  = loader.load();
            Scene  scene = new Scene(root, 920, 600);
            attachCSS(scene);

            // ── Injecter le message dans LoginController ───────
            LoginController loginCtrl = loader.getController();
            loginCtrl.afficherMessageSucces(message);

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur showLoginAvecSucces : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Accueil ───────────────────────────────────────────────
    public void showAccueil(Utilisateur utilisateur) {
        loadScene("accueil.fxml", 1200, 720, utilisateur);
    }
    
    // ── Register ───────────────────────────────────────────────
    public void showRegister() {
        loadScene("register.fxml", 980, 720, null);
    }

    // ── Méthode générique ─────────────────────────────────────
    private void loadScene(String fxmlFile, double width,
                           double height, Utilisateur utilisateur) {
        try {
            String fullPath = BASE + fxmlFile;
            URL url = getClass().getResource(fullPath);

            // ── Debug ──────────────────────────────────────────
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📂 Chemin demandé : " + fullPath);
            System.out.println("🔗 URL résolue    : " + url);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            if (url == null) {
                System.err.println("❌ Fichier introuvable : " + fullPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root  = loader.load();
            Scene  scene = new Scene(root, width, height);

            attachCSS(scene);

            if (utilisateur != null) {
                AccueilController ctrl = loader.getController();
                ctrl.init(utilisateur, this);
            }

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile
                             + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── CSS ───────────────────────────────────────────────────
    private void attachCSS(Scene scene) {
        URL cssUrl = getClass().getResource(BASE + "styles/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("✅ CSS chargé.");
        } else {
            System.err.println("⚠  style.css introuvable, styles ignorés.");
        }
    }

    public Stage getStage() { return stage; }
}