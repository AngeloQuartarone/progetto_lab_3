package server;

import java.net.*;
import java.io.*;

enum State {
    NO_LOGGED, LOGGED
}

enum NoLoggedState {
    REGISTER, LOGIN, SEARCH, SEARCH_CITY, EXIT
}

enum LoggedState {
    SEARCH, SEARCH_CITY, LOGOUT, REVIEW, BADGE, EXIT
}

public class SessionManager {
    private Socket socket = null;
    private static ComunicationManager comunication;
    private static State actualState = State.NO_LOGGED;
    // File userFile = new File("./src/server/users.json");

    public SessionManager(Socket s) {
        this.socket = s;
    }

    public void run() {
        /*
         * User user = new User("test", "test");
         * if (!User.checkUser(user)) {
         * User.appendUser(user);
         * System.out.println("User added");
         * } else {
         * System.out.println("User already exists");
         * }
         */

        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            comunication = new ComunicationManager(in, out);

            String received = null;
            
            while (!socket.isClosed() && socket.isConnected()) {
                if (actualState == State.NO_LOGGED) {
                    comunication.send("Benvenuto!\n1) Register\n2) Login\n3) Search hotel\n4)Search hotel by city\n5)Exit\n");
                } else {
                    comunication.send("Benvenuto!\n1) Search hotel\n2)Search hotel by city\n3)Logout\n4)Review\n5)Badge\n6)Exit\n");
                }
                received = comunication.receive();
                if (received == null) {
                    // Se received è null, potrebbe significare che il client ha chiuso la connessione
                    System.out.println("Connection closed by client.");
                    break; // Esce dal loop
                }
                System.out.println(received);
            }
            // Chiudi la socket se non è già chiusa
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    private static void registerUser(ComunicationManager comunication) {
        comunication.send("Insert username");
        String username = comunication.receive();
        comunication.send("Insert password");
        String password = comunication.receive();
        User user = new User(username, password);
        if (!User.checkUser(user)) {
            User.insertdUser(user);
            comunication.send("User added");
        } else {
            comunication.send("User already exists");
        }
    }

    private static void loginUser(ComunicationManager comunication) {
        comunication.send("Insert username");
        String username = comunication.receive();
        comunication.send("Insert password");
        String password = comunication.receive();
        User user = new User(username, password);
        if (User.checkUser(user)) {
            comunication.send("User logged in");
        } else {
            comunication.send("User not found");
        }
    }

    
    

    // private void
}
