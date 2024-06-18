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
    // private static Socket clientSocket = null;
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
        ReviewEngine reviewEngine = new ReviewEngine(hotelsPath);

        // Crea un'istanza di Timer
        Timer timer = new Timer();

        // Crea un'istanza di TimerTask
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                reviewEngine.calculateMeanRatesById();
                reviewEngine.updateHotelFile();
                //System.out.println("Azione schedulata eseguita - Thread: " + Thread.currentThread().getName());
                System.out.println("[" + Thread.currentThread().getName() + "] - Hotel file aggiornato");
            }
        };

        // Pianifica il TimerTask per l'esecuzione periodica ogni minuto (60000
        // millisecondi)
        timer.scheduleAtFixedRate(task, 0, /*3600000*/30000);

        try {

            while (true) {
                //System.out.println("In attesa di connessioni... Thread: " + Thread.currentThread().getName());
                System.out.println("[" + Thread.currentThread().getName() + "] - In attesa di connessioni...");
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Connessione accettata da: " + clientSocket.getInetAddress().getHostAddress()
                       // + " - Thread: " + Thread.currentThread().getName());
                System.out.println("[" + Thread.currentThread().getName() + "] - Connessione accettata da: "
                        + clientSocket.getInetAddress().getHostAddress());
                SessionManager connection = new SessionManager(clientSocket, hotelsPath);

                executor.execute(() -> {
                    //System.out.println("Gestione connessione iniziata - Thread: " + Thread.currentThread().getName());
                    System.out.println("[" + Thread.currentThread().getName() + "] - Gestione connessione iniziata");
                    connection.run();
                    //System.out.println("Gestione connessione terminata - Thread: " + Thread.currentThread().getName());
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
            //System.out.println("Hotels json file path:" + hotelsPath);
            System.out.println("[" + Thread.currentThread().getName() + "] - Server started at IP: " + ipAddr + " Port: " + port);
        
            //System.out.println("IP: " + ipAddr);
            //System.out.println("Port: " + port);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
           //System.out.println("Server started " +" - Thread: " + Thread.currentThread().getName());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        // Inizializza l'ExecutorService con un numero fisso di thread.
        // Ad esempio, crea un pool di thread con 10 thread.
        executor = Executors.newCachedThreadPool();
    }

}
