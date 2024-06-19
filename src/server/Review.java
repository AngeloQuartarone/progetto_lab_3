package server;

/**
 * Review class
 */
class Review {
    int rate;
    int cleaning;
    int position;
    int services;
    int quality;
    String timeStamp;

    /**
     * Constructor
     * 
     * @param rate     Hotel rate
     * @param cleaning Hotel cleaning
     * @param position Hotel position
     * @param services Hotel services
     * @param quality  Hotel quality
     * @param timeStamp Time stamp
     */
    public Review(int rate, int cleaning, int position, int services, int quality, String timeStamp) {
        this.rate = rate;
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
        this.timeStamp = timeStamp;
    }

}
