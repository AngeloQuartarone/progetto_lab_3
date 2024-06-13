package server;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
    private static Socket socket = null;
    private static String hotelsPath = null;
    private static ComunicationManager comunication;
    private static State actualState = State.NO_LOGGED;
    // File userFile = new File("./src/server/users.json");

    public SessionManager(Socket s, String hotelsP) {
        socket = s;
        hotelsPath = hotelsP;
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

            while (!socket.isClosed() /*&& socket.isConnected()*/ ) {
                if (actualState == State.NO_LOGGED) {
                    comunication.send(
                            "Benvenuto!\n1) Register\n2) Login\n3) Search hotel\n4)Search hotel by city\n5)Exit\n");
                } else if (actualState == State.LOGGED) {
                    comunication.send(
                            "Benvenuto!\n1) Search hotel\n2)Search hotel by city\n3)Logout\n4)Review\n5)Badge\n6)Exit\n");
                } else {
                    System.out.println("Error");
                    break;
                }

                received = comunication.receive();
                if (received == null) {
                    break;
                } else {
                    SessionManager.handleMessage(received);
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

    private static void handleMessage(String message) {
        if (actualState == State.NO_LOGGED) {
            switch (message) {
                case "1":
                    registerUser(comunication);
                    break;
                case "2":
                    loginUser(comunication);
                    break;
                case "3":
                    // searchHotel(comunication);
                    break;
                case "4":
                    // searchHotelByCity(comunication);
                    searchHotelByCity(comunication);
                    break;
                case "5":
                    exit(comunication);
                    break;
                default:
                    comunication.send("Invalid command");
                    break;
            }
        } else if (actualState == State.LOGGED) {
            switch (message) {
                case "1":
                    // searchHotel(comunication);
                    break;
                case "2":
                    // searchHotelByCity(comunication);
                    searchHotelByCity(comunication);
                    break;
                case "3":
                    // logout(comunication);
                    break;
                case "4":
                    // review(comunication);
                    break;
                case "5":
                    // badge(comunication);
                    break;
                case "6":
                    // exit(comunication);
                    exit(comunication);
                    break;
                default:
                    comunication.send("Invalid command");
                    break;
            }
        }
    }

    private static void registerUser(ComunicationManager comunication) {
        comunication.send("Insert username");
        String username = comunication.receive();
        if (username == null) {
            return;
        }
        comunication.send("Insert password");
        String password = comunication.receive();
        if (password == null) {
            return;
        }
        User user = new User(username, password);
        if (!User.checkUser(user)) {
            User.insertUser(user);
            comunication.send("User added");
        } else {
            comunication.send("User already exists");
        }
    }

    private static void loginUser(ComunicationManager comunication) {
        comunication.send("Insert username");
        String username = comunication.receive();
        if (username == null) {
            return;
        }
        comunication.send("Insert password");
        String password = comunication.receive();
        if (password == null) {
            return;
        }
        User user = new User(username, password);
        if (User.checkUser(user)) {
            comunication.send("User logged in");
            actualState = State.LOGGED;
        } else {
            comunication.send("User not found, please retry o register");
        }
    }

    private static void exit(ComunicationManager comunication) {
        try {
            comunication.send("Goodbye!");
            comunication.send("EXIT");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public static void searchHotelByCity(ComunicationManager comunication) {
        comunication.send("Insert city");
        String hotelName = comunication.receive();
        if (hotelName == null) {
            return;
        }
        
        SearchEngine searchEngine = new SearchEngine(hotelsPath);

        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = searchEngine.searchByCity(hotelName);
        String toSend = searchEngine.formatHotels(hotels);
        comunication.send(toSend);
    }
}
