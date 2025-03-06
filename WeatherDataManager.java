package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeatherDataManager {
    // Constante generale
    private static final int EARTH_RADIUS = 6371; // Raza Pământului în kilometri

    // Mesaje
    private static final String DATABASE_EMPTY_MESSAGE = "Database is empty or failed to load."; // Mesaj pentru baza de date goală
    private static final String DATABASE_LOADED_MESSAGE = "Database loaded successfully with %d locations."; // Mesaj pentru baza de date încărcată
    private static final String INVALID_JSON_MESSAGE = "Invalid JSON file: %s"; // Mesaj pentru fișier JSON invalid
    private static final String VALID_JSON_MESSAGE = "Valid JSON file: %s"; // Mesaj pentru fișier JSON valid
    private static final String NO_LOCATIONS_FOUND_MESSAGE = "No locations found in the file."; // Mesaj pentru lipsa locațiilor
    private static final String DATABASE_UPDATED_MESSAGE = "Database updated successfully with %d new locations."; // Mesaj pentru baza de date actualizată
    private static final String NO_LOCATION_RADIUS_MESSAGE = "No location found within the specified radius."; // Mesaj pentru lipsa locațiilor în rază
    private static final String CLOSEST_LOCATION_MESSAGE = "Closest location: %s, %s\n"; // Mesaj pentru locația cea mai apropiată

    private final List<GeoWeatherData> locationData; // Lista locațiilor meteo

    // Constructor: Încarcă baza de date dintr-un fișier JSON specificat
    public WeatherDataManager(String filePath) {
        locationData = loadDatabase(filePath); // Încarcă locațiile
        if (locationData.isEmpty()) {
            System.out.println(DATABASE_EMPTY_MESSAGE); // Mesaj dacă baza de date este goală
        } else {
            System.out.printf(DATABASE_LOADED_MESSAGE + "\n", locationData.size()); // Mesaj dacă baza de date a fost încărcată
        }
    }

    // Încarcă locațiile meteo dintr-un fișier JSON
    private List<GeoWeatherData> loadDatabase(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Type listType = new TypeToken<List<GeoWeatherData>>() {}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(GeoWeatherData.class, new GeoWeatherData.GeoWeatherDataDeserializer())
                    .create();
            return gson.fromJson(reader, listType); // Returnează lista locațiilor
        } catch (IOException e) {
            System.err.println("Error loading database: " + e.getMessage()); // Mesaj de eroare la încărcare
            return new ArrayList<>();
        }
    }

    // Returnează informațiile meteo pentru o locație pe baza coordonatelor și razei de căutare
    public String getWeather(double latitude, double longitude, double searchRadius) {
        Optional<GeoWeatherData> closestLocation = findClosestLocation(latitude, longitude); // Caută locația cea mai apropiată
        if (closestLocation.isPresent()) {
            GeoWeatherData location = closestLocation.get();
            StringBuilder result = new StringBuilder();
            result.append(String.format(CLOSEST_LOCATION_MESSAGE, location.getCity(), location.getCountry())); // Adaugă locația cea mai apropiată
            for (DailyForecast forecast : location.getWeatherList()) {
                result.append(forecast.toString()).append("\n"); // Adaugă prognozele meteo
            }
            return result.toString();
        } else {
            return NO_LOCATION_RADIUS_MESSAGE; // Mesaj dacă nu se găsește nicio locație
        }
    }

    // Găsește locația cea mai apropiată de coordonatele specificate
    public Optional<GeoWeatherData> findClosestLocation(double latitude, double longitude) {
        return locationData.stream()
                .min((loc1, loc2) -> Double.compare(
                        haversineDistance(latitude, longitude, loc1.getLatitude(), loc1.getLongitude()),
                        haversineDistance(latitude, longitude, loc2.getLatitude(), loc2.getLongitude())
                )); // Returnează locația cu distanța minimă
    }

    // Calculează distanța Haversine între două puncte geografice
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); // Returnează distanța în kilometri
    }

    // Validează dacă un fișier JSON este valid
    public boolean validateJsonFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.printf(INVALID_JSON_MESSAGE + "\n", filePath); // Mesaj pentru fișier invalid
            return false;
        }
        System.out.printf(VALID_JSON_MESSAGE + "\n", filePath); // Mesaj pentru fișier valid
        return true;
    }

    // Caută o locație pe baza numelui orașului
    public Optional<GeoWeatherData> findLocationByCity(String city) {
        return locationData.stream()
                .filter(location -> location.getCity().equalsIgnoreCase(city)) // Filtrează locațiile care corespund numelui orașului
                .findFirst(); // Returnează prima locație găsită
    }

    // Importează locații noi în baza de date dintr-un fișier JSON
    public boolean importDatabase(String filePath) {
        try {
            List<GeoWeatherData> newLocations = loadDatabase(filePath); // Încarcă locațiile din fișier

            if (newLocations.isEmpty()) {
                System.out.println(NO_LOCATIONS_FOUND_MESSAGE); // Mesaj pentru lipsa locațiilor
                return false;
            }

            // Elimină locațiile deja existente
            newLocations.removeIf(newLoc -> locationData.stream()
                    .anyMatch(existingLoc -> existingLoc.getCity().equalsIgnoreCase(newLoc.getCity())));

            locationData.addAll(newLocations); // Adaugă locațiile noi
            System.out.printf(DATABASE_UPDATED_MESSAGE + "\n", newLocations.size()); // Mesaj pentru locațiile adăugate
            return true;

        } catch (Exception e) {
            System.err.println("Error importing database: " + e.getMessage()); // Mesaj de eroare la import
            return false;
        }
    }
}
