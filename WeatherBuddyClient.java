package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WeatherBuddyClient {
    // Constante pentru server
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Adresa serverului
    private static final int SERVER_PORT = 12346; // Portul serverului

    // Constante pentru roluri
    private static final String ROLE_USER = "USER"; // Rol pentru utilizator
    private static final String ROLE_ADMIN = "ADMIN"; // Rol pentru administrator

    // Constante pentru mesaje
    private static final String PROMPT_ROLE = "Select role (ADMIN/USER): "; // Mesaj pentru selectarea rolului
    private static final String PROMPT_LATITUDE = "Enter latitude: "; // Mesaj pentru introducerea latitudinii
    private static final String PROMPT_LONGITUDE = "Enter longitude: "; // Mesaj pentru introducerea longitudinii
    private static final String PROMPT_RADIUS = "Enter radius (km): "; // Mesaj pentru introducerea razei
    private static final String PROMPT_JSON_PATH = "Enter JSON file path to import: "; // Mesaj pentru calea fișierului JSON
    private static final String ERROR_INVALID_ROLE = "Invalid role."; // Mesaj pentru rol invalid
    private static final String MESSAGE_WEATHER_DATA = "Weather data from server:"; // Mesaj pentru datele meteo
    private static final String MESSAGE_RESPONSE = "Response: "; // Mesaj pentru răspuns

    public static void main(String[] args) {
        // Conexiune la server și gestionarea cererilor
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner scanner = new Scanner(System.in);
            System.out.print(PROMPT_ROLE); // Solicita utilizatorului sa selecteze un rol
            String role = scanner.nextLine().toUpperCase(); // Citirea si convertirea rolului în litere mari
            writer.println(role); // Trimite rolul catre server

            if (ROLE_USER.equals(role)) {
                // Daca rolul este USER, solicita coordonatele și raza
                System.out.print(PROMPT_LATITUDE); // Solicita latitudinea
                writer.println(scanner.nextLine());
                System.out.print(PROMPT_LONGITUDE); // Solicita longitudinea
                writer.println(scanner.nextLine());
                System.out.print(PROMPT_RADIUS); // Solicita raza
                writer.println(scanner.nextLine());

                // Afiseaza datele meteo primite de la server
                System.out.println(MESSAGE_WEATHER_DATA);
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } else if (ROLE_ADMIN.equals(role)) {
                // Daca rolul este ADMIN, solicita calea fisierului JSON pentru import
                System.out.print(PROMPT_JSON_PATH); // Solicita calea fisierului JSON
                writer.println("IMPORT"); // Trimite comanda de import catre server
                writer.println(scanner.nextLine()); // Trimite calea fisierului catre server
                System.out.println(MESSAGE_RESPONSE + reader.readLine()); // Afiseaza raspunsul serverului
            } else {
                // Mesaj de eroare pentru rol invalid
                System.out.println(ERROR_INVALID_ROLE);
            }

        } catch (Exception e) {
            // Afiseaza eroarea in caz de excepție
            e.printStackTrace();
        }
    }
}
