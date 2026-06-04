package application.model;

public class Medecin extends Utilisateur {

    private String specialite;
    private String numeroOrdre;
    private String cabinet;

    // ── Constructeur vide ─────────────────────────────────────
    public Medecin() {
        super();
        setRole(Role.MEDECIN);
    }

    // ── Constructeur complet ──────────────────────────────────
    public Medecin(int id, String nom, String prenom,
                   String email, String telephone,
                   String specialite, String numeroOrdre, String cabinet) {
        super(id, nom, prenom, email, telephone, Role.MEDECIN);
        this.specialite   = specialite;
        this.numeroOrdre  = numeroOrdre;
        this.cabinet      = cabinet;
    }

    // ── Getters ───────────────────────────────────────────────
    public String getSpecialite()   { return specialite; }
    public String getNumeroOrdre()  { return numeroOrdre; }
    public String getCabinet()      { return cabinet; }

    // ── Setters ───────────────────────────────────────────────
    public void setSpecialite(String specialite)     { this.specialite  = specialite; }
    public void setNumeroOrdre(String numeroOrdre)   { this.numeroOrdre = numeroOrdre; }
    public void setCabinet(String cabinet)           { this.cabinet     = cabinet; }

    @Override
    public String toString() {
        return "Dr. " + getPrenom() + " " + getNom() +
               (specialite != null ? " (" + specialite + ")" : "");
    }
}
