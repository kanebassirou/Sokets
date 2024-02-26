
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ClientService {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public ClientService() {
    }

    public String insererEtudiant(String nom, String prenom, String telephone, String email, String dateNaissanceStr) {
        try {
            Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Date dateNaissance = format.parse(dateNaissanceStr);

            Etudiant etudiant = new Etudiant(nom, prenom, telephone, email, dateNaissance);

            output.writeObject("inserer");
            output.writeObject(etudiant);

            Object response = input.readObject();

            input.close();
            output.close();
            socket.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'insertion de l'étudiant: " + e.getMessage();
        }
    }

    public String retrouverEtudiant(String nomPrenom) {
        try {
            Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            output.writeObject("retrouver");
            output.writeObject(nomPrenom);

            Object response = input.readObject();

            input.close();
            output.close();
            socket.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la recherche de l'étudiant: " + e.getMessage();
        }
    }

    public String listerEtudiants() {
        try {
            Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            output.writeObject("lister");

            Object response = input.readObject();

            input.close();
            output.close();
            socket.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors du listage des étudiants: " + e.getMessage();
        }
    }
}
