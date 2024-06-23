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
    int numReviews;

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
    public Hotel(int id, String name, String description, String city, String phone, ArrayList<String> services, int rate, int cleaning, int position, int servicesRating, int quality, int numReviews) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = new Ratings(cleaning, position, servicesRating, quality);
        this.numReviews = numReviews;
    }

    /**
     * Get hotel name
     * @return Hotel name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get hotel description
     * @return Hotel description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get hotel city
     * @return Hotel city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Get hotel phone
     * @return Hotel phone
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Get hotel services
     * @return Hotel services
     */
    public ArrayList<String> getServices() {
        return this.services;
    }

    /**
     * Get hotel rate
     * @return Hotel rate
     */
    public int getRate() {
        return this.rate;
    }

    /**
     * Get hotel cleaning rating
     * @return Hotel cleaning rating
     */
    public int getCleaning() {
        return this.ratings.cleaning;
    }

    /**
     * Get hotel position rating
     * @return Hotel position rating
     */
    public int getPosition() {
        return this.ratings.position;
    }

    /**
     * Get hotel services rating
     * @return Hotel services rating
     */
    public int getServicesRating() {
        return this.ratings.services;
    }

    /**
     * Get hotel quality rating
     * @return Hotel quality rating
     */
    public int getQuality() {
        return this.ratings.quality;
    }

    /**
     * Get hotel id
     * @return Hotel id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get hotel number of reviews
     * @return Hotel number of reviews
     */
    public int getNumReviews() {
        return this.numReviews;
    }

    public boolean equals(Hotel hotel) {
        return this.id == hotel.id;
    }
}