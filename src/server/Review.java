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

    public int getRate() {
        return rate;
    }

    public int getCleaning() {
        return cleaning;
    }

    public int getPosition() {
        return position;
    }

    public int getServices() {
        return services;
    }

    public int getQuality() {
        return quality;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

}
