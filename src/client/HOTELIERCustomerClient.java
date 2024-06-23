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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * HOTELIERCustomerClient class
 */
public class HOTELIERCustomerClient {
    private String ipAddr = "";
    private String tcpPort = "";
    private String udpPort = "";
    private String udpIp = "";
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
            udpIp = prop.getProperty("SERVER_MULTI");
            udpPort = prop.getProperty("SERVER_UDP_PORT");
            fileInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket(ipAddr, Integer.parseInt(tcpPort));

        } catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

    /**
     * Run the CLI for the client
     */
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
                    System.out.println("[RECEIVED NULL]An error occurred. Exiting...");
                    break; // Uscire dal ciclo se il server si disconnette inattesamente
                } else {
                    if (received.equals("LOGIN")) {
                        startMulticastListener(udpIp, udpPort);
                        continue;
                    }

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

    @SuppressWarnings("deprecation")
    public void startMulticastListener(String udpIp, String udpPort) {
        new Thread(() -> {
            MulticastSocket multicastSocket = null;
            try {
                multicastSocket = new MulticastSocket(Integer.parseInt(udpPort));
                InetAddress multicastGroup = InetAddress.getByName(udpIp);
                multicastSocket.joinGroup(multicastGroup);
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                while (true) {
                    multicastSocket.receive(receivePacket);
                    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (received != null && !received.isEmpty()) {
                        System.out.println("\n\n" + received + "\n\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Errore ascolto UDP multicast: " + e.getMessage());
            } finally {
                if (multicastSocket != null) {
                    try {
                        if (!multicastSocket.isClosed()) {
                            multicastSocket.leaveGroup(InetAddress.getByName(udpIp));
                            multicastSocket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Errore nella chiusura della MulticastSocket: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

}
