package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.io.File;
import common.DatabaseManager;

public class PinkWeatherApp {
    // Constante
    private static final String APP_TITLE = "Pink Weather Application";
    private static final String HEADER_FONT = "Comic Sans MS";
    private static final String FONT_EMOJI = "Segoe UI Emoji";
    private static final int FRAME_WIDTH = 700;
    private static final int FRAME_HEIGHT = 900;

    private static final Color COLOR_PINK_LIGHT = new Color(255, 182, 193);
    private static final Color COLOR_PINK_DARK = new Color(255, 105, 180);
    private static final Color COLOR_BACKGROUND = new Color(255, 245, 238);

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 12346;

    // Variabile de instanță
    private JFrame frame;
    private JTextField cityField;
    private JTextField latitudeField;
    private JTextField longitudeField;
    private JTextField radiusField;
    private JTextArea responseArea;
    private JButton selectFileButton;
    private JLabel selectedFileLabel;
    private String selectedFilePath;

    private DatabaseManager databaseManager;
    private String currentRole;

    // Constructor: initializare app + interfata
    public PinkWeatherApp() {
        databaseManager = new DatabaseManager();

        // Configurare interfata
        frame = new JFrame(APP_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Configurare antet cu titlu
        JPanel headerPanel = new JPanel();
        JLabel headerLabel = new JLabel(APP_TITLE);
        headerLabel.setFont(new Font(HEADER_FONT, Font.BOLD, 30));
        headerLabel.setForeground(COLOR_PINK_DARK);
        headerPanel.add(headerLabel);

        // Formular de intrare pentru informațiile orașului și coordonatelor
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        cityField = new JTextField();
        latitudeField = new JTextField();
        longitudeField = new JTextField();
        radiusField = new JTextField();

        inputPanel.add(new JLabel("Oraș:"));
        inputPanel.add(cityField);
        inputPanel.add(new JLabel("Latitudine:"));
        inputPanel.add(latitudeField);
        inputPanel.add(new JLabel("Longitudine:"));
        inputPanel.add(longitudeField);
        inputPanel.add(new JLabel("Rază (km):"));
        inputPanel.add(radiusField);

        // Buton pt selectarea fis JSON (doar pentru admin)
        selectFileButton = new JButton("Selectează fișier JSON");
        selectFileButton.setEnabled(false); // Dezactivat pentru utilizator
        selectFileButton.addActionListener(e -> selectJsonFile());
        selectedFileLabel = new JLabel("Niciun fișier selectat.");
        inputPanel.add(selectFileButton);
        inputPanel.add(selectedFileLabel);

        JButton submitButton = new JButton("Trimite");
        submitButton.addActionListener(getSubmitListener());
        inputPanel.add(new JLabel());
        inputPanel.add(submitButton);

        // Zona de raspuns pentru afisarea informatiilor meteo sau erorilor
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        responseArea.setBackground(COLOR_BACKGROUND);
        responseArea.setFont(new Font(FONT_EMOJI, Font.PLAIN, 18));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setBorder(BorderFactory.createLineBorder(COLOR_PINK_DARK, 3));
        JScrollPane responseScrollPane = new JScrollPane(responseArea);
        responseScrollPane.setPreferredSize(new Dimension(FRAME_WIDTH - 50, 400));
        responseScrollPane.setBorder(BorderFactory.createTitledBorder("Informații meteo"));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(responseScrollPane, BorderLayout.SOUTH);

        frame.add(mainPanel);

        // Afișeaza dialogul de selectare a rolului
        showRoleSelectionDialog();
    }

    // Afișeaza un dialog pentru selectarea rolului (USER sau ADMIN)
    private void showRoleSelectionDialog() {
        String[] roles = {"USER", "ADMIN"};
        String selectedRole = (String) JOptionPane.showInputDialog(
                frame,
                "Selectați rolul:",
                "Selectare rol",
                JOptionPane.PLAIN_MESSAGE,
                null,
                roles,
                roles[0]);

        if ("ADMIN".equals(selectedRole)) {
            currentRole = "ADMIN";
            selectFileButton.setEnabled(true); // Activează butonul de selectare a fișierului pentru admin
            JOptionPane.showMessageDialog(frame, "Conectat ca ADMIN.");
        } else if ("USER".equals(selectedRole)) {
            currentRole = "USER";
            JOptionPane.showMessageDialog(frame, "Conectat ca USER.");
        } else {
            System.exit(0); // Ieșire dacă nu se selectează un rol
        }
    }

    // Permite adminului sa selecteze un fiș JSON pentru import
    private void selectJsonFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath();
            selectedFileLabel.setText("Selectat: " + selectedFile.getName());
        } else {
            selectedFileLabel.setText("Niciun fișier selectat.");
        }
    }

    // Trimite o cerere catre server pe baza rolului curent (USER sau ADMIN)
    private void sendRequest(String role) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(role); // Trimite rolul către server

            if ("USER".equals(role)) {
                handleUserRequest(writer, reader);
            } else if ("ADMIN".equals(role)) {
                handleAdminRequest(writer, reader);
            }

        } catch (Exception ex) {
            responseArea.setText("Eroare: " + ex.getMessage());
        }
    }

    // Gestioneaza cererile specifice utilizatorului si afisează informayiile meteo
    private void handleUserRequest(PrintWriter writer, BufferedReader reader) throws Exception {
        String city = cityField.getText().trim();

        if (!city.isEmpty()) {
            // Cautare dupa numele orasului
            writer.println("GET_WEATHER_BY_CITY");
            writer.println(city);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(parseWeatherWithEmoji(line)).append("\n");
            }

            responseArea.setText(response.toString());
            databaseManager.saveOrUpdateLocation(city, 0.0, 0.0); // Salvează locația în baza de date

        } else {
            // Cautare dupa coordonate daca numele orasului nu este completat
            String latitudeText = latitudeField.getText().trim();
            String longitudeText = longitudeField.getText().trim();
            String radiusText = radiusField.getText().trim();

            if (latitudeText.isEmpty() || longitudeText.isEmpty() || radiusText.isEmpty()) {
                responseArea.setText("Eroare: Introduceți numele orașului sau coordonate valide.");
                return;
            }

            double latitude = Double.parseDouble(latitudeText);
            double longitude = Double.parseDouble(longitudeText);
            double radius = Double.parseDouble(radiusText);

            writer.println("GET_WEATHER");
            writer.println(latitude);
            writer.println(longitude);
            writer.println(radius);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(parseWeatherWithEmoji(line)).append("\n");
            }

            if (response.toString().isEmpty()) {
                responseArea.setText("Nicio locație apropiată găsită.");
            } else {
                responseArea.setText(response.toString());
            }

            // Salvează locația curentă în baza de date
            databaseManager.saveOrUpdateLocation("Necunoscut", latitude, longitude);
        }
    }

    // Gestioneaza cererile specifice adminului pentru importul datelor
    private void handleAdminRequest(PrintWriter writer, BufferedReader reader) throws Exception {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            responseArea.setText("Eroare: Niciun fișier JSON selectat.");
            return;
        }

        writer.println("IMPORT");
        writer.println(selectedFilePath); // Trimite calea fișierului către server

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }

        responseArea.setText(response.toString()); // Afișează răspunsul serverului
    }

    // Interpreteaza informațiile meteo și adauga emoji-uri pentru lizibilitate
    private String parseWeatherWithEmoji(String line) {
        Map<String, String> emojiMap = new HashMap<>();
        emojiMap.put("Sunny", "☀️");
        emojiMap.put("Rainy", "🌧️");
        emojiMap.put("Cloudy", "☁️");
        emojiMap.put("Snow", "❄️");
        emojiMap.put("Foggy", "🌫️");

        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            if (line.contains(entry.getKey())) {
                return line + " " + entry.getValue();
            }
        }
        return line; // Returneaza linia originala dacă nu se gasește o conditie potrivita
    }

    // Returneaza listener-ul pentru butonul de trimitere, care gestioneaza cererile utilizatorului/adminului
    private ActionListener getSubmitListener() {
        return e -> {
            if ("USER".equals(currentRole)) {
                sendRequest("USER");
            } else if ("ADMIN".equals(currentRole)) {
                sendRequest("ADMIN");
            }
        };
    }

    // Afiseaza fereastra principala a aplicației
    public void display() {
        frame.setVisible(true);
    }

    // Punctul de intrare al aplicației
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PinkWeatherApp().display());
    }
}
