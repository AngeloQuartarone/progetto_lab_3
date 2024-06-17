package server;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

enum State {
    NO_LOGGED, LOGGED, SEARCH, SEARCH_CITY, REGISTER, LOGIN, LOGOUT, REVIEW, BADGE, EXIT
}

/**
 * SessionManager class
 */
public class SessionManager {
    private static Socket socket = null;
    private static String hotelsPath = null;
    private static CommunicationManager communication;
    private static State actualState = State.NO_LOGGED;
    // private SearchEngine searchEngine = null;
    // File userFile = new File("./src/server/users.json");
    private static ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = null;
    private static SearchEngine searchEngine = null;

    /**
     * Constructor
     * 
     * @param s       the socket
     * @param hotelsP the path of the hotels file
     */
    public SessionManager(Socket s, String hotelsP) {
        socket = s;
        hotelsPath = hotelsP;
    }

    /**
     * Run the session
     */
    public void run() {
        searchEngine = new SearchEngine(hotelsPath);
        DataInputStream in = null;
        DataOutputStream out = null;
        hotels = new ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>>();

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            communication = new CommunicationManager(in, out);

            while (!socket.isClosed()) {

                if (!handleMessage()) {
                    break;
                }
            }
            /*
             * if (!socket.isClosed()) {
             * try {
             * socket.close();
             * } catch (IOException e) {
             * System.err.println("Error closing socket: " + e.getMessage());
             * }
             * }
             */

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (socket != null)
                    socket.close();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e);
            }
        }

    }

    /**
     * Handle the message
     * 
     * @return true if the message is handled, false otherwise
     */
    private static boolean handleMessage() {
        String message = null;
        if (actualState == State.NO_LOGGED) {
            try {
                communication
                        .send("Benvenuto!\n\n1) Register\n2) Login\n3) Search hotel\n4) Search hotel by city\n5) Exit");
                communication.send("PROMPT");
                message = communication.receive();
            } catch (Exception e) {
                return false;
            }

            if (message == null) {
                return false;
            }
            switch (message) {
                case "1":
                    // actualState = State.REGISTER;
                    registerUser(communication);
                    break;
                case "2":
                    // actualState = State.LOGIN;
                    loginUser(communication);
                    // actualState = State.LOGGED;
                    break;
                case "3":
                    // searchHotel(communication);
                    searchHotel(communication);
                    break;
                case "4":
                    // searchHotelByCity(communication);
                    searchHotelByCity(communication);
                    break;
                case "5":
                    exit(communication);
                    break;
                default:
                    communication.send("Invalid command");
                    break;
            }
        } else if (actualState == State.LOGGED) {
            communication.send(
                    "Benvenuto!\n\n1) Search hotel\n2) Search hotel by city\n3) Logout\n4) Review\n5) Badge\n6) Exit");
            communication.send("PROMPT");
            message = communication.receive();
            switch (message) {
                case "1":
                    // searchHotel(communication);
                    searchHotel(communication);
                    break;
                case "2":
                    // searchHotelByCity(communication);
                    searchHotelByCity(communication);
                    break;
                case "3":
                    // logout(communication);
                    actualState = State.NO_LOGGED;
                    break;
                case "4":
                    // review(communication);
                    break;
                case "5":
                    // badge(communication);
                    break;
                case "6":
                    // exit(communication);
                    exit(communication);
                    break;
                default:
                    communication.send("Invalid command");
                    break;
            }
        }
        return true;

    }

    /**
     * Register a user
     * 
     * @param communication
     */
    private static void registerUser(CommunicationManager communication) {
        communication.send("Insert username");
        communication.send("PROMPT");
        String username = communication.receive();
        if (username == null) {
            return;
        }
        communication.send("Insert password");
        communication.send("PROMPT");
        String password = communication.receive();
        if (password == null) {
            return;
        }
        User user = new User(username, password);
        if (!User.checkUser(user)) {
            User.insertUser(user);
            communication.send("User added");
        } else {
            communication.send("User already exists");
        }
    }

    /**
     * Login a user
     * 
     * @param communication
     */
    private static void loginUser(CommunicationManager communication) {
        communication.send("Insert username");
        communication.send("PROMPT");
        String username = communication.receive();
        if (username == null) {
            return;
        }
        communication.send("Insert password");
        communication.send("PROMPT");
        String password = communication.receive();
        if (password == null) {
            return;
        }
        User user = new User(username, password);
        if (User.checkUser(user)) {
            communication.send("User logged in");
            actualState = State.LOGGED;
        } else {
            communication.send("User not found, please retry o register");
        }
    }

    /**
     * Search a hotel by city
     * 
     * @param communication
     */
    public static void searchHotelByCity(CommunicationManager communication) {
        LinkedBlockingQueue<Hotel> hotelList = null;
        communication.send("Insert city");
        communication.send("PROMPT");
        String hotelCity = communication.receive();
        if (hotelCity == null) {
            return;
        }

        if (hotels == null || (hotels.containsKey(hotelCity) == false)) {
            searchEngine.updateHotelListByCity(hotelCity, hotels);
        }

        hotelList = hotels.get(hotelCity);
        if (hotelList == null) {
            communication.send("No hotels found in this city");
            return;
        } else {
            String toSend = searchEngine.formatHotelsList(hotelList);
            communication.send(toSend);
        }

        // hotels = searchEngine.searchByCity(hotelName);
        // ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels =
        // searchEngine.searchByCity(hotelName);

    }

    /**
     * Search a hotel
     * 
     * @param communication
     */
    public static void searchHotel(CommunicationManager communication) {
        communication.send("Insert hotel city");
        communication.send("PROMPT");
        String hotelCity = communication.receive();
        if (hotelCity == null) {
            return;
        }
        communication.send("Insert hotel name");
        communication.send("PROMPT");
        String hotelName = communication.receive();

        if (hotelName == null) {
            return;
        }

        if (hotels == null || (hotels.containsKey(hotelCity) == false)) {
            searchEngine.updateHotelListByCity(hotelCity, hotels);
        }
        Hotel hotel = searchEngine.searchByHotelName(hotelCity, hotelName, hotels);
        String toSend = searchEngine.formatSingleHotel(hotel);
        communication.send(toSend);
    }

    /**
     * Exit the session
     * 
     * @param communication
     */
    private static void exit(CommunicationManager communication) {
        try {
            communication.send("Goodbye!");
            communication.send("EXIT");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
