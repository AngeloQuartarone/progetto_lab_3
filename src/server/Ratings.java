package server;

/**
 * Ratings class
 */
public class Ratings {
    Double cleaning;
    Double position;
    Double services;
    Double quality;

    /**
     * Constructor
     * 
     * @param cleaning
     * @param position
     * @param services
     * @param quality
     */
    public Ratings(Double c, Double p, Double s, Double q){
        this.cleaning = c;
        this.position = p;
        this.services = s;
        this.quality = q;
    }
}
