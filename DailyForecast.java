package common;

/**
 * Reprezintă informațiile meteo pentru o anumită dată.
 */
public class DailyForecast {
    // Constante pentru formatarea informațiilor despre temperatură și prognoză
    private static final String TEMPERATURE_FORMAT = "%.1f°C"; // Formatul pentru temperatură
    private static final String FORECAST_FORMAT = "%s: %s, %s"; // Formatul pentru prognoză (dată, condiții, temperatură)

    private final String date;        // Data prognozei meteo
    private final String condition;  // Condițiile meteo (ex: însorit, înnorat)
    private final double temperature; // Temperatura în grade Celsius

    // Constructor care inițializează prognoza meteo pentru o anumită dată
    public DailyForecast(String date, String condition, double temperature) {
        this.date = date;
        this.condition = condition;
        this.temperature = temperature;
    }

    // Returnează data prognozei meteo
    public String getDate() {
        return date;
    }

    // Returnează condițiile meteo
    public String getCondition() {
        return condition;
    }

    // Returnează temperatura în grade Celsius
    public double getTemperature() {
        return temperature;
    }

    // Returnează prognoza meteo formatată ca un șir de caractere
    @Override
    public String toString() {
        return String.format(FORECAST_FORMAT, date, condition, String.format(TEMPERATURE_FORMAT, temperature));
    }
}
