package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServerMain class
 */
public class ServerMain {
    private static String hotelsPath = "";
    private static String ipAddr = "";
    private static String port = "";
    private static ServerSocket serverSocket = null;
    //private static Socket clientSocket = null;
    private static ExecutorService executor = null;

    /**
     * Main method
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ServerMain server = new ServerMain();
        server.init("./src/server/serverParameter.properties");

        executor = Executors.newCachedThreadPool();
        
        try {
            // Assicurati che la variabile port sia definita e inizializzata correttamente
            // Esempio: int port = 8080;
            // serverSocket = new ServerSocket(port);

            while (true) {
                System.out.println("In attesa di connessioni... Thread: " + Thread.currentThread().getName());
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da: " + clientSocket.getInetAddress().getHostAddress() + " - Thread: " + Thread.currentThread().getName());
                SessionManager connection = new SessionManager(clientSocket, hotelsPath);
                
                executor.execute(() -> {
                    System.out.println("Gestione connessione iniziata - Thread: " + Thread.currentThread().getName());
                    connection.run();
                    System.out.println("Gestione connessione terminata - Thread: " + Thread.currentThread().getName());
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                // Chiudi la ThreadPool solo se non è null
                if (executor != null) {
                    executor.shutdown();
                }
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }


    /**
     * Initialize the server
     * 
     * @param configFile the path of the configuration file
     */
    void init(String configFile) {
        InputStream input = null;
        try {
            input = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties prop = new Properties();

        try {
            prop.load(input);

            // Leggi le proprietà
            ipAddr = prop.getProperty("IP");
            port = prop.getProperty("PORT");
            hotelsPath = prop.getProperty("HOTELSPATH");
            System.out.println("path:" + hotelsPath);
            System.out.println(ipAddr);
            System.out.println(port);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
            System.out.println("Server started");
        } catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

}
