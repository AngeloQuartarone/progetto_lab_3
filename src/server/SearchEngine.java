package server;

import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchEngine {
    private String filePath;

    public SearchEngine(String file) {
        this.filePath = file;

    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> searchByCity(String cityFilter) {
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = new ConcurrentHashMap<>();
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
                    switch (key.toLowerCase()) {
                        case "id":
                            hotel.id = reader.nextInt();
                            break;
                        case "name":
                            hotel.name = reader.nextString().toLowerCase();
                            break;
                        case "description":
                            hotel.description = reader.nextString().toLowerCase();
                            break;
                        case "city":
                            hotel.city = reader.nextString().toLowerCase();
                            break;
                        case "phone":
                            hotel.phone = reader.nextString().toLowerCase();
                            break;
                        case "services":
                            reader.beginArray();
                            while (reader.hasNext()) {
                                hotel.services.add(reader.nextString().toLowerCase());
                            }
                            reader.endArray();
                            break;
                        case "rate":
                            hotel.rate = reader.nextInt();
                            break;
                        case "ratings":
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String ratingKey = reader.nextName();
                                switch (ratingKey.toLowerCase()) {
                                    case "cleaning":
                                        c = reader.nextDouble();
                                        break;
                                    case "position":
                                        p = reader.nextDouble();
                                        break;
                                    case "services":
                                        s = reader.nextDouble();
                                        break;
                                    case "quality":
                                        q = reader.nextDouble();
                                        break;
                                }
                            }
                            reader.endObject();
                            hotel.ratings = new Ratings(c, p, s, q);
                            break;
                    }
                }
                reader.endObject();

                // Filtra per città
                if (hotel.city.equalsIgnoreCase(cityFilter)) {
                    hotels.computeIfAbsent(hotel.city, k -> new LinkedBlockingQueue<>()).add(hotel);
                }
            }
            reader.endArray();
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

    public Hotel searchByHotelName(String cityFilter, String hotelName, ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
        Hotel resultHotel = null;
        System.out.println("City: " + cityFilter);
        LinkedBlockingQueue<Hotel> hotelsInCity = hotels.get(cityFilter);
        //System.out.println(hotelsInCity.toString());
        
        if (hotelsInCity != null) {
            for (Hotel hotel : hotelsInCity) {
                if (hotel.name.equalsIgnoreCase(hotelName)) {
                    resultHotel = hotel;
                    break;
                }
            }
            if (resultHotel == null) {
                System.out.println("Hotel named " + hotelName + " not found in " + cityFilter);
            }
        } else {
            System.out.println("No hotels found in the specified city: " + cityFilter);
        }
        return resultHotel;
    }

    /*private void readHotelsFromFile() {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filePath));
            reader.beginArray();
            while (reader.hasNext()) {
                Hotel hotel = new Hotel();
                reader.beginObject();
                while (reader.hasNext()) {
                    // Parsing dell'hotel (come prima)
                }
                reader.endObject();

                // Aggiungi l'hotel alla ConcurrentHashMap
                hotels.computeIfAbsent(hotel.city, k -> new LinkedBlockingQueue<>()).add(hotel);
            }
            reader.endArray();
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
    }*/

    public String formatHotels(ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n------------------------------\n");
        for (Map.Entry<String, LinkedBlockingQueue<Hotel>> entry : hotels.entrySet()) {
            sb.append("City: ").append(entry.getKey()).append("\n");
            for (Hotel hotel : entry.getValue()) {
                //sb.append("ID: ").append(hotel.id).append("\n");
                sb.append("Name: ").append(hotel.name).append("\n");
                sb.append("Description: ").append(hotel.description).append("\n");
                sb.append("Phone: ").append(hotel.phone).append("\n");
                sb.append("Services: ");
                for (String service : hotel.services) {
                    sb.append(service).append(", ");
                }
                sb.setLength(sb.length() - 2); // Rimuove l'ultima virgola e spazio
                sb.append("\n"); // Per andare a capo dopo l'elenco dei servizi
                sb.append("Rate: ").append(hotel.rate).append("\n");
                if (hotel.ratings != null) {
                    sb.append("Ratings:\n");
                    sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                    sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                    sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                    sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
                }
                sb.append("------------------------------\n"); // Separatore per migliorare la leggibilità
            }
        }
        return sb.toString();
    }

    public String formatSingleHotel(Hotel hotel) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n------------------------------\n");
        if (hotel != null) {
            //sb.append("ID: ").append(hotel.id).append("\n");
            sb.append("Name: ").append(hotel.name).append("\n");
            sb.append("Description: ").append(hotel.description).append("\n");
            sb.append("City: ").append(hotel.city).append("\n");
            sb.append("Phone: ").append(hotel.phone).append("\n");
            sb.append("Services: ");
            for (String service : hotel.services) {
                sb.append(service).append(", ");
            }
            // Remove the last comma and space
            if (!hotel.services.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("\nRate: ").append(hotel.rate).append("\n");
            if (hotel.ratings != null) {
                sb.append("Ratings:\n");
                sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
            }
            sb.append("------------------------------\n"); // Separator for readability
        } else {
            sb.append("No hotel information available.");
        }
        return sb.toString();
    }

}
