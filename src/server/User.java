package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class User {
    public String username;
    public String password;
    private static final String filePath = "./Users.json";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    private boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    /**
     * Append a new user to the list of users
     * 
     * @param newUser
     */
    public static void insertUser(User newUser) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<User> users = new ArrayList<>();

        // Read the existing users from the file
        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(reader, userListType);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. A new file will be created.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the new user to the list
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(newUser);

        // Write the updated list of users back to the file
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the user is already in the list
     * 
     * @param user
     * @return true if the user is already in the list, false otherwise
     */
    public static boolean checkUser(User userToCheck) {
        Gson gson = new Gson();
        List<User> users = new ArrayList<>();

        // Read the existing users from the file
        try (Reader reader = new FileReader(filePath)) {
            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check if the user is already in the list
        for (User u : users) {
            if (u.username.equals(userToCheck.username) && u.checkPassword(userToCheck.password)) {
                return true; // User already exists, do not append
            }
        }
        return false;
    }

    public static boolean existUser(String userName) {
        Gson gson = new Gson();
        List<User> users = new ArrayList<>();

        // Read the existing users from the file
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
            if (u.username.equals(userName)) {
            }
        }
        return false;
    }

}
