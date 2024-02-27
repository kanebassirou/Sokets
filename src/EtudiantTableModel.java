import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EtudiantTableModel extends AbstractTableModel {
    private List<Etudiant> etudiants;
    private final String[] columnNames = {"Nom", "Prénom", "Téléphone", "Mail", "Date Naissance"};

    public EtudiantTableModel() {
        this.etudiants = new ArrayList<>();
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants;
        fireTableDataChanged(); // Notifie la vue que les données ont changé
    }

    @Override
    public int getRowCount() {
        return etudiants.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Etudiant etudiant = etudiants.get(rowIndex);
        switch (columnIndex) {
            case 0: return etudiant.getNom();
            case 1: return etudiant.getPrenom();
            case 2: return etudiant.getTelephone();
            case 3: return etudiant.getMail();
            case 4: return etudiant.getDateNaissance().toString(); // Assurez-vous du format ici
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
