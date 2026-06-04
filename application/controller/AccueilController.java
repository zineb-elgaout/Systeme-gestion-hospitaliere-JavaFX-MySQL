package application.controller;

import application.model.Admin;
import application.model.Medecin;
import application.model.Pharmacien;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class AccueilController {

    @FXML private Label      welcomeLabel;
    @FXML private Label      roleLabel;
    @FXML private Label      userInitialsLabel;
    @FXML private Label      emailLabel;
    @FXML private VBox       menuContainer;
    @FXML private BorderPane mainContent;

    private Utilisateur       utilisateur;
    private ViewManager       viewManager;
    private Button            activeButton;

    public void init(Utilisateur u, ViewManager vm) {
        this.utilisateur = u;
        this.viewManager = vm;
        updateUI();
    }

    private void updateUI() {
        welcomeLabel.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
        roleLabel.setText(getRoleLabel(utilisateur.getRole()));
        emailLabel.setText(utilisateur.getEmail());

        String initiales =
            String.valueOf(utilisateur.getPrenom().charAt(0)).toUpperCase() +
            String.valueOf(utilisateur.getNom().charAt(0)).toUpperCase();
        userInitialsLabel.setText(initiales);

        String color = switch (utilisateur.getRole()) {
            case MEDECIN    -> "#1a5276";
            case PHARMACIEN -> "#117a65";
            case ADMIN      -> "#7d3c98";
            case PATIENT    -> "#1f618d";
        };
        userInitialsLabel.setStyle(
            "-fx-background-color:" + color + ";" +
            "-fx-background-radius:30px;" +
            "-fx-text-fill:white;" +
            "-fx-font-size:22px;" +
            "-fx-font-weight:bold;" +
            "-fx-padding:14px 18px;"
        );

        buildMenu();
        if (!menuContainer.getChildren().isEmpty()) {
            Button premierBouton = (Button) menuContainer.getChildren().get(0);
            premierBouton.fire();
        }
    }

    private void buildMenu() {
        menuContainer.getChildren().clear();

        switch (utilisateur.getRole()) {
            case PATIENT -> {
                addMenuButton("📅", "Mes rendez-vous",     "patient_rendez_vous.fxml");
                addMenuButton("📋", "Mon dossier médical", "patient_dossier.fxml");
                addMenuButton("👤", "Mon profil",          "patient_profil.fxml");
                addMenuButton("💳", "Mes factures",        "patient_factures.fxml");
            }
            case MEDECIN -> {
                addMenuButton("📅", "Gestion des RDV",    "medecin_rdv.fxml");
                addMenuButton("👥", "Dossiers patients",  "medecin_dossiers.fxml");
                addMenuButton("📝", "Rédiger ordonnance", "medecin_ordonnance.fxml");
                addMenuButton("🗓", "Mon planning",       "medecin_planning.fxml");
                addMenuButton("💰", "Générer une facture","medecin_facture.fxml");
            }
            case PHARMACIEN -> {
                addMenuButton("💊", "Dispensation",     "pharmacien_dispensation.fxml");
                addMenuButton("📦", "Gestion du stock", "pharmacien_stock.fxml");
                addMenuButton("📋", "Ordonnances",      "pharmacien_ordonnances.fxml");
            }
            case ADMIN -> {
            	addMenuButton("📊", "Rapports",     "admin_rapports.fxml");
                addMenuButton("👤", "Utilisateurs", "admin_utilisateurs.fxml");
                addMenuButton("💳", "Paiements",    "admin_paiements.fxml");
            }
        }
    }

    private void addMenuButton(String icon, String texte, String fxmlFile) {
        Button btn = new Button(icon + "  " + texte);
        btn.getStyleClass().add("menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> loadSubView(fxmlFile, btn));
        menuContainer.getChildren().add(btn);
    }

    private void loadSubView(String fxmlFile, Button btn) {
        if (activeButton != null)
            activeButton.getStyleClass().remove("menu-btn-active");
        btn.getStyleClass().add("menu-btn-active");
        activeButton = btn;

        try {
            URL url = getClass().getResource(
                "/application/resources/" + fxmlFile);
            if (url == null) {
                System.err.println("❌ FXML introuvable : " + fxmlFile);
                showWelcome();
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Node vue = loader.load();
            Object ctrl = loader.getController();

            // ── Injection médecin ──────────────────────────
            if (ctrl instanceof MedecinSubController sub
                    && utilisateur instanceof Medecin m) {
                sub.initMedecin(m, viewManager, this);
            }
            if (ctrl instanceof AdminSubController sub
                    && utilisateur instanceof Admin m) {
                sub.initAdmin(m, viewManager, this);
            }

            // ✅ Injection patient — AJOUTÉE
            if (ctrl instanceof PatientSubController sub) {
                sub.initPatient(utilisateur, viewManager, this);
            }
            
            // ── Injection pharmacien ───────────────────────────
            if (ctrl instanceof PharmacienSubController sub
                    && utilisateur instanceof Pharmacien p) {
                sub.initPharmacien(p, viewManager, this);
            }

            vue.setOpacity(0);
            mainContent.setCenter(vue);
            FadeTransition ft = new FadeTransition(Duration.millis(300), vue);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile
                             + " : " + e.getMessage());
        }
    }

    public void showWelcome() {
        VBox welcome = new VBox(16);
        welcome.setAlignment(javafx.geometry.Pos.CENTER);
        welcome.setStyle("-fx-padding:40px;");

        Label icon = new Label("🏥");
        icon.setStyle("-fx-font-size:64px;");

        Label titre = new Label("Bienvenue, " + utilisateur.getPrenom() + " 👋");
        titre.getStyleClass().add("welcome-title");

        Label sous = new Label(
            "Vous êtes connecté(e) en tant que " +
            getRoleLabel(utilisateur.getRole()) + ".\n" +
            "Sélectionnez une option dans le menu."
        );
        sous.getStyleClass().add("welcome-subtitle");
        sous.setWrapText(true);
        sous.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        welcome.getChildren().addAll(icon, titre, sous);
        mainContent.setCenter(welcome);

        if (activeButton != null) {
            activeButton.getStyleClass().remove("menu-btn-active");
            activeButton = null;
        }
    }

    @FXML
    private void handleLogout() {
        viewManager.showLogin();
    }

    private String getRoleLabel(Utilisateur.Role role) {
        return switch (role) {
            case PATIENT    -> "Patient";
            case MEDECIN    -> "Médecin";
            case PHARMACIEN -> "Pharmacien";
            case ADMIN      -> "Administrateur";
        };
    }
}