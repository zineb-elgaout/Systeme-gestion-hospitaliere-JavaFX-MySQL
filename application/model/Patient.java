package application.model;

import java.time.LocalDate;
import java.time.Period;

public class Patient extends Utilisateur {

    private LocalDate dateNaissance;
    private String    adresse;
    private String    groupeSanguin;
    private String    numeroSecu;
    private String    medecinTraitantId;  // référence vers Medecin

    // ── Constructeur vide ─────────────────────────────────────
    public Patient() {
        super();
        setRole(Role.PATIENT);
    }

    // ── Constructeur complet ──────────────────────────────────
    public Patient(int id, String nom, String prenom,
                   String email, String telephone,
                   LocalDate dateNaissance, String adresse,
                   String groupeSanguin, String numeroSecu) {
        super(id, nom, prenom, email, telephone, Role.PATIENT);
        this.dateNaissance = dateNaissance;
        this.adresse       = adresse;
        this.groupeSanguin = groupeSanguin;
        this.numeroSecu    = numeroSecu;
    }

    // ── Âge calculé dynamiquement ─────────────────────────────
    public int getAge() {
        if (dateNaissance == null) return 0;
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    // ── Getters ───────────────────────────────────────────────
    public LocalDate getDateNaissance()      { return dateNaissance; }
    public String    getAdresse()            { return adresse; }
    public String    getGroupeSanguin()      { return groupeSanguin; }
    public String    getNumeroSecu()         { return numeroSecu; }
    public String    getMedecinTraitantId()  { return medecinTraitantId; }

    // ── Setters ───────────────────────────────────────────────
    public void setDateNaissance(LocalDate d)      { this.dateNaissance     = d; }
    public void setAdresse(String a)               { this.adresse           = a; }
    public void setGroupeSanguin(String g)         { this.groupeSanguin     = g; }
    public void setNumeroSecu(String n)            { this.numeroSecu        = n; }
    public void setMedecinTraitantId(String mId)   { this.medecinTraitantId = mId; }

    @Override
    public String toString() {
        return getPrenom() + " " + getNom() +
               (dateNaissance != null ? " (" + getAge() + " ans)" : "");
    }
}
