import java.io.Serializable;

import java.util.Date;

public class Etudiant implements Serializable {
    private String nom;
    private String prenom;
    private String telephone;
    private String mail;
    private String url;
    private Date dateNaissance;

    // Constructeur
    public Etudiant(String nom, String prenom, String telephone, String mail, Date dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.mail = mail;
       
        this.dateNaissance = dateNaissance;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getMail() {
        return mail;
    }

  
    public Date getDateNaissance() {
        return dateNaissance;
    }
    
    @Override
    public String toString() {
        return "Etudiant  {" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", mail='" + mail + '\'' +
                ", date Naissance=" + dateNaissance +
                   '}';
    }

    // Vous pouvez également ajouter des setters si nécessaire
}
