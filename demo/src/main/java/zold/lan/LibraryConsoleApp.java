package zold.lan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LibraryConsoleApp {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Használat: java LibraryConsoleApp <kolcsonzesek.csv> <kolcsonzok.csv>");
            System.exit(1);
        }

        String kolcsonzesekFile = args[0];
        String kolcsonzokFile = args[1];

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:library.db")) {
            createTables(connection);

            importData(connection, "kolcsonzesek", kolcsonzesekFile);
            importData(connection, "kolcsonzok", kolcsonzokFile);

            System.out.println("Adatok sikeresen importálva az adatbázisba.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        String createKolcsonzesekTable = "CREATE TABLE IF NOT EXISTS kolcsonzesek (id INTEGER PRIMARY KEY AUTOINCREMENT, iro TEXT, mufaj TEXT, cim TEXT)";
        String createKolcsonzokTable = "CREATE TABLE IF NOT EXISTS kolcsonzok (nev TEXT, szuletesidatum TEXT)";

        try (PreparedStatement statement1 = connection.prepareStatement(createKolcsonzesekTable);
             PreparedStatement statement2 = connection.prepareStatement(createKolcsonzokTable)) {
            statement1.execute();
            statement2.execute();
        }
    }

    private static void importData(Connection connection, String tableName, String csvFile) throws SQLException {
        String insertQuery = "INSERT INTO " + tableName + " (iro, mufaj, cim, nev, szuletesidatum) VALUES (?, ?, ?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile));
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            String line;
            reader.readLine(); // Skip header line
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                for (int i = 0; i < data.length; i++) {
                    statement.setString(i + 1, data[i]);
                }
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
