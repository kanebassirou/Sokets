import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class AnnuaireServeur {
    private static final int PORT = 12345;

    // Informations de connexion à la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/annuaire_etudiants";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        new AnnuaireServeur().startServer();
    }

    private Connection connecter() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connecté à la base de données MySQL.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré, en attente de connexions...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {
                    System.out.println("Client connecté.");

                    String action = (String) input.readObject();
                    System.out.println("Action reçue du client: " + action);

                    switch (action) {
                        case "inserer":
                            insererEtudiant(input, output);
                            break;
                        case "retrouver":
                            retrouverEtudiant(input, output);
                            break;
                        case "lister":
                            listerEtudiants(output);
                            break;
                        default:
                            output.writeObject("Action non reconnue");
                            output.flush();
                            break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insererEtudiant(ObjectInputStream input, ObjectOutputStream output) throws IOException {
        String SQL = "INSERT INTO etudiants(nom, prenom, telephone, mail, dateNaissance) VALUES(?,?,?,?,?)";

        try (Connection conn = connecter();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            Etudiant etudiant = (Etudiant) input.readObject();

            pstmt.setString(1, etudiant.getNom());
            pstmt.setString(2, etudiant.getPrenom());
            pstmt.setString(3, etudiant.getTelephone());
            pstmt.setString(4, etudiant.getMail());
            pstmt.setDate(5, new java.sql.Date(etudiant.getDateNaissance().getTime()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                output.writeObject("Étudiant inséré avec succès.");
            } else {
                throw new SQLException("Échec de l'insertion de l'étudiant.");
            }
            output.flush();
        } catch (SQLException | ClassNotFoundException e) {
            output.writeObject("Erreur lors de l'insertion de l'étudiant : " + e.getMessage());
            output.flush();
        }
    }

    private void retrouverEtudiant(ObjectInputStream input, ObjectOutputStream output) throws IOException {
        String SQL = "SELECT * FROM etudiants WHERE CONCAT(nom, ' ', prenom) = ?";

        try (Connection conn = connecter();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            String nomPrenom = (String) input.readObject();
            pstmt.setString(1, nomPrenom);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Etudiant etudiant = new Etudiant(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("mail"),
                        rs.getDate("dateNaissance")
                );
                output.writeObject(etudiant);
            } else {
                throw new SQLException("Étudiant introuvable.");
            }
            output.flush();
        } catch (SQLException | ClassNotFoundException e) {
            output.writeObject("Erreur lors de la recherche de l'étudiant : " + e.getMessage());
            output.flush();
        }
    }

    private void listerEtudiants(ObjectOutputStream output) throws IOException {
        String SQL = "SELECT * FROM etudiants";
        List<Etudiant> etudiants = new ArrayList<>();

        try (Connection conn = connecter();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Etudiant etudiant = new Etudiant(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("mail"),
                        rs.getDate("dateNaissance")
                );
                etudiants.add(etudiant);
            }
            output.writeObject(etudiants);
            output.flush();
        } catch (SQLException e) {
            output.writeObject("Erreur lors du listage des étudiants : " + e.getMessage());
            output.flush();
        }
    }

	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
}
