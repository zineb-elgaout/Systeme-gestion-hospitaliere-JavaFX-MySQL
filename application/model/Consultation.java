package application.model;

import java.time.LocalDate;

public class Consultation {

    private int       id;
    private int       patientId;
    private String    nomMedecin;
    private String    specialite;
    private LocalDate date;
    private String    diagnostic;
    private String    traitement;
    private String    notes;
    private String    ordonnance; // texte de l'ordonnance si elle existe

    public Consultation() {}

    // ── Getters ───────────────────────────────────────────────
    public int       getId()          { return id; }
    public int       getPatientId()   { return patientId; }
    public String    getNomMedecin()  { return nomMedecin; }
    public String    getSpecialite()  { return specialite; }
    public LocalDate getDate()        { return date; }
    public String    getDiagnostic()  { return diagnostic; }
    public String    getTraitement()  { return traitement; }
    public String    getNotes()       { return notes; }
    public String    getOrdonnance()  { return ordonnance; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)               { this.id = id; }
    public void setPatientId(int id)        { this.patientId = id; }
    public void setNomMedecin(String n)     { this.nomMedecin = n; }
    public void setSpecialite(String s)     { this.specialite = s; }
    public void setDate(LocalDate d)        { this.date = d; }
    public void setDiagnostic(String d)     { this.diagnostic = d; }
    public void setTraitement(String t)     { this.traitement = t; }
    public void setNotes(String n)          { this.notes = n; }
    public void setOrdonnance(String o)     { this.ordonnance = o; }
}