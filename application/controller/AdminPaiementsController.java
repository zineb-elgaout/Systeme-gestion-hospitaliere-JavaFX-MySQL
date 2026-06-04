package application.controller;

import java.time.LocalDate;

import application.dao.FactureDAO;
import application.model.Admin;
import application.model.Facture;
import application.model.RendezVous;
import application.view.ViewManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminPaiementsController implements AdminSubController {

    @FXML private Label adminLabel;

    @FXML private TableView<Facture> table;

    @FXML private TableColumn<Facture, String> colId;
    @FXML private TableColumn<Facture, String> colPatient;
    @FXML private TableColumn<Facture, String> colMedecin;
    @FXML private TableColumn<Facture, String> colDate;
    @FXML private TableColumn<Facture, String> colMontant;
    @FXML private TableColumn<Facture, String> colPaye;
    @FXML private TableColumn<Facture, String> colStatut;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;
    
    @FXML private DatePicker filtreDate;

    private ObservableList<Facture> toutesFactures;

    private final FactureDAO dao = new FactureDAO();

    private Admin admin;

    @Override
    public void initAdmin(Admin admin,
                          ViewManager viewManager,
                          AccueilController accueil) {

        this.admin = admin;

        adminLabel.setText(
            "Paiements — " + admin.getNomComplet()
        );

        // ─── Colonnes ─────────────────────────────

        colId.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.valueOf(c.getValue().getId())
            )
        );

        colPatient.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getNomPatient()
            )
        );

        colMedecin.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getNomMedecin()
            )
        );

        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getDateFacture().toString()
            )
        );

        colMontant.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f DH",
                    c.getValue().getMontantTotal())
            )
        );

        colPaye.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format("%.2f DH",
                    c.getValue().getMontantPaye())
            )
        );

        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getStatut().name()
            )
        );

        chargerPaiements();
    }

    // ─────────────────────────────────────────────

    private void chargerPaiements() {

        toutesFactures =
            FXCollections.observableArrayList(
                dao.getToutesFactures()
            );

        table.setItems(toutesFactures);

        messageLabel.setText(
            "✅ " + toutesFactures.size()
            + " facture(s)."
        );

        messageLabel.setStyle(
            "-fx-text-fill:#27ae60;" +
            "-fx-font-weight:bold;"
        );
    }

    // ─────────────────────────────────────────────

    @FXML
    private void handleRechercher() {

        String terme = searchField.getText()
                                  .trim()
                                  .toLowerCase();

        LocalDate date = filtreDate.getValue();

        ObservableList<Facture> filtres =
            toutesFactures.filtered(f -> {

                // ─── Filtre texte ─────────────────

                boolean texteOk =
                       terme.isEmpty()

                    || f.getNomPatient()
                        .toLowerCase()
                        .contains(terme)

                    || f.getNomMedecin()
                        .toLowerCase()
                        .contains(terme)

                    || f.getStatut()
                        .name()
                        .toLowerCase()
                        .contains(terme);

                // ─── Filtre date ──────────────────

                boolean dateOk =
                       date == null

                    || (f.getDateFacture() != null
                        && f.getDateFacture().equals(date));

                return texteOk && dateOk;
            });

        table.setItems(filtres);

        messageLabel.setText(
            "🔍 " + filtres.size()
            + " résultat(s)."
        );
    }

    // ─────────────────────────────────────────────

    @FXML
    private void handleActualiser() {

        searchField.clear();

        filtreDate.setValue(null);

        chargerPaiements();

        table.refresh();

        messageLabel.setText(
            "✅ Liste actualisée."
        );
    }
}