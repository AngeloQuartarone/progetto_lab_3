package server;

class Review {
    int rate;
    int cleaning;
    int position;
    int services;
    int quality;
    String timeStamp;

    public Review(int rate, int cleaning, int position, int services, int quality, String timeStamp) {
        this.rate = rate;
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
        this.timeStamp = timeStamp;
    }

}
