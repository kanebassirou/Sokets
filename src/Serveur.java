import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class Serveur {
    private static final int PORT = 12345;
    private Etudiants etudiants;

    public Serveur(Connection connection) {
        this.etudiants = new Etudiants(connection);
    }

    public static void main(String[] args) {
        // Configuration de la connexion à la base de données
        String url = "jdbc:mysql://localhost:3306/socket";
        String user = "root";
        String password = "";
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            new Serveur(connection).startServer();
        } catch (SQLException e) {
            System.out.println("Impossible de se connecter à la base de données");
            e.printStackTrace();
            return;
        }
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
                } catch (IOException | ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insererEtudiant(ObjectInputStream input, ObjectOutputStream output) throws IOException, SQLException {
        try {
            Etudiant etudiant = (Etudiant) input.readObject();
            etudiants.ajouterEtudiant(etudiant);
            output.writeObject("Étudiant inséré avec succès.");
            output.flush();
        } catch (ClassNotFoundException e) {
            output.writeObject("Erreur lors de la lecture de l'étudiant.");
            output.flush();
        }
    }

    private void retrouverEtudiant(ObjectInputStream input, ObjectOutputStream output) throws IOException, SQLException {
        try {
            String nomPrenom = (String) input.readObject();
            // Ici, vous devez adapter la méthode dans EtudiantDAO pour rechercher par nom et prénom
            Etudiant etudiant = etudiants.retrouverEtudiantParNomPrenom(nomPrenom, nomPrenom);
            if (etudiant == null) {
                output.writeObject("Étudiant introuvable.");
            } else {
                output.writeObject(etudiant);
            }
            output.flush();
        } catch (ClassNotFoundException e) {
            output.writeObject("Erreur lors de la lecture du nom et prénom de l'étudiant.");
            output.flush();
        }
    }

    private void listerEtudiants(ObjectOutputStream output) throws IOException {
        try {
            // Correction: Assurez-vous que le type de la liste est List<Etudiant>, pas List<Etudiants>
            List<Etudiant> etudiantsListe = etudiants.listerEtudiants(); // Utilisez le bon nom de variable et le bon type
            output.writeObject(etudiantsListe); 
            output.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            output.writeObject(new ArrayList<Etudiant>()); // Assurez-vous d'utiliser le bon type générique ici aussi
            output.flush();
        }
    }

}
