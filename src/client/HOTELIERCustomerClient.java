package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * HOTELIERCustomerClient class
 */
public class HOTELIERCustomerClient {
    private String ipAddr = "";
    private String tcpPort = "";
    private String udpPort = "";
    private int port = 0;
    private Socket socket = null;

    /**
     * Constructor
     */
    public HOTELIERCustomerClient() {
        init("./src/client/clientParameter.properties");
    }

    /**
     * Initialize the client
     * 
     * @param configFile the path of the configuration file
     */
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
            tcpPort = prop.getProperty("SERVER_TCP_PORT");
            udpPort = prop.getProperty("SERVER_UDP_PORT");
            System.out.println(ipAddr);
            System.out.println(port);
            fileInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket(ipAddr, Integer.parseInt(tcpPort));
            System.out.println("Connected");

        } catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

    /**
     * Run the CLI for the client
     */
    public void runCLI() {
        // Creazione e avvio del thread per l'ascolto UDP
        new Thread(() -> {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(Integer.parseInt(udpPort)));
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                while (true) {
                    datagramSocket.receive(receivePacket);
                    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println(received);
                }
            } catch (IOException e) {
                System.out.println("Errore ascolto UDP: " + e.getMessage());
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        }).start();

        // Continuazione della normale esecuzione per la comunicazione TCP
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
                    System.out.println("[RECEIVED NULL]An error occurred. Exiting...");
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
            try {
                if (keyboardInput != null)
                    keyboardInput.close();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
    }

}
