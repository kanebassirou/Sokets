import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AnnuaireClientGUI extends JFrame {
    private JTextField nomField, prenomField, telephoneField, mailField, dateNaissanceField;
    private JButton insererBtn, retrouverBtn, listerBtn;
    private JButton listerServicesBtn;
    private JTextArea resultatArea;

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public AnnuaireClientGUI() {
        super("Annuaire Etudiants - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(606, 504);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        nomField = new JTextField();
        prenomField = new JTextField();
        telephoneField = new JTextField();
        mailField = new JTextField();
        dateNaissanceField = new JTextField();

        insererBtn = new JButton("Inserer");
        retrouverBtn = new JButton("Retrouver");
        listerBtn = new JButton("Lister");
        resultatArea = new JTextArea(10, 30);
        resultatArea.setEditable(false);

        panel.add(new JLabel("Nom :"));
        panel.add(nomField);
        panel.add(new JLabel("Prénom :"));
        panel.add(prenomField);
        panel.add(new JLabel("Téléphone :"));
        panel.add(telephoneField);
        panel.add(new JLabel("Mail :"));
        panel.add(mailField);
        panel.add(new JLabel("Date Naissance (YYYY-MM-DD) :"));
        panel.add(dateNaissanceField);

        panel.add(insererBtn);
        panel.add(retrouverBtn);
        panel.add(listerBtn);

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(resultatArea), BorderLayout.CENTER);

        insererBtn.addActionListener(e -> insererEtudiant());
        retrouverBtn.addActionListener(e -> retrouverEtudiant());
        listerBtn.addActionListener(e -> listerEtudiants());
        listerServicesBtn = new JButton("Lister Services");
        panel.add(listerServicesBtn);

        listerServicesBtn.addActionListener(e -> listerServices());
    }

    private void insererEtudiant() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateNaissance = sdf.parse(dateNaissanceField.getText()); // Convertir la chaîne de caractères en Date
            Etudiant etudiant = new Etudiant(nomField.getText(), prenomField.getText(), telephoneField.getText(), mailField.getText(), dateNaissance);

            // Connexion au serveur et envoi de l'objet Etudiant
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("inserer");
                out.writeObject(etudiant);
                out.flush();

                String response = (String) in.readObject();
                resultatArea.setText(response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatArea.setText("Erreur lors de l'insertion de l'étudiant: " + ex.getMessage());
        }
    }

    private void retrouverEtudiant() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("retrouver");
            out.writeObject(nomField.getText() + " " + prenomField.getText());
            out.flush();

            Object response = in.readObject();
            if (response instanceof Etudiant) {
                Etudiant etudiant = (Etudiant) response;
                resultatArea.setText(etudiant.toString()); // Assurez-vous que la classe Etudiant a une méthode toString bien définie
            } else {
                resultatArea.setText((String) response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatArea.setText("Erreur lors de la recherche de l'étudiant: " + ex.getMessage());
        }
    }

    private void listerEtudiants() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("lister");
            out.flush();

            Object response = in.readObject();
            if (response instanceof List) {
                List<Etudiant> etudiants = (List<Etudiant>) response;
                StringBuilder sb = new StringBuilder();
                for (Etudiant etudiant : etudiants) {
                    sb.append(etudiant.toString()).append("\n");
                }
                resultatArea.setText(sb.toString());
            } else {
                resultatArea.setText((String) response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatArea.setText("Erreur lors du listage des étudiants: " + ex.getMessage());
        }
    }
    private void listerServices() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("listerServices");
            out.flush();

            Object response = in.readObject();
            if (response instanceof List) {
                List<String> services = (List<String>) response;
                StringBuilder sb = new StringBuilder("Services disponibles :\n");
                for (String service : services) {
                    sb.append(service).append("\n");
                }
                resultatArea.setText(sb.toString());
            } else {
                resultatArea.setText("Erreur lors de la récupération des services.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatArea.setText("Erreur lors de la demande de listage des services: " + ex.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnnuaireClientGUI());
    }
}
