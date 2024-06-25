package server;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

    public void addReview(int hotelIdentifier, int rate, int cleaning, int position, int services, int quality) {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(this.reviewPath);

        synchronized (this) {
            if (!reviewFile.exists()) {
                try {
                    reviewFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                try (FileReader fileReader = new FileReader(reviewFile);
                        JsonReader reader = new JsonReader(fileReader)) {
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
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        hotelReviews.computeIfAbsent(hotelIdentifier, k -> new ArrayList<>())
                .add(new Review(rate, cleaning, position, services, quality, timestamp));

        synchronized (this) {
            try (Writer writer = new FileWriter(this.reviewPath)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(hotelReviews, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the reviews for all hotels
     * 
     * @return
     */
    public ConcurrentHashMap<Integer, List<Review>> getReviews() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(this.reviewPath);

        if (!reviewFile.exists()) {
            return null;
        } else {
            synchronized (this) {
                try (FileReader fileReader = new FileReader(reviewFile);
                        JsonReader reader = new JsonReader(fileReader)) {
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
        }

        return hotelReviews;
    }

    /**
     * Calculate mean rates for all hotels
     * 
     * @param hotelId
     * @return
     */
    public ConcurrentHashMap<Integer, List<Review>> calculateMeanRatesById() {
        // Crea una hashmap per memorizzare le recensioni degli hotel
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        // inizializza File con il path delle reviews
        File reviewFile = new File(reviewPath);

        // Sincronizza il blocco per garantire che solo un thread alla volta possa
        // leggere e scrivere nel file delle recensioni
        synchronized (this) {
            try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
                // Definisce il tipo della mappa delle recensioni
                Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {
                }.getType();
                // Utilizza Gson per deserializzare il file JSON in una mappa
                hotelReviews = new Gson().fromJson(reader, reviewMapType);
                // Se non ci sono recensioni, ritorna null
                if (hotelReviews == null) {
                    System.out.println("No reviews to calculate.");
                    return null;
                }
            } catch (FileNotFoundException e) {
                // Se il file non esiste, ritorna null
                System.out.println("Review file not found.");
                return null;
            } catch (IOException e) {
                // Gestisce eccezioni
                e.printStackTrace();
                return null;
            }
        }

        // Formattatore per le date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Ottiene la data e l'ora correnti
        LocalDateTime currentDate = LocalDateTime.now();

        // Crea una nuova mappa concorrente per memorizzare le recensioni aggiornate
        ConcurrentHashMap<Integer, List<Review>> updatedReviews = new ConcurrentHashMap<>();

        // Itera su ogni hotel e le sue recensioni
        hotelReviews.forEach((hotelId, reviews) -> {
            // Se non ci sono recensioni per l'hotel, salta all'iterazione successiva
            if (reviews.isEmpty()) {
                return;
            }

            double weightedRate = 0, sumCleaning = 0, sumPosition = 0, sumServices = 0, sumQuality = 0, totalWeight = 0;
            // Calcola il peso in base al numero di recensioni utilizzando la funzione
            // logaritmica
            double reviewCountWeight = Math.log1p(reviews.size());

            // Itera su ogni recensione
            for (Review review : reviews) {
                // recupera la data della recensione
                LocalDateTime reviewDate = LocalDateTime.parse(review.timeStamp, formatter);
                // Calcola i giorni trascorsi dalla data della recensione alla data corrente
                long daysBetween = ChronoUnit.DAYS.between(reviewDate, currentDate);
                // Calcola il peso di recentezza (30 giorni è il massimo, oltre i 30 giorni il
                // peso è 0)
                // la scelta dei 30 giorni come periodo è una scelta arbitraria e personale e
                // vuole essere solo un esempio
                double recencyWeight = Math.max(0, 30 - daysBetween) / 30.0;
                // Calcola il peso totale come prodotto del peso di recentezza e del peso basato
                // sul numero di recensioni
                double weight = recencyWeight * reviewCountWeight;
                // Aggiorna le somme pesate per ogni categoria di rating
                weightedRate += review.rate * weight;
                sumCleaning += review.cleaning * weight;
                sumPosition += review.position * weight;
                sumServices += review.services * weight;
                sumQuality += review.quality * weight;
                // Aggiorna la somma totale dei pesi
                totalWeight += weight;
            }

            // Se il totale dei pesi è maggiore di zero, calcola i rating medi pesati
            if (totalWeight > 0) {
                double avgRate = weightedRate / totalWeight;
                int avgCleaning = (int) Math.round(sumCleaning / totalWeight);
                int avgPosition = (int) Math.round(sumPosition / totalWeight);
                int avgServices = (int) Math.round(sumServices / totalWeight);
                int avgQuality = (int) Math.round(sumQuality / totalWeight);

                // Crea una nuova lista di recensioni aggiornata con i rating medi pesati
                List<Review> updatedHotelReviews = new ArrayList<>(reviews);
                updatedHotelReviews.set(0, new Review(
                        (int) avgRate,
                        avgCleaning,
                        avgPosition,
                        avgServices,
                        avgQuality,
                        currentDate.format(formatter)));
                // Aggiunge l'hotel con le recensioni aggiornate alla mappa aggiornata
                updatedReviews.put(hotelId, updatedHotelReviews);
            } else {
                // Se il totale dei pesi è zero, aggiunge le recensioni originali senza
                // modifiche
                updatedReviews.put(hotelId, reviews);
            }
        });

        // Restituisce la mappa delle recensioni aggiornate
        return updatedReviews;
    }

    /**
     * Update the hotel file with the latest reviews
     */
    public void updateHotelFile(ConcurrentHashMap<Integer, List<Review>> reviewHashMap) {
        if (reviewHashMap == null) {
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
                        hotel.numReviews = reviews.size();
                    }
                }
            }
        }
        searchEngine.saveHotelsHashMap(hotels, hotelPath);
    }
}
