import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.sql.*;
import java.util.HashMap;

public class ListBoxApp extends JFrame {

    private JList<String> list1, list2;
    private DefaultListModel<String> model1, model2;
    private JButton moveButton;
    private HashMap<Integer, String> customers;
    private Connection connection;

    public ListBoxApp() {
        setTitle("ListBox App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 3));

        // Adatbáziskapcsolat inicializálása
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/kolcsonzesek", "felhasznalonev", "jelszo");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ügyfelek betöltése
        customers = new HashMap<>();
        loadCustomers();

        // JList-ek létrehozása
        model1 = new DefaultListModel<>();
        for (int customerId : customers.keySet()) {
            model1.addElement(customers.get(customerId));
        }
        list1 = new JList<>(model1);

        model2 = new DefaultListModel<>();
        list2 = new JList<>(model2);

        // Gombok létrehozása
        moveButton = new JButton("Elemek visszahozása");
        moveButton.setEnabled(false);
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Elemek áthelyezése az első listából a másodikba
                String selectedCustomer = list1.getSelectedValue();
                if (selectedCustomer != null) {
                    int customerId = getCustomerIdByName(selectedCustomer);
                    loadBooksForCustomer(customerId);
                    moveButton.setEnabled(false);
                }
            }
        });

        JButton closeButton = new JButton("Bezárás");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Kilépés az alkalmazásból
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        // List1 változtatásainak figyelése
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!list1.isSelectionEmpty()) {
                    moveButton.setEnabled(true);
                } else {
                    moveButton.setEnabled(false);
                }
            }
        });

        // List2 változtatásainak figyelése
        list2.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!list2.isSelectionEmpty()) {
                    moveButton.setEnabled(true);
                } else {
                    moveButton.setEnabled(false);
                }
            }
        });

        // Panel létrehozása és hozzáadása az ablakhoz
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(moveButton);
        panel.add(closeButton);

        add(new JScrollPane(list1));
        add(panel);
        add(new JScrollPane(list2));
    }

    private void loadCustomers() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM customers");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                customers.put(id, name);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCustomerIdByName(String name) {
        for (int customerId : customers.keySet()) {
            if (customers.get(customerId).equals(name)) {
                return customerId;
            }
        }
        return -1;
    }

    private void loadBooksForCustomer(int customerId) {
        model2.clear();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT title FROM books WHERE customer_id = ?");
            preparedStatement.setInt(1, customerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                model2.addElement(title);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ListBoxApp app = new ListBoxApp();
                app.setVisible(true);
            }
        });
    }
}
