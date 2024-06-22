package server;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * ReviewEngine class
 */
public class ReviewEngine {
    private String reviewPath = "./Reviews.json";
    private String hotelPath;

    /**
     * Constructor
     * 
     * @param hotelPath
     */
    public ReviewEngine(String hotelPath) {
        this.hotelPath = hotelPath;
    }

    synchronized public void addReview(int hotelIdentifier, int rate, int cleaning, int position, int services,
            int quality) {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(this.reviewPath);

        // Verifica l'esistenza del file e lo crea se non esiste
        if (!reviewFile.exists()) {
            try {
                reviewFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return; // Termina la funzione se non riesce a creare il file
            }
        } else {
            // Caricamento delle recensioni esistenti
            try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
                Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {}.getType();
                hotelReviews = new Gson().fromJson(reader, reviewMapType);
                if (hotelReviews == null) {
                    hotelReviews = new ConcurrentHashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Generazione del timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Calcolo del numero attuale di recensioni per l'hotel
        int numReviews = hotelReviews.getOrDefault(hotelIdentifier, new ArrayList<>()).size() + 1;

        // Aggiunta della nuova recensione alla lista corrispondente, includendo il
        // timestamp e il numero aggiornato di recensioni
        hotelReviews.computeIfAbsent(hotelIdentifier, k -> new ArrayList<>())
                .add(new Review(rate, cleaning, position, services, quality, numReviews, timestamp));

        // Sovrascrittura del file con la mappa aggiornata
        try (Writer writer = new FileWriter(this.reviewPath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(hotelReviews, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the reviews from the review file
     * 
     * @return ConcurrentHashMap<Integer, List<Review>>
     */
    synchronized public ConcurrentHashMap<Integer, List<Review>> getReviews() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(this.reviewPath);

        // Verifica l'esistenza del file e lo crea se non esiste
        if (!reviewFile.exists()) {
            return null;
        } else {
            // Caricamento delle recensioni esistenti
            try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
                Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {
                }.getType();
                hotelReviews = new Gson().fromJson(reader, reviewMapType);
                if (hotelReviews == null) {
                    hotelReviews = new ConcurrentHashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return hotelReviews;
    }

    /**
     * Get the reviews for a specific hotel
     * 
     * @param hotelIdentifier
     * @return List<Review>
     */
    public void calculateMeanRatesById() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(reviewPath);

        if (!reviewFile.exists()) {
            try {
                if (reviewFile.createNewFile()) {
                    System.out.println("File created: " + reviewFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
            Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {}.getType();
            hotelReviews = new Gson().fromJson(reader, reviewMapType);
            if (hotelReviews == null) {
                hotelReviews = new ConcurrentHashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();

        for (Integer hotelId : hotelReviews.keySet()) {
            List<Review> reviews = hotelReviews.get(hotelId);
            double weightedRate = 0;
            double sumCleaning = 0;
            double sumPosition = 0;
            double sumServices = 0;
            double sumQuality = 0;
            double totalWeight = 0;
            double reviewCountWeight = Math.log1p(reviews.size());
            LocalDateTime mostRecentReviewDate = LocalDateTime.MIN;

            for (Review review : reviews) {
                LocalDateTime reviewDate = LocalDateTime.parse(review.timeStamp, formatter);
                if (reviewDate.isAfter(mostRecentReviewDate)) {
                    mostRecentReviewDate = reviewDate;
                }
                long daysBetween = ChronoUnit.DAYS.between(reviewDate, currentDate);
                double recencyWeight = Math.max(0, 30 - daysBetween) / 30.0;
                double weight = recencyWeight * reviewCountWeight;

                weightedRate += review.rate * weight;
                sumCleaning += review.cleaning * weight;
                sumPosition += review.position * weight;
                sumServices += review.services * weight;
                sumQuality += review.quality * weight;
                totalWeight += weight;
            }

            // Verifica se l'hotel non ha ricevuto recensioni negli ultimi 6 mesi
            long monthsSinceLastReview = ChronoUnit.MONTHS.between(mostRecentReviewDate, currentDate);
            if (monthsSinceLastReview > 6) {
                // Imposta le recensioni al minimo
                List<Review> minimumReview = Arrays.asList(new Review(1, 1, 1, 1, 1, 0, currentDate.format(formatter)));
                hotelReviews.put(hotelId, minimumReview);
            } else if (totalWeight > 0) {
                List<Review> weightedReview = Arrays.asList(new Review(
                        (int) (weightedRate / totalWeight),
                        (int) (sumCleaning / totalWeight),
                        (int) (sumPosition / totalWeight),
                        (int) (sumServices / totalWeight),
                        (int) (sumQuality / totalWeight),
                        reviews.size(),
                        mostRecentReviewDate.format(formatter)
                ));
                hotelReviews.put(hotelId, weightedReview);
            }
        }

        try (Writer writer = new FileWriter(reviewFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(hotelReviews, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update the hotel file with the latest reviews
     */
    synchronized public void updateHotelFile() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = getReviews(); // Ottieni le recensioni aggiornate
        SearchEngine searchEngine = new SearchEngine(hotelPath);
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = searchEngine.getHotelsHashMap();

        for (String city : hotels.keySet()) {
            LinkedBlockingQueue<Hotel> cityHotels = hotels.get(city);
            for (Hotel hotel : cityHotels) {
                int hotelId = hotel.getId();
                if (hotelReviews.containsKey(hotelId)) {
                    List<Review> reviews = hotelReviews.get(hotelId);
                    Review review = reviews.get(0);
                    hotel.rate = review.rate;
                    hotel.ratings.cleaning = review.cleaning;
                    hotel.ratings.position = review.position;
                    hotel.ratings.services = review.services;
                    hotel.ratings.quality = review.quality;
                    hotel.numReviews = reviews.size();
                }
            }
        }

        searchEngine.saveHotelsHashMap(hotels, hotelPath);
    }

}