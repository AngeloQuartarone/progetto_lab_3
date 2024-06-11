package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Properties;

import server.ComunicationManager;

public class HOTELIERCustomerClient {
    private String ipAddr = "";
    private String port = "";
    private Socket socket = null;

    public HOTELIERCustomerClient() {
        init("./src/client/clientParameter.properties");
    }

    private void init(String configFile) {
        InputStream fileInput = null;

        try {
            fileInput = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties prop = new Properties();

        try {
            prop.load(fileInput);

            // Leggi le proprietà
            ipAddr = prop.getProperty("SERVER_IP");
            port = prop.getProperty("SERVER_PORT");
            System.out.println(ipAddr);
            System.out.println(port);
            fileInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket(ipAddr, Integer.parseInt(port));
            System.out.println("Connected");

        } catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

    public void runCLI() {
        BufferedReader keyboardInput = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            keyboardInput = new BufferedReader(new InputStreamReader(System.in));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        ComunicationManager comunication = null;
        try {
            comunication = new ComunicationManager(in, out);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        comunication.send("HELLO");

    }
}
