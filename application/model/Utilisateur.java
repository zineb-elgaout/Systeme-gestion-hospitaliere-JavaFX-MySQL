package application.model;

public class Utilisateur {

    public enum Role {
        PATIENT, MEDECIN, PHARMACIEN, ADMIN
    }

    private int    id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Role   role;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom,
                       String email, String telephone, Role role) {
        this.id        = id;
        this.nom       = nom;
        this.prenom    = prenom;
        this.email     = email;
        this.telephone = telephone;
        this.role      = role;
    }

    // ── Getters ──────────────────────────────────────────────
    public int    getId()           { return id; }
    public String getNom()          { return nom; }
    public String getPrenom()       { return prenom; }
    public String getEmail()        { return email; }
    public String getTelephone()    { return telephone; }
    public Role   getRole()         { return role; }
    public String getNomComplet()   { return prenom + " " + nom; }

    // ── Setters ──────────────────────────────────────────────
    public void setId(int id)           { this.id = id; }
    public void setNom(String nom)      { this.nom = nom; }
    public void setPrenom(String p)     { this.prenom = p; }
    public void setEmail(String e)      { this.email = e; }
    public void setTelephone(String t)  { this.telephone = t; }
    public void setRole(Role role)      { this.role = role; }
}