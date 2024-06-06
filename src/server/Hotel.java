package server;

import java.util.ArrayList;

public class Hotel {
    int id;
    String name;
    String description;
    String city;
    String phone;
    ArrayList<String> services = new ArrayList<String>();
    int rate;
    Ratings ratings;
}
