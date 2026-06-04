package application.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {

	public enum Statut { EN_ATTENTE, CONFIRME, REFUSE, ANNULE, TERMINE }

    private int       id;
    private int       patientId;
    private int       medecinId;
    private String    nomMedecin;
    private String    specialite;
    private LocalDate date;
    private LocalTime heure;
    private String    motif;
    private Statut    statut;

    public RendezVous() {}

    // ── Getters ───────────────────────────────────────────────
    public int       getId()          { return id; }
    public int       getPatientId()   { return patientId; }
    public int       getMedecinId()   { return medecinId; }
    public String    getNomMedecin()  { return nomMedecin; }
    public String    getSpecialite()  { return specialite; }
    public LocalDate getDate()        { return date; }
    public LocalTime getHeure()       { return heure; }
    public String    getMotif()       { return motif; }
    public Statut    getStatut()      { return statut; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)                   { this.id = id; }
    public void setPatientId(int id)            { this.patientId = id; }
    public void setMedecinId(int id)            { this.medecinId = id; }
    public void setNomMedecin(String n)         { this.nomMedecin = n; }
    public void setSpecialite(String s)         { this.specialite = s; }
    public void setDate(LocalDate d)            { this.date = d; }
    public void setHeure(LocalTime h)           { this.heure = h; }
    public void setMotif(String m)              { this.motif = m; }
    public void setStatut(Statut s)             { this.statut = s; }

   public String getStatutLabel() {
    return switch (statut) {
        case EN_ATTENTE -> "En attente";
        case CONFIRME   -> "Confirmé";
        case REFUSE     -> "Refusé";
        case ANNULE     -> "Annulé";
        case TERMINE    -> "Terminé";
    };
}
}