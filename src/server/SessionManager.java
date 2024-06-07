package server;

import java.net.*;
import java.io.*;

public class SessionManager {
    private Socket socket = null;

    public SessionManager(Socket s) {
        this.socket = s;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            ComunicationManager comunication = new ComunicationManager(in, out);
            comunication.send("ciao dal server!");
            String x = comunication.receive();
            System.out.println(x);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
