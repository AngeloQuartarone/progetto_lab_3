package server;

import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import com.google.gson.stream.JsonWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
     * 
     * @param cityFilter
     * @param existingHotels
     */
    public void updateHotelHashByCity(String cityFilter,
            ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> existingHotels) {
        int id = 0, rate = 0, c = 0, p = 0, s = 0, q = 0, numReviews = 0;
        String name = "", description = "", city = "", phone = "";
        ArrayList<String> services = new ArrayList<>();

        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filePath));
            System.out.println("[" + Thread.currentThread().getName() + "] - Access to hotel file in path" + filePath);

            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                services = new ArrayList<>();
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
                                        c = reader.nextInt();
                                        break;
                                    case "position":
                                        p = reader.nextInt();
                                        break;
                                    case "services":
                                        s = reader.nextInt();
                                        break;
                                    case "quality":
                                        q = reader.nextInt();
                                        break;
                                }
                            }
                            reader.endObject();
                            break;
                        case "numreviews":
                            numReviews = reader.nextInt();
                            break;
                    }
                }
                reader.endObject();

                Hotel hotel = new Hotel(id, name, description, city, phone, services, rate, c, p, s, q, numReviews);

                if (hotel.city.equalsIgnoreCase(cityFilter)) {
                    synchronized (existingHotels) {
                        existingHotels.computeIfAbsent(hotel.city, k -> new LinkedBlockingQueue<>()).add(hotel);
                    }
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
     * Get the hotels from the file
     * 
     * @return a ConcurrentHashMap with the hotels
     */
    public ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> getHotelsHashMap() {
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> existingHotels = new ConcurrentHashMap<>();
        int id = 0, rate = 0, c = 0, p = 0, s = 0, q = 0, numReviews = 0;
        String name = "", description = "", city = "", phone = "";

        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filePath));
            System.out.println("[" + Thread.currentThread().getName() + "] - Access to hotel file in path" + filePath);

            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                ArrayList<String> services = new ArrayList<>();
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
                                        c = reader.nextInt();
                                        break;
                                    case "position":
                                        p = reader.nextInt();
                                        break;
                                    case "services":
                                        s = reader.nextInt();
                                        break;
                                    case "quality":
                                        q = reader.nextInt();
                                        break;
                                }
                            }
                            reader.endObject();
                            break;
                        case "numreviews":
                            numReviews = reader.nextInt();
                            break;
                    }
                }
                reader.endObject();

                Hotel hotel = new Hotel(id, name, description, city, phone, services, rate, c, p, s, q, numReviews);

                synchronized (existingHotels) {
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
        return existingHotels;
    }

    /**
     * Save the hotels to a file
     * 
     * @param hotelsMap the ConcurrentHashMap with the hotels
     * @param filePath  the path of the file to save
     */
    public void saveHotelsHashMap(ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotelsMap, String filePath) {
        synchronized (hotelsMap) {
            try (JsonWriter writer = new JsonWriter(new FileWriter(filePath))) {
                writer.beginArray();
                for (LinkedBlockingQueue<Hotel> hotels : hotelsMap.values()) {
                    for (Hotel hotel : hotels) {
                        writer.beginObject();
                        writer.name("id").value(hotel.getId());
                        writer.name("name").value(hotel.getName());
                        writer.name("description").value(hotel.getDescription());
                        writer.name("city").value(hotel.getCity());
                        writer.name("phone").value(hotel.getPhone());
                        writer.name("rate").value(hotel.getRate());
                        writer.name("numReviews").value(hotel.getNumReviews());

                        writer.name("services");
                        writer.beginArray();
                        for (String service : hotel.getServices()) {
                            writer.value(service);
                        }
                        writer.endArray();

                        writer.name("ratings");
                        writer.beginObject();
                        writer.name("cleaning").value(hotel.getCleaning());
                        writer.name("position").value(hotel.getPosition());
                        writer.name("services").value(hotel.getServicesRating());
                        writer.name("quality").value(hotel.getQuality());
                        writer.endObject();

                        writer.endObject();
                    }
                }
                writer.endArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Search for a hotel by name
     * 
     * @param cityFilter the city to search in
     * @param hotelName  the name of the hotel to search
     * @param hotels     the ConcurrentHashMap with the hotels
     * @return the hotel if found, null otherwise
     */
    public Hotel searchByHotelName(String cityFilter, String hotelName,
            ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels) {
        Hotel resultHotel = null;
        LinkedBlockingQueue<Hotel> hotelsInCity = hotels.get(cityFilter);

        if (hotelsInCity != null) {
            synchronized (hotelsInCity) {
                for (Hotel hotel : hotelsInCity) {
                    if (hotel.name.equalsIgnoreCase(hotelName)) {
                        resultHotel = hotel;
                        break;
                    }
                }
                if (resultHotel == null) {
                    System.out.println("Hotel named " + hotelName + " not found in " + cityFilter);
                }
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
        synchronized (hotels) {
            for (Map.Entry<String, LinkedBlockingQueue<Hotel>> entry : hotels.entrySet()) {
                sb.append("City: ").append(entry.getKey()).append("\n");
                synchronized (entry.getValue()) {
                    for (Hotel hotel : entry.getValue()) {
                        sb.append("Name: ").append(hotel.name).append("\n");
                        sb.append("Description: ").append(hotel.description).append("\n");
                        sb.append("Phone: ").append(hotel.phone).append("\n");
                        sb.append("Services: ");
                        for (String service : hotel.services) {
                            sb.append(service).append(", ");
                        }
                        sb.setLength(sb.length() - 2);
                        sb.append("\n");
                        sb.append("Rate: ").append(hotel.rate).append("\n");
                        if (hotel.ratings != null) {
                            sb.append("Ratings:\n");
                            sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                            sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                            sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                            sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
                        }
                        sb.append("------------------------------\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Format the hotels list in a readable way
     * 
     * @param hotels the LinkedBlockingQueue with the hotels
     * @return a formatted string with the hotels
     */

    public String formatHotelsList(LinkedBlockingQueue<Hotel> hotelsQueue) {
        List<Hotel> hotelsList;
        synchronized (hotelsQueue) {
            hotelsList = hotelsQueue.stream().collect(Collectors.toList());
        }

        Collections.sort(hotelsList, new Comparator<Hotel>() {
            @Override
            public int compare(Hotel h1, Hotel h2) {
                return Double.compare(h2.rate, h1.rate);
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n------------------------------\n");
        for (Hotel hotel : hotelsList) {
            sb.append("Name: ").append(hotel.name).append("\n");
            sb.append("Description: ").append(hotel.description).append("\n");
            sb.append("Phone: ").append(hotel.phone).append("\n");
            sb.append("Services: ");
            for (String service : hotel.services) {
                sb.append(service).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("\n");
            sb.append("Rate: ").append(hotel.rate).append("\n");
            sb.append("Reviews number: ").append(hotel.numReviews).append("\n");
            if (hotel.ratings != null) {
                sb.append("Ratings:\n");
                sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
            }
            sb.append("------------------------------\n");
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
            sb.append("Name: ").append(hotel.name).append("\n");
            sb.append("Description: ").append(hotel.description).append("\n");
            sb.append("City: ").append(hotel.city).append("\n");
            sb.append("Phone: ").append(hotel.phone).append("\n");
            sb.append("Services: ");
            for (String service : hotel.services) {
                sb.append(service).append(", ");
            }
            if (!hotel.services.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("\nRate: ").append(hotel.rate).append("\n");
            sb.append("Reviews number: ").append(hotel.numReviews).append("\n");
            if (hotel.ratings != null) {
                sb.append("Ratings:\n");
                sb.append("\tCleaning: ").append(hotel.ratings.cleaning).append("\n");
                sb.append("\tPosition: ").append(hotel.ratings.position).append("\n");
                sb.append("\tServices: ").append(hotel.ratings.services).append("\n");
                sb.append("\tQuality: ").append(hotel.ratings.quality).append("\n");
            }
            sb.append("------------------------------\n");
        } else {
            sb.append("No hotel information available.");
        }
        return sb.toString();
    }

    /**
     * Get the best hotels in each city
     * 
     * @return a ConcurrentHashMap with the best hotels
     */
    public ConcurrentHashMap<String, Hotel> getBestHotelsMap() {
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = getHotelsHashMap();
        ConcurrentHashMap<String, Hotel> bestHotels = new ConcurrentHashMap<>();
        hotels.forEach((city, hotelsInCity) -> {
            Hotel bestHotel;
            synchronized (hotelsInCity) {
                bestHotel = hotelsInCity.stream()
                    .max(Comparator.comparing(Hotel::getRate)
                    .thenComparing(Hotel::getNumReviews))
                    .orElse(null);
            }
            if (bestHotel != null) {
                bestHotels.put(city, bestHotel);
            }
        });
        return bestHotels;
    }

    /**
     * Get the changed hotels
     * 
     * @param previousHotels the previous hotels
     * @param updatedHotels  the updated hotels
     * @return a string with the changed hotels
     */
    public String getChangedHotelsString(ConcurrentHashMap<String, Hotel> previousHotels, ConcurrentHashMap<String, Hotel> updatedHotels) {
        StringBuilder result = new StringBuilder();
        updatedHotels.forEach((city, updatedHotel) -> {
            Hotel previousHotel;
            synchronized (previousHotels) {
                previousHotel = previousHotels.get(city);
            }
            if (previousHotel == null || !previousHotel.equals(updatedHotel)) {
                if (previousHotel == null) {
                    result.append("New hotel in ");
                } else if (!previousHotel.equals(updatedHotel)) {
                    result.append("Changed best hotel in ");
                }
                result.append(city)
                      .append(": ")
                      .append(updatedHotel.name)
                      .append(" with rate ")
                      .append(updatedHotel.rate);
            }
        });
        return result.toString();
    }

}
