package application.controller;

import application.dao.AdminDAO;
import application.model.Admin;
import application.model.Utilisateur;
import application.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class AdminUtilisateursController implements AdminSubController {

    // ── Tableau ───────────────────────────────────────────────
    @FXML private TableView<Utilisateur>            table;
    @FXML private TableColumn<Utilisateur, String>  colId;
    @FXML private TableColumn<Utilisateur, String>  colNom;
    @FXML private TableColumn<Utilisateur, String>  colPrenom;
    @FXML private TableColumn<Utilisateur, String>  colEmail;
    @FXML private TableColumn<Utilisateur, String>  colTel;
    @FXML private TableColumn<Utilisateur, String>  colRole;

    // ── Filtres ───────────────────────────────────────────────
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  filtreRole;

    // ── Formulaire ────────────────────────────────────────────
    @FXML private TextField         nomField;
    @FXML private TextField         prenomField;
    @FXML private TextField         emailField;
    @FXML private TextField         telField;
    @FXML private PasswordField     mdpField;
    @FXML private ComboBox<String>  roleCombo;
    @FXML private Label             messageLabel;
    @FXML private Button            btnAjouter;
    @FXML private Button            btnModifier;
    @FXML private Button            btnSupprimer;
    @FXML private Label             adminLabel;

    private final AdminDAO        dao = new AdminDAO();
    private Admin                 admin;
    private ViewManager           viewManager;
    private AccueilController     accueil;
    private Utilisateur           selection;

    @Override
    public void initAdmin(Admin admin, ViewManager viewManager,
                          AccueilController accueil) {
        this.admin       = admin;
        this.viewManager = viewManager;
        this.accueil     = accueil;

        adminLabel.setText("Administrateur : " + admin.getNomComplet());

        // Remplir les combos
        filtreRole.getItems().addAll(
            "Tous", "MEDECIN", "PATIENT", "PHARMACIEN", "ADMIN");
        filtreRole.setValue("Tous");

        roleCombo.getItems().addAll(
            "MEDECIN", "PATIENT", "PHARMACIEN", "ADMIN");

        // Colonnes
        colId.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNom.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEmail()));
        colTel.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getTelephone()));
        colRole.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRole().name()));

        // Sélection dans le tableau → remplir formulaire
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nouveau) -> {
                if (nouveau != null) remplirFormulaire(nouveau);
            });

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        ObservableList<Utilisateur> liste = dao.getTousUtilisateurs();
        table.setItems(liste);
        afficherMessage("✅ " + liste.size() + " utilisateurs chargés.", false);
    }

    private void remplirFormulaire(Utilisateur u) {
        this.selection = u;
        nomField.setText(u.getNom());
        prenomField.setText(u.getPrenom());
        emailField.setText(u.getEmail());
        telField.setText(u.getTelephone());
        roleCombo.setValue(u.getRole().name());
        mdpField.clear();
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire(true)) return;

        if (dao.emailExiste(emailField.getText().trim())) {
            afficherMessage("❌ Cet email existe déjà.", true);
            return;
        }

        Utilisateur u = new Utilisateur();
        u.setNom(nomField.getText().trim());
        u.setPrenom(prenomField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setTelephone(telField.getText().trim());
        u.setRole(Utilisateur.Role.valueOf(roleCombo.getValue()));

        if (dao.ajouterUtilisateur(u, mdpField.getText())) {
            afficherMessage("✅ Utilisateur ajouté avec succès.", false);
            chargerUtilisateurs();
            viderFormulaire();
        } else {
            afficherMessage("❌ Erreur lors de l'ajout.", true);
        }
    }

    @FXML
    private void handleModifier() {
        if (selection == null) {
            afficherMessage("⚠ Sélectionnez un utilisateur.", true);
            return;
        }
        if (!validerFormulaire(false)) return;

        selection.setNom(nomField.getText().trim());
        selection.setPrenom(prenomField.getText().trim());
        selection.setEmail(emailField.getText().trim());
        selection.setTelephone(telField.getText().trim());
        selection.setRole(Utilisateur.Role.valueOf(roleCombo.getValue()));

        if (dao.modifierUtilisateur(selection)) {
            afficherMessage("✅ Utilisateur modifié avec succès.", false);
            chargerUtilisateurs();
            viderFormulaire();
        } else {
            afficherMessage("❌ Erreur lors de la modification.", true);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selection == null) {
            afficherMessage("⚠ Sélectionnez un utilisateur.", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + selection.getNomComplet() + " ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (dao.supprimerUtilisateur(selection.getId())) {
                    afficherMessage("✅ Utilisateur supprimé.", false);
                    chargerUtilisateurs();
                    viderFormulaire();
                } else {
                    afficherMessage("❌ Erreur lors de la suppression.", true);
                }
            }
        });
    }

    @FXML
    private void handleReinitMdp() {
        if (selection == null) {
            afficherMessage("⚠ Sélectionnez un utilisateur.", true);
            return;
        }
        String nouveauMdp = "Hopital2024!";
        if (dao.reinitialiserMotDePasse(selection.getId(), nouveauMdp)) {
            afficherMessage("✅ Mot de passe réinitialisé : " + nouveauMdp, false);
        } else {
            afficherMessage("❌ Erreur réinitialisation.", true);
        }
    }

    @FXML
    private void handleRechercher() {
        String terme = searchField.getText().trim();
        String role  = filtreRole.getValue();

        if (terme.isEmpty() && (role == null || role.equals("Tous"))) {
            chargerUtilisateurs();
        } else if (!terme.isEmpty()) {
            ObservableList<Utilisateur> res = dao.rechercherUtilisateur(terme);
            table.setItems(res);
            afficherMessage("🔍 " + res.size() + " résultat(s).", false);
        } else {
            ObservableList<Utilisateur> res = dao.getUtilisateursByRole(role);
            table.setItems(res);
            afficherMessage("🔍 " + res.size() + " résultat(s).", false);
        }
    }

    @FXML
    private void handleViderFormulaire() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        nomField.clear(); prenomField.clear();
        emailField.clear(); telField.clear();
        mdpField.clear(); roleCombo.setValue(null);
        selection = null;
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        table.getSelectionModel().clearSelection();
    }

    private boolean validerFormulaire(boolean mdpObligatoire) {
        if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                || emailField.getText().isBlank() || roleCombo.getValue() == null) {
            afficherMessage("⚠ Nom, prénom, email et rôle sont obligatoires.", true);
            return false;
        }
        if (mdpObligatoire && mdpField.getText().isBlank()) {
            afficherMessage("⚠ Le mot de passe est obligatoire pour un nouvel utilisateur.", true);
            return false;
        }
        if (!emailField.getText().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            afficherMessage("⚠ Format d'email invalide.", true);
            return false;
        }
        return true;
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg);
        messageLabel.setStyle(erreur
            ? "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"
            : "-fx-text-fill:#27ae60;-fx-font-weight:bold;");
    }
    @FXML
    private void handleActualiser() {

        chargerUtilisateurs(); // recharge table

        table.refresh();
        
        searchField.clear();
        
        messageLabel.setText("Liste actualisée.");
    }
}