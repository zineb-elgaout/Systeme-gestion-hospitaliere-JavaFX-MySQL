package application.model;

public class Pharmacien extends Utilisateur {

    private String numeroOrdre;
    private String nomPharmacie;
    private String adressePharmacie;

    public Pharmacien() {
        super();
        setRole(Role.PHARMACIEN);
    }

    public Pharmacien(int id, String nom, String prenom,
                      String email, String telephone,
                      String numeroOrdre, String nomPharmacie,
                      String adressePharmacie) {
        super(id, nom, prenom, email, telephone, Role.PHARMACIEN);
        this.numeroOrdre      = numeroOrdre;
        this.nomPharmacie     = nomPharmacie;
        this.adressePharmacie = adressePharmacie;
    }

    public String getNumeroOrdre()      { return numeroOrdre; }
    public String getNomPharmacie()     { return nomPharmacie; }
    public String getAdressePharmacie() { return adressePharmacie; }

    public void setNumeroOrdre(String n)      { this.numeroOrdre      = n; }
    public void setNomPharmacie(String n)     { this.nomPharmacie     = n; }
    public void setAdressePharmacie(String a) { this.adressePharmacie = a; }

    @Override
    public String toString() {
        return getPrenom() + " " + getNom() +
               (nomPharmacie != null ? " — " + nomPharmacie : "");
    }
}