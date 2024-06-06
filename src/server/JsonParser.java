package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
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

    public ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> parse() throws Exception {
        JsonReader reader = new JsonReader(new FileReader(this.filePath));
        ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>> hotels = new ConcurrentHashMap<String, LinkedBlockingQueue<Hotel>>();
        Hotel hotel = new Hotel();
        Double c = 0.0, p = 0.0, s = 0.0, q = 0.0;
        reader.beginArray();

        while (reader.hasNext()) {
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
                        if (key.equalsIgnoreCase("cleaning")) {
                            c = reader.nextDouble();
                        } else if (key.equalsIgnoreCase("position")) {
                            p = reader.nextDouble();
                        } else if (key.equalsIgnoreCase("services")) {
                            s = reader.nextDouble();
                        } else if (key.equalsIgnoreCase("quality")) {
                            q = reader.nextDouble();
                        }

                    }
                    reader.endObject();
                    hotel.ratings = new Ratings(c, p, s, q);
                }
            }
            reader.endObject();
            String actualName = hotel.city;
            if(hotels.containsKey(actualName)){
                hotels.get(actualName).add(hotel);
            } else {
                LinkedBlockingQueue x = new LinkedBlockingQueue<Hotel>();
                x.add(hotel);
                hotels.put(actualName, x);
            }

            //aggiungere l'hotel alla lista se c'è la lista, se no creala e aggiungi.
        }
        reader.endArray();
        reader.close();

        return new HashMap<>();
    }
}
