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
import java.util.Properties;

import server.CommunicationManager;

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
        String received = null;
        String toSend = null;
        try {
            keyboardInput = new BufferedReader(new InputStreamReader(System.in));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        CommunicationManager communication = null;
        try {
            communication = new CommunicationManager(in, out);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        do {
            received = communication.receive();
            if (received == null) {
                System.out.println("[RECEIVED NULL]An error occurred. Exiting...");
                continue;
            } else {
                try {
                    if (!received.equals("PROMPT")) {
                        // System.out.println("\033[H\033[2J");
                        // System.out.flush();
                        System.out.println(received);
                    } else {
                        System.out.print("-> ");
                        while ((toSend = keyboardInput.readLine()) == null || toSend.trim().isEmpty()) {
                            System.out.println("No valid input received. Please try again.");
                        }
                        communication.send(toSend);
                    }

                } catch (IOException e) {
                    System.out.println(e);
                    return;
                }
            }
        } while (!received.equals("EXIT"));
    }

}
