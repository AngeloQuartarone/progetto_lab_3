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

        if (!reviewFile.exists()) {
            try {
                reviewFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
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
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int numReviews = hotelReviews.getOrDefault(hotelIdentifier, new ArrayList<>()).size() + 1;

        hotelReviews.computeIfAbsent(hotelIdentifier, k -> new ArrayList<>())
                .add(new Review(rate, cleaning, position, services, quality, numReviews, timestamp));

        try (Writer writer = new FileWriter(this.reviewPath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(hotelReviews, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the reviews for all hotels
     * 
     * @return
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
     * Calculate mean rates for all hotels
     * 
     * @param hotelId
     * @return
     */
    synchronized public ConcurrentHashMap<Integer, List<Review>> calculateMeanRatesById() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(reviewPath);
    
        if (!reviewFile.exists()) {
            System.out.println("File does not exist.");
            return null;
        }
    
        try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
            Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {}.getType();
            hotelReviews = new Gson().fromJson(reader, reviewMapType);
            if (hotelReviews == null) {
                System.out.println("No reviews to calculate.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();
    
        // Creazione di un nuovo ConcurrentHashMap per le recensioni aggiornate
        ConcurrentHashMap<Integer, List<Review>> updatedReviews = new ConcurrentHashMap<>();
    
        hotelReviews.forEach((hotelId, reviews) -> {
            if (reviews.isEmpty()) return;
    
            double weightedRate = 0, sumCleaning = 0, sumPosition = 0, sumServices = 0, sumQuality = 0, totalWeight = 0;
            double reviewCountWeight = Math.log1p(reviews.size());
    
            for (Review review : reviews) {
                LocalDateTime reviewDate = LocalDateTime.parse(review.timeStamp, formatter);
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
    
            if (totalWeight > 0) {
                int numReviews = reviews.size();
                double avgRate = weightedRate / totalWeight;
                int avgCleaning = (int) (sumCleaning / totalWeight);
                int avgPosition = (int) (sumPosition / totalWeight);
                int avgServices = (int) (sumServices / totalWeight);
                int avgQuality = (int) (sumQuality / totalWeight);
    
                // Aggiungi le recensioni aggiornate a updatedReviews mantenendo quelle esistenti
                List<Review> updatedHotelReviews = new ArrayList<>(reviews);
                updatedHotelReviews.set(0, new Review(
                        (int) avgRate,
                        avgCleaning,
                        avgPosition,
                        avgServices,
                        avgQuality,
                        numReviews,
                        currentDate.format(formatter)
                ));
                updatedReviews.put(hotelId, updatedHotelReviews);
            } else {
                // Se non ci sono recensioni valide, mantieni le recensioni esistenti
                updatedReviews.put(hotelId, reviews);
            }
        });
    
        return updatedReviews;
    }
    
    /**
     * Update the hotel file with the latest reviews
     */
    synchronized public void updateHotelFile(ConcurrentHashMap<Integer, List<Review>> reviewHashMap) {

        if(reviewHashMap == null) {
            return;
        }
        SearchEngine searchEngine = new SearchEngine(hotelPath);
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = searchEngine.getHotelsHashMap();
    
        for (String city : hotels.keySet()) {
            LinkedBlockingQueue<Hotel> cityHotels = hotels.get(city);
            for (Hotel hotel : cityHotels) {
                int hotelId = hotel.getId();
                if (reviewHashMap.containsKey(hotelId)) {
                    List<Review> reviews = reviewHashMap.get(hotelId);
                    if (!reviews.isEmpty()) {
                        Review review = reviews.get(0);
                        hotel.rate = review.rate;
                        hotel.ratings.cleaning = review.cleaning;
                        hotel.ratings.position = review.position;
                        hotel.ratings.services = review.services;
                        hotel.ratings.quality = review.quality;
                        hotel.numReviews = review.numReviews;
                    }
                }
            }
        }
    
        searchEngine.saveHotelsHashMap(hotels, hotelPath);
    }
}
    