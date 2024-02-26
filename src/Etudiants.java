import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Etudiants {
    private Connection connection;

    public Etudiants(Connection connection) {
        this.connection = connection;
    }

    public void ajouterEtudiant(Etudiant etudiant) throws SQLException {
        String sql = "INSERT INTO etudiants (nom, prenom, telephone, mail, dateNaissance) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, etudiant.getNom());
            statement.setString(2, etudiant.getPrenom());
            statement.setString(3, etudiant.getTelephone());
            statement.setString(4, etudiant.getMail());
            statement.setDate(5, new java.sql.Date(etudiant.getDateNaissance().getTime()));
            statement.executeUpdate();
        }
    }

    public Etudiant retrouverEtudiantParNomPrenom(String nom, String prenom) throws SQLException {
        String sql = "SELECT * FROM etudiants WHERE nom = ? AND prenom = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nom);
            statement.setString(2, prenom);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Etudiant(
                        resultSet.getString("nom"),
                        resultSet.getString("prenom"),
                        resultSet.getString("telephone"),
                        resultSet.getString("mail"),
                        resultSet.getDate("dateNaissance")
                );
            }
        }
        return null;
    }

    public List<Etudiant> listerEtudiants() throws SQLException {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiants";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Etudiant etudiant = new Etudiant(
                        resultSet.getString("nom"),
                        resultSet.getString("prenom"),
                        resultSet.getString("telephone"),
                        resultSet.getString("mail"),
                        resultSet.getDate("dateNaissance")
                );
                etudiants.add(etudiant);
            }
        }
        return etudiants;
    }

    // Ajoutez ici d'autres méthodes au besoin, comme la mise à jour ou la suppression d'étudiants.
}
