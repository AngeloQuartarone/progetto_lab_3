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

    /**
     * Add a review to the review file
     * 
     * @param hotelIdentifier
     * @param rate
     * @param cleaning
     * @param position
     * @param services
     * @param quality
     */
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

        // Generazione del timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Aggiunta della nuova recensione alla lista corrispondente, includendo il
        // timestamp
        hotelReviews.computeIfAbsent(hotelIdentifier, k -> new ArrayList<>())
                .add(new Review(rate, cleaning, position, services, quality, timestamp));

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
        // Mappa concorrente per memorizzare le recensioni degli hotel
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        // File delle recensioni
        File reviewFile = new File(reviewPath);
    
        // Se il file delle recensioni non esiste, prova a crearlo
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
    
        // Blocco try-with-resources per leggere il file JSON delle recensioni
        try (FileReader fileReader = new FileReader(reviewFile); JsonReader reader = new JsonReader(fileReader)) {
            // Tipo della mappa delle recensioni
            Type reviewMapType = new TypeToken<ConcurrentHashMap<Integer, List<Review>>>() {}.getType();
            // Deserializza il file JSON in una mappa
            hotelReviews = new Gson().fromJson(reader, reviewMapType);
            // Se la mappa è null, crea una nuova mappa vuota
            if (hotelReviews == null) {
                hotelReviews = new ConcurrentHashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di eccezione
        }
    
        // Formattatore per le date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Data e ora corrente
        LocalDateTime currentDate = LocalDateTime.now();
    
        // Itera su tutti gli ID degli hotel nella mappa
        for (Integer hotelId : hotelReviews.keySet()) {
            // Ottiene la lista delle recensioni per l'hotel corrente
            List<Review> reviews = hotelReviews.get(hotelId);
            // Inizializza variabili per il calcolo dei pesi e delle somme delle valutazioni
            double weightedRate = 0;
            double sumCleaning = 0;
            double sumPosition = 0;
            double sumServices = 0;
            double sumQuality = 0;
            double totalWeight = 0;
            // Peso basato sul numero di recensioni
            double reviewCountWeight = Math.log1p(reviews.size());
    
            // Itera su tutte le recensioni dell'hotel corrente
            for (Review review : reviews) {
                // Parsea la data della recensione
                LocalDateTime reviewDate = LocalDateTime.parse(review.timeStamp, formatter);
                // Calcola i giorni trascorsi dalla recensione
                long daysBetween = ChronoUnit.DAYS.between(reviewDate, currentDate);
                // Calcola il peso basato sulla recenza (decresce nel tempo, 0 dopo un mese)
                double recencyWeight = Math.max(0, 30 - daysBetween) / 30.0;
                // Combina i pesi di recenza e numero di recensioni
                double weight = recencyWeight * reviewCountWeight;
    
                // Aggiunge i valori pesati alle somme totali
                weightedRate += review.rate * weight;
                sumCleaning += review.cleaning * weight;
                sumPosition += review.position * weight;
                sumServices += review.services * weight;
                sumQuality += review.quality * weight;
                totalWeight += weight;
            }
    
            // Calcola le medie ponderate se il peso totale è maggiore di zero
            if (totalWeight > 0) {
                // Crea una nuova lista con una singola recensione media ponderata
                List<Review> weightedReview = Arrays.asList(new Review(
                        (int) (weightedRate / totalWeight),
                        (int) (sumCleaning / totalWeight),
                        (int) (sumPosition / totalWeight),
                        (int) (sumServices / totalWeight),
                        (int) (sumQuality / totalWeight),
                        currentDate.format(formatter)
                ));
                // Aggiorna la mappa con la nuova recensione ponderata
                hotelReviews.put(hotelId, weightedReview);
            }
        }
    
        // Scrive la mappa aggiornata nel file JSON
        try (Writer writer = new FileWriter(reviewFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(hotelReviews, writer);
        } catch (IOException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di eccezione
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
                }
            }
        }

        searchEngine.saveHotelsHashMap(hotels, hotelPath);
    }

}