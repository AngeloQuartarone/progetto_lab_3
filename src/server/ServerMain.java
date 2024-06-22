package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Timer;

/**
 * ServerMain class
 */
public class ServerMain {
    private static String hotelsPath = "";
    private static String ipAddr = "";
    private static String port = "";
    private static ServerSocket serverSocket = null;
    private static ExecutorService executor = null;

    /**
     * Main method
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Period not specified. Usage: java -cp bin:lib/gson.jar server.ServerMain <period in milliseconds>");
            return;
        }

        long period = Long.parseLong(args[0]); // Converti l'argomento in long

        ServerMain server = new ServerMain();
        server.init("./src/server/serverParameter.properties");
        ReviewEngine reviewEngine = new ReviewEngine(hotelsPath);

        // Crea un'istanza di Timer
        Timer timer = new Timer();

        // Crea un'istanza di TimerTask
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                reviewEngine.calculateMeanRatesById();
                reviewEngine.updateHotelFile();
                System.out.println("[" + Thread.currentThread().getName() + "] - Hotel file aggiornato");
            }
        };

        timer.scheduleAtFixedRate(task, 0, period);

        try {

            while (true) {
                System.out.println("[" + Thread.currentThread().getName() + "] - In attesa di connessioni...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("[" + Thread.currentThread().getName() + "] - Connessione accettata da: "
                        + clientSocket.getInetAddress().getHostAddress());
                SessionManager connection = new SessionManager(clientSocket, hotelsPath);

                executor.execute(() -> {
                    System.out.println("[" + Thread.currentThread().getName() + "] - Gestione connessione iniziata");
                    connection.run();
                    System.out.println("[" + Thread.currentThread().getName() + "] - Gestione connessione terminata");
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
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
            ipAddr = prop.getProperty("IP");
            port = prop.getProperty("PORT");
            hotelsPath = prop.getProperty("HOTELSPATH");
            System.out.println("[" + Thread.currentThread().getName() + "] - Server started at IP: " + ipAddr + " Port: " + port);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
        } catch (IOException e) {
            System.out.println(e);
            return;
        }
        
        executor = Executors.newCachedThreadPool();
    }

}
