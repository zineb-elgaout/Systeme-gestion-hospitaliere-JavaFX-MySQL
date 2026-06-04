package application.model;

public class Admin extends Utilisateur {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int id, String nom, String prenom,
                 String email, String telephone) {
        super(id, nom, prenom, email, telephone, Role.ADMIN);
    }

    @Override
    public String toString() {
        return getPrenom() + " " + getNom() + " (Admin)";
    }
}