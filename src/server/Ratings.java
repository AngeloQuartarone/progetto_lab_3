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
    public Ratings(int c, int p, int s, int q){
        this.cleaning = c;
        this.position = p;
        this.services = s;
        this.quality = q;
    }
}
