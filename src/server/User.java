package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * User class
 */
public class User {
    public String username;
    public String password;
    private static final String filePath = "./Users.json";
    private int reviewCount = 0;

    /**
     * Constructor
     * 
     * @param username
     * @param password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.reviewCount = loadReviewCount(username);

    }

    private int loadReviewCount(String username) {
        List<User> users = getUsersFromFile();
        for (User user : users) {
            if (user.username.equals(username)) {
                return user.reviewCount;
            }
        }
        return 0; // Se l'utente non esiste, ritorna 0
    }

    private static List<User> getUsersFromFile() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            List<User> users = gson.fromJson(reader, userListType);
            return users != null ? users : new ArrayList<>();
        } catch (FileNotFoundException e) {
            System.out.println("File not found. A new file will be created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Check if the password is correct
     * 
     * @param password
     * @return true if the password is correct, false otherwise
     */
    private boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    /**
     * Append a new user to the list of users
     * 
     * @param newUser
     */
    synchronized public void insertUser(User newUser) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<User> users = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(reader, userListType);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. A new file will be created.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(newUser);

        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the user exists
     * 
     * @param user
     * @return true if the user is already in the list, false otherwise
     */
    synchronized public boolean checkUser(User userToCheck) {
        Gson gson = new Gson();
        List<User> users = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (User u : users) {
            if (u.username.equals(userToCheck.username) && u.checkPassword(userToCheck.password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the username exists
     * 
     * @param user
     * @return true if the username is already in the list, false otherwise
     */
    synchronized static public boolean checkUserName(String usernameToCheck) {
        Gson gson = new Gson();
        List<User> users = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (User u : users) {
            if (u.username.equals(usernameToCheck)) {
                return true;
            }
        }
        return false;
    }

    synchronized public void addReviewCount() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<User> users = new ArrayList<>();
        this.reviewCount++; // Incrementa il contatore delle recensioni per l'utente corrente

        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found. A new file will be created.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Trova l'utente corrente nell'elenco e aggiorna il suo reviewCount
        for (User user : users) {
            if (user.username.equals(this.username)) {
                user.reviewCount = this.reviewCount;
                break;
            }
        }

        // Sovrascrivi il file con l'elenco aggiornato degli utenti
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getReviewCount() {
        return this.reviewCount;
    }

}
