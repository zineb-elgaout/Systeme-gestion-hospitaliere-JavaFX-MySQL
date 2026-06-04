package application.model;

public class Medicament {

    private int    id;
    private String nom;
    private String description;
    private int    quantiteStock;
    private int    seuilAlerte;
    private double prixUnitaire;

    public Medicament() {}

    public Medicament(int id, String nom, String description,
                      int quantiteStock, int seuilAlerte,
                      double prixUnitaire) {
        this.id            = id;
        this.nom           = nom;
        this.description   = description;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte   = seuilAlerte;
        this.prixUnitaire  = prixUnitaire;
    }

    public int    getId()            { return id; }
    public String getNom()           { return nom; }
    public String getDescription()   { return description; }
    public int    getQuantiteStock() { return quantiteStock; }
    public int    getSeuilAlerte()   { return seuilAlerte; }
    public double getPrixUnitaire()  { return prixUnitaire; }

    public void setId(int id)                { this.id            = id; }
    public void setNom(String nom)           { this.nom           = nom; }
    public void setDescription(String d)     { this.description   = d; }
    public void setQuantiteStock(int q)      { this.quantiteStock = q; }
    public void setSeuilAlerte(int s)        { this.seuilAlerte   = s; }
    public void setPrixUnitaire(double p)    { this.prixUnitaire  = p; }

    public boolean estEnRupture() {
        return quantiteStock <= seuilAlerte;
    }

    @Override
    public String toString() { return nom; }
}