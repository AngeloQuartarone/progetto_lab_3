package server;

import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * SearchEngine class
 */
public class SearchEngine {
    private String filePath;

    /**
     * Constructor
     * 
     * @param file the path of the file to parse
     */
    public SearchEngine(String file) {
        this.filePath = file;

    }

    /**
     * Update the hotel list by city (if is not present yet)
     * @param cityFilter
     * @param existingHotels
     */
    synchronized public void updateHotelListByCity(String cityFilter, ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> existingHotels) {
        Double c = 0.0, p = 0.0, s = 0.0, q = 0.0;
        int id = 0, rate = 0;
        String name = "", description = "", city = "", phone = "";
        ArrayList<String> services = new ArrayList<>();

        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filePath));
            System.out.println(filePath);

            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    switch (key.toLowerCase()) {
                        case "id":
                            id = reader.nextInt();
                            break;
                        case "name":
                            name = reader.nextString().toLowerCase();
                            break;
                        case "description":
                            description = reader.nextString().toLowerCase();
                            break;
                        case "city":
                            city = reader.nextString().toLowerCase();
                            break;
                        case "phone":
                            phone = reader.nextString().toLowerCase();
                            break;
                        case "services":
                            services.clear(); // Clear previous hotel services
                            reader.beginArray();
                            while (reader.hasNext()) {
                                services.add(reader.nextString().toLowerCase());
                            }
                            reader.endArray();
                            break;
                        case "rate":
                            rate = reader.nextInt();
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
                            break;
                    }
                }
                reader.endObject();

                // Create a new Hotel instance with the new constructor
                Hotel hotel = new Hotel(id, name, description, city, phone, rate, c, p, s, q);

                // Filtra per città e aggiunge alla mappa esistente
                if (hotel.city.equalsIgnoreCase(cityFilter)) {
                    existingHotels.computeIfAbsent(hotel.city, k -> new LinkedBlockingQueue<>()).add(hotel);
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
    }

    /**
     * Search for a hotel by name
     * 
     * @param cityFilter the city to search in
     * @param hotelName the name of the hotel to search
     * @param hotels the ConcurrentHashMap with the hotels
     * @return the hotel if found, null otherwise
     */
    synchronized public Hotel searchByHotelName(String cityFilter, String hotelName, ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
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


    /**
     * Format the hotels in a readable way
     * 
     * @param hotels the ConcurrentHashMap with the hotels
     * @return a formatted string with the hotels
     */
    public String formatHotelsHash(ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
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

    public String formatHotelsList(LinkedBlockingQueue<Hotel> hotels) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n------------------------------\n");
        for (Hotel hotel : hotels) {
            //sb.append("ID: ").append(hotel.id).append("\n");
            sb.append("Name: ").append(hotel.name).append("\n");
            sb.append("Description: ").append(hotel.description).append("\n");
            sb.append("Phone: ").append(hotel.phone).append("\n");
            sb.append("Services: ");
            for (String service : hotel.services) {
                sb.append(service).append(", ");
            }
            sb.setLength(sb.length() - 2); // Remove the last comma and space
            sb.append("\n"); // New line after listing services
            sb.append("Rate: ").append(hotel.rate).append("\n");
            if (hotel.ratings != null) {
                sb.append("Ratings:\n");
                sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
            }
            sb.append("------------------------------\n"); // Separator for readability
        }
        return sb.toString();
    }

    /**
     * Format a single hotel in a readable way
     * 
     * @param hotel the hotel to format
     * @return a formatted string with the hotel
     */
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
