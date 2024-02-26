import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class AnnuaireServeurGUI extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private ServerSocket serverSocket;
    private Thread serverThread;

    private static final int PORT = 12345;
    private static final String URL = "jdbc:mysql://localhost:3306/annuaire_etudiants";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public AnnuaireServeurGUI() {
        super("Serveur Annuaire");
        prepareGUI();
    }

    private void prepareGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        startButton = new JButton("Démarrer le serveur");

        startButton.addActionListener(e -> toggleServer());

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(startButton, BorderLayout.SOUTH);
    }

    private void toggleServer() {
        if (serverSocket == null || serverSocket.isClosed()) {
            startServer();
            startButton.setText("Arrêter le serveur");
        } else {
            stopServer();
            startButton.setText("Démarrer le serveur");
        }
    }

    public void startServer() {
        log("Démarrage du serveur...");
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                log("Serveur démarré, en attente de connexions...");

                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    log("Client connecté.");
                    
                    // Traiter chaque client dans un nouveau thread
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                log("Erreur du serveur : " + e.getMessage());
            }
        });
        serverThread.start();
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log("Serveur arrêté.");
            }
        } catch (IOException e) {
            log("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {

            String action = (String) input.readObject();
            log("Action reçue du client: " + action);

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
            log("Erreur avec le client : " + e.getMessage());
        }
    }

    // Méthodes connecter, insererEtudiant, retrouverEtudiant, listerEtudiants restent inchangées
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
        return serverSocket != null && !serverSocket.isClosed();
    }



    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            AnnuaireServeurGUI frame = new AnnuaireServeurGUI();
            frame.setVisible(true);
        });
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
}
