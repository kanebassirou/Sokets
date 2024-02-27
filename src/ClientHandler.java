import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String command;
            while ((command = reader.readLine()) != null) {
                switch (command) {
                    case "list":
                        // Lister les étudiants
                        writer.println("Liste des étudiants...");
                        break;
                    case "add":
                        // Ajouter un étudiant
                        writer.println("Ajouter un étudiant...");
                        break;
                        
                    // Ajoutez d'autres cas selon les fonctionnalités requises
                    default:
                        writer.println("Commande inconnue");
                        break;
                }
            }

            socket.close();
        } catch (IOException ex) {
            System.out.println("Erreur serveur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
