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
    int numReviews;
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

    /*
     * Get the rate
     */
    public int getRate() {
        return rate;
    }

    /*
     * Get the cleaning
     */
    public int getCleaning() {
        return cleaning;
    }

    /*
     * Get the position
     */
    public int getPosition() {
        return position;
    }

    /*
     * Get the services
     */
    public int getServices() {
        return services;
    }

    /*
     * Get the quality
     */
    public int getQuality() {
        return quality;
    }

    /*
     * Get the number of reviews
     */
    public int getNumReviews() {
        return numReviews;
    }

    /*
     * Get the time stamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

}
