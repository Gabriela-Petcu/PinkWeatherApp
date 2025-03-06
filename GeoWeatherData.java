package common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.*;
import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

public class GeoWeatherData {
    // Constante pentru mesaje
    private static final String NO_WEATHER_MESSAGE = "No weather data available.\n"; // Mesaj pentru lipsa datelor meteo
    private static final String WEATHER_HEADER_FORMAT = "Weather data for %s, %s (%.6f, %.6f):\n"; // Format pentru antetul prognozei

    // Chei JSON utilizate pentru deserializare
    private static final String JSON_CITY = "city"; // Cheie pentru oraș
    private static final String JSON_COUNTRY = "country"; // Cheie pentru țară
    private static final String JSON_COORDINATES = "coordinates"; // Cheie pentru coordonate
    private static final String JSON_LATITUDE = "latitude"; // Cheie pentru latitudine
    private static final String JSON_LONGITUDE = "longitude"; // Cheie pentru longitudine
    private static final String JSON_FORECAST = "forecast"; // Cheie pentru prognoză

    private final double latitude; // Latitudinea locației
    private final double longitude; // Longitudinea locației
    private final String city; // Orașul locației
    private final String country; // Țara locației
    private final List<DailyForecast> weatherList; // Lista prognozelor meteo

    // Constructor care inițializează obiectul GeoWeatherData
    public GeoWeatherData(String city, String country, Coordinates coordinates, List<DailyForecast> weatherList) {
        this.city = city;
        this.country = country;
        this.latitude = coordinates.getLatitude();
        this.longitude = coordinates.getLongitude();
        this.weatherList = weatherList != null ? new ArrayList<>(weatherList) : new ArrayList<>();
    }

    // Returnează latitudinea locației
    public double getLatitude() {
        return latitude;
    }

    // Returnează longitudinea locației
    public double getLongitude() {
        return longitude;
    }

    // Returnează numele orașului
    public String getCity() {
        return city;
    }

    // Returnează numele țării
    public String getCountry() {
        return country;
    }

    // Returnează lista prognozelor meteo
    public List<DailyForecast> getWeatherList() {
        return new ArrayList<>(weatherList);
    }

    // Compară două obiecte GeoWeatherData pe baza coordonatelor și informațiilor locației
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoWeatherData that = (GeoWeatherData) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country);
    }

    // Returnează codul hash al obiectului pe baza coordonatelor și informațiilor locației
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, city, country);
    }

    // Formatează informațiile despre prognoza meteo într-un șir de caractere
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(WEATHER_HEADER_FORMAT, city, country, latitude, longitude));

        if (weatherList.isEmpty()) {
            sb.append(NO_WEATHER_MESSAGE); // Adaugă mesajul pentru lipsa datelor
        } else {
            // Sortează prognozele în ordine cronologică și le adaugă în șirul de caractere
            weatherList.stream()
                    .sorted((f1, f2) -> f1.getDate().compareTo(f2.getDate()))
                    .forEach(forecast -> sb.append(forecast).append("\n"));
        }

        return sb.toString();
    }

    // Clasă internă pentru coordonatele locației
    public static class Coordinates {
        private final double latitude; // Latitudinea
        private final double longitude; // Longitudinea

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    // Clasă pentru deserializarea JSON-ului într-un obiect GeoWeatherData
    public static class GeoWeatherDataDeserializer implements JsonDeserializer<GeoWeatherData> {
        @Override
        public GeoWeatherData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Extrage datele din JSON
            String city = jsonObject.get(JSON_CITY).getAsString();
            String country = jsonObject.get(JSON_COUNTRY).getAsString();

            JsonObject coordinatesJson = jsonObject.getAsJsonObject(JSON_COORDINATES);
            Coordinates coordinates = new Coordinates(
                    coordinatesJson.get(JSON_LATITUDE).getAsDouble(),
                    coordinatesJson.get(JSON_LONGITUDE).getAsDouble()
            );

            // Deserializare prognoze meteo
            List<DailyForecast> forecast = context.deserialize(jsonObject.getAsJsonArray(JSON_FORECAST), new TypeToken<List<DailyForecast>>() {}.getType());

            // Creează și returnează obiectul GeoWeatherData
            return new GeoWeatherData(city, country, coordinates, forecast);
        }
    }
}
