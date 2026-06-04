package application.controller;

import application.model.Pharmacien;
import application.view.ViewManager;

public interface PharmacienSubController {
    void initPharmacien(Pharmacien pharmacien, ViewManager viewManager,
                        AccueilController accueil);
}