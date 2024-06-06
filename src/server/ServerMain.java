package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

public class ServerMain {

    public static void main(String[] args) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("./src/server/serverParameter.properties"));

            // Leggi le proprietà
            String ipAddr = prop.getProperty("IP");
            String port = prop.getProperty("PORT");
            System.out.println(ipAddr);
            System.out.println(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        



    }
}