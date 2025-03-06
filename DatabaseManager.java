package common;

import java.sql.*;

public class DatabaseManager {
    // Constante pentru configurarea conexiunii la baza de date
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/weather_app"; // URL-ul bazei de date PostgreSQL
    private static final String DB_USER = "postgres"; // Utilizatorul bazei de date
    private static final String DB_PASSWORD = "1q2w3e"; // Parola bazei de date

    // Constructor: Creează o conexiune la baza de date și initializează tabelul dacă acesta nu există
    public DatabaseManager() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                System.out.println("Conectat la baza de date PostgreSQL.");
                createTable(); // Creează tabelul dacă nu există
            }
        } catch (SQLException e) {
            System.err.println("Eroare la conectarea la baza de date: " + e.getMessage());
        }
    }

    // Creează tabelul pentru locația clientului dacă nu există deja
    private void createTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS client_location (
                id SERIAL PRIMARY KEY,
                city TEXT NOT NULL,
                latitude DOUBLE PRECISION NOT NULL,
                longitude DOUBLE PRECISION NOT NULL
            );
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL); // Execută comanda SQL pentru crearea tabelului
            System.out.println("Tabelul client_location a fost creat sau există deja.");
        } catch (SQLException e) {
            System.err.println("Eroare la crearea tabelului: " + e.getMessage());
        }
    }

    // Salvează sau actualizează locația curentă a clientului în tabelul client_location
    public void saveOrUpdateLocation(String city, double latitude, double longitude) {
        String deleteSQL = "DELETE FROM client_location"; // Șterge locația existentă
        String insertSQL = "INSERT INTO client_location(city, latitude, longitude) VALUES(?, ?, ?)"; // Inserează noua locație
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement deleteStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            deleteStmt.executeUpdate(deleteSQL); // Șterge locația veche
            insertStmt.setString(1, city); // Setează numele orașului
            insertStmt.setDouble(2, latitude); // Setează latitudinea
            insertStmt.setDouble(3, longitude); // Setează longitudinea
            insertStmt.executeUpdate(); // Execută inserarea
            System.out.println("Locația a fost salvată cu succes.");
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea locației: " + e.getMessage());
        }
    }

    // Returnează locația curentă a clientului din tabelul client_location
    public String getCurrentLocation() {
        String sql = "SELECT * FROM client_location"; // Comandă SQL pentru obținerea locației
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                // Concatenează numele orașului, latitudinea și longitudinea într-un șir
                return rs.getString("city") + " (" + rs.getDouble("latitude") + ", " + rs.getDouble("longitude") + ")";
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obținerea locației: " + e.getMessage());
        }
        return "Nicio locație găsită."; // Returnează un mesaj dacă locația nu există
    }
}
