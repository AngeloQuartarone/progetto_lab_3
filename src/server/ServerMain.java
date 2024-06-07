package server;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.stream.JsonReader;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerMain {
    private static String hotelsPath = "";
    private static String ipAddr = "";
    private static String port = "";

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        ServerMain server = new ServerMain();
        server.init("./src/server/serverParameter.properties");
        JsonParser x = new JsonParser(hotelsPath);
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = new ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>>();
        // hotels = x.parse();
        // x.printAll(hotels);
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));

            while (true) {
                clientSocket = serverSocket.accept();
                SessionManager connection = new SessionManager(clientSocket);
                connection.run();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket.close();
            clientSocket.close();
        } catch (IOException i) {
            System.out.println(i);
        }

    }

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
    }

}
