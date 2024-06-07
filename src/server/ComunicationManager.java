package server;
import java.net.*;
import java.io.*;

public class ComunicationManager {
    private DataInputStream in;
    private DataOutputStream out;

    public ComunicationManager(DataInputStream i, DataOutputStream o) {
        this.in = i;
        this.out = o;
    }

    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            return in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
