package application.model;
 
import java.time.LocalDate;
 
public class DossierMedical {
 
    private int       id;
    private int       patientId;
    private LocalDate dateCreation;
    private String    antecedents;
    private String    allergies;       // présent dans dossiermedical + patient
    private String    groupeSanguin;   // vient de la table patient
 
    public DossierMedical() {}
 
    // ── Getters ───────────────────────────────────────────────
    public int       getId()             { return id; }
    public int       getPatientId()      { return patientId; }
    public LocalDate getDateCreation()   { return dateCreation; }
    public String    getAntecedents()    { return antecedents; }
    public String    getAllergies()       { return allergies; }
    public String    getGroupeSanguin()  { return groupeSanguin; }
 
    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)                   { this.id = id; }
    public void setPatientId(int id)            { this.patientId = id; }
    public void setDateCreation(LocalDate d)    { this.dateCreation = d; }
    public void setAntecedents(String a)        { this.antecedents = a; }
    public void setAllergies(String a)          { this.allergies = a; }
    public void setGroupeSanguin(String g)      { this.groupeSanguin = g; }
}