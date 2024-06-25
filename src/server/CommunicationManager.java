package server;

import java.io.*;

/**
 * CommunicationManager class
 */
public class CommunicationManager {
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Constructor
     * 
     * @param i DataInputStream
     * @param o DataOutputStream
     */
    public CommunicationManager(DataInputStream i, DataOutputStream o) {
        this.in = i;
        this.out = o;
    }


    /**
     * send a string to DataOutputStream
     * 
     * @param message the string to send
     */
    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            return;
        }
    }

    /**
     * receive a string from DataInputStream
     * 
     * @return the string received if it is not null, null otherwise
     */
    public String receive() {
        try {
            String x = in.readUTF();
            if (x == null || x.equals("")) {
                return null;
            } else {
                return x;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
