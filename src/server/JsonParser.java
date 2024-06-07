package server;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonParser {
    private String filePath;

    public JsonParser(String file) {
        this.filePath = file;
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> parse() {
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = new ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>>();
        Double c = 0.0, p = 0.0, s = 0.0, q = 0.0;

        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filePath));
            System.out.println(filePath);

            reader.beginArray();
            while (reader.hasNext()) {
                Hotel hotel = new Hotel();
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    if (key.equalsIgnoreCase("id")) {
                        hotel.id = reader.nextInt();
                    } else if (key.equalsIgnoreCase("name")) {
                        hotel.name = reader.nextString();
                    } else if (key.equalsIgnoreCase("description")) {
                        hotel.description = reader.nextString();
                    } else if (key.equalsIgnoreCase(("city"))) {
                        hotel.city = reader.nextString();
                    } else if (key.equalsIgnoreCase("phone")) {
                        hotel.phone = reader.nextString();
                    } else if (key.equalsIgnoreCase("services")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            hotel.services.add(reader.nextString());
                        }
                        reader.endArray();
                    } else if (key.equalsIgnoreCase("rate")) {
                        hotel.rate = reader.nextInt();
                    } else if (key.equalsIgnoreCase("ratings")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String ratingKey = reader.nextName();
                            if (ratingKey.equalsIgnoreCase("cleaning")) {
                                c = reader.nextDouble();
                            } else if (ratingKey.equalsIgnoreCase("position")) {
                                p = reader.nextDouble();
                            } else if (ratingKey.equalsIgnoreCase("services")) {
                                s = reader.nextDouble();
                            } else if (ratingKey.equalsIgnoreCase("quality")) {
                                q = reader.nextDouble();
                            }

                        }
                        reader.endObject();
                        hotel.ratings = new Ratings(c, p, s, q);
                    }
                    // reader.endArray();
                }
                reader.endObject();
                String actualName = hotel.city;
                if (hotels.containsKey(actualName)) {
                    hotels.get(actualName).add(hotel);
                } else {
                    LinkedBlockingQueue<Hotel> x = new LinkedBlockingQueue<Hotel>();
                    x.add(hotel);
                    hotels.put(actualName, x);
                }

                // aggiungere l'hotel alla lista se c'è la lista, se no creala e aggiungi.
            } reader.endArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hotels;
    }

    public void printAll(ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
        for (Map.Entry<String, LinkedBlockingQueue<Hotel>> entry : hotels.entrySet()) {
            System.out.println("City: " + entry.getKey());
            for (Hotel hotel : entry.getValue()) {
                System.out.println("ID: " + hotel.id);
                System.out.println("Name: " + hotel.name);
                System.out.println("Description: " + hotel.description);
                System.out.println("Phone: " + hotel.phone);
                System.out.print("Services: ");
                for (String service : hotel.services) {
                    System.out.print(service + ", ");
                }
                System.out.println(); // Per andare a capo dopo l'elenco dei servizi
                System.out.println("Rate: " + hotel.rate);
                if (hotel.ratings != null) {
                    System.out.println("Ratings:");
                    System.out.println("\tCleaning: " + hotel.ratings.cleaning);
                    System.out.println("\tPosition: " + hotel.ratings.position);
                    System.out.println("\tServices: " + hotel.ratings.services);
                    System.out.println("\tQuality: " + hotel.ratings.quality);
                }
                System.out.println("------------------------------"); // Separatore per migliorare la leggibilità
            }
        }
    }
}
