package server;

import java.util.ArrayList;

/**
 * Hotel class
 */
public class Hotel {
    int id;
    String name;
    String description;
    String city;
    String phone;
    ArrayList<String> services = new ArrayList<String>();
    int rate;
    Ratings ratings;

    /**
     * Constructor
     * 
     * @param name        Hotel name
     * @param description Hotel description
     * @param city        Hotel city
     * @param phone       Hotel phone
     * @param services    Hotel services
     * @param rate        Hotel rate
     * @param ratings     Hotel ratings
     */
    public Hotel(int id, String name, String description, String city, String phone,int rate, double cleaning, double position, double services, double quality) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.rate = rate;
        this.ratings = new Ratings(cleaning, position, services, quality);
    }

    public int getId() {
        return this.id;
    }
}