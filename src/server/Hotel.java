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
    ArrayList<String> services;
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
    public Hotel(int id, String name, String description, String city, String phone, ArrayList<String> services, int rate, int cleaning, int position, int servicesRating, int quality) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = new Ratings(cleaning, position, servicesRating, quality);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCity() {
        return this.city;
    }

    public String getPhone() {
        return this.phone;
    }

    public ArrayList<String> getServices() {
        return this.services;
    }

    public int getRate() {
        return this.rate;
    }

    public int getCleaning() {
        return this.ratings.cleaning;
    }

    public int getPosition() {
        return this.ratings.position;
    }

    public int getServicesRating() {
        return this.ratings.services;
    }

    public int getQuality() {
        return this.ratings.quality;
    }

    public int getId() {
        return this.id;
    }
}