package server;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReviewEngine {
    private String reviewPath;
    private String hotelPath;
    // private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ReviewEngine(String hotelPath) {
        this.reviewPath = "./Reviews.json";
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

   

    public void calculateMeanRatesById() {
        ConcurrentHashMap<Integer, List<Review>> hotelReviews = new ConcurrentHashMap<>();
        File reviewFile = new File(reviewPath);

        if (!reviewFile.exists()) {
            return;
        } else {
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();

        for (Integer hotelId : hotelReviews.keySet()) {
            List<Review> reviews = hotelReviews.get(hotelId);
            double weightedRate = 0;
            double weightedCleaning = 0;
            double weightedPosition = 0;
            double weightedServices = 0;
            double weightedQuality = 0;
            double totalWeight = 0;

            for (Review review : reviews) {
                LocalDateTime reviewDate = LocalDateTime.parse(review.timeStamp, formatter);
                long daysBetween = ChronoUnit.DAYS.between(reviewDate, currentDate);
                double weight = Math.max(0, 365 - daysBetween) / 365.0; // Peso decresce con il passare dei giorni, 0 dopo un anno

                weightedRate += review.rate * weight;
                weightedCleaning += review.cleaning * weight;
                weightedPosition += review.position * weight;
                weightedServices += review.services * weight;
                weightedQuality += review.quality * weight;
                totalWeight += weight;
            }

            if (totalWeight > 0) {
                List<Review> weightedReview = Arrays.asList(new Review(
                    (int) (weightedRate / totalWeight),
                    (int) (weightedCleaning / totalWeight),
                    (int) (weightedPosition / totalWeight),
                    (int) (weightedServices / totalWeight),
                    (int) (weightedQuality / totalWeight),
                    (String) currentDate.format(formatter)

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
}