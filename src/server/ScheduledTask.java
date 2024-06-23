package server;

import java.util.concurrent.ConcurrentHashMap;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ScheduledTask implements Runnable {
    // private String hotelsPath = "";
    private SearchEngine searchEngine = null;
    private ReviewEngine reviewEngine = null;
    private ConcurrentHashMap<String, Hotel> oldHotelsMap = null;
    private int udpPort = 0;

    public ScheduledTask(String hotelsPath, int udpPort) {
        this.searchEngine = new SearchEngine(hotelsPath);
        this.reviewEngine = new ReviewEngine(hotelsPath);
        this.oldHotelsMap = new ConcurrentHashMap<String, Hotel>();
        this.udpPort = udpPort;
    }

    public void run() {
        try {
            reviewEngine.updateHotelFile(reviewEngine.calculateMeanRatesById());
            ConcurrentHashMap<String, Hotel> newHotelsMap = searchEngine.getBestHotelsMap();
            String toSend = searchEngine.getChangedHotelsString(oldHotelsMap, newHotelsMap);
    
            // Prepara il messaggio UDP per il broadcast
            byte[] message = toSend.getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255"); // Indirizzo di broadcast
            DatagramPacket packet = new DatagramPacket(message, message.length, broadcastAddress, udpPort);
    
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.send(packet); // Invia il messaggio
                System.out.println("[" + Thread.currentThread().getName() + "] - Messaggio UDP inviato in broadcast");
            }
    
            oldHotelsMap = newHotelsMap;
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }

}
