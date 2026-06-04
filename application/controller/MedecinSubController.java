package application.controller;

import application.model.Medecin;
import application.view.ViewManager;

/**
 * Interface implémentée par toutes les sous-vues de l'espace Médecin.
 * Permet à AccueilController d'injecter le médecin connecté.
 */
public interface MedecinSubController {

    /**
     * Appelé après le chargement FXML pour injecter les dépendances.
     *
     * @param medecin     Le médecin connecté (sous-type de Utilisateur)
     * @param viewManager Le gestionnaire de navigation
     * @param accueil     Référence vers AccueilController (retour d'accueil)
     */
    void initMedecin(Medecin medecin, ViewManager viewManager,
                     AccueilController accueil);
}
