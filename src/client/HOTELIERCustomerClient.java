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

        try {
            do {
                received = communication.receive();
                if (received == null) {
                    System.out.println("[RECEIVED NULL] An error occurred. Exiting...");
                    break; // Uscire dal ciclo se il server si disconnette inattesamente
                } else {
                    if (!received.equals("PROMPT")) {
                        System.out.println(received);
                    } else {
                        System.out.print("-> ");
                        while ((toSend = keyboardInput.readLine()) == null || toSend.trim().isEmpty()) {
                            System.out.println("No valid input received. Please try again.");
                        }
                        communication.send(toSend);
                    }
                }
            } while (!received.equals("EXIT"));
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            // Chiudi tutte le risorse nel blocco finally per assicurarti che vengano sempre chiuse correttamente
            try {
                if (keyboardInput != null) keyboardInput.close();
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
    }

}
