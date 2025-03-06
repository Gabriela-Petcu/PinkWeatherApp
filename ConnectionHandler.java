package server;

import common.GeoWeatherData;
import common.WeatherDataManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * Clasa ConnectionHandler gestionează cererile clienților conectați la server.
 * Aceasta implementează Runnable pentru a rula în thread-uri separate pentru fiecare client.
 */
class ConnectionHandler implements Runnable {
    // Mesaje constante pentru diverse scenarii
    private static final String MESSAGE_ROLE_NOT_PROVIDED = "Role not provided. Connection closed."; // Rol nedefinit
    private static final String MESSAGE_INVALID_ROLE = "Invalid role. Connection closed."; // Rol invalid
    private static final String MESSAGE_DATABASE_UPDATED = "Database successfully updated."; // Baza de date actualizată cu succes
    private static final String MESSAGE_DATABASE_UPDATE_FAILED = "Failed to update database."; // Eșec la actualizarea bazei de date
    private static final String MESSAGE_INVALID_JSON_FILE = "Invalid JSON file."; // Fișier JSON invalid
    private static final String MESSAGE_UNKNOWN_ADMIN_COMMAND = "Unknown admin command."; // Comandă necunoscută pentru admin
    private static final String MESSAGE_CITY_NOT_FOUND = "City not found. Please enter latitude and longitude."; // Oraș negăsit
    private static final String MESSAGE_UNKNOWN_USER_COMMAND = "Unknown user command."; // Comandă necunoscută pentru utilizator
    private static final String MESSAGE_INVALID_INPUT = "Invalid input. Coordinates and radius must be numeric."; // Input invalid

    private final Socket clientSocket; // Socket-ul clientului
    private final WeatherDataManager weatherDataManager; // Managerul datelor meteo

    /**
     * Constructorul clasei ConnectionHandler.
     * @param clientSocket Socket-ul clientului.
     * @param weatherDataManager Instanța WeatherDataManager pentru manipularea datelor meteo.
     */
    public ConnectionHandler(Socket clientSocket, WeatherDataManager weatherDataManager) {
        this.clientSocket = clientSocket;
        this.weatherDataManager = weatherDataManager;
    }

    /**
     * Metoda principală care rulează pentru fiecare client.
     */
    @Override
    public void run() {
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String userRole = inputReader.readLine(); // Citește rolul utilizatorului
            if (userRole == null) {
                outputWriter.println(MESSAGE_ROLE_NOT_PROVIDED); // Mesaj pentru lipsa rolului
                return;
            }

            // Gestionează cererile în funcție de rolul utilizatorului
            switch (userRole.toUpperCase()) {
                case "ADMIN" -> handleAdmin(inputReader, outputWriter);
                case "USER" -> handleUser(inputReader, outputWriter);
                default -> outputWriter.println(MESSAGE_INVALID_ROLE); // Mesaj pentru rol invalid
            }

        } catch (IOException e) {
            e.printStackTrace(); // Tratează excepțiile de intrare/ieșire
        }
    }

    /**
     * Gestionează cererile utilizatorului cu rol de admin.
     * @param inputReader Buffer pentru citirea datelor de la client.
     * @param outputWriter Buffer pentru scrierea răspunsurilor către client.
     */
    private void handleAdmin(BufferedReader inputReader, PrintWriter outputWriter) throws IOException {
        String command = inputReader.readLine(); // Citește comanda
        if ("IMPORT".equalsIgnoreCase(command)) {
            String filePath = inputReader.readLine(); // Citește calea fișierului JSON
            if (weatherDataManager.validateJsonFile(filePath)) {
                if (weatherDataManager.importDatabase(filePath)) {
                    outputWriter.println(MESSAGE_DATABASE_UPDATED); // Mesaj pentru actualizare reușită
                } else {
                    outputWriter.println(MESSAGE_DATABASE_UPDATE_FAILED); // Mesaj pentru actualizare eșuată
                }
            } else {
                outputWriter.println(MESSAGE_INVALID_JSON_FILE); // Mesaj pentru fișier JSON invalid
            }
        } else {
            outputWriter.println(MESSAGE_UNKNOWN_ADMIN_COMMAND); // Mesaj pentru comandă necunoscută
        }
    }

    /**
     * Gestionează cererile utilizatorului obișnuit.
     * @param inputReader Buffer pentru citirea datelor de la client.
     * @param outputWriter Buffer pentru scrierea răspunsurilor către client.
     */
    private void handleUser(BufferedReader inputReader, PrintWriter outputWriter) throws IOException {
        String command = inputReader.readLine(); // Citește comanda utilizatorului

        if ("GET_WEATHER_BY_CITY".equalsIgnoreCase(command)) {
            String city = inputReader.readLine(); // Citește numele orașului
            Optional<GeoWeatherData> location = weatherDataManager.findLocationByCity(city);

            if (location.isPresent()) {
                GeoWeatherData geoData = location.get();
                outputWriter.println("Weather for " + city + ":"); // Mesaj pentru prognoză
                geoData.getWeatherList().forEach(forecast -> outputWriter.println(forecast.toString())); // Afișează prognozele
                outputWriter.println("LATITUDE:" + geoData.getLatitude());
                outputWriter.println("LONGITUDE:" + geoData.getLongitude());
            } else {
                outputWriter.println("Error: City not found in database."); // Mesaj pentru oraș negăsit
            }
        } else if ("GET_WEATHER".equalsIgnoreCase(command)) {
            try {
                double latitude = Double.parseDouble(inputReader.readLine()); // Citește latitudinea
                double longitude = Double.parseDouble(inputReader.readLine()); // Citește longitudinea
                double radius = Double.parseDouble(inputReader.readLine()); // Citește raza

                String weatherData = weatherDataManager.getWeather(latitude, longitude, radius); // Obține prognoza
                outputWriter.println(weatherData); // Trimite prognoza
            } catch (NumberFormatException e) {
                outputWriter.println("Error: Invalid coordinates or radius."); // Mesaj pentru coordonate/raza invalide
            }
        } else {
            outputWriter.println("Error: Unknown user command."); // Mesaj pentru comandă necunoscută
        }
    }

    /**
     * Obține prognoza meteo pe baza coordonatelor și razei specificate.
     * @param inputReader Buffer pentru citirea datelor de la client.
     * @param outputWriter Buffer pentru scrierea răspunsurilor către client.
     */
    private void fetchWeather(BufferedReader inputReader, PrintWriter outputWriter) throws IOException {
        try {
            double latitude = Double.parseDouble(inputReader.readLine()); // Citește latitudinea
            double longitude = Double.parseDouble(inputReader.readLine()); // Citește longitudinea
            double radius = Double.parseDouble(inputReader.readLine()); // Citește raza
            String weatherData = weatherDataManager.getWeather(latitude, longitude, radius); // Obține prognoza
            outputWriter.println(weatherData); // Trimite prognoza către client
        } catch (NumberFormatException e) {
            outputWriter.println(MESSAGE_INVALID_INPUT); // Mesaj pentru input invalid
        }
    }
}
