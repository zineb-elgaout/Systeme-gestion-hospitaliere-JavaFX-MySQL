package application.controller;

import application.model.Utilisateur;
import application.view.ViewManager;

/** Interface commune à tous les controllers de sous-vues patient. */
public interface PatientSubController {
    void initPatient(Utilisateur utilisateur,
                     ViewManager viewManager,
                     AccueilController parent);
}