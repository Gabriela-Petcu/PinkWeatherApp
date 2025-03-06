package server;

import common.WeatherDataManager;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Clasa PinkWeatherServer reprezintă serverul principal al aplicației de prognoză meteo.
 * Acesta gestionează conexiunile clientului și utilizează un manager de date meteo pentru procesarea cererilor.
 */
public class PinkWeatherServer {
    // Constante
    private static final int PORT = 12346; // Portul pe care serverul ascultă conexiunile
    private static final String SERVER_START_MESSAGE = "Starting Pink Weather Server on port "; // Mesaj la pornirea serverului
    private static final String SERVER_RUNNING_MESSAGE = "Server is running and listening on port "; // Mesaj când serverul rulează
    private static final String CLIENT_CONNECTED_MESSAGE = "New client connected: "; // Mesaj pentru client conectat
    private static final String CLIENT_ERROR_MESSAGE = "Error handling client connection: "; // Mesaj pentru eroare conexiune client
    private static final String SERVER_FAIL_MESSAGE = "Failed to start server: "; // Mesaj pentru eroare la pornirea serverului
    private static final String DEFAULT_DB_FILE_PATH = "D:\\Facultate\\Anul2Sem1\\MIP\\vreme\\WeatherApp\\src\\main\\resources\\pink_weather.json"; // Calea implicită pentru fișierul bazei de date

    private final WeatherDataManager weatherDataManager; // Managerul de date meteo

    /**
     * Constructorul clasei PinkWeatherServer.
     * @param dbFilePath Calea către fișierul json
     */
    public PinkWeatherServer(String dbFilePath) {
        this.weatherDataManager = new WeatherDataManager(dbFilePath);
    }

    /**
     * Pornirea serverului pt a asc conexiunile clientului
     */
    public void start() {
        System.out.println(SERVER_START_MESSAGE + PORT); // Mesaj la pornire

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Creează un server socket
            System.out.println(SERVER_RUNNING_MESSAGE + PORT); // Mesaj că serverul rulează

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Acceptă conexiunea unui client
                    System.out.println(CLIENT_CONNECTED_MESSAGE + clientSocket.getInetAddress()); // Mesaj despre client conectat
                    new Thread(new ConnectionHandler(clientSocket, weatherDataManager)).start(); // Creează un thread pentru client
                } catch (IOException e) {
                    System.err.println(CLIENT_ERROR_MESSAGE + e.getMessage()); // Mesaj pentru eroare conexiune client
                }
            }
        } catch (IOException e) {
            System.err.println(SERVER_FAIL_MESSAGE + e.getMessage()); // Mesaj pentru eroare la pornirea serverului
            e.printStackTrace(); // Afișează detaliile erorii
        }
    }

    /**
     * Punctul de intrare al aplicației server.
     * @param args Argumente opționale pentru linia de comandă.
     */
    public static void main(String[] args) {
        String dbFilePath = DEFAULT_DB_FILE_PATH; // Utilizează calea implicită pentru baza de date
        new PinkWeatherServer(dbFilePath).start(); // Pornește serverul
    }
}
