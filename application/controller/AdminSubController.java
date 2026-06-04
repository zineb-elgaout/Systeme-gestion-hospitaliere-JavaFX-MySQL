package application.controller;

import application.model.Admin;
import application.view.ViewManager;

public interface AdminSubController {
    void initAdmin(Admin admin, ViewManager viewManager,
                   AccueilController accueil);
}