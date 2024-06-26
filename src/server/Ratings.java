package server;

/**
 * Ratings class
 */
public class Ratings {
    int cleaning;
    int position;
    int services;
    int quality;

    /**
     * Constructor
     * 
     * @param cleaning
     * @param position
     * @param services
     * @param quality
     */
    public Ratings(int c, int p, int s, int q) {
        this.cleaning = c;
        this.position = p;
        this.services = s;
        this.quality = q;
    }

    /**
     * Get cleaning rating
     * 
     * @return cleaning rating
     */
    public int getCleaning() {
        return this.cleaning;
    }

    /**
     * Get position rating
     * 
     * @return position rating
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Get services rating
     * 
     * @return services rating
     */
    public int getServices() {
        return this.services;
    }

    /**
     * Get quality rating
     * 
     * @return quality rating
     */
    public int getQuality() {
        return this.quality;
    }
}
