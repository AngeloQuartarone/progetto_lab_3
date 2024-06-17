package server;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

enum State {
    NO_LOGGED, LOGGED
}

public class SessionManager implements Runnable {
    private Socket socket = null;
    private String hotelsPath = null;
    private CommunicationManager communication;
    private State actualState = State.NO_LOGGED;
    private ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = new ConcurrentHashMap<>();
    private SearchEngine searchEngine = null;

    public SessionManager(Socket s, String hotelsP) {
        this.socket = s;
        this.hotelsPath = hotelsP;
    }

    @Override
    public void run() {
        this.searchEngine = new SearchEngine(hotelsPath);
        DataInputStream in = null;
        DataOutputStream out = null;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.communication = new CommunicationManager(in, out);

            while (!socket.isClosed()) {
                if (!handleMessage()) {
                    break;
                }
            }
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
    private boolean handleMessage() {
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
                    registerUser(communication);
                    break;
                case "2":
                    loginUser(communication);
                    break;
                case "3":
                    searchHotel(communication);
                    break;
                case "4":
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
                    searchHotel(communication);
                    break;
                case "2":
                    searchHotelByCity(communication);
                    break;
                case "3":
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
    private void registerUser(CommunicationManager communication) {
        communication.send("Insert username");
        communication.send("PROMPT");
        String username = communication.receive();
        if (username == null) {
            return;
        }

        if(User.checkUserName(username)) {
            communication.send("Username already exists");
            return;
        }
        communication.send("Insert password");
        communication.send("PROMPT");
        String password = communication.receive();
        if (password == null) {
            return;
        }
        User user = new User(username, password);
        
        if (!user.checkUser(user)) {
            user.insertUser(user);
            communication.send("User added");
        } else {
            communication.send("User already exists, do you want to login?");
        }
    }

    /**
     * Login a user
     * 
     * @param communication
     */
    private void loginUser(CommunicationManager communication) {
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
        if (user.checkUser(user)) {
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
    public void searchHotelByCity(CommunicationManager communication) {
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
    }

    /**
     * Search a hotel
     * 
     * @param communication
     */
    public void searchHotel(CommunicationManager communication) {
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
    private void exit(CommunicationManager communication) {
        try {
            communication.send("Goodbye!");
            communication.send("EXIT");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
