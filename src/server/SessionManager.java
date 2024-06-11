package server;

import java.net.*;

public class SessionManager {
    private Socket socket = null;
    // File userFile = new File("./src/server/users.json");

    public SessionManager(Socket s) {
        this.socket = s;
    }

    public void run() {
        // openUserFile();
        User user = new User("test", "test");
        if (!User.checkUser(user)) {
            User.appendUser(user);
            System.out.println("User added");
        } else {
            System.out.println("User already exists");
        }
        // checkUser(User user);
        /*
         * try {
         * DataInputStream in = new DataInputStream(socket.getInputStream());
         * DataOutputStream out = new DataOutputStream(socket.getOutputStream());
         * ComunicationManager comunication = new ComunicationManager(in, out);
         * String received = null;
         * while ((received = comunication.receive()) != null) {
         * if (received.equals("ciao")) {
         * System.out.println("received: " + received);
         * comunication.send("ciao dal server!");
         * } else {
         * System.out.println("received: " + received);
         * 
         * comunication.send("errore");
         * }
         * }
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         */

    }

    // private void
}
