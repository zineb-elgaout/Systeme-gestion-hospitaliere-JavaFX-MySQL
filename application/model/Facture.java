package application.model;

import java.time.LocalDate;

public class Facture {

    public enum Statut { EN_ATTENTE, PAYEE, ANNULEE }

    private int       id;
    private int       patientId;
    private int       medecinId;
    private String    nomMedecin;
    private LocalDate dateFacture;
    private double    montantTotal;
    private double    montantPaye;
    private Statut    statut;
    private String    description;
    private String    nomPatient;


    public Facture() {}

    // ── Getters ───────────────────────────────────────────────
    public int       getId()           { return id; }
    public int       getPatientId()    { return patientId; }
    public int       getMedecinId()    { return medecinId; }
    public String    getNomMedecin()   { return nomMedecin; }
    public LocalDate getDateFacture()  { return dateFacture; }
    public double    getMontantTotal() { return montantTotal; }
    public double    getMontantPaye()  { return montantPaye; }
    public Statut    getStatut()       { return statut; }
    public String    getDescription()  { return description; }
    public String getNomPatient() {
        return nomPatient;
    }


    public double getMontantRestant() {
        return montantTotal - montantPaye;
    }

    public String getStatutLabel() {
        return switch (statut) {
            case EN_ATTENTE -> "En attente";
            case PAYEE      -> "Payée";
            case ANNULEE    -> "Annulée";
        };
    }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)                    { this.id = id; }
    public void setPatientId(int id)             { this.patientId = id; }
    public void setMedecinId(int id)             { this.medecinId = id; }
    public void setNomMedecin(String n)          { this.nomMedecin = n; }
    public void setDateFacture(LocalDate d)      { this.dateFacture = d; }
    public void setMontantTotal(double m)        { this.montantTotal = m; }
    public void setMontantPaye(double m)         { this.montantPaye = m; }
    public void setStatut(Statut s)              { this.statut = s; }
    public void setDescription(String d)         { this.description = d; }
    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

}