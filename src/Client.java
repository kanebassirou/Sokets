import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        new Client().startClient();
    }

    public void startClient() {
        while (true) {
            try (Socket socket = new Socket(HOST, PORT);
                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                Scanner scanner = new Scanner(System.in);
                showMenu();
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1":
                        insererEtudiant(output, input, scanner);
                        break;
                    case "2":
                        retrouverEtudiant(output, input, scanner);
                        break;
                    case "3":
                        listerEtudiants(output, input);
                        break;
                    case "4":
                        System.out.println("Fermeture de l'application.");
                        return; // Sortir de startClient()
                    default:
                        System.out.println("Choix non valide, veuillez réessayer.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Insérer un nouvel étudiant");
        System.out.println("2. Rechercher un étudiant");
        System.out.println("3. Lister tous les étudiants");
        System.out.println("4. Quitter");
        System.out.print("Entrez votre choix: ");
    }

    private void insererEtudiant(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) throws IOException {
        System.out.println("Entrer le nom, prénom, téléphone, email, date de naissance (JJ/MM/AAAA):");
        String nom = scanner.nextLine();
        String prenom = scanner.nextLine();
        String telephone = scanner.nextLine();
        String email = scanner.nextLine();
        String dateNaissanceStr = scanner.nextLine();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date dateNaissance = null;
        try {
            dateNaissance = format.parse(dateNaissanceStr);
        } catch (ParseException e) {
            System.out.println("Erreur de format de la date de naissance. Utilisez le format JJ/MM/AAAA.");
            return; // Sortie anticipée en cas d'erreur de format
        }

        Etudiant etudiant = new Etudiant(nom, prenom, telephone, email, dateNaissance);

        output.writeObject("inserer");
        output.writeObject(etudiant);
        try {
            Object response = input.readObject();
            System.out.println(response);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void retrouverEtudiant(ObjectOutputStream output, ObjectInputStream input, Scanner scanner) throws IOException {
        System.out.println("Entrer le nom et prénom de l'étudiant à rechercher:");
        String nomPrenom = scanner.nextLine();
        
        output.writeObject("retrouver");
        output.writeObject(nomPrenom);
        try {
            Object response = input.readObject();
            if (response instanceof Etudiant) {
                Etudiant etudiant = (Etudiant) response;
                // Afficher les informations de l'étudiant (à implémenter selon la structure de votre classe Etudiant)
                System.out.println("Étudiant trouvé : " + etudiant);
            } else {
                System.out.println(response);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void listerEtudiants(ObjectOutputStream output, ObjectInputStream input) throws IOException {
        output.writeObject("lister");
        try {
            Object response = input.readObject();
            if (response instanceof java.util.List) {
                java.util.List<Etudiant> etudiants = (java.util.List<Etudiant>) response;
                for (Etudiant etudiant : etudiants) {
                    // Afficher les informations de chaque étudiant (à implémenter selon la structure de votre classe Etudiant)
                    System.out.println(etudiant);
                }
            } else {
                System.out.println(response);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
